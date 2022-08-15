package com.mich01.spidersms.Crypto;

import static com.mich01.spidersms.Data.StringsConstants.AndroidKeyStore;
import static com.mich01.spidersms.Data.StringsConstants.AppName;
import static com.mich01.spidersms.Data.StringsConstants.SHA256;
import static com.mich01.spidersms.Data.StringsConstants.corruptedEncryption;
import static com.mich01.spidersms.Data.StringsConstants.cypherInstanceAES;
import static com.mich01.spidersms.Data.StringsConstants.cypherInstanceRSA;
import static com.mich01.spidersms.Data.StringsConstants.keySize;
import static com.mich01.spidersms.Data.StringsConstants.pswdIterations;
import static com.mich01.spidersms.Data.StringsConstants.requestResend;
import static com.mich01.spidersms.Data.StringsConstants.secretKeyInstance;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.BackendFunctions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;


public class PKI_Cipher
{

    private final Context context;


    public PKI_Cipher(Context context) {
        this.context = context;
    }

    public static String ComputeHash(String input)
    {
        MessageDigest digest;
        StringBuilder sb = new StringBuilder();
        try {
            digest = MessageDigest.getInstance(SHA256);
            digest.reset();
            digest.update(input.getBytes( StandardCharsets.UTF_8));
            byte[] byteData = digest.digest(input.getBytes());
            for (byte byteDatum : byteData) {
                sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException ignored) {}
        return sb.toString();
    }

    public String Encrypt(String PlainText, String Key,String AESSalt, String initializationVector)
    {
        String CipherText=null;
        try
        {
        SecretKeySpec skeySpec = new SecretKeySpec(getRaw(Key, AESSalt), cypherInstanceAES);
        Cipher cipher = Cipher.getInstance(cypherInstanceAES);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(initializationVector.getBytes()));
        byte[] encrypted = cipher.doFinal(PlainText.getBytes());
            CipherText =Base64.encodeToString(encrypted, Base64.NO_PADDING);
        }catch (Exception ignored){}
        return CipherText;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public String Decrypt(String CipherText, String Key, String AESSalt, String initializationVector)
    {
        String PlainText=null;
        try {
            byte[] encryted_bytes = Base64.decode(CipherText, Base64.NO_PADDING);
            SecretKeySpec skeySpec = new SecretKeySpec(getRaw(Key, AESSalt), cypherInstanceAES);
            Cipher cipher = Cipher.getInstance(cypherInstanceAES);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(initializationVector.getBytes()));
            byte[] decrypted = cipher.doFinal(encryted_bytes);
            PlainText = new String(decrypted, StandardCharsets.UTF_8);
        }catch (Exception e){
            new BackendFunctions().AlertUser(context, corruptedEncryption,requestResend);

        }
        return PlainText;
    }

    private byte[] getRaw(String plainText, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyInstance);
            KeySpec spec = new PBEKeySpec(plainText.toCharArray(), salt.getBytes(), pswdIterations, keySize);
            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ignored){}
        return new byte[0];
    }

    public static String GenerateNewKey()
    {
        int n =16;
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new SecureRandom().nextBytes(array);
        String randomString
                = new String(array, StandardCharsets.UTF_8);
        // Create a StringBuffer to store the result
        StringBuilder r = new StringBuilder();
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


    public static void GeneratePrivateKey()
    {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore);
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            AppName,
                            KeyProperties.PURPOSE_DECRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .setUserAuthenticationRequired(true)
                            .build());
            keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException ignored) {}
    }

    public static String SharePublicKey()
    {
        String Public_Key = null;
        try
        {
            KeyStore keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(AppName).getPublicKey();
            Public_Key = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException ignored){}
        return Public_Key;
    }


    public String EncryptPKI(String data, String public_Key )
    {
        String CipherText = null;
        Cipher cipher;
        try {
            byte[] publicBytes = Base64.decode(public_Key,Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publiKey = keyFactory.generatePublic(keySpec);
            OAEPParameterSpec spec = new OAEPParameterSpec(SHA256, "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
            cipher = Cipher.getInstance(cypherInstanceRSA);
            cipher.init(Cipher.ENCRYPT_MODE, publiKey,spec);
            CipherText =  Base64.encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ignored){}
        return CipherText;
    }

    public String DecryptPKI(String data)
    {
        String PlainText = null;
        OAEPParameterSpec spec = new OAEPParameterSpec(SHA256, "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
        Cipher cipher;
        try {
            KeyStore keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(AppName, null);
            cipher = Cipher.getInstance(cypherInstanceRSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey,spec);
            byte[] PlainBytes= cipher.doFinal(Base64.decode(data.getBytes(),Base64.DEFAULT));
            PlainText =new String(PlainBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | KeyStoreException | UnrecoverableKeyException | CertificateException | IOException ignored) {}
        return PlainText;
    }
}
