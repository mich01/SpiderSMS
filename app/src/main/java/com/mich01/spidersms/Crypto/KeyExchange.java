package com.mich01.spidersms.Crypto;


import android.util.Base64;
import android.util.Log;

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

public class KeyExchange
{
    public static void KeySetup(String SecretMessage)
    {
        byte[] publicKey = null;
        byte[] privateKey =null;
        byte [] SecretMsg = SecretMessage.getBytes();
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Curve25519KeyPair keyPair = Curve25519.getInstance(Curve25519.BEST).generateKeyPair();
        publicKey =keyPair.getPublicKey();
        privateKey = keyPair.getPrivateKey();
        //Calculate Shared Secret
        byte[]  sharedSecret = cipher.calculateAgreement(publicKey, privateKey);
        //Calculating Signature
        byte[] signature = cipher.calculateSignature(privateKey, SecretMsg);
        //ByteBuffer buffer = ByteBuffer.wrap(publicKey);
        //buffer.order(ByteOrder.BIG_ENDIAN);
        //System.out.println(buffer.getLong());
        //buffer.order(ByteOrder.LITTLE_ENDIAN);
        //System.out.println(buffer.getLong());
        //String Message = cipher.
        //Log.i("Message: ",)
        Log.i("Keys: ", publicKey+" -- "+Base64.encodeToString(privateKey,0)+" -- "+Base64.encodeToString(sharedSecret,0));
        //boolean validSignature = cipher.verifySignature(publicKey, SecretMsg, signature);
    }

    public static boolean VerifyContact(byte[] publicKey, byte[] SecretMsg, byte[] signature)
    {
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Log.i("Keys: ", Base64.encodeToString(publicKey,0)+" -- "+Base64.encodeToString(SecretMsg,0)+" -- "+Base64.encodeToString(signature,0));

        return cipher.verifySignature(publicKey, SecretMsg, signature);
    }
}
