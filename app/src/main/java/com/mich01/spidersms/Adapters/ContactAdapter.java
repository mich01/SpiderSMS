package com.mich01.spidersms.Adapters;


import static com.mich01.spidersms.Data.StringsConstants.C_ID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.Contact;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.DataQRGenerator;
import com.mich01.spidersms.UI.HomeActivity;
import com.mich01.spidersms.UI.TextDrawable.TextDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact>
{
    ArrayList<Contact> myContact;
    Context context;

    public ContactAdapter(@NonNull Context c, int resource, @NonNull ArrayList<Contact> contacts) {
        super(c, resource, contacts);
        this.myContact = contacts;
        context =c;
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @NonNull
    @Override
    public View getView(int position, View convertView,  ViewGroup parent)
    {
        char contactInitials ='-';
        if(convertView ==null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
        }
        if(myContact.get(position).getContactNames()!=null)
        {
            contactInitials = myContact.get(position).getContactNames().charAt(0);
        }
        TextDrawable drawable = TextDrawable.builder().buildRect(String.valueOf(contactInitials), R.id.contact_img);
        ImageView images = convertView.findViewById(R.id.contact_img);
        TextView contactID = convertView.findViewById(R.id.contact_id);
        TextView status = convertView.findViewById(R.id.contact_status);
        images.setImageDrawable(drawable);
        contactID.setText(myContact.get(position).getContactNames());
        status.setText(myContact.get(position).getCID());
        convertView.setBackgroundResource(R.color.in_app);
        convertView.setOnClickListener(v -> {
            if(myContact.get(position).getCType()==1)
            {
                Toast.makeText(context, myContact.get(position).getContactNames(), Toast.LENGTH_LONG).show();
                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra("ContactID", myContact.get(position).getCID());
                chatIntent.putExtra("ContactName", myContact.get(position).getContactNames());
                chatIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
                chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chatIntent);
                //ERROR REPORTED HERE
                ((ContactsActivity)getContext()).finish();
            }
            else
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getRootView().getContext());
                alert.setTitle("SPerform Action "+myContact.get(position).getContactNames());
                alert.setPositiveButton("INVITE", (dialog, whichButton) -> {
                    String appURL = context.getString(R.string.share_app);
                    new SMSHandler(v.getRootView().getContext()).sendPlainSMS(myContact.get(position).getCID(),appURL);
                });

                alert.setNeutralButton("DELETE",
                        (dialog, whichButton) -> {
                    new DBManager(context).DeleteContact(myContact.get(position).getCID());
                    HomeActivity.rePopulateChats(context);
                    ContactsActivity.repopulateContacts(context);
                    dialog.dismiss();
                });
                alert.show();
                alert.setNegativeButton("CANCEL",
                        (dialog, whichButton) -> {
                        });
                alert.show();
                }
            });
        convertView.setOnLongClickListener(v -> {
            if(myContact.get(position).getCType()==1)
            {
                JSONObject contactJson = new JSONObject();
                try {
                    contactJson.put("Data","Contact");
                    contactJson.put(C_ID,  myContact.get(position).getCID());
                    contactJson.put("CName", myContact.get(position).getContactNames());
                    contactJson.put("PubKey", myContact.get(position).getPubKey());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent qrIntent = new Intent(context, DataQRGenerator.class);
                qrIntent.putExtra("Contact", contactJson.toString());
                qrIntent.putExtra("SContactID", myContact.get(position).getCID());
                qrIntent.putExtra(C_ID, myContact.get(position).getCID());
                qrIntent.putExtra("ContactName", myContact.get(position).getContactNames());
                qrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(qrIntent);
                ((ContactsActivity)context).finish();
            }
            else
            {
                Toast.makeText(context,"You cant Share this Contact" , Toast.LENGTH_LONG).show();
            }
            return false;
        });
        if(myContact.get(position).getCType()==0)
        {

            contactID.setTypeface(null, Typeface.NORMAL);
            status.setTypeface(null, Typeface.NORMAL);
            contactID.setTextColor(ContextCompat.getColor(context,R.color.white));
            status.setTextColor(Color.GRAY);
            convertView.setBackgroundResource(R.color.not_in_app);
            images.setBackground(ContextCompat.getDrawable(context,R.drawable.background_gradient));
        }
        else
        {
            images.setBackgroundResource(R.color.gold);
            convertView.setBackgroundResource(R.color.in_app);
            contactID.setTextColor(ContextCompat.getColor(context,R.color.gold));
            status.setTextColor(ContextCompat.getColor(context,R.color.white));
        }
        return convertView;
    }
}
