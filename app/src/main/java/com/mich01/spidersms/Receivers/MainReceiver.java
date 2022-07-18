package com.mich01.spidersms.Receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Crypto.KeyExchange;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MainReceiver extends BroadcastReceiver {
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String senderNum = null;
        String DecryptedSMS=null;
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
                    JSONObject smsJSON;
                    if(contactJSON.length()>0 && contactJSON.getInt("Confirmed")==1)
                    {
                        DecryptionKey = contactJSON.getString("PrivKey");
                        String AES_Salt = contactJSON.getString("Salt");
                        String IV = contactJSON.getString("Secret");
                        DecryptedSMS = new PKI_Cipher(context).Decrypt(message.replace( ">",""),DecryptionKey, AES_Salt,IV);
                        smsJSON = new JSONObject(DecryptedSMS);
                        ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);
                    }
                    else
                    {
                        DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( ">",""));
                        smsJSON = new JSONObject(DecryptedSMS);
                        ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);
                    }
                } catch (Exception ex)
                {
                }
            }
            else if(message.startsWith("-"))
            {
                try
                {
                    JSONObject smsJSON;
                        DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( "-",""));
                        smsJSON = new JSONObject(DecryptedSMS);
                        ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);

                } catch (Exception e)
                {
                    DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( ">",""));
                }
            }
            else if(message.startsWith("+"))
            {
                JSONObject smsJSON = null;
                try {
                    DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( "+",""));
                    smsJSON = new JSONObject(DecryptedSMS);
                    ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void ProcessMessage(Context context, String senderNum, JSONObject smsJSON)
    {
        try {
            if(smsJSON.getInt("x")==1)
            {
                new BackendFunctions().UpdateMessage(context, senderNum, smsJSON.getString("Body"));
            }
            else if(smsJSON.getInt("x")==2)
            {
                new BackendFunctions().UpdateMessage(context, smsJSON.getString("t"), smsJSON.getString("Body"));
            }
            else if(smsJSON.getInt("x")==3)
            {
                smsJSON.getString("t");
                smsJSON.getString("SecretKey");
                smsJSON.getString("Secret");
                //smsJSON.getString("Salt");
                new DBManager(context).UpdateContactSpec(smsJSON);
            }
            else if(smsJSON.getInt("x")==4)
            {
                new KeyExchange(context).VerifyContact(smsJSON);
            }
            else if(smsJSON.getInt("x")==5)
            {
                smsJSON.getString("Secret");
                new DBManager(context).VerifyContactPK(smsJSON.getString("t"), smsJSON.getString("Secret"));
            }
            else if(smsJSON.getInt("x")==6)
            {
                smsJSON.getString("t");
                smsJSON.getString("Secret");
            }
            else if(smsJSON.getInt("x")==7)
            {
                new KeyExchange(context).RespondWithKey(smsJSON.getString("t"));
            }
            else if(smsJSON.getInt("x")==8)
            {
                smsJSON.put("CID",smsJSON.getString("t"));
                new DBManager(context).UpdateContact(smsJSON, smsJSON.getString("K").replaceAll("\"\"", "").replaceAll("\\++", "+").replace(" ", ""));
            }
        } catch (JSONException e) {
        }
    }
}