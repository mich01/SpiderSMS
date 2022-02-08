package com.mich01.spidersms.UI;



import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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

public class ChatActivity extends AppCompatActivity {

    EditText userInput;
    public static RecyclerView recyclerView;
    public static List<ResponseMessage> responseMessageList;
    public static MessageAdapter messageAdapter;
    public static String ContactID;
    static Context context;
    private String activityReference;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = this;
        //new MainApplication().onActivityCreated(this,savedInstanceState);
        //new MainApplication().onActivityStarted(this);
        Bundle bundle = getIntent().getExtras();
        ContactID = bundle.getString("ContactID");
        activityReference =  bundle.getString("CalledBy");
        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.chat_conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(messageAdapter);
        this.getSupportActionBar().setTitle(bundle.getString("ContactName"));
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
                    Log.i("sending msg: ",userInput.getText().toString());
                    responseMessageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    if(!isLastVisible())
                    {
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()-1);
                    }
                }
                userInput.setText("");
                return false;
            }
        });
        //PopulateChats(context);
    }

    @SuppressLint("Range")
    public static void PopulateChatView()
    {
        Cursor cur = new DBManager(context.getApplicationContext()).getCIDChats(ContactID);
        int index=0;
        responseMessageList.clear();
        while (cur != null && cur.moveToNext())
        {
            if(cur.getInt(cur.getColumnIndex("inorout"))==0)
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")),true,2));
            }
            else
            {
                responseMessageList.add(new ResponseMessage(cur.getString(cur.getColumnIndex("MessageBody")),false,2));
            }
            //index++;
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
        //PopulateChats(context);
        //new MainApplication().onActivityDestroyed(this);
        //MainApplication.ActivityName =activityReference;
    }
    /*public static void PopulateChats(Context context)
    {
        ChatsList = new ArrayList<LastChat>();
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
                        ChatsList.add(new LastChat(cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("MessageText")),
                                cur.getString(cur.getColumnIndex("Timestamp")),
                                cur.getInt(cur.getColumnIndex("ReadStatus")),
                                R.drawable.contact_icon));
                    }
                    else {
                        Log.i("Record: ", cur.getString(cur.getColumnIndex("ContactName")));
                        ChatsList.add(new LastChat(cur.getString(cur.getColumnIndex("CID")),
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
                    ChatsAdapter UpdatedChats =new ChatsAdapter(context, R.layout.chat_list_item,ChatsList);
                    ChatListView.setAdapter(UpdatedChats);
                }
            }
        });

    }*/

}