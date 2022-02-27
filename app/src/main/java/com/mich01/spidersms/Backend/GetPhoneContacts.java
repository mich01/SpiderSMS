package com.mich01.spidersms.Backend;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
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
            while (cur != null && cur.moveToNext())
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
                    ContactsActivity.ContactNames.add(name);
                    ContactsActivity.CID.add(phoneNo);
                    ContactsActivity.ContactImgs.add(R.drawable.contact_icon);
                    ContactsActivity.ContactStatus.add(phoneNo);
                    ContactsActivity.CType.add(0);
                    JSONObject ContactJSON = new JSONObject();
                    try {
                        ContactJSON.put("CID",phoneNo);
                        ContactJSON.put("ContactName",name);
                        new DBManager(ContactContext).UpdatePhoneBook(ContactJSON);
                    }
                    catch (Exception e){}
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }
    public void getMyContacts(Context context)
    {
        ContactsActivity.ContactNames.clear();
        ContactsActivity.CID.clear();
        ContactsActivity.ContactImgs.clear();
        ContactsActivity.ContactStatus.clear();
        int index=0;
        Cursor cur = new DBManager(context).getContacts();
        while (cur != null && cur.moveToNext())
        {
            @SuppressLint("Range") String CID = cur.getString(cur.getColumnIndex("CID"));
            @SuppressLint("Range") String Name = cur.getString(cur.getColumnIndex("ContactName"));
            String ContactStatus = CID;
            ContactsActivity.ContactNames.add(Name);
            ContactsActivity.CID.add(CID);
            ContactsActivity.ContactImgs.add(R.drawable.contact_icon);
            ContactsActivity.ContactStatus.add(ContactStatus);
            ContactsActivity.CType.add(1);
            index++;
        }
    }
}
