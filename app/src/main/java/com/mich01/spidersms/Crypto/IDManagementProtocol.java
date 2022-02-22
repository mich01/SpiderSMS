package com.mich01.spidersms.Crypto;

import static android.content.Context.ALARM_SERVICE;


import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;


import com.mich01.spidersms.Prefs.PrefsMgr;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

public class IDManagementProtocol
{
    public static String GenerateNewID()
    {
        int n =4;
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // remove all spacial char
        String  AlphaNumericString
                = randomString
                .replaceAll("[^A-Z0-9]", "");

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < AlphaNumericString.length(); k++) {

            if (Character.isLetter(AlphaNumericString.charAt(k))
                    && (n > 0)
                    || Character.isDigit(AlphaNumericString.charAt(k))
                    && (n > 0)) {

                r.append(AlphaNumericString.charAt(k));
                n--;
            }
        }
        // return the resultant string
        return r.toString();
    }
    public static String GenerateNewKey()
    {
        int n =16;
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // remove all spacial char
        String  AlphaNumericString
                = randomString
                .replaceAll("[^A-Za-z0-9]", "");

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < AlphaNumericString.length(); k++) {

            if (Character.isLetter(AlphaNumericString.charAt(k))
                    && (n > 0)
                    || Character.isDigit(AlphaNumericString.charAt(k))
                    && (n > 0)) {

                r.append(AlphaNumericString.charAt(k));
                n--;
            }
        }
        // return the resultant string
        return r.toString();
    }
    public static String ComputeHash(String input)
    {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(input.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] byteData = digest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < byteData.length; i++){
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    public static boolean ExtractPrivateContact(Context context,String Contact)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(Contact);
            return true;//new DBManager(context).insertNewContact(jsonObject);
        } catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean ShareContact(Context context,String Contactid)
    {
        String PrivKey = GenerateNewKey();
        String StegKey = GenerateNewKey();
        String CryptoAlg = GenerateNewKey();
        String Secret = GenerateNewKey();
        try
        {
            SharedPreferences preferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Data", "Contact");
            //jsonObject.put("CID", MyContact);
            jsonObject.put("CName", preferences.getString("PublicName","0"));
            jsonObject.put("PubKey", preferences.getString("PublicKey","123456"));
            jsonObject.put("PrivKey", PrivKey);
            jsonObject.put("StegKey", StegKey);
            jsonObject.put("CryptoAlg", CryptoAlg);
            jsonObject.put("Secret", Secret);
           /* if(new DBManager(context).insertNewContact(jsonObject))
            {
                SendConfigs(jsonObject.toString(),Contactid);
                return true;
            }
            else
            {
                return false;
            }*/
            return true;
        } catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean ContactAddOK(String ContactID)
    {
        return true;
    }
    public static boolean ID_Key_ExchangeFail(String Stage)
    {
        return true;
    }
    public static boolean ExchangeSuccess(String Stage)
    {
        return true;
    }

    public static JSONObject PKI_CURVE_25519()
    {
        PublicKey publicKey = null;
        PrivateKey privateKey =null;
        KeyPair keyPair = null;
        try {
            X9ECParameters curveParams = CustomNamedCurves.getByName("Curve25519");
            ECParameterSpec ecSpec = new ECParameterSpec(curveParams.getCurve(), curveParams.getG(), curveParams.getN(), curveParams.getH(), curveParams.getSeed());

            KeyPairGenerator kpg = null;
            kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            kpg.initialize(ecSpec);
            keyPair = kpg.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        JSONObject KeyJSON = new JSONObject();
        try {
            KeyJSON.put("PublicKey",publicKey);
            KeyJSON.put("PrivateKey",privateKey);
            KeyJSON.put("KeyPair",keyPair);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Keys: ",publicKey+" -- "+privateKey+" -- "+keyPair);
        return KeyJSON;
    }
}
