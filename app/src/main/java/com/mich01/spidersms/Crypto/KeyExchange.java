package com.mich01.spidersms.Crypto;


import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

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
    public boolean FirstExchange(String ContactID, String PublicKey,String SecretKey,String SharedSecret)
    {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean Completed = false;
        JSONObject ContactKeyJSON = new JSONObject();
        try {
            ContactKeyJSON.put("x","3");
            ContactKeyJSON.put("target",ContactID);
            ContactKeyJSON.put("SecretKey",SecretKey);
            ContactKeyJSON.put("CName",preferences.getString("ContactName","NewContact"));
            ContactKeyJSON.put("Secret",SharedSecret);
            //ContactKeyJSON.put("PubKey",PublicKey);
            new SMSHandler(context).SendSMSOnline(ContactID,ContactKeyJSON.toString(),PublicKey);
            Log.i("Key Step 3","Key Sent ");
        } catch (JSONException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return Completed;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void ThirdExchange(JSONObject ContactObject)
    {
        String Contact;
        String ContactHash;
        String KeyHash;
        String PublicKey;
        JSONObject ContactKeyJSON = new JSONObject();
        JSONObject LocalContact;
        try {
            Contact =ContactObject.getString("target");
            LocalContact = new DBManager(context).GetContact(Contact);
            KeyHash = PKI_Cipher.ComputeHash(LocalContact.getString("Secret"));
            ContactHash = PKI_Cipher.ComputeHash(LocalContact.getString("PrivKey"));
            PublicKey = LocalContact.getString("PubKey");
            /*if(ContactHash.equals(ContactObject.getString("Secret")) ||
                    KeyHash.equals(ContactObject.getString("PrivKey")))
            {

            }
            else
            {
                Log.i("Contact ","Doesn't Match");
            }*/
            ContactKeyJSON.put("x","4");
            ContactKeyJSON.put("target",Contact);
            ContactKeyJSON.put("Secret",ContactHash);
            ContactKeyJSON.put("KeyHash",KeyHash);
            //new DBManager(context).UpdateContactSpec(ContactKeyJSON);
            Log.i("Contact ",ContactKeyJSON.toString());
            new SMSHandler(context).SendSMSOnline(Contact,ContactKeyJSON.toString(),PublicKey);
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void VerifyContact(JSONObject ContactObject)
    {
        String Contact;
        String ContactHash;
        String KeyHash;
        String PublicKey;
        JSONObject ContactKeyJSON = new JSONObject();
        JSONObject LocalContact;
        Log.i("Key Step 6 ","Contact Verification Process Started");
        try
        {
            Contact =ContactObject.getString("target");
            LocalContact = new DBManager(context).GetContact(Contact);
            ContactHash = PKI_Cipher.ComputeHash(LocalContact.getString("Secret"));
            KeyHash = PKI_Cipher.ComputeHash(LocalContact.getString("PrivKey"));
            PublicKey = LocalContact.getString("PubKey");
            Log.i("Contact Hash:",ContactHash+" Key Hash "+ContactObject.getString("Secret"));
            if(ContactHash.equals(ContactObject.getString("Secret")) ||
                    KeyHash.equals(ContactObject.getString("SecretKey")))
            {
                ContactKeyJSON.put("x","6");
                ContactKeyJSON.put("target",Contact);
                ContactKeyJSON.put("Secret",ContactHash);
                ContactKeyJSON.put("ACK","1");
                ContactKeyJSON.put("Confirmed","1");
                Log.i("Key Step 6","Contact Key Verified");
                new DBManager(context).VerifyContactPK(Contact,ContactHash);
                new SMSHandler(context).SendSMSOnline(Contact,ContactKeyJSON.toString(),PublicKey);
            }
            else
            {
                Log.i("Key Step 6 Error","Doesn't Match");
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            Log.i("Key Step 6 Error",e.getMessage());
        }
    }
    public static boolean VerifyContact(byte[] publicKey, byte[] SecretMsg, byte[] signature)
    {
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Log.i("Keys: ", Base64.encodeToString(publicKey,0)+" -- "+Base64.encodeToString(SecretMsg,0)+" -- "+Base64.encodeToString(signature,0));
        return cipher.verifySignature(publicKey, SecretMsg, signature);
    }

}
