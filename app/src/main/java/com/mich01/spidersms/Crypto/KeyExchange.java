package com.mich01.spidersms.Crypto;


import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.Contact;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class KeyExchange
{
    Context context;
    SharedPreferences preferences;
    public KeyExchange(Context context) {
        this.context = context;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void FirstExchange(String ContactID, String PublicKey, JSONObject ContactKeyJSON)
    {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            Log.i("Key Niks",ContactKeyJSON.toString());
            ContactKeyJSON.put("t",preferences.getString("MyContact","0"));
            ContactKeyJSON.put("CName",preferences.getString("ContactName","--"));
            if(BackendFunctions.isConnectedOnline(context) && !preferences.getString("ServerURL","---").equals("---"))
            {
                new SMSHandler(context).SendSMSOnline(ContactID, ContactKeyJSON, PublicKey);
            }
            else
            {
                new SMSHandler(context).sendEncryptedSMS(ContactID, ContactKeyJSON, PublicKey,3);
            }
            Log.i("Key Step 3","Key Sent ");
        } catch (JSONException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void VerifyContact(JSONObject ContactObject)
    {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String Contact;
        String ContactHash;
        String KeyHash;
        String PublicKey;
        JSONObject ContactKeyJSON = new JSONObject();
        JSONObject LocalContact;
        Log.i("Key Step 4 ","Contact Verification Process Started");
        try
        {
            Log.i("Key Stage: ", String.valueOf(ContactObject.getInt("x")));
            Contact =ContactObject.getString("t");
            LocalContact = new DBManager(context).GetContact(Contact);
            Log.i("Key Step -->","KEY SEMA HII "+ LocalContact.toString());
            ContactHash = PKI_Cipher.ComputeHash(LocalContact.getString("Secret"));
            KeyHash = PKI_Cipher.ComputeHash(LocalContact.getString("PrivKey"));
            PublicKey = LocalContact.getString("PubKey");
            Log.i("Contact Hash:",ContactHash+" Key Hash "+ContactObject.getString("Secret"));
            if(ContactHash.equals(ContactObject.getString("Secret")) ||
                    KeyHash.equals(ContactObject.getString("SecretKey")))
            {
                if(ContactObject.getInt("x")==4)
                {
                    ContactKeyJSON.put("x",5);
                }
                else if(ContactObject.getInt("x")==5)
                {
                    ContactKeyJSON.put("x",6);
                }
                Log.i("Key Step -->","KEY NDIO HII "+ ContactKeyJSON);
                ContactKeyJSON.put("t",preferences.getString("MyContact","NewContact"));
                ContactKeyJSON.put("Secret",ContactObject.getString("Secret"));
                ContactKeyJSON.put("SecretKey",KeyHash);
                Log.i("Key Step -->","Contact Key Verified "+ ContactKeyJSON);
                new DBManager(context).VerifyContactPK(Contact,ContactHash);
                if(BackendFunctions.isConnectedOnline(context) && !preferences.getString("ServerURL","---").equals("---"))
                {
                    Log.i("Key Step -->","NIKO HAPA "+ ContactKeyJSON);
                    new SMSHandler(context).SendSMSOnline(Contact, ContactKeyJSON, PublicKey);
                }
                else
                {
                    Log.i("Key Step -->","AMA HAPA "+ ContactKeyJSON);
                    new SMSHandler(context).sendEncryptedSMS(Contact, ContactKeyJSON, PublicKey,3);
                }
            }
            else
            {
                Log.i("Key Step 4 Error","Doesn't Match");
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            Log.i("Key Step 4 Error",e.getMessage());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void RequestContact(String CID)
    {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONObject MyContactJSON = new JSONObject();
        try {
            MyContactJSON.put("t",preferences.getString("MyContact","0"));
            MyContactJSON.put("x",7);
            if(BackendFunctions.isConnectedOnline(context) && !preferences.getString("ServerURL","---").equals("---"))
            {
                new SMSHandler(context).SendSMSOnline(CID, MyContactJSON, "--");
            }
            else
            {
                new SMSHandler(context).sendPlainSMS(CID, MyContactJSON.toString());
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void RespondWithKey(String CID)
    {

        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONObject OtherContactJSON = new DBManager(context).GetContact(CID);
        JSONObject MyContactJSON = new JSONObject();
        try {
            MyContactJSON.put("t",preferences.getString("MyContact","0"));
            MyContactJSON.put("x",8);
            MyContactJSON.put("K",PKI_Cipher.SharePublicKey());
            Log.i("Key Responding",MyContactJSON.toString());
            if(BackendFunctions.isConnectedOnline(context) && !preferences.getString("ServerURL","---").equals("---"))
            {
                Log.i("Key to send","Online here");
                new SMSHandler(context).SendSMSOnline(CID, MyContactJSON, OtherContactJSON.getString("PubKey"));
            }
            else
            {
                Log.i("Key to send","Offline here");
                new SMSHandler(context).sendPlainSMS(CID, MyContactJSON.toString());
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

}
