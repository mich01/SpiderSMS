package com.mich01.spidersms.Backend;

import static com.mich01.spidersms.Data.StringsConstants.C_ID;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.DEFAULT_PREF_VALUE;
import static com.mich01.spidersms.Data.StringsConstants.PubKey;

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
        String phoneNo = "";
        ContentResolver cr = ContactContext.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0)
        {
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
                    loadPhoneContacts(phoneNo, name, ContactContext);
                }
            }

        }
        if(cur!=null){
            cur.close();
        }
    }

    private void loadPhoneContacts(String phoneNo, String name, Context ContactContext) {
        JSONObject ContactJSON = new JSONObject();
        try {
            assert phoneNo != null;
            ContactJSON.put(C_ID,phoneNo.replace(" ",""));
            ContactJSON.put(ContactName,name);
            if(new DBManager(ContactContext).GetContact(phoneNo.replace(" ","")).length()<1)
            {
                ContactsActivity.contacts.add(new Contact(phoneNo, name, DEFAULT_PREF_VALUE, 0));
                new DBManager(ContactContext).UpdatePhoneBook(ContactJSON);
            }
        }
        catch (Exception ignored){}
    }

    @SuppressLint("Range")
    public void getMyContacts(Context context)
    {
        ContactsActivity.contacts.clear();
        Cursor cur = new DBManager(context).getContacts();
        assert cur!= null;
        while (cur.moveToNext())
        {
            @SuppressLint("Range") String CID = cur.getString(cur.getColumnIndex(C_ID));
            @SuppressLint("Range") String Name = cur.getString(cur.getColumnIndex(ContactName));
            int ContactType;
            if (cur.getString(cur.getColumnIndex(PubKey))!=null && cur.getString(cur.getColumnIndex(PubKey)).equals(DEFAULT_PREF_VALUE))
            {
                ContactType =0;
            }else
            {
                ContactType =1;
            }
            ContactsActivity.contacts.add(new Contact(CID,Name
                    , cur.getString(cur.getColumnIndex(PubKey)), ContactType));
        }
        cur.close();
    }
}
