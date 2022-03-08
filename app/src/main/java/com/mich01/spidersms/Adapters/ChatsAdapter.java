package com.mich01.spidersms.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.LastChat;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.HomeActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatsAdapter extends ArrayAdapter<LastChat>
{
    static ArrayList<LastChat> chatsList;
    @SuppressLint("StaticFieldLeak")
    static TextView LastText;

    public ChatsAdapter(@NonNull Context c, int resource, ArrayList<LastChat> chatsList)
    {
        super(c, resource, chatsList);
       ChatsAdapter.chatsList = chatsList;
       //Log.i("Chat Lists: ","Construct---- and count: "+chatsList.size());
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public LastChat getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(@Nullable LastChat item) {
        return super.getPosition(item);
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.i("Chat Lists: ","Called here---- "+position);
        if(convertView ==null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_item,parent,false);
        }
        TextDrawable drawable = TextDrawable.builder().buildRect(String.valueOf(chatsList.get(position).getContactName().charAt(0)), R.id.last_contact_img);
        ImageView images = convertView.findViewById(R.id.last_contact_img);
        TextView contactID = convertView.findViewById(R.id.last_contact_id);
        TextView TimeStamp = convertView.findViewById(R.id.time_stamp);
        LastText = convertView.findViewById(R.id.last_chat_text);
        images.setImageDrawable(drawable);
        contactID.setText(chatsList.get(position).getContactName());
        String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(Long.parseLong(chatsList.get(position).getTimestamp()), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
        TimeStamp.setText(niceDateStr);
        if(chatsList.get(position).getContactID().equalsIgnoreCase(chatsList.get(position).getContactName()))
        {
            Log.i("Contact: ", chatsList.get(position).getContactID() + " -- " + chatsList.get(position).getContactName());
            contactID.setTypeface(null, Typeface.BOLD_ITALIC);
            //contactID.setTextColor(Color.GRAY);
        }
        else
        {
            contactID.setTypeface(null, Typeface.BOLD);
        }
        if(chatsList.get(position).getStatus()==0)
        {
            LastText.setTypeface(null, Typeface.BOLD);
            LastText.setTextColor(Color.BLACK);
            TimeStamp.setTextColor(Color.BLACK);
            convertView.setBackgroundResource(R.color.gold);
        }
        else
        {
            LastText.setTypeface(null, Typeface.NORMAL);
            contactID.setTextColor(Color.WHITE);
            LastText.setTextColor(Color.WHITE);
            TimeStamp.setTextColor(Color.WHITE);
            convertView.setBackgroundResource(R.color.darkblue);
        }
        LastText.setText(chatsList.get(position).getLastMessage());

        convertView.setOnClickListener(v -> {
            Intent ChatIntent = new Intent(getContext(), ChatActivity.class);
            ChatIntent.putExtra("ContactID", chatsList.get(position).getContactID());
            ChatIntent.putExtra("ContactName", chatsList.get(position).getContactName());
            ChatIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
            ChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(ChatIntent);
        });
        convertView.setOnLongClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Are you sure you want to delete this conversation");
            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                new DBManager(getContext()).DeleteAllChats(chatsList.get(position).getContactID());
                HomeActivity.PopulateChats(getContext());
                HomeActivity.adapter.notifyDataSetChanged();
            });

            alert.setNegativeButton("Cancel",
                    (dialog, whichButton) -> {
                    });
            alert.show();
            return false;
        });
        return convertView;
    }
    public static void updateText(int position)
    {
        LastText.setText(chatsList.get(position).getLastMessage());
    }

}
