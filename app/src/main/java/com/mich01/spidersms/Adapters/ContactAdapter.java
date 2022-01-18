package com.mich01.spidersms.Adapters;


import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


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
        ImageView images = contact_row.findViewById(R.id.contact_img);
        TextView contactID = contact_row.findViewById(R.id.contact_id);
        TextView status = contact_row.findViewById(R.id.contact_status);
        images.setImageResource(C_Imgs.get(position));
        contactID.setText(C_Names.get(position));
        status.setText(C_Status.get(position));
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
                    Intent smsIntent = new Intent(context, ChatActivity.class);
                    smsIntent.putExtra("ContactID", C_ID.get(position));
                    smsIntent.putExtra("ContactName", C_Status.get(position));
                    smsIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
                    smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //((ContactsActivity)getContext()).finish();
                    if (ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.SEND_SMS) !=
                            PackageManager.PERMISSION_GRANTED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //((ContactsActivity)getContext()).requestPermissions(new String[]{Manifest.permission.SEND_SMS},1111);
                            //context.getApplicationContext().requestPermissions(new String[]{Manifest.permission.SEND_SMS},1111);
                        }
                    }
                    else
                    {
                        /*Intent smsIntent = new Intent(context, ChatActivity.class);
                        smsIntent.putExtra("ContactID", C_ID.get(position));
                        smsIntent.putExtra("ContactName", C_Status.get(position));
                        smsIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
                        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(smsIntent);*/
                    }

                    }
                }
        });
        contact_row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if(is_App.get(position)==1)
                {
                    JSONObject ContactJson = new JSONObject();
                    try {
                        ContactJson.put("Data","Contact");
                        ContactJson.put("CID", MyPrefs.getString("CID",null));
                        ContactJson.put("CName", MyPrefs.getString("UserName",null));
                        ContactJson.put("PubKey", MyPrefs.getString("PublicKey",null));
                        ContactJson.put("StegKey", MyPrefs.getString("StegKey",null));
                        ContactJson.put("Alg", MyPrefs.getString("CryptoAlg",null));
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
                    ((ContactsActivity)context).finish();
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
            contact_row.setBackgroundColor(0xfff2f2f2);
        }
        return contact_row;
    }
}
