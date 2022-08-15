package com.mich01.spidersms.services;

import static com.mich01.spidersms.Data.StringsConstants.ALARM_LENGTH;
import static com.mich01.spidersms.Data.StringsConstants.AppName;
import static com.mich01.spidersms.Data.StringsConstants.CHANNEL_ID;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mich01.spidersms.R;
import com.mich01.spidersms.SplashActivity;


public class MainService extends Service {
    Context context;
    Uri notificationMessageSound;
    PendingIntent pendingIntent;
    NotificationManager manager;


    @Override
    public void onCreate()
    {
        super.onCreate();
        context = getApplicationContext();
        notificationMessageSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID, AppName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.spider_sms_round);
        manager.createNotificationChannel(chan);
        Intent notifyIntent = new Intent(context, SplashActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context, 0, notifyIntent, PendingIntent.FLAG_MUTABLE
        );
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder.setContentIntent(notifyPendingIntent);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.spider_logo)
                .setLargeIcon(icon)
                .setContentTitle(context.getResources().getString(R.string.app_active))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1337, notification);
        Intent alarmIntent = new Intent(getApplicationContext(), SplashActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + ALARM_LENGTH, pendingIntent);
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}