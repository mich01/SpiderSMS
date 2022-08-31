package com.mich01.spidersms;


import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;
import static com.mich01.spidersms.Crypto.PKI_Cipher.GeneratePrivateKey;
import static com.mich01.spidersms.Data.StringsConstants.SetupComplete;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.getPrefs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Setup.SetupActivity;
import com.mich01.spidersms.UI.UnlockActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity
{
    int First_Run = 0;
    AlertDialog alertDialog;
    int permissionsCount = 0;
    ArrayList<String> permissionsList;
    final String[] appPermissions =new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_CONTACTS,Manifest.permission.CAMERA,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_EXTERNAL_STORAGE};
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;
                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(appPermissions[i])) {
                                    permissionsList.add(appPermissions[i]);
                                } else if (!hasPermission(SplashActivity.this, appPermissions[i])) {
                                    permissionsCount++;
                                }
                            }
                            if (permissionsList.size() > 0) {
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                showPermissionDialog();
                            } else {
                                checkInstallationStatus();
                            }
                        }
                    });
    ActivityResultLauncher<Intent> defaultSMSActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                {
                    permissionsList = new ArrayList<>();
                    permissionsList.addAll(Arrays.asList(appPermissions));
                    askForPermissions(permissionsList);
                }
                else if (result.getResultCode() == Activity.RESULT_CANCELED)
                {
                    permissionsList = new ArrayList<>();
                    permissionsList.addAll(Arrays.asList(appPermissions));
                    askForPermissions(permissionsList);
                }
            });
    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("You need to Allow these permissions to use this app")
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }
    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
            showPermissionDialog();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); Objects.requireNonNull(getSupportActionBar()).hide();
        if (!BackendFunctions.checkRoot() || BackendFunctions.checkRoot())
        {
            checkInstallationStatus();
        }
        else
        {
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
    private void checkIfDefaultSMSApp()
    {
        if(!getPackageName().equals(getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(this))))
        {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        RoleManager roleManager = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
                            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS);
                            defaultSMSActivityResultLauncher.launch(intent);
                        }
                        else
                        {
                            Intent intent = new Intent(ACTION_CHANGE_DEFAULT_DIALER)
                                    .putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                            startActivity(intent);
                            dialog.dismiss();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        permissionsList = new ArrayList<>();
                        permissionsList.addAll(Arrays.asList(appPermissions));
                        askForPermissions(permissionsList);
                        dialog.dismiss();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.set_default_sms_message).setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
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
            checkIfDefaultSMSApp();
        }else
        {
            new Handler(Looper.getMainLooper()).postDelayed(() ->
            {
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
}