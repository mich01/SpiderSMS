package com.mich01.spidersms.Receivers;

import static com.mich01.spidersms.Data.StringsConstants.APPEND_PARAM;
import static com.mich01.spidersms.Data.StringsConstants.AppTrigger;
import static com.mich01.spidersms.Data.StringsConstants.Body;
import static com.mich01.spidersms.Data.StringsConstants.C_ID;
import static com.mich01.spidersms.Data.StringsConstants.Confirmed;
import static com.mich01.spidersms.Data.StringsConstants.ContactTarget;
import static com.mich01.spidersms.Data.StringsConstants.KeyConfirmTrigger;
import static com.mich01.spidersms.Data.StringsConstants.MAIN_INTENT;
import static com.mich01.spidersms.Data.StringsConstants.MessageType;
import static com.mich01.spidersms.Data.StringsConstants.PrivKey;
import static com.mich01.spidersms.Data.StringsConstants.Pub_Key;
import static com.mich01.spidersms.Data.StringsConstants.Salt;
import static com.mich01.spidersms.Data.StringsConstants.Secret;
import static com.mich01.spidersms.Data.StringsConstants.SecretKey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Crypto.KeyExchange;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MMSReceiver extends BroadcastReceiver
{
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String senderNum = null;
        String DecryptedSMS;
        StringBuilder messageChunk =new StringBuilder();
        String message =null;
        if (intent.getAction().equals(MAIN_INTENT))
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
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj,bundle.getString("format") );
                        senderNum = currentMessage.getDisplayOriginatingAddress();
                        messageChunk.append(currentMessage.getMessageBody());
                    } // end for loop
                    message =messageChunk.toString();
                } // bundle is null
            } catch (Exception ignored) {}
            assert message != null;
            if(message.startsWith(AppTrigger))
            {
                decryptPrivateKeyConversation(senderNum, message, context);
            }
            else if(message.startsWith(KeyConfirmTrigger))
            {
                decryptPKIConversation(senderNum, message, context);
            }
            else if(message.startsWith(APPEND_PARAM))
            {
                decryptPublicProxiedSMS(senderNum, message, context);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void decryptPublicProxiedSMS(String senderNum, String message, Context context)
    {
        String DecryptedSMS;
        JSONObject smsJSON;
        try {
            DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( APPEND_PARAM,""));
            smsJSON = new JSONObject(DecryptedSMS);
            ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);
        } catch (JSONException ignored){}
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void decryptPKIConversation(String senderNum, String message, Context context)
    {
        String DecryptedSMS;
        try
        {
            JSONObject smsJSON;
            DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( KeyConfirmTrigger,""));
            smsJSON = new JSONObject(DecryptedSMS);
            ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);

        } catch (Exception ignored){}
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void decryptPrivateKeyConversation(String senderNum, String message, Context context)
    {
        String DecryptedSMS;
        try
        {
            JSONObject contactJSON = new DBManager(context).GetContact(senderNum);
            String DecryptionKey;
            JSONObject smsJSON;
            if(contactJSON.length()>0 && contactJSON.getInt(Confirmed)==1)
            {
                DecryptionKey = contactJSON.getString(PrivKey);
                String AES_Salt = contactJSON.getString(Salt);
                String IV = contactJSON.getString(Secret);
                DecryptedSMS = new PKI_Cipher(context).Decrypt(message.replace( AppTrigger,""),DecryptionKey, AES_Salt,IV);
                smsJSON = new JSONObject(DecryptedSMS);
                ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);
            }
            else
            {
                DecryptedSMS = new PKI_Cipher(context).DecryptPKI(message.replace( AppTrigger,""));
                smsJSON = new JSONObject(DecryptedSMS);
                ProcessMessage(context.getApplicationContext(),senderNum,smsJSON);
            }
        } catch (Exception ignored){}
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void ProcessMessage(Context context, String senderNum, JSONObject smsJSON)
    {
        try {
            if(smsJSON.getInt(MessageType)==1)
            {
                new BackendFunctions().updateMessage(context, senderNum, smsJSON.getString(Body));
            }
            else if(smsJSON.getInt(MessageType)==2)
            {
                new BackendFunctions().updateMessage(context, smsJSON.getString(ContactTarget), smsJSON.getString(Body));
            }
            else if(smsJSON.getInt(MessageType)==3)
            {
                smsJSON.getString(ContactTarget);
                smsJSON.getString(SecretKey);
                smsJSON.getString(Secret);
                new DBManager(context).UpdateContactSpec(smsJSON);
            }
            else if(smsJSON.getInt(MessageType)==4)
            {
                new KeyExchange(context).VerifyContact(smsJSON);
            }
            else if(smsJSON.getInt(MessageType)==5)
            {
                smsJSON.getString(Secret);
                new DBManager(context).VerifyContactPK(smsJSON.getString(ContactTarget), smsJSON.getString(Secret));
            }
            else if(smsJSON.getInt(MessageType)==6)
            {
                smsJSON.getString(ContactTarget);
                smsJSON.getString(Secret);
            }
            else if(smsJSON.getInt(MessageType)==7)
            {
                new KeyExchange(context).RespondWithKey(smsJSON.getString(ContactTarget));
            }
            else if(smsJSON.getInt(MessageType)==8)
            {
                smsJSON.put(C_ID,smsJSON.getString(ContactTarget));
                new DBManager(context).UpdateContact(smsJSON, smsJSON.getString(Pub_Key).replace("\"\"", "").replace("\\++", APPEND_PARAM).replace(" ", ""));
            }
        } catch (JSONException ignored){}
    }
}
