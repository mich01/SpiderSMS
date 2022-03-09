package com.mich01.spidersms.Setup;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.HomeActivity;
import com.mich01.spidersms.UI.UnlockActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SetupConfig
{
    public static void ReadScan(Context context, JSONObject Input) throws JSONException {
        switch (Input.getString("Data"))
        {
            case "Config":
                ConfirmUpdate(context, Input);
                break;
            case "HelloContact":
                if(new DBManager(context).AddContact(Input))
                {
                    context.startActivity(new Intent(context, HomeActivity.class));
                    Toast.makeText(context.getApplicationContext(), "New Contact Added", Toast.LENGTH_LONG).show();
                    ((ScannerSetupActivity)context).finish();
                }
                break;
            case "Group":
                Toast.makeText(context.getApplicationContext(), "Group Data", Toast.LENGTH_LONG).show();
                break;
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
            Toast.makeText(context , context.getResources().getString(R.string.config_update_success), Toast.LENGTH_LONG).show();
            ((ScannerSetupActivity)context).finish();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Config Data Corrupted", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    public static void ConfirmUpdate(Context context, JSONObject Input)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Are you sure you want to Change your App Settings?");
        alert.setPositiveButton("Update", (dialog, whichButton) -> {
            if(ConfigServer(context,Input.toString()))
            {
                if(MyPrefs.getInt("DBExists",0)==0)
                {
                    context.startActivity(new Intent(context, UnlockActivity.class));
                    //((SetupConfig) context).finish();
                }
                else
                {
                    context.startActivity(new Intent(context, HomeActivity.class));
                    //((ScannerSetupActivity) context).finish();
                }
                Toast.makeText(context, "Server Config Setup Successfull", Toast.LENGTH_LONG).show();
            }
        });

        alert.setNegativeButton("Cancel",
                (dialog, whichButton) -> ((ScannerSetupActivity)context).finish());
        alert.show();
    }
}
