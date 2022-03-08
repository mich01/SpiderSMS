package com.mich01.spidersms.Backend;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.Contact;
import com.mich01.spidersms.UI.ContactsActivity;

import org.json.JSONObject;


public class GetPhoneContacts
{

    @SuppressLint("Range")
    public void getPhoneContacts(Context ContactContext)
    {
        String phoneNo = null;
        ContentResolver cr = ContactContext.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        //int Index =ContactsActivity.ContactNames.length;
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext())
            {
                @SuppressLint("Range") String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext())
                    {
                        phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    }
                    pCur.close();
                    ContactsActivity.Contacts.add(new Contact(name,
                            phoneNo, "000000", 0));
                    JSONObject ContactJSON = new JSONObject();
                    try {
                        ContactJSON.put("CID",phoneNo);
                        ContactJSON.put("ContactName",name);
                        new DBManager(ContactContext).UpdatePhoneBook(ContactJSON);
                    }

                    catch (Exception e){e.printStackTrace();}
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }
    @SuppressLint("Range")
    public void getMyContacts(Context context)
    {
        ContactsActivity.Contacts.clear();
        Cursor cur = new DBManager(context).getContacts();
        while (cur != null && cur.moveToNext())
        {
            @SuppressLint("Range") String CID = cur.getString(cur.getColumnIndex("CID"));
            @SuppressLint("Range") String Name = cur.getString(cur.getColumnIndex("ContactName"));
            int ContactType;
            if (cur.getString(cur.getColumnIndex("PubKey")).equals("000000"))
            {
                ContactType =0;
            }else
            {
                ContactType =1;
            }
            ContactsActivity.Contacts.add(new Contact(CID,Name
                    , cur.getString(cur.getColumnIndex("PubKey")), ContactType));
        }
        assert cur != null;
        cur.close();
    }

}
