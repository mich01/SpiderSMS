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
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.getPrefs;

import android.content.Context;
import android.os.Build;

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
    public KeyExchange(Context context) {
        this.context = context;
    }
    public void FirstExchange(String contact_ID, String PublicKey, JSONObject ContactKeyJSON)
    {

        MyPrefs = getPrefs(context);
        try {
            ContactKeyJSON.put(ContactTarget,MyPrefs.getString(MyContact,"0"));
            ContactKeyJSON.put(CName,MyPrefs.getString(ContactName,"--"));
            if(BackendFunctions.isConnectedOnline(context) && !MyPrefs.getString(ServerURL,"---").equals("---"))
            {
                new SMSHandler(context).sendSMSOnline(contact_ID, ContactKeyJSON, PublicKey);
            }
            else
            {
                new SMSHandler(context).sendEncryptedSMS(contact_ID, ContactKeyJSON, PublicKey, null,null,3);
            }
        } catch (JSONException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NullPointerException ignored) {}
    }
    public void VerifyContact(JSONObject ContactObject)
    {
        MyPrefs = getPrefs(context);
        String Contact_ID;
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
                ContactKeyJSON.put(ContactTarget,MyPrefs.getString(MyContact,NewContact));
                ContactKeyJSON.put(Secret,ContactObject.getString(Secret));
                ContactKeyJSON.put(SecretKey,KeyHash);
                new DBManager(context).VerifyContactPK(Contact_ID,ContactHash);
                if(BackendFunctions.isConnectedOnline(context) && !MyPrefs.getString(ServerURL,"---").equals("---"))
                {
                    new SMSHandler(context).sendSMSOnline(Contact_ID, ContactKeyJSON, PublicKey);
                }
                else
                {
                    new SMSHandler(context).sendEncryptedSMS(Contact_ID, ContactKeyJSON, PublicKey,null,null,3);
                }
            }
        } catch (NullPointerException | JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException ignored) {}
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void RequestContact(String CID)
    {
        MyPrefs = getPrefs(context);
        JSONObject MyContactJSON = new JSONObject();
        try {
            MyContactJSON.put(ContactTarget,MyPrefs.getString(MyContact,"0"));
            MyContactJSON.put(MessageType,7);
            if(BackendFunctions.isConnectedOnline(context) && !MyPrefs.getString(ServerURL,"---").equals("---"))
            {
                new SMSHandler(context).sendSMSOnline(CID, MyContactJSON, "--");
            }
            else
            {
                new SMSHandler(context).sendPlainSMS(CID, MyContactJSON.toString());
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException ignored) {}
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void RespondWithKey(String CID)
    {

        MyPrefs = getPrefs(context);
        JSONObject OtherContactJSON = new DBManager(context).GetContact(CID);
        JSONObject MyContactJSON = new JSONObject();
        try {
            MyContactJSON.put(ContactTarget,MyPrefs.getString(MyContact,"0"));
            MyContactJSON.put(MessageType,8);
            MyContactJSON.put(Pub_Key,PKI_Cipher.SharePublicKey());
            if(BackendFunctions.isConnectedOnline(context) && !MyPrefs.getString(ServerURL,"---").equals("---"))
            {
                new SMSHandler(context).sendSMSOnline(CID, MyContactJSON, OtherContactJSON.getString(PubKey));
            }
            else
            {
                new SMSHandler(context).sendPlainSMS(CID, MyContactJSON.toString());
            }
        } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException ignored) {}
    }

}
