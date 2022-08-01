package com.mich01.spidersms.Backend;

import static com.mich01.spidersms.Data.StringsConstants.ApiKey;
import static com.mich01.spidersms.Data.StringsConstants.AppTrigger;
import static com.mich01.spidersms.Data.StringsConstants.Generic_Failure;
import static com.mich01.spidersms.Data.StringsConstants.KeyConfirmTrigger;
import static com.mich01.spidersms.Data.StringsConstants.KeyRequestTrigger;
import static com.mich01.spidersms.Data.StringsConstants.MAX_SMS_MESSAGE_LENGTH;
import static com.mich01.spidersms.Data.StringsConstants.MessageType;
import static com.mich01.spidersms.Data.StringsConstants.No_Service;
import static com.mich01.spidersms.Data.StringsConstants.Null_PDU;
import static com.mich01.spidersms.Data.StringsConstants.Radio_Off;
import static com.mich01.spidersms.Data.StringsConstants.SMS_DELIVERED;
import static com.mich01.spidersms.Data.StringsConstants.SMS_NOT_DELIVERED;
import static com.mich01.spidersms.Data.StringsConstants.SMS_SENT;
import static com.mich01.spidersms.Data.StringsConstants.ServerURL;
import static com.mich01.spidersms.Data.StringsConstants.ServerUserName;
import static com.mich01.spidersms.Data.StringsConstants.global_pref;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;

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
    SmsManager manager;
    public SMSHandler(Context context)
    {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void sendPlainSMS(String phoneNo, String smsText)
    {
        if(smsText.isEmpty() || phoneNo.isEmpty())
        {
            errorAlert();
        }
        else
        {
            String sms;
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, SMS_SENT,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, Generic_Failure,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, No_Service,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, Null_PDU,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, Radio_Off,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, "Exception",
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
                            Toast.makeText(context, SMS_DELIVERED,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(context, SMS_NOT_DELIVERED,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, "Exception",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(SMS_DELIVERED));
            MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
            new DBManager(context).updateLastMessage(phoneNo, smsText, 1, 1);
            new DBManager(context).AddChatMessage(phoneNo, 0, smsText, 0);
            manager = SmsManager.getDefault();
            PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE);
            PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE);
            sms = KeyRequestTrigger.concat(smsText);
            if(sms.length()>=MAX_SMS_MESSAGE_LENGTH)
            {
                ArrayList<String> parts =manager.divideMessage(sms);
                int numParts = parts.size();
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                for (int i = 0; i < numParts; i++)
                {
                    sentIntents.add(PendingIntent.getBroadcast(context, 0,  new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE));
                }
                manager.sendMultipartTextMessage(phoneNo,null, parts, sentIntents, deliveryIntents);
            }
            else
            {
                manager.sendTextMessage(phoneNo, null, sms, piSend, piDelivered);
            }}
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendEncryptedSMS(String phoneNo, JSONObject smsText, String encryptionKey,String aesSalt, String iv, int cryptType) throws JSONException {
        if(smsText.toString().isEmpty() || phoneNo.isEmpty() || encryptionKey.isEmpty())
        {
            errorAlert();
        }
        else
        {
            String sms;
            //---when the SMS has been sent---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,1);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, "Generic failure",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, "No service",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, "Null PDU",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,0);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, "Radio off",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,0);
                            break;
                        default:
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
                            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,2);
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(context, "SMS not delivered",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,0);
                            break;
                        default:
                            break;
                    }
                }
            }, new IntentFilter(SMS_DELIVERED));
            if(smsText.getInt(MessageType)==1 || smsText.getInt(MessageType)==2) {
                smsText.put("R", new Random().nextInt(26) + 'a');
            }
            manager = SmsManager.getDefault();
            PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE);
            PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE);
            if(cryptType==1) {
                sms =AppTrigger+new PKI_Cipher(context).EncryptPKI(smsText.toString(), encryptionKey);
            }else if(cryptType==3)
            {
                sms =KeyConfirmTrigger+new PKI_Cipher(context).EncryptPKI(smsText.toString(), encryptionKey);
            }
            else
            {
                sms = AppTrigger + new PKI_Cipher(context).Encrypt(smsText.toString(), encryptionKey, aesSalt, iv);
            }
            if(sms.length()>=MAX_SMS_MESSAGE_LENGTH)
            {
                ArrayList<String> parts =manager.divideMessage(sms);
                int numParts = parts.size();
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                for (int i = 0; i < numParts; i++)
                {
                    sentIntents.add(PendingIntent.getBroadcast(context, 0,  new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE));
                }
                manager.sendMultipartTextMessage(phoneNo,null, parts, sentIntents, deliveryIntents);
            }
            else
            {
                manager.sendTextMessage(phoneNo, null, sms, piSend, piDelivered);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void proxyEncryptedSMS(JSONObject smsText, String encryptionKey, String aesSalt, String iv) throws JSONException {
        MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        String proxyNumber = MyPrefs.getString("proxyNumber", "---");
        if(smsText.toString().isEmpty() ||  encryptionKey.isEmpty())
        {
            errorAlert();
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
                            new DBManager(context).UpdateMessageStatus(proxyNumber,smsText,1);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, "Generic failure",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(proxyNumber,smsText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, "No service",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(proxyNumber,smsText,0);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, "Null PDU",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(proxyNumber,smsText,0);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, "Radio off",
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(proxyNumber,smsText,0);
                            break;
                        default:
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
                            Toast.makeText(context, SMS_DELIVERED,
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(proxyNumber,smsText,2);
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(context, SMS_NOT_DELIVERED,
                                    Toast.LENGTH_SHORT).show();
                            new DBManager(context).UpdateMessageStatus(proxyNumber,smsText,0);
                            break;
                        default:
                            break;
                    }
                }
            }, new IntentFilter(SMS_DELIVERED));
            manager = SmsManager.getDefault();
            PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE);
            PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE);
            String sms = AppTrigger.concat(new PKI_Cipher(context).Encrypt(smsText.toString(), encryptionKey, aesSalt,iv));
            smsText.put("R", new Random().nextInt(26) + 'a');
            if (sms.length() >= MAX_SMS_MESSAGE_LENGTH) {
                ArrayList<String> parts = manager.divideMessage(sms);
                int numParts = parts.size();
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                for (int i = 0; i < numParts; i++) {
                    sentIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_MUTABLE));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_MUTABLE));
                }
                manager.sendMultipartTextMessage(proxyNumber, null, parts, sentIntents, deliveryIntents);
            } else {
                manager.sendTextMessage(proxyNumber, null, sms, piSend, piDelivered);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendSMSOnline(String phoneNo, JSONObject smsText, String encryptionKey) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, JSONException {
        MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        if(smsText.toString().isEmpty() || phoneNo.isEmpty() || encryptionKey.isEmpty() ||
                MyPrefs.getString(ServerURL,"---").equals("---") ||
                MyPrefs.getString(ApiKey ,"---").equals("---"))
        {
            errorAlert();
        }
        else {
            MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
            final OkHttpClient client = new OkHttpClient();
            String url = MyPrefs.getString(ServerURL, "---");
            String apiUserName = MyPrefs.getString(ServerUserName, "---");
            String apiKey = MyPrefs.getString(ApiKey, "---");
            String smsBody;
            if(smsText.getInt(MessageType)==7 | smsText.getInt(MessageType )==8)
            {
                smsBody = KeyRequestTrigger+smsText;
            }
            else
            {
                smsBody =AppTrigger + new PKI_Cipher(context).EncryptPKI(smsText.toString(), encryptionKey);
            }
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", apiUserName)
                    .addFormDataPart("to", phoneNo)
                    .addFormDataPart("message", smsBody)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader(ApiKey, apiKey)
                    .post(requestBody)
                    .build();
            new DBManager(context).UpdateMessageStatus(phoneNo,smsText,1);
            Handler h = new Handler(context.getMainLooper());
            h.post(() -> client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    new DBManager(context).UpdateMessageStatus(phoneNo,smsText,1);
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    response.close();
                    new DBManager(context).UpdateMessageStatus(phoneNo,smsText,1);
                }
            }));
        }
    }
    public void errorAlert()
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
