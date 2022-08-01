package com.mich01.spidersms.Crypto;


import static com.mich01.spidersms.Data.StringsConstants.CName;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.ContactTarget;
import static com.mich01.spidersms.Data.StringsConstants.MessageType;
import static com.mich01.spidersms.Data.StringsConstants.MyContact;
import static com.mich01.spidersms.Data.StringsConstants.NewContact;
import static com.mich01.spidersms.Data.StringsConstants.PrivKey;
import static com.mich01.spidersms.Data.StringsConstants.PubKey;
import static com.mich01.spidersms.Data.StringsConstants.Pub_Key;
import static com.mich01.spidersms.Data.StringsConstants.Secret;
import static com.mich01.spidersms.Data.StringsConstants.SecretKey;
import static com.mich01.spidersms.Data.StringsConstants.ServerURL;
import static com.mich01.spidersms.Data.StringsConstants.global_pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

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
    public void FirstExchange(String contact_ID, String PublicKey, JSONObject ContactKeyJSON)
    {
        preferences = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        try {
            ContactKeyJSON.put(ContactTarget,preferences.getString(MyContact,"0"));
            ContactKeyJSON.put(CName,preferences.getString(ContactName,"--"));
            if(BackendFunctions.isConnectedOnline(context) && !preferences.getString(ServerURL,"---").equals("---"))
            {
                new SMSHandler(context).sendSMSOnline(contact_ID, ContactKeyJSON, PublicKey);
            }
            else
            {
                new SMSHandler(context).sendEncryptedSMS(contact_ID, ContactKeyJSON, PublicKey, null,null,3);
            }
        } catch (JSONException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void VerifyContact(JSONObject ContactObject)
    {
        preferences = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        String Contact_ID = null;
        String ContactHash;
        String KeyHash;
        String PublicKey;
        JSONObject ContactKeyJSON = new JSONObject();
        JSONObject LocalContact;
        try
        {
            Contact_ID =ContactObject.getString(ContactTarget);
            LocalContact = new DBManager(context).GetContact(Contact_ID);
            ContactHash = PKI_Cipher.ComputeHash(LocalContact.getString(Secret));
            KeyHash = PKI_Cipher.ComputeHash(LocalContact.getString(PrivKey));
            PublicKey = LocalContact.getString(PubKey);
            if(ContactHash.equals(ContactObject.getString(Secret)) &&
                    KeyHash.equals(ContactObject.getString(SecretKey)))
            {
                if(ContactObject.getInt(MessageType)==4)
                {
                    ContactKeyJSON.put(MessageType,5);
                }
                else if(ContactObject.getInt(MessageType)==5)
                {
                    ContactKeyJSON.put(MessageType,6);
                }
                Log.i("key M!",Contact_ID);
                ContactKeyJSON.put(ContactTarget,preferences.getString(MyContact,NewContact));
                ContactKeyJSON.put(Secret,ContactObject.getString(Secret));
                ContactKeyJSON.put(SecretKey,KeyHash);
                new DBManager(context).VerifyContactPK(Contact_ID,ContactHash);
                if(BackendFunctions.isConnectedOnline(context) && !preferences.getString(ServerURL,"---").equals("---"))
                {
                    new SMSHandler(context).sendSMSOnline(Contact_ID, ContactKeyJSON, PublicKey);
                }
                else
                {
                    new SMSHandler(context).sendEncryptedSMS(Contact_ID, ContactKeyJSON, PublicKey,null,null,3);
                }
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void RequestContact(String CID)
    {
        preferences = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        JSONObject MyContactJSON = new JSONObject();
        try {
            MyContactJSON.put(ContactTarget,preferences.getString(MyContact,"0"));
            MyContactJSON.put(MessageType,7);
            if(BackendFunctions.isConnectedOnline(context) && !preferences.getString(ServerURL,"---").equals("---"))
            {
                new SMSHandler(context).sendSMSOnline(CID, MyContactJSON, "--");
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

        preferences = context.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
        JSONObject OtherContactJSON = new DBManager(context).GetContact(CID);
        JSONObject MyContactJSON = new JSONObject();
        try {
            MyContactJSON.put(ContactTarget,preferences.getString(MyContact,"0"));
            MyContactJSON.put(MessageType,8);
            MyContactJSON.put(Pub_Key,PKI_Cipher.SharePublicKey());
            if(BackendFunctions.isConnectedOnline(context) && !preferences.getString(ServerURL,"---").equals("---"))
            {
                new SMSHandler(context).sendSMSOnline(CID, MyContactJSON, OtherContactJSON.getString(PubKey));
            }
            else
            {
                new SMSHandler(context).sendPlainSMS(CID, MyContactJSON.toString());
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

}
