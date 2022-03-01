package com.mich01.spidersms.UI;



import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    static ArrayList<LastChat> chatsList;
    public static ListView ChatListView;
    private String activityReference;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = this;
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.contact_action_bar);
        //this.getSupportActionBar().setTitle("Contacts")
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //new MainApplication().onActivityCreated(this,savedInstanceState);
        //new MainApplication().onActivityStarted(this);
        Bundle bundle = getIntent().getExtras();
        ContactID = bundle.getString("ContactID");
        activityReference =  bundle.getString("CalledBy");
        new DBManager(ChatActivity.this).UpdateMessageStatus(ContactID,1);
        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.chat_conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(messageAdapter);
        userInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_send_24, 0);
        getSupportActionBar().setTitle(bundle.getString("ContactName"));
        PopulateChatView();
        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                new DBManager(getApplicationContext()).AddChatMessage(bundle.getString("ContactID"),0,userInput.getText().toString(), true);
                if(actionId == EditorInfo.IME_ACTION_SEND)
                {
                    ResponseMessage message = new ResponseMessage(userInput.getText().toString(), true,1);
                    new SMSHandler(context).sendEncryptedSMS(bundle.getString("ContactID"),userInput.getText().toString());
                    new DBManager(context).updateLastMessage(bundle.getString("ContactID"), userInput.getText().toString(), 1,1);
                    Log.i("Spider MS: ","MESSAGE SENT to: "+bundle.getString("ContactID"));
                    Log.i("sending msg: ",userInput.getText().toString());
                    responseMessageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    if(!isLastVisible())
                    {
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()-1);
                    }
                }
                HomeActivity.PopulateChats(context);
                HomeActivity.adapter.notifyDataSetChanged();
                userInput.setText("");
                return false;
            }

        });
    }

    @SuppressLint("Range")
    public static void PopulateChatView()
    {
        Cursor cur = new DBManager(context.getApplicationContext()).getCIDChats(ContactID);
        responseMessageList.clear();
        while (cur != null && cur.moveToNext())
        {
            Log.i("here here",cur.getString(cur.getColumnIndex("MessageBody")));
            if(cur.getInt(cur.getColumnIndex("inorout"))==0)
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")),true,2));
            }
            else
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")),false,2));
            }
            responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")),false,2));
        }
        if(cur!=null)
        {
            ChatActivity.messageAdapter.notifyDataSetChanged();
            if(!ChatActivity.isLastVisible())
            {
                if(cur.getCount()>0) {
                    ChatActivity.recyclerView.smoothScrollToPosition(ChatActivity.messageAdapter.getItemCount() - 1);
                }
            }
        }

    }

    public static boolean isLastVisible()
    {
        LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int positionOfLastMessage = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = recyclerView.getAdapter().getItemCount();
        return (positionOfLastMessage >=numItems);
    }

    public void UpdateChatPosition()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ChatActivity.messageAdapter.notifyDataSetChanged();
                if(!ChatActivity.isLastVisible())
                {
                    ChatActivity.recyclerView.smoothScrollToPosition(ChatActivity.messageAdapter.getItemCount()-1);
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HomeActivity.PopulateChats(context);
        HomeActivity.adapter.notifyDataSetChanged();
    }
    public void PopulateChats(Context context)
    {
        chatsList = new ArrayList<LastChat>();
        Handler h = new Handler(getMainLooper());
        h.post(new Runnable()
        {
            @SuppressLint("Range")
            @Override
            public void run()
            {
                int index=0;
                Cursor cur = new DBManager(context).getLastChatList();
                while (cur != null && cur.moveToNext())
                {
                    if(cur.getString(cur.getColumnIndex("ContactName"))==null)
                    {
                        Log.i("Record: ", cur.getString(cur.getColumnIndex("CID")));
                        chatsList.add(new LastChat(cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("MessageText")),
                                cur.getString(cur.getColumnIndex("Timestamp")),
                                cur.getInt(cur.getColumnIndex("ReadStatus")),
                                R.drawable.contact_icon));
                    }
                    else {
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
                synchronized(this)
                {
                    ChatsAdapter UpdatedChats =new ChatsAdapter(context, R.layout.chat_list_item,chatsList);
                    //ChatListView.setAdapter(UpdatedChats);
                }
            }
        });

    }

}