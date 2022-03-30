package com.mich01.spidersms.Backend;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mich01.spidersms.Crypto.KeyExchange;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.SplashActivity;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.HomeActivity;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        return connected;
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void UpdateMessage(Context context, String Sender, String MessageReceived)
    {
        String CHANNEL_ID = "1337";
        try {
            new DBManager(context).updateLastMessage(Sender, MessageReceived, 0, 0);
            new DBManager(context).AddChatMessage(Sender,1,MessageReceived,0);
            ChatActivity.PopulateChatView(context.getApplicationContext());
            ChatActivity.messageAdapter.notifyDataSetChanged();
            HomeActivity.RePopulateChats(context.getApplicationContext());
            HomeActivity.adapter.notifyDataSetChanged();
            Intent notifyIntent = new Intent(context, SplashActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_MUTABLE
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
        }catch (Exception e){
            e.printStackTrace();
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
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void NotifyDecryptionError(Context context)
    {
        Log.i("OPT 2","FAIL TRIGGER");
        String CHANNEL_ID = "1337";
        try {
            Intent notifyIntent = new Intent(context, SplashActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_MUTABLE
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
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("Range")
            @Override
            public boolean handleMessage(@NonNull Message message)
            {
                ContactsActivity.Contacts.clear();
                Cursor cur = new DBManager(context).getAPPContacts();
                while (cur != null && cur.moveToNext()) {
                    @SuppressLint("Range") String CID = cur.getString(cur.getColumnIndex("CID"));
                    //new KeyExchange(context).FirstExchange(CID,cur.getString(cur.getColumnIndex("PubKey")),cur.getString(cur.getColumnIndex("PrivKey")),cur.getString(cur.getColumnIndex("Secret")));
                }
                assert cur != null;
                cur.close();
                return true;
            }
        });
    }
    public String convertTime(long time)
    {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }
}
