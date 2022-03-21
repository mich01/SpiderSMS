package com.mich01.spidersms.Backend;

import static android.app.AlarmManager.*;
import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Adapters.ContactAdapter;
import com.mich01.spidersms.Crypto.KeyExchange;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.Contact;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Receivers.AlarmReceiver;
import com.mich01.spidersms.SplashActivity;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class BackendFunctions
{
    public static Uri notificationMessageSound;
    public static NotificationManager manager;
    public static NotificationCompat.Builder notificationBuilder;
    public static Notification notification;
    @SuppressLint("NotifyDataSetChanged")
    public static boolean CheckRoot()
    {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            if(process !=null)
            {
                try {
                    process.destroy();
                }
                catch (Exception ignored){}
            }
        }
        return true;
    }

    public static boolean isConnectedOnline(Context context)
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else {
            connected = false;
        }
        return connected;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean UpdateMessage(Context context, String Sender, String MessageReceived)
    {
        String CHANNEL_ID = "1337";
        try {
            new DBManager(context).updateLastMessage(Sender, MessageReceived, 0, 0);
            new DBManager(context).AddChatMessage(Sender,1,MessageReceived,0);
            ChatActivity.PopulateChatView(context);
            ChatActivity.messageAdapter.notifyDataSetChanged();
            HomeActivity.RePopulateChats(context);
            HomeActivity.adapter.notifyDataSetChanged();
            Intent notifyIntent = new Intent(context, SplashActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );
            notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
            notification = notificationBuilder
                    .setSmallIcon(R.drawable.spider)
                    .setContentTitle("You have received a new Encrypted SMS")
                    .setContentText("Tap to read")
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.spider))
                    .setContentIntent(notifyPendingIntent)
                    .build();
            notificationBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notificationBuilder.build());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean KeyStatusChanged(Context context, String Sender, String MessageReceived)
    {
        try {
            new DBManager(context).AddChatMessage(Sender,1,MessageReceived,3);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public void NotifyDecryptionError(Context context)
    {
        Log.i("OPT 2","FAIL TRIGGER");
        String CHANNEL_ID = "1337";
        try {
            Intent notifyIntent = new Intent(context, SplashActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );
            notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
            notification = notificationBuilder
                    .setSmallIcon(R.drawable.spider)
                    .setContentTitle("Received Corrupted Encrypted SMS")
                    .setContentText("Message Decryption Failed Please request for a resend")
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.spider))
                    .setContentIntent(notifyPendingIntent)
                    .build();
            notificationBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notificationBuilder.build());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void UpdateContactPrivateKeys(Context context)
    {
        Handler h = new Handler(new Handler.Callback() {
            Intent AlarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, AlarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("Range")
            @Override
            public boolean handleMessage(@NonNull Message message)
            {
                ContactsActivity.Contacts.clear();
                Cursor cur = new DBManager(context).getAPPContacts();
                while (cur != null && cur.moveToNext()) {
                    @SuppressLint("Range") String CID = cur.getString(cur.getColumnIndex("CID"));
                    @SuppressLint("Range") String Name = cur.getString(cur.getColumnIndex("ContactName"));
                    new KeyExchange(context).FirstExchange(CID,cur.getString(cur.getColumnIndex("PubKey")),cur.getString(cur.getColumnIndex("PrivKey")),cur.getString(cur.getColumnIndex("Secret")));
                }
                assert cur != null;
                cur.close();
                return true;
            }
        });
    }
}
