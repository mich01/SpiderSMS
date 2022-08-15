package com.mich01.spidersms.DB;

import static com.mich01.spidersms.Data.StringsConstants.DBName;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
    Context context;
    private static final int DATABASE_VERSION = 1;

    private DBHelper(Context context) {
        super(context, DBName, null, DATABASE_VERSION);
        this.context=context;
    }
    private static DBHelper instance = null;
    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
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
}
