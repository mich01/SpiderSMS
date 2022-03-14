package com.mich01.spidersms.Crypto;


import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;


public class PKI_Cipher
{
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void TestCrypto()
    {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            "SpiderSMS",
                            KeyProperties.PURPOSE_DECRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .build());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            String publicKey = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
            String Cyphertext =new PKI_Cipher().Encrypt("Hi there mr Michael", keyPair.getPublic());
            //String privateKey = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);
            //String privateKey = keyPair.getPrivate().toString();
            //PrivateKey PrivKey =new PKI_Cipher().getPrivateKey(privateKey);
            //String KeyPair=  Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);
            String PlainText = new PKI_Cipher().Decrypt(Cyphertext,keyPair.getPrivate());
            //System.out.println(" This is the Key "+publicKey);
            Log.i("TAG"," This is the CipherText "+Cyphertext);
            //System.out.println(" This is the Key "+privateKey);
            System.out.println("\n----------------------------- \n");
            System.out.println(" This is the Plaintext "+ PlainText);
        } catch (NoSuchAlgorithmException | NullPointerException | NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public KeyPair generateKeyPair(String Key_Input)
    {
        KeyPair keyPair = null;
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            "SpiderSMS",
                            KeyProperties.PURPOSE_DECRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .build());
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NullPointerException | NoSuchProviderException | InvalidAlgorithmParameterException  e) {
            e.printStackTrace();
        }
        return keyPair;
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void GenerateKeyPair()
    {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            "SpiderSMS",
                            KeyProperties.PURPOSE_DECRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .build());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
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
       byte[] data = Input.getBytes(StandardCharsets.UTF_16);
       String base64 = Base64.encodeToString(data, Base64.DEFAULT);
       return base64;
   }
    public static String Decode(String Input)
    {
        byte[] data = Base64.decode(Input, Base64.DEFAULT);
        String text = null;
        try {
            text = new String(data, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    public String Encrypt(String data, PublicKey publicKey) throws NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String CipherText = null;
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey,spec);
            CipherText =  Base64.encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
          return CipherText;
    }

    public String Decrypt(String data, PrivateKey PrivKey)
    {
        String PlainText = null;
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, PrivKey,spec);
            byte[] PlainBytes= cipher.doFinal(Base64.decode(data.getBytes(),Base64.DEFAULT));
            PlainText =new String(PlainBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
         return PlainText;
    }
}
