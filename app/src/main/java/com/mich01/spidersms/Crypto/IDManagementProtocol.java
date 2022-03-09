package com.mich01.spidersms.Crypto;

import static android.content.Context.ALARM_SERVICE;


import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Prefs.PrefsMgr;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.KeyAgreement;

public class IDManagementProtocol
{
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
        JSONObject KeyPair = new JSONObject();
        try {
            KeyPair.put("PublicKey",publicKey);
            KeyPair.put("PrivateKey",privateKey);
            KeyPair.put("KeyPair",keyPair);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Keys: ",publicKey+" -- "+privateKey+" -- "+keyPair);
        return KeyPair;
    }
   public String Encode(String Input)
   {
       byte[] data = Input.getBytes(StandardCharsets.UTF_8);
       String base64 = Base64.encodeToString(data, Base64.DEFAULT);
       return base64;
   }
    public static String Decode(String Input)
    {
        byte[] data = Base64.decode(Input, Base64.DEFAULT);
        String text = null;
        try {
            text = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void KEYPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        "eckeypair",
                        KeyProperties.PURPOSE_AGREE_KEY)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        .build());
        KeyPair myKeyPair = keyPairGenerator.generateKeyPair();
        // Exchange public keys with server. A new ephemeral key MUST be used for every message.
        PublicKey serverEphemeralPublicKey = null; // Ephemeral key received from server.

        // Create a shared secret based on our private key and the other party's public key.
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "AndroidKeyStore");
        keyAgreement.init(myKeyPair.getPrivate());
        keyAgreement.doPhase(serverEphemeralPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();
        // sharedSecret cannot safely be used as a key yet. We must run it through a key derivation
        // function with some other data: "salt" and "info". Salt is an optional random value,
        // omitted in this example. It's good practice to include both public keys and any other
        // key negotiation data in info. Here we use the public keys and a label that indicates
        // messages encrypted with this key are coming from the server.
        byte[] salt = {};
        ByteArrayOutputStream info = new ByteArrayOutputStream();
        info.write("ECDH secp256r1 AES-256-GCM-SIV\0".getBytes(StandardCharsets.UTF_8));
        info.write(myKeyPair.getPublic().getEncoded());
        info.write(serverEphemeralPublicKey.getEncoded());
        // This example uses the Tink library and the HKDF key derivation function.
        /*AesGcmSiv key = new AesGcmSiv(Hkdf.computeHkdf(
                "HMACSHA256", sharedSecret, salt, info.toByteArray(), 32));
        byte[] associatedData = {};
        return key.decrypt(ciphertext, associatedData);*/
    }
}
