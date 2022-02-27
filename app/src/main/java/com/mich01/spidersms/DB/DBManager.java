package com.mich01.spidersms.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
        db.execSQL("create Table Contacts(CID TEXT primary key, ContactName TEXT, PubKey TEXT, PrivKey TEXT, CryptoAlg TEXT, StegKey TEXT, Secret TEXT, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("create Table EncryptedSMS(MessageID INTEGER primary key AUTOINCREMENT NOT NULL,SenderNumber TEXT,CID TEXT,inorout INTEGER, PubKey TEXT, CryptoAlg TEXT, StegKey TEXT,Timestamp)");
        db.execSQL("create Table LastChats(CID TEXT primary key,MessageText TEXT,inorout INTEGER,ReadStatus INEGER, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("drop table if exists UserProfile");
        db.execSQL("drop table if exists Contacts");
        db.execSQL("drop table if exists EncryptedSMS");
    }
    public boolean insertUserDetails(String CID, String PhoneNo, String UserName, String ServerURL)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CID", CID);
        contentValues.put("PhoneNo", PhoneNo);
        contentValues.put("UserName", UserName);
        contentValues.put("ServerURL", ServerURL);
        long result = DB.insert("UserProfile", null, contentValues);
        if(result==-1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    //Add Contacts
    public boolean insertNewContact(JSONObject ContactObject)
    {
        boolean status = false;
        try
        {
            String CID = ContactObject.getString("CID");
            SQLiteDatabase DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("CID", CID);
            contentValues.put("PubKey", ContactObject.getString("PubKey"));
            contentValues.put("PrivKey", ContactObject.getString("PrivKey"));
            contentValues.put("ContactName", ContactObject.getString("CName"));
            contentValues.put("StegKey", ContactObject.getString("StegKey"));
            contentValues.put("CryptoAlg", ContactObject.getString("CryptoAlg"));
            contentValues.put("Secret", ContactObject.getString("Secret"));
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=? LIMIT 1", new String[] {CID});
            Log.i("Full Contact",contentValues.toString());
            long result = DB.insert("Contacts", null, contentValues);
            if (result == -1) {
            } else {
            }
            status = true;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            status = false;
        }
        return status;
    }
    public boolean AddContact(JSONObject ContactObject)
    {
        boolean status = false;
        try
        {
            String CID = ContactObject.getString("CID");
            SQLiteDatabase DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("CID", CID);
            contentValues.put("PubKey", ContactObject.getString("PubKey"));
            contentValues.put("ContactName", ContactObject.getString("CName"));
            contentValues.put("PrivKey", "0000");
            contentValues.put("StegKey", "0000");
            contentValues.put("CryptoAlg", "0000");
            contentValues.put("Secret", "0000");
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=?", new String[] {CID});
            if(cursor.getCount()>0)
            {
               insertNewContact(ContactObject);
            }
            else
            {
                long result = DB.insert("Contacts", null, contentValues);
                if(result==-1)
                {
                    status= false;
                }
                else
                {
                    status= true;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            status = false;
        }
        return status;
    }
    public Cursor getContacts()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from Contacts", null);
        return cursor;
    }
    //ChatsTable
    public boolean AddChatMessage(String CID, int InorOut, String ChatMessage, boolean Status)
    {
        int Status_Int;
        if(Status==true)
        {
            Status_Int =1;
        }
        else
        {
            Status_Int=0;
        }
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CID", CID);
        contentValues.put("MessageBody", ChatMessage);
        contentValues.put("inorout", InorOut);
        contentValues.put("Status",Status_Int);
        long result = DB.insert("Chats", null, contentValues);
        if(result==-1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    public Cursor getCIDChats(String CID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from Chats where CID=? Order By Timestamp ASC", new String[]{CID});
        return cursor;
    }
    public int DeleteContact(String CID)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        int result = DB.delete("Contacts", "CID=?", new String[]{CID});
        Toast.makeText(context,"Contact Deleted: "+CID, Toast.LENGTH_LONG).show();
        return result;
    }
    public int UpdateContact(String CID, JSONObject ContactDetails)
    {
        int result =0;
        String OLD_CID = null;
        String NEW_CID = null;
            String Secret = null;
        try
        {
            OLD_CID = ContactDetails.getString("OCID");
            ContentValues contentValues = new ContentValues();
            contentValues.put("CID", ContactDetails.getString("NewCID"));
            contentValues.put("PubKey", ContactDetails.getString("PubKey"));
            contentValues.put("ContactName", ContactDetails.getString("CName"));
            contentValues.put("Secret", ContactDetails.getString("NewSecret"));
            contentValues.put("StegKey", ContactDetails.getString("StegKey"));
            contentValues.put("CryptoAlg", ContactDetails.getString("Alg"));
            SQLiteDatabase DB = this.getWritableDatabase();
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=? LIMIT 1", new String[]{OLD_CID});
                while (cursor != null && cursor.moveToNext())
                {
                    //Log.i("Contact Update: ", OLD_CID+" ---- Hash "+ContactDetails.getString("Secret"+" Compare: "+ComputeHash(cursor.getString(cursor.getColumnIndex("Secret")))));
                    /*if(ComputeHash(cursor.getString(cursor.getColumnIndex("Secret"))).equals(ContactDetails.getString("Secret")))
                    {
                        SQLiteDatabase DBUpdateContact = this.getWritableDatabase();
                        SQLiteDatabase DBUpdateChats = this.getWritableDatabase();
                        result = DBUpdateContact.update("Contacts", contentValues, "CID=?", new String[]{OLD_CID});
                        ContentValues chatValues = new ContentValues();
                        chatValues.put("CID",CID);
                        result = DBUpdateChats.update("Chats", chatValues, "CID=?", new String[]{OLD_CID});
                        Toast.makeText(context.getApplicationContext(), ContactDetails.getString("CName")+" Has Updated their Contacts", Toast.LENGTH_LONG).show();
                    }*/

                        SQLiteDatabase DBUpdateContact = this.getWritableDatabase();
                        SQLiteDatabase DBUpdateChats = this.getWritableDatabase();
                        result = DBUpdateContact.update("Contacts", contentValues, "CID=?", new String[]{OLD_CID});
                        ContentValues chatValues = new ContentValues();
                        chatValues.put("CID",CID);
                        result = DBUpdateChats.update("Chats", chatValues, "CID=?", new String[]{OLD_CID});
                        Toast.makeText(context.getApplicationContext(), ContactDetails.getString("CName")+" Has Updated their Contacts", Toast.LENGTH_LONG).show();

                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    public Cursor getLastChatList()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select LastChats.CID, " +
                "LastChats.MessageText, " +
                "LastChats.Timestamp, " +
                "LastChats.ReadStatus, " +
                "LastChats.inorout, " +
                "Contacts.ContactName  " +
                "from LastChats LEFT JOIN Contacts on  Contacts.CID = LastChats.CID Order By LastChats.Timestamp DESC",null);
        return cursor;
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
        Log.i("Current ID: ",CID);
        Cursor ChatCursor = DB.rawQuery("select * from LastChats where CID=?", new String[]{CID});
        if (ChatCursor.getCount() > 0) {
            long result = DB.update("LastChats", contentValues, "CID=?", new String[]{CID});
            if (result == -1) {
                status = false;
            }
            else
            {
                status = true;
            }
        }
        else
        {
            long result = DB.insert("LastChats", null, contentValues);
            if (result == -1) {
                status =false;
            }
            else
            {
                status = true;
            }
        }
        return status;
    }
    public boolean UpdatePhoneBook(JSONObject ContactObject)
    {
        boolean status = false;
        try
        {
            String CID = ContactObject.getString("CID");
            SQLiteDatabase DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("CID", CID);
            contentValues.put("PubKey", "000000");
            contentValues.put("ContactName", ContactObject.getString("ContactName"));
            contentValues.put("PrivKey", "0000");
            contentValues.put("StegKey", "0000");
            contentValues.put("CryptoAlg", "0000");
            contentValues.put("Secret", "0000");
            Cursor cursor = DB.rawQuery("select * from Contacts where CID=?", new String[] {CID});
            if(cursor.getCount()<1)
            {
                long result = DB.insert("Contacts", null, contentValues);
                if(result==-1)
                {
                    status= false;
                }
                else
                {
                    status= true;
                }
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
}
