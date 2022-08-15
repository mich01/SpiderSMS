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

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PrefsMgr
{
    public static EncryptedSharedPreferences MyPrefs;
    public static SharedPreferences.Editor MyPrefsEditor;
    Context _context;
    // shared pref mode

    public PrefsMgr(Context context)
    {
        this._context = context;
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
    public static EncryptedSharedPreferences getPrefs(Context context)
    {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    global_pref,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        }catch (GeneralSecurityException | IOException | NullPointerException ignored) {}
        return null;
    }
}
