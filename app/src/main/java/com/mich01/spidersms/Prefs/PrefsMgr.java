package com.mich01.spidersms.Prefs;

import static com.mich01.spidersms.Data.StringsConstants.ApiKey;
import static com.mich01.spidersms.Data.StringsConstants.DBExists;
import static com.mich01.spidersms.Data.StringsConstants.PhoneNumber;
import static com.mich01.spidersms.Data.StringsConstants.ProxyNumber;
import static com.mich01.spidersms.Data.StringsConstants.ServerPublicKey;
import static com.mich01.spidersms.Data.StringsConstants.SeverURL;
import static com.mich01.spidersms.Data.StringsConstants.UserName;
import static com.mich01.spidersms.Data.StringsConstants.global_pref;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsMgr
{
    public static SharedPreferences MyPrefs;
    public static SharedPreferences.Editor MyPrefsEditor;
    Context _context;
    // shared pref mode

    public PrefsMgr(Context context)
    {
        this._context = context;
        MyPrefs = _context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        MyPrefsEditor = MyPrefs.edit();
        MyPrefsEditor.putInt(DBExists, 0);
        MyPrefs.getString(UserName,null);
        MyPrefs.getString(PhoneNumber,null);
        MyPrefs.getString(SeverURL,"---");
        MyPrefs.getString(ApiKey,"---");
        MyPrefs.getString(ProxyNumber,"---");
        MyPrefs.getString(ServerPublicKey,"---");
        MyPrefsEditor.apply();
        MyPrefsEditor.commit();
    }
}
