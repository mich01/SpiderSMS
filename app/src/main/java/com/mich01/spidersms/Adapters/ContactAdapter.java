package com.mich01.spidersms.Adapters;


import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;


import com.amulyakhare.textdrawable.TextDrawable;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.DataQRGenerator;
import com.mich01.spidersms.UI.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<String>
{
    ArrayList<String> C_ID;
    ArrayList<String> C_Names;
    ArrayList<String> C_Status;
    ArrayList<Integer> C_Imgs;
    ArrayList<Integer> is_App;
    Context context;
    public ContactAdapter(Context c, ArrayList<String> CID, ArrayList<String> CNames, ArrayList<String> Status, ArrayList<Integer> imgs, ArrayList<Integer> CType)
    {
        super(c, R.layout.contact_item, R.id.contact_id,CID);
        this.context = c;
        this.C_ID =CID;
        this.C_Names =CNames;
        this.C_Status = Status;
        this.C_Imgs = imgs;
        this.is_App = CType;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView,  ViewGroup parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contact_row  = layoutInflater.inflate(R.layout.contact_item, parent, false);
        TextDrawable drawable = TextDrawable.builder().buildRect(String.valueOf(C_Names.get(position).charAt(0)), R.id.contact_img);
        ImageView images = contact_row.findViewById(R.id.contact_img);
        TextView contactID = contact_row.findViewById(R.id.contact_id);
        TextView status = contact_row.findViewById(R.id.contact_status);
        images.setImageDrawable(drawable);
        contactID.setText(C_Names.get(position));
        status.setText(C_Status.get(position));
        contact_row.setBackgroundResource(R.color.in_app);
        contact_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(is_App.get(position)==1)
                {
                    Toast.makeText(context, C_ID.get(position), Toast.LENGTH_LONG).show();
                    Intent ChatIntent = new Intent(context, ChatActivity.class);
                    ChatIntent.putExtra("ContactID", C_ID.get(position));
                    ChatIntent.putExtra("ContactName", C_Names.get(position));
                    ChatIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
                    ChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(ChatIntent);
                    ((ContactsActivity)getContext()).finish();
                }
                else
                {
                    Toast.makeText(context, C_ID.get(position), Toast.LENGTH_LONG).show();
                    Intent SMSIntent = new Intent(context, ChatActivity.class);
                    SMSIntent.putExtra("ContactID", C_ID.get(position));
                    SMSIntent.putExtra("ContactName", C_Names.get(position));
                    SMSIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
                    SMSIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(SMSIntent);
                    ((ContactsActivity)getContext()).finish();

                    }
                }
        });
        contact_row.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public boolean onLongClick(View v)
            {
                if(is_App.get(position)==1)
                {
                    JSONObject ContactJson = new JSONObject();
                    try {
                        ContactJson.put("Data","Contact");
                        ContactJson.put("CID",  C_ID.get(position));
                        ContactJson.put("CName", C_Names.get(position));
                        //ContactJson.put("PubKey", MyPrefs.getString("PublicKey",null));
                        //ContactJson.put("StegKey", MyPrefs.getString("StegKey",null));
                        //ContactJson.put("Alg", MyPrefs.getString("CryptoAlg",null));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent QRIntent = new Intent(context, DataQRGenerator.class);
                    QRIntent.putExtra("Contact", ContactJson.toString());
                    QRIntent.putExtra("SContactID", C_ID.get(position));
                    QRIntent.putExtra("CID", C_ID.get(position));
                    QRIntent.putExtra("ContactName", C_Names.get(position));
                    QRIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(QRIntent);
                    //((ContactsActivity)context).finish();
                }
                else
                {
                    Toast.makeText(context,"You cant Share this Contact" , Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        if(is_App.get(position)==0)
        {

            contactID.setTypeface(null, Typeface.NORMAL);
            status.setTypeface(null, Typeface.NORMAL);
            contactID.setTextColor(context.getResources().getColor(R.color.white));
            status.setTextColor(Color.GRAY);
            images.setBackground(context.getResources().getDrawable(R.drawable.background_gradient));
        }
        else
        {
            images.setBackgroundResource(R.color.gold);
            contact_row.setBackgroundResource(R.color.in_app);
            contactID.setTextColor(context.getResources().getColor(R.color.gold));
            status.setTextColor(context.getResources().getColor(R.color.white));
        }
        return contact_row;
    }
}
