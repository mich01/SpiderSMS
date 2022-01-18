package com.mich01.spidersms.Prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsMgr
{
    public static final String PREF_NAME = "global";
    public static final String IS_NEW_SETUP = "SetupComplete";
    public static SharedPreferences MyPrefs;
    public static SharedPreferences.Editor MyPrefsEditor;
    Context _context;
    // shared pref mode

    public PrefsMgr(Context context)
    {
        this._context = context;
        MyPrefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        MyPrefsEditor = MyPrefs.edit();
        MyPrefsEditor.putInt("DBExists", 0);
        MyPrefs.getString("UserName",null);
        MyPrefs.getString("PhoneNumber",null);
        MyPrefs.getString("SeverURL","127.0.0.1");
        MyPrefs.getInt("Port",0);
        MyPrefs.getString("ServerUserName",null);
        MyPrefs.getString("ServerPassword",null);
        MyPrefs.getString("BlockID",null);
        MyPrefs.getString("ServerPubKey",null);
        MyPrefsEditor.apply();
        MyPrefsEditor.commit();
    }
}
