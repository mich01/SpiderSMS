package com.mich01.spidersms;


import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Setup.SetupConfig;
import com.mich01.spidersms.UI.HomeActivity;
import com.mich01.spidersms.UI.LoginActivity;
import com.mich01.spidersms.UI.SetupActivity;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    Handler h = new Handler();
    int First_Run = 0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (!BackendFunctions.CheckRoot())
        {
            SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            if (preferences.getLong("InstalledTimestamp", 0) == 0)
            {
                SharedPreferences.Editor PrefEditor;
                PrefEditor = preferences.edit();
                PrefEditor.putLong("InstalledTimestamp", System.currentTimeMillis());
                PrefEditor.putBoolean("Licensed", false);
                PrefEditor.apply();
            }
                                    h.postDelayed(() -> {
                First_Run = preferences.getInt("SetupComplete", 0);
                Intent i;
                if (First_Run == 0)
                {
                    i = new Intent(SplashActivity.this, HomeActivity.class);

                } else {
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(i);
                finish();
            }, 1000);
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
}