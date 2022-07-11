package com.mich01.spidersms.Backend;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SMSHandler {
    Context context;
    private static final int MAX_SMS_MESSAGE_LENGTH = 160;
    private static final String SMS_SENT = "SMS_SENT";
    private static final String SMS_DELIVERED = "SMS_DELIVERED";
    String AppTrigger = ">";
    String KeyConfirmTrigger = "-";
    String KeyRequestTrigger = "*";
    public SMSHandler(Context context)
    {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void sendPlainSMS(String PhoneNo, String SMSText)
    {
        if(SMSText.isEmpty() || PhoneNo.isEmpty())
        {
            ErrorAlert();
        }
        else
        {
            String SMS;
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, "Generic failure",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, "No service",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, "Null PDU",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, "Radio off",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(SMS_SENT));

            //---when the SMS has been delivered---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, "SMS delivered",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(context, "SMS not delivered",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(SMS_DELIVERED));
            MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            new DBManager(context).updateLastMessage(PhoneNo, SMSText, 1, 1);
            new DBManager(context).AddChatMessage(PhoneNo, 0, SMSText, 0);
            SmsManager manager = SmsManager.getDefault();
            PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE);
            PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE);
            SMS = KeyRequestTrigger.concat(SMSText);
            if(SMS.length()>=MAX_SMS_MESSAGE_LENGTH)
            {
                ArrayList<String> parts =manager.divideMessage(SMS);
                int numParts = parts.size();
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                for (int i = 0; i < numParts; i++)
                {
                    sentIntents.add(PendingIntent.getBroadcast(context, 0,  new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE));
                }
                manager.sendMultipartTextMessage(PhoneNo,null, parts, sentIntents, deliveryIntents);
            }
            else
            {
                manager.sendTextMessage(PhoneNo, null, SMS, piSend, piDelivered);
            }}
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendEncryptedSMS(String PhoneNo, JSONObject SMSText, String EncryptionKey,String AES_Salt, String IV, int CryptType) throws JSONException {
        if(SMSText.toString().isEmpty() || PhoneNo.isEmpty() || EncryptionKey.isEmpty())
        {
            ErrorAlert();
        }
        else
        {
            String SMS;
            //---when the SMS has been sent---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,1);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, "Generic failure",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, "No service",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, "Null PDU",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,0);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, "Radio off",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,0);
                            break;
                    }
                }
            }, new IntentFilter(SMS_SENT));

            //---when the SMS has been delivered---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, "SMS delivered",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,2);
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(context, "SMS not delivered",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,0);
                            break;
                    }
                }
            }, new IntentFilter(SMS_DELIVERED));
            if(SMSText.getInt("x")==1 || SMSText.getInt("x")==2) {
                SMSText.put("R", new Random().nextInt(26) + 'a');
            }
            SmsManager manager = SmsManager.getDefault();
            PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE);
            PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE);
            if(CryptType==1) {
                SMS =AppTrigger+new PKI_Cipher(context).EncryptPKI(SMSText.toString(), EncryptionKey);
            }else if(CryptType==3)
            {
                SMS =KeyConfirmTrigger+new PKI_Cipher(context).EncryptPKI(SMSText.toString(), EncryptionKey);
            }
            else
            {
                SMS = AppTrigger + new PKI_Cipher(context).Encrypt(SMSText.toString(), EncryptionKey, AES_Salt, IV);
            }
            if(SMS.length()>=MAX_SMS_MESSAGE_LENGTH)
            {
                ArrayList<String> parts =manager.divideMessage(SMS);
                int numParts = parts.size();
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                for (int i = 0; i < numParts; i++)
                {
                    sentIntents.add(PendingIntent.getBroadcast(context, 0,  new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE));
                }
                manager.sendMultipartTextMessage(PhoneNo,null, parts, sentIntents, deliveryIntents);
            }
            else
            {
                manager.sendTextMessage(PhoneNo, null, SMS, piSend, piDelivered);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void proxyEncryptedSMS(JSONObject SMSText, String EncryptionKey, String AES_Salt, String IV) throws JSONException {
        MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String ProxyNumber = MyPrefs.getString("ProxyNumber", "---");
        if(SMSText.toString().isEmpty() ||  EncryptionKey.isEmpty())
        {
            ErrorAlert();
        }
        else {
            //---when the SMS has been sent---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(ProxyNumber,SMSText,1);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, "Generic failure",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(ProxyNumber,SMSText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, "No service",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(ProxyNumber,SMSText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, "Null PDU",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(ProxyNumber,SMSText,0);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, "Radio off",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(ProxyNumber,SMSText,0);
                            break;
                    }
                }
            }, new IntentFilter(SMS_SENT));

            //---when the SMS has been delivered---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, "SMS delivered",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(ProxyNumber,SMSText,2);
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(context, "SMS not delivered",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(ProxyNumber,SMSText,0);
                            break;
                    }
                }
            }, new IntentFilter(SMS_DELIVERED));
            SmsManager manager = SmsManager.getDefault();
            PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE);
            PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE);
            String SMS = AppTrigger.concat(new PKI_Cipher(context).Encrypt(SMSText.toString(), EncryptionKey, AES_Salt,IV));
            SMSText.put("R", new Random().nextInt(26) + 'a');
            if (SMS.length() >= MAX_SMS_MESSAGE_LENGTH) {
                ArrayList<String> parts = manager.divideMessage(SMS);
                int numParts = parts.size();
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                for (int i = 0; i < numParts; i++) {
                    sentIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE));
                }
                manager.sendMultipartTextMessage(ProxyNumber, null, parts, sentIntents, deliveryIntents);
            } else {
                manager.sendTextMessage(ProxyNumber, null, SMS, piSend, piDelivered);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void SendSMSOnline(String PhoneNo, JSONObject SMSText, String EncryptionKey) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, JSONException {
        MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if(SMSText.toString().isEmpty() || PhoneNo.isEmpty() || EncryptionKey.isEmpty() ||
                MyPrefs.getString("ServerURL","---").equals("---") ||
                MyPrefs.getString("ApiKey","---").equals("---"))
        {
            ErrorAlert();
        }
        else {
            MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            final OkHttpClient client = new OkHttpClient();
            String url = MyPrefs.getString("ServerURL", "---");
            String API_UserName = MyPrefs.getString("ServerUserName", "---");
            String API_Key = MyPrefs.getString("ApiKey", "---");
            String SMSBody= null;
            if(SMSText.getInt("x")==7 | SMSText.getInt("x")==8)
            {
                SMSBody = KeyRequestTrigger+SMSText.toString();
            }
            else
            {
                SMSBody =AppTrigger + new PKI_Cipher(context).EncryptPKI(SMSText.toString(), EncryptionKey);
            }
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", API_UserName)
                    .addFormDataPart("to", PhoneNo)
                    .addFormDataPart("message", SMSBody)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("ApiKey", API_Key)
                    .post(requestBody)
                    .build();
            new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,1);
            Handler h = new Handler(context.getMainLooper());
            h.post(() -> client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,1);
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    response.close();
                    new DBManager(context).UpdateMessageStatus(PhoneNo,SMSText,1);
                    //Toast.makeText(context, "Message Sent",Toast.LENGTH_SHORT).show();
                }
            }));
        }
    }
    public void ErrorAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alertDialog = builder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setIcon(R.drawable.ic_baseline_error_outline_24);
        alertDialog.setTitle(R.string.error);
        alertDialog.setMessage(context.getString(Integer.parseInt(context.getResources().getString(R.string.empty_message_sent))));
        alertDialog.show();
    }
}
