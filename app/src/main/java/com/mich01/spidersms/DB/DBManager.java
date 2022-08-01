package com.mich01.spidersms.DB;

import static com.mich01.spidersms.Data.StringsConstants.Body;
import static com.mich01.spidersms.Data.StringsConstants.CName;
import static com.mich01.spidersms.Data.StringsConstants.C_ID;
import static com.mich01.spidersms.Data.StringsConstants.Confirmed;
import static com.mich01.spidersms.Data.StringsConstants.ContactDeleted;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.ContactTarget;
import static com.mich01.spidersms.Data.StringsConstants.Contacts;
import static com.mich01.spidersms.Data.StringsConstants.DBName;
import static com.mich01.spidersms.Data.StringsConstants.EncryptedTable;
import static com.mich01.spidersms.Data.StringsConstants.LastChats;
import static com.mich01.spidersms.Data.StringsConstants.MessageBody;
import static com.mich01.spidersms.Data.StringsConstants.MessageDeleted;
import static com.mich01.spidersms.Data.StringsConstants.MessageID;
import static com.mich01.spidersms.Data.StringsConstants.MessageText;
import static com.mich01.spidersms.Data.StringsConstants.MessageType;
import static com.mich01.spidersms.Data.StringsConstants.MyContact;
import static com.mich01.spidersms.Data.StringsConstants.NewContact;
import static com.mich01.spidersms.Data.StringsConstants.PrivKey;
import static com.mich01.spidersms.Data.StringsConstants.PubKey;
import static com.mich01.spidersms.Data.StringsConstants.ReadStatus;
import static com.mich01.spidersms.Data.StringsConstants.Salt;
import static com.mich01.spidersms.Data.StringsConstants.Secret;
import static com.mich01.spidersms.Data.StringsConstants.SecretKey;
import static com.mich01.spidersms.Data.StringsConstants.ServerURL;
import static com.mich01.spidersms.Data.StringsConstants.Status;
import static com.mich01.spidersms.Data.StringsConstants.Timestamp;
import static com.mich01.spidersms.Data.StringsConstants.global_pref;
import static com.mich01.spidersms.Data.StringsConstants.inorout;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.Crypto.KeyExchange;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.ScannerSetupActivity;
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
    private String privateKey;
    public DBManager(Context context) {
        super(context, DBName, null, 1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create Table UserProfile(CID TEXT primary key, PhoneNo TEXT, UserName TEXT, ServerURL TEXT)");
        db.execSQL("create Table Contacts(CID TEXT primary key, ContactName TEXT, PubKey TEXT, PrivKey TEXT, Secret TEXT, Salt TEXT, Confirmed INTEGER, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
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
        privateKey = PKI_Cipher.GenerateNewKey();
        String SharedSecret = PKI_Cipher.GenerateNewKey();
        String ContactSalt = PKI_Cipher.GenerateNewKey();
        try
        {
            SharedPreferences preferences = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
            String contactID = ContactObject.getString(C_ID);
            SQLiteDatabase DB = this.getWritableDatabase();
            String PublicKey =ContactObject.getString(PubKey);
            ContentValues contentValues = new ContentValues();
            contentValues.put(PubKey, PublicKey);
            contentValues.put(PrivKey, privateKey);
            contentValues.put(Secret, SharedSecret);
            contentValues.put(Salt, ContactSalt);
            contentValues.put(Confirmed, 0);
            ContactObject.remove(PubKey);
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=?", new String[] {contactID});
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
                contentValues.put(C_ID, contactID);
                contentValues.put(ContactName, ContactObject.getString(CName));
                long result = DB.insert(Contacts, null, contentValues);
                if(result!=-1)
                {
                    ContactObject.put(ContactTarget, preferences.getString(MyContact,NewContact));
                    ContactObject.put(MessageType, 3);
                    ContactObject.put(SecretKey, privateKey);
                    ContactObject.put(Secret, SharedSecret);
                    contentValues.put(Salt, ContactSalt);
                    new KeyExchange(context).FirstExchange(contactID,PublicKey,ContactObject);
                    status= true;
                    ((ScannerSetupActivity)context).finish();
                }
            }
            cursor.close();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return status;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void UpdateContact(JSONObject ContactDetails, String PublicKey)
    {
        int result;
        String Contact_ID;
        privateKey = PKI_Cipher.GenerateNewKey();
        String SharedSecret = PKI_Cipher.GenerateNewKey();
        String ContactSalt = PKI_Cipher.GenerateNewKey();
        try
        {
            Contact_ID = ContactDetails.getString(C_ID);
            SQLiteDatabase DBUpdateContact = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(PubKey, PublicKey);
            contentValues.put(PrivKey, privateKey);
            contentValues.put(Secret, SharedSecret);
            contentValues.put(Salt, ContactSalt);
            contentValues.put(Confirmed, 0);
            result = DBUpdateContact.update(Contacts, contentValues, C_ID+"=?", new String[]{Contact_ID});
            if(result==-1)
            {
                Toast.makeText(context.getApplicationContext(), ContactDetails.getString(CName)+" Has Failed to Update", Toast.LENGTH_LONG).show();
            }
            else
            {
                SharedPreferences preferences = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
                ContactDetails.put(MessageType, 3);
                ContactDetails.put(SecretKey, privateKey);
                ContactDetails.put(Secret, SharedSecret);
                contentValues.put(Secret, SharedSecret);
                contentValues.put(Salt, ContactSalt);
                ContactDetails.remove(PubKey);
                ContactDetails.put(ContactTarget, preferences.getString(MyContact,NewContact));
                //Log.i("key",ContactTarget);
                new KeyExchange(context).FirstExchange(ContactTarget,PublicKey,ContactDetails);
                Toast.makeText(context.getApplicationContext(), ContactDetails.getString(CName)+" Has Updated their Contacts", Toast.LENGTH_LONG).show();
                HomeActivity.rePopulateChats(context);
                ContactsActivity.repopulateContacts(context);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void UpdateMessageStatus(String ContactID, JSONObject SMSMessage, int ContactStatus)
    {
        try {
            if(!SMSMessage.getString(Body).isEmpty())
            {
                SQLiteDatabase DBUpdateChats = this.getWritableDatabase();
                ContentValues chatValues = new ContentValues();
                chatValues.put(Status,ContactStatus);
                DBUpdateChats.update(EncryptedTable, chatValues, C_ID+"=? AND "+MessageBody+"=?", new String[]{ContactID,SMSMessage.getString(Body)});
            }
        } catch (JSONException ignored) {
            //Do Nothing
        }

    }
    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void UpdateContactSpec(JSONObject ContactDetails)
    {
        String ContactID;
        String PublicKey=null;
        JSONObject ContactVerificationJSON = new JSONObject();
        MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        try
        {
            ContactID = ContactDetails.getString(ContactTarget);
            ContentValues contentValues = new ContentValues();
            SQLiteDatabase DBUpdateContact = this.getWritableDatabase();
            @SuppressLint("Recycle") Cursor cursor = DBUpdateContact.rawQuery("select * from Contacts where CID=?  LIMIT 1", new String[]{ContactID});
            if(cursor.getCount()>0 && cursor.moveToNext())
            {
                PublicKey =cursor.getString(cursor.getColumnIndex(PubKey));
                contentValues.put(PrivKey, ContactDetails.getString(SecretKey));
                contentValues.put(PubKey, PublicKey);
                contentValues.put(Confirmed, 0);
                contentValues.put(Secret, ContactDetails.getString(Secret));
                DBUpdateContact.update(Contacts, contentValues, C_ID+"=?", new String[]{ContactID});
                DBUpdateContact.close();
            }
            else
            {
                contentValues.put(C_ID, ContactID);
                contentValues.put(PrivKey, ContactDetails.getString(SecretKey));
                contentValues.put(PubKey, PublicKey);
                contentValues.put(ContactName, ContactDetails.getString(CName));
                contentValues.put(Confirmed, 0);
                contentValues.put(Secret, ContactDetails.getString(Secret));
                DBUpdateContact.insert(Contacts, null, contentValues);
                DBUpdateContact.close();
            }
            if(PublicKey!=null)
            {
                ContactVerificationJSON.put(MessageType,4);
                ContactVerificationJSON.put(ContactTarget,MyPrefs.getString(MyContact,"--"));
                ContactVerificationJSON.put(Secret,PKI_Cipher.ComputeHash(ContactDetails.getString(Secret)));
                ContactVerificationJSON.put(SecretKey,PKI_Cipher.ComputeHash(ContactDetails.getString(SecretKey)));
                if(BackendFunctions.isConnectedOnline(context) && !MyPrefs.getString(ServerURL,"---").equals("---"))
                {
                    new SMSHandler(context).sendSMSOnline(ContactID, ContactVerificationJSON, PublicKey);
                }
                else
                {
                    new SMSHandler(context).sendEncryptedSMS(ContactID, ContactVerificationJSON, PublicKey, null,null,3);
                }
            }
            else
            {
                new KeyExchange(context).RequestContact(ContactID);
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
    public Cursor getContacts()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        return DB.rawQuery("select * from Contacts Order By PubKey DESC, ContactName ASC", null);
    }
    //ChatsTable
    public void AddChatMessage(String ContactID, int InorOut, String ChatMessage, int MessageStatus)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(C_ID, ContactID);
        contentValues.put(MessageBody, ChatMessage);
        contentValues.put(inorout, InorOut);
        contentValues.put(Status,MessageStatus);
        contentValues.put(Timestamp, System.currentTimeMillis());
        DB.insert(EncryptedTable, null, contentValues);
    }
    public Cursor getCIDChats(String ContactID) {
        SQLiteDatabase DB = this.getWritableDatabase();
        return DB.rawQuery("select * from EncryptedSMS where CID=? Order By Timestamp ASC", new String[]{ContactID});
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
    public void updateLastMessage(String ContactID, String SMSText, int InOrOut, int Read_Status)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(C_ID, ContactID);
        contentValues.put(MessageText, SMSText);
        contentValues.put(inorout, InOrOut);
        contentValues.put(ReadStatus, Read_Status);
        contentValues.put(Timestamp, System.currentTimeMillis());
        final String TableName = "LastChats";
        Cursor ChatCursor = DB.rawQuery("select * from "+TableName+" where CID=?", new String[]{ContactID});
        if (ChatCursor.getCount() > 0) {
           DB.update(TableName, contentValues, "CID=?", new String[]{ContactID});
        }
        else
        {
            DB.insert(TableName, null, contentValues);
        }
        ChatCursor.close();
    }
    public void UpdatePhoneBook(JSONObject ContactObject)
    {
        try
        {
            String ContactID = ContactObject.getString(C_ID);
            SQLiteDatabase DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(C_ID, ContactID);
            contentValues.put(PubKey, "000000");
            contentValues.put(ContactName, ContactObject.getString(ContactName));
            contentValues.put(PrivKey, "0000");
            contentValues.put(Secret, "0000");
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=?", new String[] {ContactID});
            if(cursor.getCount()<1)
            {
                DB.insert(Contacts, null, contentValues);
                cursor.close();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    public void DeleteContact(String ContactID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.delete(Contacts, C_ID+"=?", new String[]{ContactID});
        Toast.makeText(context,ContactDeleted+ContactID, Toast.LENGTH_LONG).show();
    }
    public void DeleteMessage(String messageID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.delete(EncryptedTable, MessageID+"=?", new String[]{messageID});
        Toast.makeText(context,MessageDeleted+messageID, Toast.LENGTH_LONG).show();
    }
    public void UpdateMessageStatus(String ContactID, int smsStatus)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReadStatus, smsStatus);
        DB.update(LastChats, contentValues, C_ID+"=?", new String[]{ContactID});
        DB.close();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void VerifyContactPK(String ContactID, String SecretHash)
    {
        try(SQLiteDatabase DB = this.getWritableDatabase()) {
            JSONObject LocalContact = new DBManager(context).GetContact(ContactID);
            if(PKI_Cipher.ComputeHash(LocalContact.getString(Secret)).equals(SecretHash)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Confirmed, 1);
                DB.update(Contacts, contentValues, C_ID+"=?", new String[]{ContactID});
                new BackendFunctions().keyStatusChanged(context, ContactID, "---Shared Key Changed---");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("Range")
    public JSONObject GetContact(String ContactID)
    {

        JSONObject ContactJSON = new JSONObject();
        try(SQLiteDatabase DB = this.getWritableDatabase())
        {
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=? LIMIT 1", new String[]{ContactID});
            while (cursor != null && cursor.moveToNext())
            {
                ContactJSON.put(PrivKey,cursor.getString(cursor.getColumnIndex(PrivKey)));
                ContactJSON.put(PubKey,cursor.getString(cursor.getColumnIndex(PubKey)));
                ContactJSON.put(Secret,cursor.getString(cursor.getColumnIndex(Secret)));
                ContactJSON.put(Confirmed,cursor.getInt(cursor.getColumnIndex(Confirmed)));
                ContactJSON.put(Salt,cursor.getInt(cursor.getColumnIndex(Salt)));
            }
            if(cursor!=null) {
                cursor.close();
            }
        }catch (JSONException | NullPointerException e){e.printStackTrace();}
        return ContactJSON;
    }

    public void DeleteAllChats(String ContactID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.delete(EncryptedTable, C_ID+"=?", new String[]{ContactID});
        DB.delete(LastChats, C_ID+"=?", new String[]{ContactID});
        Toast.makeText(context,context.getString(R.string.conversation_deleted)+ContactID, Toast.LENGTH_LONG).show();
    }

}
