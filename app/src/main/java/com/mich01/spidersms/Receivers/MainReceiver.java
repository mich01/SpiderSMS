package com.mich01.spidersms.Receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MainReceiver extends BroadcastReceiver {

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
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                        senderNum = currentMessage.getDisplayOriginatingAddress();
                        messageChunk.append(currentMessage.getDisplayMessageBody());
                    } // end for loop
                    message =messageChunk.toString();
                    Log.i("Total Msg",message);
                } // bundle is null
            } catch (Exception ignored) {
            }
            assert message != null;
            if(message.contains("|>"))
            {
                String DecryptedSMS = message;
                try {
                    DecryptedSMS = PKI_Cipher.Decode(DecryptedSMS);
                    JSONObject smsJSON = new JSONObject(DecryptedSMS);
                    senderNum = smsJSON.getString("target");
                    message = smsJSON.getString("Body");
                } catch (Exception e) {
                    //DecryptedSMS = PKI_Cipher.Decode(DecryptedSMS);
                    message =DecryptedSMS;
                }
                new DBManager(context).updateLastMessage(senderNum, message, 0, 0);
                new DBManager(context).AddChatMessage(senderNum,1,message,false);
                ChatActivity.PopulateChatView(context);
                ChatActivity.messageAdapter.notifyDataSetChanged();
                HomeActivity.PopulateChats(context);
                HomeActivity.adapter.notifyDataSetChanged();
                Toast.makeText(context, "Message from: "+senderNum,Toast.LENGTH_LONG).show();
            }
        }
    }
}