package com.mich01.spidersms;


import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.Setup.SetupActivity;
import com.mich01.spidersms.UI.UnlockActivity;

import org.json.JSONObject;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    Handler h = new Handler();
    SharedPreferences preferences;
    int First_Run = 0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new PKI_Cipher().Decrypt("|>"+new PKI_Cipher().Encrypt("{target\":\"+254724724008\",\"Body\":\"QCE1OCQ559 Confirmed. Ksh10.00 sent to SAFARICOM POSTPAID BUNDLES for account 254 on 14\\/3\\/22 at 6:26 PM New M-PESA balance is Ksh1,618.33. Transaction cost, Ksh0.00.Amount you can transact within the day is 299,990.00.Separate personal and business funds through Pochi la Biashara on *334#.}","1234567890QWERTY"),"1234567890QWERTY");
        //Log.i("Process",new PKI_Cipher().decrypt(new PKI_Cipher().encrypt("Hello Mike...","MySecretKey"),"MySecretKey"));
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (!BackendFunctions.CheckRoot())
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
            {
                CheckPermissions();
            }
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(SplashActivity.this).create();
            alertDialog.setTitle("Error");
            alertDialog.setIcon(R.drawable.ic_baseline_error_outline_24);
            alertDialog.setMessage(getString(R.string.rooted_device_error));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.exit_btn),
                    (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    });
            alertDialog.show();
        }
    }
    public void CheckPermissions()
    {
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
                First_Run=preferences.getInt("SetupComplete", 0);
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