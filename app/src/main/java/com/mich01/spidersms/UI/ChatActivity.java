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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mich01.spidersms.Adapters.MessageAdapter;
import com.mich01.spidersms.Backend.ResponseMessage;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = this;
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        ContactID = bundle.getString("ContactID");
        new DBManager(ChatActivity.this).UpdateMessageStatus(ContactID, 1);
        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.chat_conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);
        sendButton = findViewById(R.id.sendButton);
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
                MyPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                Option1.setText(SendTo);
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
                if(MyPrefs.getString("ProxyNumber", "---").equals("---"))
                {
                    Option2.setEnabled(false);
                }
                if(MyPrefs.getString("SeverURL", "---").equals("---"))
                {
                    Option3.setEnabled(false);
                }
                Option1.setOnClickListener(view1 ->
                {
                    MessageText =userInput.getText().toString();
                    ResponseMessage message = new ResponseMessage(userInput.getText().toString(), true, 1);
                    new SMSHandler(context).sendEncryptedSMS(ContactID, MessageText);
                    responseMessageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    UpdateChatPosition();
                    userInput.setText("");
                    HomeActivity.RePopulateChats(context);
                    if (isLastVisible()) {
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                    alertDialog.cancel();
                });
                Option2.setOnClickListener(view12 ->{
                    userInput.setText("");
                    new SMSHandler(context).proxyEncryptedSMS(ContactID, MessageText);
                        alertDialog.cancel();});
                Option3.setOnClickListener(view13 -> {
                    userInput.setText("");
                    try {
                        new SMSHandler(context).SendSMSOnline(ContactID, MessageText);
                    }catch (Exception e){e.printStackTrace();}
                    alertDialog.cancel();});
            }
        });
    }

    @SuppressLint({"Range", "NotifyDataSetChanged"})
    public static void PopulateChatView(Context c) {
        Cursor cur = new DBManager(c.getApplicationContext()).getCIDChats(ContactID);
        responseMessageList.clear();
        while (cur != null && cur.moveToNext())
        {
            if (cur.getInt(cur.getColumnIndex("inorout")) == 0)
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")), true, 2));
            } else
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")), false, 2));
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

}