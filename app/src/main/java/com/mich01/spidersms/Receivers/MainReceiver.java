package com.mich01.spidersms.Receivers;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Crypto.KeyExchange;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.SplashActivity;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.HomeActivity;

import org.json.JSONObject;

public class MainReceiver extends BroadcastReceiver {
    public static Uri notificationMessageSound;
    public static NotificationManager manager;
    public static NotificationCompat.Builder notificationBuilder;
    public static Notification notification;
    @SuppressLint("NotifyDataSetChanged")
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String senderNum = null;
        StringBuilder messageChunk =new StringBuilder();
        String message =null;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            // Retrieves a map of extended data from the intent.
            final Bundle bundle = intent.getExtras();
            try {
                if (bundle != null)
                {
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");
                    assert pdusObj != null;
                    for (Object aPdusObj : pdusObj)
                    {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj );
                        senderNum = currentMessage.getDisplayOriginatingAddress();
                        messageChunk.append(currentMessage.getMessageBody());
                    } // end for loop
                    message =messageChunk.toString();
                } // bundle is null
            } catch (Exception ignored) {
            }
            assert message != null;
            if(message.startsWith(">"))
            {
                try
                {
                    JSONObject contactJSON = new DBManager(context).GetContact(senderNum);
                    String DecryptionKey;
                    String DecryptedSMS;
                    if(contactJSON.length()>0 && contactJSON.getString("Confirmed").equals("1"))
                    {
                        Log.i("OPT Msg", "Using Confirmed Key");
                        DecryptionKey = contactJSON.getString("PrivKey");
                        Log.i("Key Encr",DecryptionKey);
                        DecryptedSMS = new PKI_Cipher(context).Decrypt(message.replace( ">",""),DecryptionKey);
                    }
                    else
                    {
                        Log.i("OPT 2","here here");
                        MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( ">",""));
                        Log.i("OPT 2",DecryptedSMS);
                    }
                    Log.i("Message Arrived",DecryptedSMS);
                    JSONObject smsJSON = new JSONObject(DecryptedSMS);
                    if(smsJSON.getString("x").equals("1"))
                    {
                        new BackendFunctions().UpdateMessage(context, senderNum, smsJSON.getString("Body"));
                    }
                    else if(smsJSON.getString("x").equals("2"))
                    {
                        new BackendFunctions().UpdateMessage(context, smsJSON.getString("target"), smsJSON.getString("Body"));
                    }
                    else if(smsJSON.getString("x").equals("3"))
                    {
                        Log.i("Key Step 4","Message Received ");
                        smsJSON.getString("target");
                        smsJSON.getString("SecretKey");
                        smsJSON.getString("Secret");
                        smsJSON.put("KeyStage","2");
                        new DBManager(context).UpdateContactSpec(smsJSON);
                    }
                    else if(smsJSON.getString("x").equals("4"))
                    {
                        Log.i("Key Step 4","Message Received ");
                        smsJSON.getString("target");
                        smsJSON.put("KeyStage","3");
                        smsJSON.getString("Secret");
                        Log.i("OPT 4",smsJSON.toString());
                        new KeyExchange(context).VerifyContact(smsJSON);
                    }
                    else if(smsJSON.getString("x").equals("5"))
                    {
                        smsJSON.getString("Secret");
                        smsJSON.put("KeyStage","5");
                        Log.i("Key Step 6 ","Contact To be Verified "+smsJSON.toString());
                        new KeyExchange(context).VerifyContact(smsJSON);
                    }
                    else if(smsJSON.getString("x").equals("6"))
                    {
                        smsJSON.getString("target");
                        smsJSON.put("KeyStage","");
                        smsJSON.getString("Secret");
                    }
                } catch (Exception e)
                {
                    Log.i("OPT Error",e.getMessage());
                }
                //
            }
        }
    }
}