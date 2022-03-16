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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.HomeActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Objects;

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
    String AppTrigger = "|>";

    public SMSHandler(Context context) {
        this.context = context;
    }

    public void sendPlainSMS(String PhoneNo, String SMSText)
    {
        MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        new DBManager(context).updateLastMessage(PhoneNo, SMSText, 1, 1);
        new DBManager(context).AddChatMessage(PhoneNo, 0, SMSText, false);
        Log.i("SMS Handler: ", "Message sent to " + PhoneNo + " Message: " + SMSText);
        SmsManager manager = SmsManager.getDefault();
        PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), 0);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0);
        manager.sendTextMessage(PhoneNo, null, SMSText, piSend, piDelivered);
    }

    public void sendEncryptedSMS(String PhoneNo, String SMSText) {
        String SMS;
        new DBManager(context).updateLastMessage(PhoneNo, SMSText, 1, 1);
        new DBManager(context).AddChatMessage(PhoneNo, 0, SMSText, false);
        //---when the SMS has been sent---
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
        SmsManager manager = SmsManager.getDefault();
        PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0);
        JSONObject SMSBody = new JSONObject();
        try {
            SMSBody.put("target",PhoneNo);
            SMSBody.put("Body",SMSText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SMS =AppTrigger+new PKI_Cipher().Encrypt(SMSBody.toString(),"1234567890QWERTY");
        if(SMS.length()>=MAX_SMS_MESSAGE_LENGTH)
        {
            ArrayList<String> parts =manager.divideMessage(SMS);
            int numParts = parts.size();
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
            for (int i = 0; i < numParts; i++)
            {
                Log.i("ssssr",parts.get(i));
                sentIntents.add(PendingIntent.getBroadcast(context, 0,  new Intent(SMS_SENT), 0));
                deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0));
            }
            manager.sendMultipartTextMessage(PhoneNo,null, parts, sentIntents, deliveryIntents);
        }
        else
        {
            manager.sendTextMessage(PhoneNo, null, SMS, piSend, piDelivered);
        }
    }

    public void proxyEncryptedSMS(String PhoneNo, String SMSText)
    {
        new DBManager(context).updateLastMessage(PhoneNo, SMSText, 1, 1);
        new DBManager(context).AddChatMessage(PhoneNo, 0, SMSText, false);
        //---when the SMS has been sent---
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
        String ProxyNumber =MyPrefs.getString("ProxyNumber", "---");
        JSONObject SMSBody = new JSONObject();
        try {
            SMSBody.put("target",PhoneNo);
            SMSBody.put("Body",SMSText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("SMS Handler: ", "Message sent to " + PhoneNo + " Message: " + SMSText);
        SmsManager manager = SmsManager.getDefault();
        if(SMSText.length()>158) {
            ArrayList<String> parts = manager.divideMessage(AppTrigger+new PKI_Cipher().Encrypt(SMSBody.toString(),"1234567890QWERTY"));
            int numParts = parts.size();

            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();

            for (int i = 0; i < numParts; i++) {
                PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), 0);
                PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0);
            }
            manager.sendMultipartTextMessage(ProxyNumber, null, parts, sentIntents, deliveryIntents);
        }
        else
        {
            PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), 0);
            PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0);
            manager.sendTextMessage(ProxyNumber, null, AppTrigger+new PKI_Cipher().Encrypt(SMSBody.toString(),"1234567890QWERTY"), piSend, piDelivered);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void SendSMSOnline(String PhoneNo, String SMSText) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        final OkHttpClient client = new OkHttpClient();
        String url = MyPrefs.getString("ServerURL","---");
        String API_UserName = MyPrefs.getString("ServerUserName","---");
        String API_Key = MyPrefs.getString("ApiKey","---");
        JSONObject SMSBody = new JSONObject();
        try {
            SMSBody.put("target",PhoneNo);
            SMSBody.put("Body",SMSText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new DBManager(context).updateLastMessage(PhoneNo, SMSText, 1, 1);
        new DBManager(context).AddChatMessage(PhoneNo, 0, SMSText, false);
        ChatActivity.PopulateChatView(context);
        ChatActivity.messageAdapter.notifyDataSetChanged();
        HomeActivity.RePopulateChats(context);
        HomeActivity.adapter.notifyDataSetChanged();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", API_UserName)
                .addFormDataPart("to", PhoneNo)
                .addFormDataPart("message", AppTrigger+new PKI_Cipher().Encrypt(SMSBody.toString(),"1234567890QWERTY"))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("ApiKey", API_Key)
                .post(requestBody)
                .build();
        Handler h = new Handler(context.getMainLooper());
        h.post(() -> client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //Toast.makeText(context, "Message Sent",Toast.LENGTH_SHORT).show();
            }
        }));

    }
}
