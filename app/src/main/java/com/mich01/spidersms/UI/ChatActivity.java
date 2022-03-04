package com.mich01.spidersms.UI;


import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Adapters.ChatsAdapter;
import com.mich01.spidersms.Adapters.MessageAdapter;
import com.mich01.spidersms.Backend.ResponseMessage;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.LastChat;
import com.mich01.spidersms.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    EditText userInput;
    public static RecyclerView recyclerView;
    public static List<ResponseMessage> responseMessageList;
    public static MessageAdapter messageAdapter;
    public static String ContactID;
    static Context context;
    ImageButton sendButton;
    static ArrayList<LastChat> chatsList;
    public static ListView ChatListView;
    private String activityReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        ContactID = bundle.getString("ContactID");
        activityReference = bundle.getString("CalledBy");
        new DBManager(ChatActivity.this).UpdateMessageStatus(ContactID, 1);
        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.chat_conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);
        sendButton = findViewById(R.id.sendButton);
        getSupportActionBar().setTitle(bundle.getString("ContactName"));
        PopulateChatView();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button Option1, Option2, Option3;
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.send_options_modal, viewGroup, false);
                builder.setView(dialogView);
                String SendTo = new StringBuilder().append(getString(R.string.send_option1)).append(" ").append(bundle.getString("ContactName")).toString();
                Option1 = dialogView.findViewById(R.id.send_option_1);
                Option2 = dialogView.findViewById(R.id.send_option_2);
                Option3 = dialogView.findViewById(R.id.send_option_3);
                Option1.setText(SendTo);
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
                Option1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ResponseMessage message = new ResponseMessage(userInput.getText().toString(), true, 1);
                        new SMSHandler(context).sendEncryptedSMS(bundle.getString("ContactID"), userInput.getText().toString());
                        Log.i("Spider MS: ", "MESSAGE SENT to: " + bundle.getString("ContactID"));
                        Log.i("sending msg: ", userInput.getText().toString());
                        responseMessageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        userInput.setText("");
                        HomeActivity.PopulateChats(context);
                        if (!isLastVisible()) {
                            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                        alertDialog.cancel();
                    }
                });
                Option2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
                Option3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
            }
        });
    }

    @SuppressLint("Range")
    public static void PopulateChatView() {
        Cursor cur = new DBManager(context.getApplicationContext()).getCIDChats(ContactID);
        responseMessageList.clear();
        while (cur != null && cur.moveToNext())
        {
            Log.i("here here", cur.getString(cur.getColumnIndex("MessageBody")));
            if (cur.getInt(cur.getColumnIndex("inorout")) == 0)
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")), true, 2));
            } else
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")), false, 2));
            }
        }
        if (cur != null) {
            ChatActivity.messageAdapter.notifyDataSetChanged();
            if (!ChatActivity.isLastVisible()) {
                if (cur.getCount() > 0) {
                    ChatActivity.recyclerView.smoothScrollToPosition(ChatActivity.messageAdapter.getItemCount() - 1);
                }
            }
        }

    }

    public static boolean isLastVisible() {
        LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int positionOfLastMessage = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = recyclerView.getAdapter().getItemCount();
        return (positionOfLastMessage >= numItems);
    }

    public void UpdateChatPosition() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ChatActivity.messageAdapter.notifyDataSetChanged();
                if (!ChatActivity.isLastVisible()) {
                    ChatActivity.recyclerView.smoothScrollToPosition(ChatActivity.messageAdapter.getItemCount() - 1);
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new DBManager(ChatActivity.this).UpdateMessageStatus(ContactID, 1);        HomeActivity.PopulateChats(context);
        HomeActivity.adapter.notifyDataSetChanged();
    }

    public void PopulateChats(Context context) {
        chatsList = new ArrayList<LastChat>();
        Handler h = new Handler(getMainLooper());
        h.post(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                int index = 0;
                Cursor cur = new DBManager(context).getLastChatList();
                while (cur != null && cur.moveToNext()) {
                    if (cur.getString(cur.getColumnIndex("ContactName")) == null) {
                        Log.i("Record: ", cur.getString(cur.getColumnIndex("CID")));
                        chatsList.add(new LastChat(cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("MessageText")),
                                cur.getString(cur.getColumnIndex("Timestamp")),
                                cur.getInt(cur.getColumnIndex("ReadStatus")),
                                R.drawable.contact_icon));
                    } else {
                        Log.i("Record: ", cur.getString(cur.getColumnIndex("ContactName")));
                        chatsList.add(new LastChat(cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("ContactName")),
                                cur.getString(cur.getColumnIndex("MessageText")),
                                cur.getString(cur.getColumnIndex("Timestamp")),
                                cur.getInt(cur.getColumnIndex("ReadStatus")),
                                R.drawable.contact_icon));
                    }
                    index++;
                }
                synchronized (this) {
                    ChatsAdapter UpdatedChats = new ChatsAdapter(context, R.layout.chat_list_item, chatsList);
                    //ChatListView.setAdapter(UpdatedChats);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}