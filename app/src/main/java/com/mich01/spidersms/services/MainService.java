package com.mich01.spidersms.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mich01.spidersms.R;
import com.mich01.spidersms.Receivers.AlarmReceiver;
import com.mich01.spidersms.Receivers.MainReceiver;


public class MainService extends Service {
    private final String CHANNEL_ID = "1337";
    static Context context;
    public static Uri notificationMessageSound;
    public static PendingIntent pendingIntent;
    public static long[] VibrationPattern ={100,300,300,300};
    public static NotificationManager manager;


    @Override
    public void onCreate()
    {
        super.onCreate();
        context = getApplicationContext();
        Log.i("SpiderMan", "onStart here: " );
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        notificationMessageSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String channelName = "Android";
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Active")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1337, notification);
        Intent AlarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, AlarmIntent, 0);
        SharedPreferences preferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (preferences.getInt("TimeSpan",0)  * 1000), pendingIntent);
        return START_STICKY;
    }

    @SuppressLint("NewApi")
    public static void ResetTimer(Context context)
    {
        Log.i("Agent: ","Disconnected");
        context.startForegroundService(new Intent(context, MainService.class));
        Intent AlarmIntent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, AlarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(1 * 1000), pendingIntent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}