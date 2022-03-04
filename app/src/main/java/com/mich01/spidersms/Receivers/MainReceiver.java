package com.mich01.spidersms.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.mich01.spidersms.DB.DBManager;

public class MainReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            // Retrieves a map of extended data from the intent.
            //abortBroadcast();
            final Bundle bundle = intent.getExtras();
            try {
                if (bundle != null) {

                    final Object[] pdusObj = (Object[]) bundle.get("pdus");
                    assert pdusObj != null;
                    for (Object aPdusObj : pdusObj) {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                        String senderNum = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();
                        if(message.contains("SM:||")||message.toLowerCase().contains(" "))
                        {
                            new DBManager(context).updateLastMessage(bundle.getString("ContactID"), message, 0, 0);
                            new DBManager(context).AddChatMessage(bundle.getString("ContactID"),1,message,false);
                            Toast.makeText(context, "Message from: "+senderNum,Toast.LENGTH_LONG).show();
                        }
                    } // end for loop
                } // bundle is null
            } catch (Exception ignored) {
            }
        }
        else
        {
            Log.i("Intent Received: ",intent.getAction());
        }
    }
}