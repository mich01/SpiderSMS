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
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;


public class PKI_Cipher
{
    private static byte[] KeyCharArray;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void TestCrypto()
    {
        String Cyphertext =null;
        KeyPairGenerator keyPairGenerator;
        try
        {
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
            publicKey =keyPair.getPublic();
            privateKey =keyPair.getPrivate();
            //Cyphertext =new PKI_Cipher().Encrypt("Hi there mr Michael", publicKey);
           // Log.i("Encrypt"," This is the Public Key "+publicKey);
            //Log.i("Encrypt"," This is the Private Key "+privateKey);
           // Log.i("Encrypt"," This is the Ciphertext "+ Cyphertext);
            //Log.i("Encrypt","\n--------------------------------------\n");
            //String PlainText = new PKI_Cipher().Decrypt(Cyphertext,keyPair.getPrivate());
            //Log.i("Encrypt"," This is the Plaintext "+ PlainText);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
        }

            //Log.i("TAG"," This is the CipherText "+Cyphertext);
        //PrivateKey privateKey;
        //String publicKey = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
        //String privateKey = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);
        //String privateKey = keyPair.getPrivate().toString();
        //PrivateKey PrivKey =new PKI_Cipher().getPrivateKey(privateKey);
        //String KeyPair=  Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);\

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

    private static final int pswdIterations = 10;
    private static final int keySize = 128;
    private static final String cypherInstance = "AES/CBC/PKCS5Padding";
    private static final String secretKeyInstance = "PBKDF2WithHmacSHA1";
    private static final String plainText = "sampleText";
    private static final String AESSalt = "ddddd";
    private static final String initializationVector = "8119745113154120";

    public String Encrypt(String PlainText, String Key)
    {
        String CipherText=null;
        try
        {
        SecretKeySpec skeySpec = new SecretKeySpec(getRaw(Key, AESSalt), "AES");
        Cipher cipher = Cipher.getInstance(cypherInstance);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(initializationVector.getBytes()));
        byte[] encrypted = cipher.doFinal(PlainText.getBytes());
            CipherText =Base64.encodeToString(encrypted, Base64.NO_PADDING);
        }catch (Exception e){e.printStackTrace();}
        Log.i("Encrypt"," This is the CipherText "+ CipherText);
        return CipherText;
    }

    public String Decrypt(String CipherText, String Key)
    {
        String PlainText=null;
        try {
            byte[] encryted_bytes = Base64.decode(CipherText, Base64.NO_PADDING);
            SecretKeySpec skeySpec = new SecretKeySpec(getRaw(Key, AESSalt), "AES");
            Cipher cipher = Cipher.getInstance(cypherInstance);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(initializationVector.getBytes()));
            byte[] decrypted = cipher.doFinal(encryted_bytes);
            PlainText = new String(decrypted, "UTF-8");
        }catch (Exception e){e.printStackTrace();}
        Log.i("Encrypt"," This is the PLainText "+ PlainText);
        return PlainText;
    }

    private byte[] getRaw(String plainText, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyInstance);
            KeySpec spec = new PBEKeySpec(plainText.toCharArray(), salt.getBytes(), pswdIterations, keySize);
            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}
