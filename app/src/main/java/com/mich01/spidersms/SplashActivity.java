package com.mich01.spidersms;


import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.UI.SetupActivity;
import com.mich01.spidersms.UI.UnlockActivity;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    Handler h = new Handler();
    SharedPreferences preferences;
    int First_Run = 0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setupWindowAnimations();
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (!BackendFunctions.CheckRoot())
        {
            CheckPermissions();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(SplashActivity.this).create();
            alertDialog.setTitle("Error");
            //alertDialog.setIcon(R.drawable.ic_stop_no_action);
            alertDialog.setMessage("App Wont run on rooted Devices");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "EXIT",
                    (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    });
            alertDialog.show();
        }
    }
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setExitTransition(fade);
    }
    public void CheckPermissions()
    {
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (preferences.getLong("InstalledTimestamp", 0) == 0)
        {
            SharedPreferences.Editor PrefEditor;
            PrefEditor = preferences.edit();
            PrefEditor.putLong("InstalledTimestamp", System.currentTimeMillis());
            PrefEditor.putBoolean("Licensed", false);
            PrefEditor.apply();
        }
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)  != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)  != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS)  != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED)
        {
            RequestPermissions();
        }
        else
        {
            h.postDelayed(() -> {
                preferences.getInt("SetupComplete", 0);
                Intent i;
                if (First_Run == 0)
                {
                    i = new Intent(SplashActivity.this, SetupActivity.class);

                } else {
                    i = new Intent(SplashActivity.this, UnlockActivity.class);
                }
                startActivity(i);
                overridePendingTransition(R.transition.fade_in, R.transition.fade_out);
                finish();
            }, 1000);
        }
    }
    public void RequestPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_CONTACTS,Manifest.permission.CAMERA,Manifest.permission.RECEIVE_SMS,},1111);
        }
        CheckPermissions();
    }
}