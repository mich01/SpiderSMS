package com.mich01.spidersms.Adapters;


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

import com.amulyakhare.textdrawable.TextDrawable;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.Data.Contact;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.DataQRGenerator;
import com.mich01.spidersms.UI.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact>
{
    static ArrayList<Contact> MyContact;
    Context context;

    public ContactAdapter(@NonNull Context c, int resource, @NonNull ArrayList<Contact> contacts) {
        super(c, resource, contacts);
        MyContact =contacts;
        context =c;
        //Log.i("Chat Lists: ","Construct---- and count: "+contacts.size());


    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.R)
    @NonNull
    @Override
    public View getView(int position, View convertView,  ViewGroup parent)
    {
        if(convertView ==null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
        }
        TextDrawable drawable = TextDrawable.builder().buildRect(String.valueOf(MyContact.get(position).getContactNames().charAt(0)), R.id.contact_img);
        ImageView images = convertView.findViewById(R.id.contact_img);
        TextView contactID = convertView.findViewById(R.id.contact_id);
        TextView status = convertView.findViewById(R.id.contact_status);
        images.setImageDrawable(drawable);
        contactID.setText(MyContact.get(position).getContactNames());
        status.setText(MyContact.get(position).getCID());
        convertView.setBackgroundResource(R.color.in_app);
        convertView.setOnClickListener(v -> {
            if(MyContact.get(position).getCType()==1)
            {
                Toast.makeText(context, MyContact.get(position).getContactNames(), Toast.LENGTH_LONG).show();
                Intent ChatIntent = new Intent(context, ChatActivity.class);
                ChatIntent.putExtra("ContactID", MyContact.get(position).getCID());
                ChatIntent.putExtra("ContactName", MyContact.get(position).getContactNames());
                ChatIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
                ChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ChatIntent);
                //eRROR REPORTED HERE
                ((ContactsActivity)getContext()).finish();
            }
            else
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getRootView().getContext());
                alert.setTitle("Send Invite to "+MyContact.get(position).getContactNames());
                alert.setPositiveButton("Send Invite", (dialog, whichButton) -> {
                    String AppURL = context.getString(R.string.share_app);
                    new SMSHandler(v.getRootView().getContext()).sendPlainSMS(MyContact.get(position).getCID(),AppURL);
                });

                alert.setNegativeButton("Cancel",
                        (dialog, whichButton) -> {
                        });
                alert.show();
                }
            });
        convertView.setOnLongClickListener(v -> {
            if(MyContact.get(position).getCType()==1)
            {
                JSONObject ContactJson = new JSONObject();
                try {
                    ContactJson.put("Data","Contact");
                    ContactJson.put("CID",  MyContact.get(position).getCID());
                    ContactJson.put("CName", MyContact.get(position).getContactNames());
                    ContactJson.put("PubKey", MyContact.get(position).getPubKey());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent QRIntent = new Intent(context, DataQRGenerator.class);
                QRIntent.putExtra("Contact", ContactJson.toString());
                QRIntent.putExtra("SContactID", MyContact.get(position).getCID());
                QRIntent.putExtra("CID", MyContact.get(position).getCID());
                QRIntent.putExtra("ContactName", MyContact.get(position).getContactNames());
                QRIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(QRIntent);
                //((ContactsActivity)context).finish();
            }
            else
            {
                Toast.makeText(context,"You cant Share this Contact" , Toast.LENGTH_LONG).show();
            }
            return false;
        });
        if(MyContact.get(position).getCType()==0)
        {

            contactID.setTypeface(null, Typeface.NORMAL);
            status.setTypeface(null, Typeface.NORMAL);
            contactID.setTextColor(context.getResources().getColor(R.color.white));
            status.setTextColor(Color.GRAY);
            convertView.setBackgroundResource(R.color.not_in_app);
            images.setBackground(context.getResources().getDrawable(R.drawable.background_gradient));
        }
        else
        {
            images.setBackgroundResource(R.color.gold);
            convertView.setBackgroundResource(R.color.in_app);
            contactID.setTextColor(context.getResources().getColor(R.color.gold));
            status.setTextColor(context.getResources().getColor(R.color.white));
        }
        return convertView;
    }
}
