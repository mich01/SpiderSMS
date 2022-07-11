package com.mich01.spidersms.UI;


import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    EditText userInput;
    public static RecyclerView recyclerView;
    public static List<ResponseMessage> responseMessageList;
    public static MessageAdapter messageAdapter;
    public static String ContactID;
    Context context;
    ImageButton sendButton;
    private String MessageText;
    JSONObject ContactJSON;
    int CypherType;
    private static String EncryptionKey;
    private static String AES_Salt;
    private static String IV;
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
        ContactID = bundle.getString("ContactID");
        ContactJSON = new DBManager(context).GetContact(ContactID);
        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.chat_conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList,context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);
        sendButton = findViewById(R.id.sendButton);
        if(ContactJSON.length()==0)
        {
            userInput.setText("Contact Unavailable");
            userInput.setEnabled(false);
            sendButton.setVisibility(View.GONE);
        }
        try {
            if(ContactJSON.getInt("Confirmed")!=0 && ContactJSON.getInt("Confirmed")==1)
            {
                CypherType=2;
                EncryptionKey =ContactJSON.getString("PrivKey");
                AES_Salt =ContactJSON.getString("Salt");
                IV =ContactJSON.getString("IV");
            }
            else if(ContactJSON.getInt("Confirmed")!=0 && ContactJSON.getInt("Confirmed")==0)
            {
                CypherType=1;
                EncryptionKey = ContactJSON.getString("PubKey");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        new DBManager(ChatActivity.this).UpdateMessageStatus(ContactID, 1);

        getSupportActionBar().setTitle(bundle.getString("ContactName"));
        PopulateChatView(context);
        sendButton.setOnClickListener(view ->
        {
            if(userInput.getText().toString().isEmpty())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                AlertDialog alertDialog = builder.create();
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.setIcon(R.drawable.ic_baseline_error_outline_24);
                alertDialog.setTitle(getString(R.string.error));
                alertDialog.setMessage(getString(R.string.cannot_send_empty_message));
                alertDialog.show();
            }
            else
            {
                MessageText = userInput.getText().toString();
                Button Option1, Option2, Option3;
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.send_options_modal, viewGroup, false);
                builder.setView(dialogView);
                String SendTo = getString(R.string.send_option1) + " " + bundle.getString("ContactName");
                Option1 = dialogView.findViewById(R.id.send_option_1);
                Option2 = dialogView.findViewById(R.id.send_option_2);
                Option3 = dialogView.findViewById(R.id.send_option_3);
                Option3.setEnabled(false);
                Option2.setEnabled(false);
                MyPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                Option1.setText(SendTo);
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
                if(MyPrefs.getString("ProxyNumber", "---").equals("---"))
                {
                    Option2.setEnabled(false);
                }
                if(!MyPrefs.getString("ServerURL", "---").equals("---") && BackendFunctions.isConnectedOnline(context))
                {
                    Option3.setEnabled(true);
                }
                Option1.setOnClickListener(view1 ->
                {
                    MessageText =userInput.getText().toString();
                    JSONObject SMSBody = new JSONObject();
                    try {
                        SMSBody.put("x",1);
                        SMSBody.put("Body",MessageText);
                        UpdateChatMessages(ContactID,MessageText);
                        new SMSHandler(context).sendEncryptedSMS(ContactID, SMSBody,EncryptionKey,AES_Salt, IV,CypherType);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        alertDialog.cancel();
                    }
                });
                Option2.setOnClickListener(view12 ->{
                    JSONObject SMSBody = new JSONObject();
                    try {
                        SMSBody.put("x",2);
                        SMSBody.put("t","+"+MyPrefs.getString("MyContact","000000"));
                        SMSBody.put("Body",MessageText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        EncryptionKey = ContactJSON.getString("PubKey");
                        UpdateChatMessages(ContactID,MessageText);
                        new SMSHandler(context).proxyEncryptedSMS(SMSBody,EncryptionKey, AES_Salt,IV);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }finally {
                        alertDialog.cancel();
                    }});
                Option3.setOnClickListener(view13 ->
                {
                    JSONObject SMSBody = new JSONObject();
                    try {
                        SMSBody.put("x",2);
                        SMSBody.put("t",MyPrefs.getString("MyContact","000000"));
                        SMSBody.put("Body",MessageText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    UpdateChatMessages(ContactID,MessageText);
                    try {
                        EncryptionKey = ContactJSON.getString("PubKey");
                        new SMSHandler(context).SendSMSOnline(ContactID, SMSBody,EncryptionKey);
                    }catch (Exception e){e.printStackTrace();}
                    finally {
                        alertDialog.cancel();
                    }});
            }
        });
    }
    @SuppressLint({"Range", "NotifyDataSetChanged"})
    public static void PopulateChatView(Context c) {
        Cursor cur = new DBManager(c.getApplicationContext()).getCIDChats(ContactID);
        responseMessageList.clear();
        while (cur != null && cur.moveToNext())
        {
            if (cur.getInt(cur.getColumnIndex("inorout")) == 0 && cur.getInt(cur.getColumnIndex("Status")) <3)
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")), true, cur.getInt(cur.getColumnIndex("Status")),
                        cur.getString(cur.getColumnIndex("Timestamp"))));
            }
            else if (cur.getInt(cur.getColumnIndex("inorout")) == 1 && cur.getInt(cur.getColumnIndex("Status"))<3 )
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")), false, cur.getInt(cur.getColumnIndex("Status")),
                        cur.getString(cur.getColumnIndex("Timestamp"))));
            }
            else
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")), false, 3,
                        cur.getString(cur.getColumnIndex("Timestamp"))));
            }
        }
        if (cur != null) {
            messageAdapter.notifyDataSetChanged();
            if (ChatActivity.isLastVisible()) {
                if (cur.getCount() > 0) {
                    ChatActivity.recyclerView.smoothScrollToPosition(ChatActivity.messageAdapter.getItemCount() - 1);
                }
            }
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
    public void UpdateChatPosition() {
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new DBManager(ChatActivity.this).UpdateMessageStatus(ContactID, 1);
        HomeActivity.RePopulateChats(context);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void UpdateChatMessages(String Recepient, String SMS)
    {
        new DBManager(context).updateLastMessage(Recepient, SMS, 1, 1);
        new DBManager(context).AddChatMessage(Recepient, 0, SMS, 0);
        UpdateChatPosition();
        userInput.setText("");
        HomeActivity.RePopulateChats(context);
        if (isLastVisible()) {
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
}