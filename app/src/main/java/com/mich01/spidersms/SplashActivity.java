package com.mich01.spidersms;


import static com.mich01.spidersms.Crypto.PKI_Cipher.GeneratePrivateKey;
import static com.mich01.spidersms.Data.StringsConstants.SetupComplete;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.getPrefs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Setup.SetupActivity;
import com.mich01.spidersms.UI.UnlockActivity;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity
{
    int First_Run = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); Objects.requireNonNull(getSupportActionBar()).hide();
        if (!BackendFunctions.checkRoot() || BackendFunctions.checkRoot())
        {
           checkInstallationStatus();
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(SplashActivity.this).create();
            alertDialog.setTitle(R.string.error);
            alertDialog.setIcon(R.drawable.ic_baseline_error_outline_24);
            alertDialog.setMessage(getString(R.string.rooted_device_error));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.exit_btn),
                    (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    });
            alertDialog.show();
        }
    }

    private void checkInstallationStatus() {
        MyPrefs = getPrefs(this);
        if (MyPrefs.getLong("InstalledTimestamp", 0) == 0)
        {
            GeneratePrivateKey();
            SharedPreferences.Editor PrefEditor;
            PrefEditor = MyPrefs.edit();
            PrefEditor.putLong("InstalledTimestamp", System.currentTimeMillis());
            PrefEditor.apply();
            CheckPermissions();
        }else
        {
            CheckPermissions();
        }
    }

    public void CheckPermissions()
    {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)  != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)  != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS)  != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED )
        {
            RequestPermissions();
        }
        else
        {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                MyPrefs = getPrefs(this);
                First_Run=MyPrefs.getInt(SetupComplete, 0);
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
            requestPermissions(new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_CONTACTS,Manifest.permission.CAMERA,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_EXTERNAL_STORAGE},1111);
        CheckPermissions();
    }
}