package com.mich01.spidersms.UI;


import static com.mich01.spidersms.Data.StringsConstants.APPEND_PARAM;
import static com.mich01.spidersms.Data.StringsConstants.Body;
import static com.mich01.spidersms.Data.StringsConstants.Confirmed;
import static com.mich01.spidersms.Data.StringsConstants.ContactID;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.ContactTarget;
import static com.mich01.spidersms.Data.StringsConstants.DEFAULT_PREF_VALUE;
import static com.mich01.spidersms.Data.StringsConstants.MessageBody;
import static com.mich01.spidersms.Data.StringsConstants.MessageType;
import static com.mich01.spidersms.Data.StringsConstants.MyContact;
import static com.mich01.spidersms.Data.StringsConstants.PrivKey;
import static com.mich01.spidersms.Data.StringsConstants.ProxyNumber;
import static com.mich01.spidersms.Data.StringsConstants.PubKey;
import static com.mich01.spidersms.Data.StringsConstants.Salt;
import static com.mich01.spidersms.Data.StringsConstants.Secret;
import static com.mich01.spidersms.Data.StringsConstants.ServerURL;
import static com.mich01.spidersms.Data.StringsConstants.Status;
import static com.mich01.spidersms.Data.StringsConstants.Timestamp;
import static com.mich01.spidersms.Data.StringsConstants.inorout;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.getPrefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mich01.spidersms.Adapters.MessageAdapter;
import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Backend.ResponseMessage;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ChatActivity extends AppCompatActivity {

    EditText userInput;
    private static RecyclerView recyclerView;
    private static List<ResponseMessage> responseMessageList;
    private static MessageAdapter messageAdapter;
    private static String currentcontact;
    Context context;
    ImageButton sendButton;
    private String messageText;
    JSONObject contactJSON;
    int cypherType;
    private String encryptionKey;
    private String aesSalt;
    private String iv;
    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_chat);
        context = this;
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        currentcontact = bundle.getString(ContactID);
        contactJSON = new DBManager(context).GetContact(currentcontact);
        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.chat_conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList,context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);
        sendButton = findViewById(R.id.sendButton);
        if(contactJSON.length()==0)
        {
            userInput.setText(R.string.contact_unavailable);
            userInput.setEnabled(false);
            sendButton.setVisibility(View.GONE);
        }
        try {
            if(contactJSON.getInt(Confirmed)==1)
            {
                cypherType=2;
                encryptionKey =contactJSON.getString(PrivKey);
                aesSalt =contactJSON.getString(Salt);
                iv =contactJSON.getString(Secret);
            }
            else if(contactJSON.getInt(Confirmed)==0)
            {
                cypherType=1;
                encryptionKey = contactJSON.getString(PubKey);
            }

        } catch (JSONException ignored) {}
        new DBManager(ChatActivity.this).UpdateMessageStatus(currentcontact, 1);

        getSupportActionBar().setTitle(bundle.getString(ContactName));
        populateChatView(context);
        sendButton.setOnClickListener(view ->
        {
            if(userInput.getText().toString().isEmpty())
            {
               alertEmptySMS();
            }
            else
            {
                showSMSOptions(view, bundle.getString(ContactName));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void showSMSOptions(View view, String ContactName) {
        messageText = userInput.getText().toString();
        Button option1;
        Button option2;
        Button option3;
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.send_options_modal, viewGroup, false);
        builder.setView(dialogView);
        String sendTo = getString(R.string.send_option1) + " " + ContactName;
        option1 = dialogView.findViewById(R.id.send_option_1);
        option2 = dialogView.findViewById(R.id.send_option_2);
        option3 = dialogView.findViewById(R.id.send_option_3);
        option3.setEnabled(false);
        option2.setEnabled(false);
        option1.setText(sendTo);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        MyPrefs = getPrefs(context);
        if(MyPrefs.getString(ProxyNumber, "---").equals("---"))
        {
            option2.setEnabled(false);
        }
        if(!MyPrefs.getString(ServerURL, "---").equals("---") && BackendFunctions.isConnectedOnline(context))
        {
            option3.setEnabled(true);
        }
        option1.setOnClickListener(view1 ->
        {

            messageText =userInput.getText().toString();
            JSONObject smsBody = new JSONObject();
            try {
                sendEncrypted(smsBody);
            } catch (Exception ignored){}finally {
                alertDialog.cancel();
            }
        });
        option2.setOnClickListener(view12 ->{
            JSONObject smsBody = new JSONObject();
            try {
                smsBody.put(MessageType,2);
                smsBody.put(ContactTarget,APPEND_PARAM+MyPrefs.getString(MyContact,DEFAULT_PREF_VALUE));
                smsBody.put(Body,messageText);
            } catch (JSONException ignored){}
            try {
                sendProxyedSMS(smsBody);
            } catch (JSONException ignored){}
            finally {
                alertDialog.cancel();
            }});
        option3.setOnClickListener(view13 ->
        {
            JSONObject smsBody = new JSONObject();
            try {
                sendSMSAPI(smsBody);
            } catch (JSONException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException ignored) {}
            alertDialog.cancel();
            });
    }

    private void sendSMSAPI(JSONObject smsBody) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, JSONException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
            smsBody.put(MessageType,2);
            smsBody.put(ContactTarget,MyPrefs.getString(MyContact,DEFAULT_PREF_VALUE));
            smsBody.put(Body,messageText);
            updateChatMessages(currentcontact,messageText);
            encryptionKey = contactJSON.getString(PubKey);
            new SMSHandler(context).sendSMSOnline(currentcontact, smsBody,encryptionKey);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void sendProxyedSMS(JSONObject smsBody) throws JSONException {
        encryptionKey = contactJSON.getString(PubKey);
        updateChatMessages(currentcontact,messageText);
        new SMSHandler(context).proxyEncryptedSMS(smsBody,encryptionKey, aesSalt,iv);
    }

    private void sendEncrypted(JSONObject smsBody) throws JSONException {
        smsBody.put(MessageType,1);
        smsBody.put(Body,messageText);
        updateChatMessages(currentcontact,messageText);
        new SMSHandler(context).sendEncryptedSMS(currentcontact, smsBody,encryptionKey,aesSalt, iv,cypherType);
    }

    private void alertEmptySMS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        AlertDialog alertDialog = builder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setIcon(R.drawable.ic_baseline_error_outline_24);
        alertDialog.setTitle(getString(R.string.error));
        alertDialog.setMessage(getString(R.string.cannot_send_empty_message));
        alertDialog.show();
    }

    @SuppressLint({"Range", "NotifyDataSetChanged"})
    public static void populateChatView(Context c) {
        if(currentcontact!=null)
        {
            Cursor cur = new DBManager(c.getApplicationContext()).getCIDChats(currentcontact);
            responseMessageList.clear();
            while (cur != null && cur.moveToNext())
            {
                if (cur.getInt(cur.getColumnIndex(inorout)) == 0 && cur.getInt(cur.getColumnIndex(Status)) <3)
                {
                    responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex(MessageBody)), true, cur.getInt(cur.getColumnIndex(Status)),
                            cur.getString(cur.getColumnIndex(Timestamp))));
                }
                else if (cur.getInt(cur.getColumnIndex(inorout)) == 1 && cur.getInt(cur.getColumnIndex(Status))<3 )
                {
                    responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex(MessageBody)), false, cur.getInt(cur.getColumnIndex(Status)),
                            cur.getString(cur.getColumnIndex(Timestamp))));
                }
                else
                {
                    responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex(MessageBody)), false, 3,
                            cur.getString(cur.getColumnIndex(Timestamp))));
                }
            }
            if (cur != null) {
                messageAdapter.notifyDataSetChanged();
                if (ChatActivity.isLastVisible() && cur.getCount() > 0) {
                        ChatActivity.recyclerView.smoothScrollToPosition(ChatActivity.messageAdapter.getItemCount() - 1);
                }
            }
            ChatActivity.messageAdapter.notifyDataSetChanged();
        }
    }

    public static boolean isLastVisible() {
        LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        assert linearLayoutManager != null;
        int positionOfLastMessage = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
        return (positionOfLastMessage < numItems);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateChatPosition() {
        ResponseMessage message = new ResponseMessage(userInput.getText().toString(), true, 1,"0");
        responseMessageList.add(message);
        runOnUiThread(() -> {
            ChatActivity.messageAdapter.notifyDataSetChanged();
            if (ChatActivity.isLastVisible()) {
                ChatActivity.recyclerView.smoothScrollToPosition(ChatActivity.messageAdapter.getItemCount() - 1);
            }
        });

    }
    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new DBManager(ChatActivity.this).UpdateMessageStatus(currentcontact, 1);
        HomeActivity.rePopulateChats(context);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        finish();
    }

    public void updateChatMessages(String recepient, String sms)
    {
        new DBManager(context).updateLastMessage(recepient, sms, 1, 1);
        new DBManager(context).AddChatMessage(recepient, 0, sms, 0);
        updateChatPosition();
        userInput.setText("");
        HomeActivity.rePopulateChats(context);
        if (isLastVisible()) {
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
}