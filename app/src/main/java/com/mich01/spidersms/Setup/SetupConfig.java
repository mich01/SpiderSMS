package com.mich01.spidersms.Setup;

import static android.service.controls.ControlsProviderService.TAG;

import static com.mich01.spidersms.Crypto.IDManagementProtocol.GenerateNewKey;
import static com.mich01.spidersms.Crypto.IDManagementProtocol.ShareContact;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mich01.spidersms.DB.DBManager;
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
                if(ConfigServer(context,Input.toString()))
                {
                    if(MyPrefs.getInt("DBExists",0)==0) {
                        context.startActivity(new Intent(context, UnlockActivity.class));
                        Toast.makeText(context, "Server Config Setup Successfull", Toast.LENGTH_LONG).show();
                        //((SetupConfig) context).finish();
                    }
                    else
                    {
                        context.startActivity(new Intent(context, HomeActivity.class));
                        Toast.makeText(context, "Server Config Setup Successfull", Toast.LENGTH_LONG).show();
                        //((ScannerSetupActivity) context).finish();
                    }
                }
                break;
            case "HelloContact":
                if(new DBManager(context).AddContact(Input))
                {
                    context.startActivity(new Intent(context, HomeActivity.class));
                    Toast.makeText(context.getApplicationContext(), "New Contact Added", Toast.LENGTH_LONG).show();
                    ShareContact(context,Input.getString("CID"));
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
            Log.i(TAG, String.valueOf(Data));
            JSONObject ConfigJson = new JSONObject(Data);
            MyPrefsEditor.putString("SeverURL", String.valueOf(ConfigJson.get("ServerURL")));
            MyPrefsEditor.putInt("Port",(Integer) ConfigJson.get("Port"));
            MyPrefsEditor.putString("ServerUserName",String.valueOf(ConfigJson.get("ServerUserName")));
            MyPrefsEditor.putString("OriginalID",String.valueOf(ConfigJson.get("ServerUserName")));
            MyPrefsEditor.putString("Domain",String.valueOf(ConfigJson.get("Domain")));
            MyPrefsEditor.putString("SetupDomain",String.valueOf(ConfigJson.get("SetupDomain")));
            MyPrefsEditor.putString("ServerPassword",String.valueOf(ConfigJson.get("ServerPassword")));
            MyPrefsEditor.putString("Secret", GenerateNewKey());
            MyPrefsEditor.apply();
            MyPrefsEditor.commit();
            return true;
        } catch (JSONException e) {
            Toast.makeText(context, "Config Data Corrupted", Toast.LENGTH_LONG).show();
            return false;
        }

    }
}
