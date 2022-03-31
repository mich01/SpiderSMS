package com.mich01.spidersms.DB;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.Crypto.KeyExchange;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.Prefs.PrefsMgr;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.ScannerSetupActivity;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DBManager extends SQLiteOpenHelper
{
    Context context;
    public DBManager(Context context) {
        super(context, "Chats.db", null, 1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create Table UserProfile(CID TEXT primary key, PhoneNo TEXT, UserName TEXT, ServerURL TEXT)");
        db.execSQL("create Table Contacts(CID TEXT primary key, ContactName TEXT, PubKey TEXT, PrivKey TEXT, KeyStage TEXT, Secret TEXT, Confirmed TEXT, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("create Table EncryptedSMS(MessageID INTEGER primary key AUTOINCREMENT NOT NULL,CID TEXT, MessageBody  TEXT,inorout INTEGER, Status INTEGER,Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("create Table LastChats(CID TEXT primary key,MessageText TEXT,inorout INTEGER,ReadStatus INEGER, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("drop table if exists UserProfile");
        db.execSQL("drop table if exists Contacts");
        db.execSQL("drop table if exists EncryptedSMS");
    }
    //Add Contacts
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean AddContact(JSONObject ContactObject)
    {
        boolean status = false;
        String PrivateKey = PKI_Cipher.GenerateNewKey();
        String SharedSecret = PKI_Cipher.GenerateNewKey();
        try
        {
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Log.i("OPT",ContactObject.toString());
            String CID = ContactObject.getString("CID");
            SQLiteDatabase DB = this.getWritableDatabase();
            final String PublicKey =ContactObject.getString("PubKey");
            ContentValues contentValues = new ContentValues();
            contentValues.put("PubKey", ContactObject.getString("PubKey"));
            contentValues.put("PrivKey", PrivateKey);
            contentValues.put("Secret", SharedSecret);
            contentValues.put("KeyStage", "1");
            contentValues.put("Confirmed", "0");
            ContactObject.remove("PubKey");
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=?", new String[] {CID});
            if(cursor.getCount()>0)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(R.string.are_you_sure_update_contact);
                alert.setPositiveButton("Update", (dialog, whichButton) -> UpdateContact(ContactObject,PublicKey));
                alert.setNegativeButton("Cancel",
                        (dialog, whichButton) -> {
                        });
                alert.show();
            }
            else
            {
                contentValues.put("CID", CID);
                contentValues.put("ContactName", ContactObject.getString("CName"));
                long result = DB.insert("Contacts", null, contentValues);
                if(result==-1)
                {
                    status= false;
                }
                else
                {
                    //Log.i("Key Step 2","Public Key "+ContactObject.getString("PrivKey"));
                    ContactObject.put("t", preferences.getString("MyContact","NewContact"));
                    ContactObject.put("x", "3");
                    ContactObject.put("SecretKey", PrivateKey);
                    ContactObject.put("Secret", SharedSecret);
                    new KeyExchange(context).FirstExchange(CID,PublicKey,ContactObject);
                    status= true;
                    ((ScannerSetupActivity)context).finish();
                }
            }
            cursor.close();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            status = false;
        }
        return status;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public int UpdateContact(JSONObject ContactDetails, String PublicKey)
    {
        int result =0;
        String CID;
        String PrivateKey = PKI_Cipher.GenerateNewKey();
        String SharedSecret = PKI_Cipher.GenerateNewKey();
        try
        {
            Log.i("ContactKey", ContactDetails.toString());
            CID = ContactDetails.getString("CID");
            SQLiteDatabase DBUpdateContact = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("PubKey", PublicKey);
            contentValues.put("PrivKey", PrivateKey);
            contentValues.put("Secret", SharedSecret);
            contentValues.put("KeyStage", "1");
            contentValues.put("Confirmed", "0");
            result = DBUpdateContact.update("Contacts", contentValues, "CID=?", new String[]{CID});
            if(result==-1)
            {
                Toast.makeText(context.getApplicationContext(), ContactDetails.getString("CName")+" Has Failed to Update", Toast.LENGTH_LONG).show();
            }
            else
            {
                SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                ContactDetails.put("x", "3");
                ContactDetails.put("SecretKey", PrivateKey);
                ContactDetails.put("Secret", SharedSecret);
                Log.i("Key Step 2","Public Key "+ContactDetails);
                ContactDetails.remove("PubKey");
                ContactDetails.put("t", preferences.getString("MyContact","NewContact"));
                new KeyExchange(context).FirstExchange(CID,PublicKey,ContactDetails);
                Toast.makeText(context.getApplicationContext(), ContactDetails.getString("CName")+" Has Updated their Contacts", Toast.LENGTH_LONG).show();
                ((ScannerSetupActivity)context).finish();
                HomeActivity.RePopulateChats(context);
                ContactsActivity.RepopulateContacts(context);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    public void UpdateMessageStatus(String CID, JSONObject SMSMessage, int Status)
    {
        try {
            if(!SMSMessage.getString("Body").isEmpty())
            {
                SQLiteDatabase DBUpdateChats = this.getWritableDatabase();
                ContentValues chatValues = new ContentValues();
                chatValues.put("Status",Status);
                Log.i("HERE HERE", CID+" -- "+SMSMessage);
                DBUpdateChats.update("EncryptedSMS", chatValues, "CID=? AND MessageBody=?", new String[]{CID,SMSMessage.getString("Body")});
            }
        } catch (JSONException e) {

        }finally {
        }

    }
    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean UpdateContactSpec(JSONObject ContactDetails)
    {
        String CID =null;
        String PublicKey=null;
        JSONObject ContactVerificationJSON = new JSONObject();
        MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean Status =false;
        try
        {
            CID = ContactDetails.getString("t");
            ContentValues contentValues = new ContentValues();
            SQLiteDatabase DBUpdateContact = this.getWritableDatabase();
            Cursor cursor = DBUpdateContact.rawQuery("select * from Contacts where CID=?  LIMIT 1", new String[]{CID});
            if(cursor.getCount()>0 && cursor.moveToNext())
            {
                Log.i("Key Step 3","Contact Updating "+cursor.getString(cursor.getColumnIndex("PubKey")));
                PublicKey =cursor.getString(cursor.getColumnIndex("PubKey"));
                contentValues.put("PrivKey", ContactDetails.getString("SecretKey"));
                contentValues.put("PubKey", PublicKey);
                contentValues.put("Confirmed", "0");
                contentValues.put("KeyStage", "2");
                contentValues.put("Secret", ContactDetails.getString("Secret"));
                DBUpdateContact.update("Contacts", contentValues, "CID=?", new String[]{CID});
                DBUpdateContact.close();
                Log.i("Key Step 3","Contact Updated");
            }
            else
            {
                contentValues.put("CID", CID);
                contentValues.put("PrivKey", ContactDetails.getString("SecretKey"));
                contentValues.put("PubKey", PublicKey);
                contentValues.put("ContactName", ContactDetails.getString("CName"));
                contentValues.put("KeyStage", "2");
                contentValues.put("Confirmed", "0");
                contentValues.put("KeyStage", "2");
                contentValues.put("Secret", ContactDetails.getString("Secret"));
                DBUpdateContact.insert("Contacts", null, contentValues);
                DBUpdateContact.close();
                Log.i("Key Step 3","Contact Added");
            }
            Log.i("Key Step Next 4","Sending Contact Verification for stage 4");
            ContactVerificationJSON.put("x","4");
            ContactVerificationJSON.put("t",MyPrefs.getString("MyContact","--"));
            ContactVerificationJSON.put("Secret",PKI_Cipher.ComputeHash(ContactDetails.getString("Secret")));
            ContactVerificationJSON.put("SecretKey",PKI_Cipher.ComputeHash(ContactDetails.getString("SecretKey")));
            Log.i("Key Step Sending 4",ContactVerificationJSON.toString());
            if(BackendFunctions.isConnectedOnline(context) && !MyPrefs.getString("ServerURL","---").equals("---"))
            {
                new SMSHandler(context).SendSMSOnline(CID, ContactVerificationJSON, PublicKey);
            }
            else
            {
                new SMSHandler(context).sendEncryptedSMS(CID, ContactVerificationJSON, PublicKey,3);
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            Log.i("Key Step 3 Error",e.getLocalizedMessage());
            e.printStackTrace();
        }
        return Status;
    }
    public Cursor getContacts()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from Contacts Order By PubKey DESC, ContactName ASC", null);
        return cursor;
    }
    public Cursor getAPPContacts()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from Contacts where PubKey !=? and Confirmed =? Order By ContactName ASC", new String[]{"000000", "0"});
        return cursor;
    }
    //ChatsTable
    public boolean AddChatMessage(String CID, int InorOut, String ChatMessage, int Status)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CID", CID);
        contentValues.put("MessageBody", ChatMessage);
        contentValues.put("inorout", InorOut);
        contentValues.put("Status",Status);
        contentValues.put("Timestamp", System.currentTimeMillis());
        long result = DB.insert("EncryptedSMS", null, contentValues);
        if(result==-1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    public Cursor getCIDChats(String CID) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from EncryptedSMS where CID=? Order By Timestamp ASC", new String[]{CID});
        return cursor;
    }


    public Cursor getLastChatList()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        return DB.rawQuery("select LastChats.CID, " +
                "LastChats.MessageText, " +
                "LastChats.Timestamp, " +
                "LastChats.ReadStatus, " +
                "LastChats.inorout, " +
                "Contacts.ContactName  " +
                "from LastChats LEFT JOIN Contacts on  Contacts.CID = LastChats.CID Order By LastChats.Timestamp DESC",null);
    }
    public boolean updateLastMessage(String CID, String MessageText, int InOrOut, int ReadStatus)
    {
        boolean status = false;
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CID", CID);
        contentValues.put("MessageText", MessageText);
        contentValues.put("inorout", InOrOut);
        contentValues.put("ReadStatus", ReadStatus);
        contentValues.put("Timestamp", System.currentTimeMillis());
        Cursor ChatCursor = DB.rawQuery("select * from LastChats where CID=?", new String[]{CID});
        if (ChatCursor.getCount() > 0) {
            long result = DB.update("LastChats", contentValues, "CID=?", new String[]{CID});
            if (result == -1) {
            }
            else
            {
                status = true;
            }
        }
        else
        {
            long result = DB.insert("LastChats", null, contentValues);
            status = result != -1;
        }
        ChatCursor.close();
        return status;
    }
    public boolean UpdatePhoneBook(JSONObject ContactObject)
    {
        boolean status;
        try
        {
            String CID = ContactObject.getString("CID");
            SQLiteDatabase DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("CID", CID);
            contentValues.put("PubKey", "000000");
            contentValues.put("ContactName", ContactObject.getString("ContactName"));
            contentValues.put("PrivKey", "0000");
            contentValues.put("Secret", "0000");
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=?", new String[] {CID});
            if(cursor.getCount()<1)
            {
                long result = DB.insert("Contacts", null, contentValues);
                status= result != -1;
                cursor.close();
            }
            else
            {
                status = false;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            status = false;
        }
        return status;
    }
    public int DeleteContact(String CID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        int result = DB.delete("Contacts", "CID=?", new String[]{CID});
        Toast.makeText(context,"Contact Deleted: "+CID, Toast.LENGTH_LONG).show();
        return result;
    }
    public int DeleteMessage(String MessageID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        int result = DB.delete("EncryptedSMS", "MessageID=?", new String[]{MessageID});
        Toast.makeText(context,"Message Deleted: "+MessageID, Toast.LENGTH_LONG).show();
        return result;
    }
    public boolean UpdateMessageStatus(String CID, int Status)
    {
        Log.i("Current ID: ",CID);
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ReadStatus", Status);
        long result = DB.update("LastChats", contentValues, "CID=?", new String[]{CID});
        return result != -1;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void VerifyContactPK(String CID, String SecretHash)
    {
        try {
            JSONObject LocalContact = new DBManager(context).GetContact(CID);
            if(PKI_Cipher.ComputeHash(LocalContact.getString("Secret")).equals(SecretHash)) {
                Log.i("Key step  Verified ", CID);
                SQLiteDatabase DB = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("Confirmed", "1");
                DB.update("Contacts", contentValues, "CID=?", new String[]{CID});
                DB.close();
                new BackendFunctions().KeyStatusChanged(context, CID, "---Shared Key Changed---");
            }
        }catch (Exception e){}
    }
    public int DeleteAllContacts(String PublicKey)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        int result = DB.delete("Contacts", "PubKey==?", new String[]{PublicKey});
        Toast.makeText(context,"Contact Deleted: "+PublicKey, Toast.LENGTH_LONG).show();
        return result;
    }
    public void DeleteAllChats(String CID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.delete("EncryptedSMS", "CID=?", new String[]{CID});
        DB.delete("LastChats", "CID=?", new String[]{CID});
        Toast.makeText(context,context.getString(R.string.conversation_deleted)+CID, Toast.LENGTH_LONG).show();
    }
    @SuppressLint("Range")
    public JSONObject GetContact(String CID)
    {
        JSONObject ContactJSON = new JSONObject();
        try{
            SQLiteDatabase DB = this.getReadableDatabase();
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=? LIMIT 1", new String[]{CID});
            while (cursor != null && cursor.moveToNext())
            {
                ContactJSON.put("PrivKey",cursor.getString(cursor.getColumnIndex("PrivKey")));
                ContactJSON.put("PubKey",cursor.getString(cursor.getColumnIndex("PubKey")));
                ContactJSON.put("Secret",cursor.getString(cursor.getColumnIndex("Secret")));
                ContactJSON.put("Confirmed",cursor.getString(cursor.getColumnIndex("Confirmed")));
            }
        }catch (Exception e){e.printStackTrace();}

        return ContactJSON;
    }
}
