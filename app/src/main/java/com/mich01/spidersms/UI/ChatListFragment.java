package com.mich01.spidersms.UI;

import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Adapters.ChatsAdapter;
import com.mich01.spidersms.Adapters.LastChat;
import com.mich01.spidersms.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static ListView ChatListView;
    public static ChatsAdapter adapter;
    static Activity myActivity;
    View view;
    private static ArrayList<LastChat> ChatsList;

    public ChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatListFragment newInstance(String param1, String param2) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity =getActivity();
        ChatsList = new ArrayList<LastChat>();
        view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        getActivity().runOnUiThread(new Runnable()
        {
            @SuppressLint("Range")
            @Override
            public void run() {
                int index=0;
                Cursor cur = new DBManager(getContext()).getLastChatList();
                Log.i("Record Number: ", String.valueOf(cur.getCount()));

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
            }
        });
        Log.i("Chat Numbers:",String.valueOf(ChatsList.size()));
        adapter = new ChatsAdapter(getActivity(),R.layout.chat_list_item,ChatsList);
        ChatListView = view.findViewById(R.id.chats_list);
        ChatListView.setAdapter(adapter);
        return view;
    }
    public static void PopulateChats(Context context)
    {
        ChatsList = new ArrayList<LastChat>();
        Handler h = new Handler(getMainLooper());
        h.post(new Runnable()
        {
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

    }


}