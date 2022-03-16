package com.mich01.spidersms.Crypto;


import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.SMSHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class KeyExchange
{
    Context context;

    public KeyExchange(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean FirstExchange(String ContactID, String PublicKey)
    {
        boolean Completed = false;
        String SecretKey = GenerateNewKey();
        String SharedSecret = GenerateNewKey();
        JSONObject ContactKeyJSON = new JSONObject();
        try {
            ContactKeyJSON.put("CID",ContactID);
            ContactKeyJSON.put("SecretKey",SecretKey);
            ContactKeyJSON.put("SharedSecret",SharedSecret);
            String EncryptedText = new PKI_Cipher().Encrypt(ContactKeyJSON.toString(),PublicKey);
            new SMSHandler(context).SendSMSOnline(ContactID,EncryptedText);
        } catch (JSONException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return Completed;
    }
    public static boolean VerifyContact(byte[] publicKey, byte[] SecretMsg, byte[] signature)
    {
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Log.i("Keys: ", Base64.encodeToString(publicKey,0)+" -- "+Base64.encodeToString(SecretMsg,0)+" -- "+Base64.encodeToString(signature,0));

        return cipher.verifySignature(publicKey, SecretMsg, signature);
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
}
