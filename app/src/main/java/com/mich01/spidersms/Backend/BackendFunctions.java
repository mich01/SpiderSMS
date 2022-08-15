package com.mich01.spidersms.Backend;

import static com.mich01.spidersms.Data.StringsConstants.CHANNEL_ID;
import static com.mich01.spidersms.Data.StringsConstants.date_Format;
import static com.mich01.spidersms.Data.StringsConstants.su;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.SplashActivity;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.HomeActivity;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackendFunctions
{
    NotificationCompat.Builder notificationBuilder;
    Notification notification;
    @SuppressLint("NotifyDataSetChanged")
    public static boolean checkRoot()
    {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(su);
        } catch (Exception e)
        {
            return false;
        }
        finally {
            if(process !=null)
            {
                try {
                    process.destroy();
                } finally {
                    process.destroy();
                }
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
    public void updateMessage(Context context, String sender, String messageReceived)
    {
        try {
            new DBManager(context).updateLastMessage(sender, messageReceived, 0, 0);
            new DBManager(context).AddChatMessage(sender,1,messageReceived,0);
        }finally {
            ChatActivity.populateChatView(context.getApplicationContext());
            HomeActivity.rePopulateChats(context.getApplicationContext());
            HomeActivity.adapter.notifyDataSetChanged();
            AlertUser(context, context.getString(R.string.new_message_alert), context.getString(R.string.action_message));
        }
    }
    public void keyStatusChanged(Context context, String Sender, String MessageReceived)
    {
        try {
            new DBManager(context).AddChatMessage(Sender,1,MessageReceived,3);
        }
        finally {
            Toast.makeText(context, Sender+" KeyChanged",Toast.LENGTH_LONG).show();
        }
    }

    public String convertTime(long time)
    {
        Date date = new Date(time);
        Format format = new SimpleDateFormat(date_Format);
        return format.format(date);
    }
    public void AlertUser(Context context, String AlertMessage, String ActionMessage)
    {
        Intent notifyIntent = new Intent(context, SplashActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context, 0, notifyIntent, PendingIntent.FLAG_MUTABLE
        );
        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notification = notificationBuilder
                .setSmallIcon(R.drawable.spider)
                .setContentTitle(AlertMessage)
                .setContentText(ActionMessage)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.spider))
                .setContentIntent(notifyPendingIntent)
                .build();
        notificationBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notificationBuilder.build());
    }
}
