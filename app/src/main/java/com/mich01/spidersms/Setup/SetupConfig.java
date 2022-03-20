package com.mich01.spidersms.Setup;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
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
    public static void ReadScan(Context context, JSONObject Input) throws JSONException {
        switch (Input.getString("Data"))
        {
            case "Config":
                ConfirmUpdate(context, Input);
                break;
            case "Config-proxy":
                UpdateProxySMS(context, Input);
                break;
            case "Config-api":
                ConfirmOnlineAPI(context, Input);
                break;
            case "HelloContact":
                Log.i("Key Step 1","Contact Scanned");
                if(new DBManager(context).AddContact(Input))
                {
                    context.startActivity(new Intent(context, HomeActivity.class));
                    Toast.makeText(context.getApplicationContext(), "New Contact Added", Toast.LENGTH_LONG).show();
                    ((ScannerSetupActivity)context).finish();
                }
                break;
            default:
                new SetupConfig().SnackBarAlert(context.getResources().getString(R.string.config_update_success),context);
        }
    }
    public static boolean ConfigServer(Context context,String Data)
    {
        try
        {
            JSONObject ConfigJson = new JSONObject(Data);
            MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            MyPrefsEditor = MyPrefs.edit();
            MyPrefsEditor.putString("ServerURL", ConfigJson.get("ServerURL").toString());
            MyPrefsEditor.putString("ProxyNumber",ConfigJson.get("ProxyNumber").toString());
            MyPrefsEditor.putString("ServerUserName",ConfigJson.get("ServerUserName").toString());
            MyPrefsEditor.putString("ApiKey",ConfigJson.get("ApiKey").toString());
            MyPrefsEditor.apply();
            MyPrefsEditor.commit();
            new SetupConfig().SnackBarAlert(context.getResources().getString(R.string.config_update_success),context);
            ((ScannerSetupActivity)context).finish();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            new SetupConfig().SnackBarError(context.getString(R.string.error_invalid_data),context);
            return false;
        }
    }
    public static void ConfirmUpdate(Context context, JSONObject Input)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.are_you_sure_app_settings);
        alert.setPositiveButton("Update", (dialog, whichButton) -> {
            if(ConfigServer(context,Input.toString()))
            {
                if(MyPrefs.getInt("DBExists",0)==0)
                {
                    context.startActivity(new Intent(context, UnlockActivity.class));
                    ((ScannerSetupActivity) context).finish();
                }
                else
                {
                    context.startActivity(new Intent(context, HomeActivity.class));
                    ((ScannerSetupActivity) context).finish();
                }
                new SetupConfig().SnackBarAlert(context.getString(R.string.server_setup_complete),context);
            }
        });

        alert.setNegativeButton("Cancel",
                (dialog, whichButton) -> ((ScannerSetupActivity)context).finish());
        alert.show();
    }
    public static void UpdateProxySMS(Context context, JSONObject ConfigJson)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.are_you_sure_proxy_update);
        alert.setPositiveButton("Update", (dialog, whichButton) -> {
            try {
                MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                MyPrefsEditor = MyPrefs.edit();
                MyPrefsEditor.putString("ProxyNumber",ConfigJson.getString("ProxyNumber"));
                MyPrefsEditor.putString("ProxyPubKey",ConfigJson.getString("ProxyPubKey"));
                MyPrefsEditor.apply();
                MyPrefsEditor.commit();
                new SetupConfig().SnackBarAlert(context.getString(R.string.proxy_number_updated),context);
                ((ScannerSetupActivity)context).finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        alert.setNegativeButton("Cancel",
                (dialog, whichButton) -> ((ScannerSetupActivity)context).finish());
        alert.show();
    }
    public static void ConfirmOnlineAPI(Context context, JSONObject ConfigJson)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.are_you_sure_api);
        alert.setPositiveButton("Update", (dialog, whichButton) -> {
            try {
                MyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                MyPrefsEditor = MyPrefs.edit();
                MyPrefsEditor.putString("ServerURL", ConfigJson.get("ServerURL").toString());
                MyPrefsEditor.putString("ServerUserName",ConfigJson.get("ServerUserName").toString());
                MyPrefsEditor.putString("ApiKey",ConfigJson.get("ApiKey").toString());
                MyPrefsEditor.apply();
                MyPrefsEditor.commit();
                new SetupConfig().SnackBarAlert(context.getString(R.string.api_updated_success), context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        alert.setNegativeButton("Cancel",
                (dialog, whichButton) -> ((ScannerSetupActivity)context).finish());
        alert.show();
    }
    public void SnackBarAlert(String AlertMessage, Context context)
    {
        Snackbar mSnackBar;
        try {
            mSnackBar = Snackbar.make(((ScannerSetupActivity) context).findViewById(android.R.id.content), AlertMessage, Snackbar.LENGTH_LONG);
        }catch (Exception e) {
            mSnackBar = Snackbar.make(((HomeActivity) context).findViewById(android.R.id.content), AlertMessage, Snackbar.LENGTH_LONG);
        }
        TextView SnackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
        SnackBarView.setTextColor(ContextCompat.getColor(context, R.color.black));
        SnackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
        SnackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        SnackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
        mSnackBar.show();
    }
    public void SnackBarError(String AlertMessage, Context context)
    {
        Snackbar mSnackBar;
        try {
            mSnackBar = Snackbar.make(((ScannerSetupActivity) context).findViewById(android.R.id.content), AlertMessage, Snackbar.LENGTH_LONG);
        }catch (Exception e) {
            mSnackBar = Snackbar.make(((HomeActivity) context).findViewById(android.R.id.content), AlertMessage, Snackbar.LENGTH_LONG);
        }
        TextView SnackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
        SnackBarView.setTextColor(ContextCompat.getColor(context, R.color.white));
        SnackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.error));
        SnackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        SnackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
        mSnackBar.show();
    }
}
