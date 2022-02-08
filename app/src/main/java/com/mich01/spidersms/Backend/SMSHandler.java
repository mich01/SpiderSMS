package com.mich01.spidersms.Backend;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class SMSHandler
{
    Context context;
    private static final int MAX_SMS_MESSAGE_LENGTH = 160;
    private static final String SMS_SENT = "SMS_SENT";
    private static final int SMS_PORT = 8091;
    private static final String SMS_DELIVERED = "SMS_DELIVERED";


    public SMSHandler(Context context) {
        this.context = context;
    }

    private void checkForSmsPermission() {
        int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.i("SMS Permissions", "GRANTED");
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission already granted. Enable the SMS button.
            Log.i("SMS Permissions", "UMENYIMWA");
        }
    }
    public void sendEncryptedSMS(String PhoneNo, String SMSText)
    {
        Log.i("SMS Handler: ","Message sent to "+PhoneNo+" Message: "+SMSText);
        SmsManager manager = SmsManager.getDefault();
        PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0);
        manager.sendTextMessage(PhoneNo, null, SMSText, piSend, piDelivered);
    }
}
