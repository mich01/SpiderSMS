package com.mich01.spidersms.Setup;

import static com.mich01.spidersms.Data.StringsConstants.ApiKey;
import static com.mich01.spidersms.Data.StringsConstants.Config;
import static com.mich01.spidersms.Data.StringsConstants.Config_api;
import static com.mich01.spidersms.Data.StringsConstants.Config_proxy;
import static com.mich01.spidersms.Data.StringsConstants.DBExists;
import static com.mich01.spidersms.Data.StringsConstants.Data;
import static com.mich01.spidersms.Data.StringsConstants.HelloContact;
import static com.mich01.spidersms.Data.StringsConstants.ProxyNumber;
import static com.mich01.spidersms.Data.StringsConstants.ProxyPubKey;
import static com.mich01.spidersms.Data.StringsConstants.ServerURL;
import static com.mich01.spidersms.Data.StringsConstants.ServerUserName;
import static com.mich01.spidersms.Data.StringsConstants.global_pref;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.HomeActivity;
import com.mich01.spidersms.UI.UnlockActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SetupConfig
{
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void readScan(Context context, JSONObject input) throws JSONException {
        switch (input.getString(Data))
        {
            case Config:
                confirmUpdate(context, input);
                break;
            case Config_proxy:
                updateProxySMS(context, input);
                break;
            case Config_api:
                confirmOnlineAPI(context, input);
                break;
            case HelloContact:
                if(new DBManager(context).AddContact(input))
                {
                    context.startActivity(new Intent(context, HomeActivity.class));
                    Toast.makeText(context.getApplicationContext(), R.string.contact_added, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                new SetupConfig().snackBarAlert(context.getResources().getString(R.string.config_update_success),context);
        }
    }
    public static boolean configServer(Context context,String data)
    {
        try
        {
            JSONObject configJson = new JSONObject(data);
            MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
            MyPrefsEditor = MyPrefs.edit();
            MyPrefsEditor.putString(ServerURL, configJson.get(ServerURL).toString());
            MyPrefsEditor.putString(ProxyNumber,configJson.get(ProxyNumber).toString());
            MyPrefsEditor.putString(ServerUserName,configJson.get(ServerUserName).toString());
            MyPrefsEditor.putString(ApiKey,configJson.get(ApiKey).toString());
            MyPrefsEditor.apply();
            MyPrefsEditor.commit();
            new SetupConfig().snackBarAlert(context.getResources().getString(R.string.config_update_success),context);
            ((ScannerSetupActivity)context).finish();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            new SetupConfig().snackBarError(context.getString(R.string.error_invalid_data),context);
            return false;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void confirmUpdate(Context context, JSONObject input)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.are_you_sure_app_settings);
        alert.setPositiveButton(R.string.update, (dialog, whichButton) -> {
            if(configServer(context,input.toString()))
            {
                if(MyPrefs.getInt(DBExists,0)==0)
                {
                    context.startActivity(new Intent(context, UnlockActivity.class));
                }
                else
                {
                    context.startActivity(new Intent(context, HomeActivity.class));
                }
                ((ScannerSetupActivity) context).finish();
                new SetupConfig().snackBarAlert(context.getString(R.string.server_setup_complete),context);
            }
        });

        alert.setNegativeButton(R.string.cancel,
                (dialog, whichButton) -> ((ScannerSetupActivity)context).finish());
        alert.show();
    }
    public static void updateProxySMS(Context context, JSONObject configJson)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.are_you_sure_proxy_update);
        alert.setPositiveButton(R.string.update, (dialog, whichButton) -> {
            try {
                MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
                MyPrefsEditor = MyPrefs.edit();
                MyPrefsEditor.putString(ProxyNumber,configJson.getString(ProxyNumber));
                MyPrefsEditor.putString(ProxyPubKey,configJson.getString(ProxyPubKey));
                MyPrefsEditor.apply();
                MyPrefsEditor.commit();
                new SetupConfig().snackBarAlert(context.getString(R.string.proxy_number_updated),context);
                ((ScannerSetupActivity)context).finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        alert.setNegativeButton(R.string.cancel,
                (dialog, whichButton) -> ((ScannerSetupActivity)context).finish());
        alert.show();
    }
    public static void confirmOnlineAPI(Context context, JSONObject configJson)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.are_you_sure_api);
        alert.setPositiveButton(R.string.update, (dialog, whichButton) -> {
            try {
                MyPrefs = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
                MyPrefsEditor = MyPrefs.edit();
                MyPrefsEditor.putString(ServerURL, configJson.get(ServerURL).toString());
                MyPrefsEditor.putString(ServerUserName,configJson.get(ServerUserName).toString());
                MyPrefsEditor.putString(ApiKey,configJson.get(ApiKey).toString());
                MyPrefsEditor.apply();
                MyPrefsEditor.commit();
                new SetupConfig().snackBarAlert(context.getString(R.string.api_updated_success), context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        alert.setNegativeButton(R.string.cancel,
                (dialog, whichButton) -> ((ScannerSetupActivity)context).finish());
        alert.show();
    }
    public void snackBarAlert(String alertMessage, Context context)
    {
        Snackbar mSnackBar;
        try {
            mSnackBar = Snackbar.make(((ScannerSetupActivity) context).findViewById(android.R.id.content), alertMessage, Snackbar.LENGTH_LONG);
            TextView snackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
            snackBarView.setTextColor(ContextCompat.getColor(context, R.color.black));
            snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
            snackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
            mSnackBar.show();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void snackBarError(String alertMessage, Context context)
    {
        Snackbar mSnackBar;
        try {
            mSnackBar = Snackbar.make(((ScannerSetupActivity) context).findViewById(android.R.id.content), alertMessage, Snackbar.LENGTH_LONG);
            TextView snackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
            snackBarView.setTextColor(ContextCompat.getColor(context, R.color.white));
            snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.error));
            snackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
            mSnackBar.show();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
