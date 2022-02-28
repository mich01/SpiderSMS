package com.mich01.spidersms.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.LastChat;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.HomeActivity;

import java.util.ArrayList;

public class ChatsAdapter extends ArrayAdapter<LastChat>
{
    static ArrayList<LastChat> chatsList;
    public static TextView LastText;

    public ChatsAdapter(@NonNull Context c, int resource, ArrayList<LastChat> chatsList)
    {
        super(c, resource, chatsList);
       this.chatsList = chatsList;
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
            Log.i("Chat Lists: ","Null----");
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_item,parent,false);
        }
        ImageView images = convertView.findViewById(R.id.last_contact_img);
        TextView contactID = convertView.findViewById(R.id.last_contact_id);
        LastText = convertView.findViewById(R.id.last_chat_text);
        images.setImageResource(chatsList.get(position).getProfilePicture());
        contactID.setText(chatsList.get(position).getContactName());
        if(chatsList.get(position).getContactID().equalsIgnoreCase(chatsList.get(position).getContactName()))
        {
            Log.i("Contact: ", String.valueOf(chatsList.get(position).getContactID()+" -- "+chatsList.get(position).getContactName()));
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
        }
        else
        {
            LastText.setTypeface(null, Typeface.NORMAL);
            LastText.setTextColor(Color.GRAY);
        }
        LastText.setText(chatsList.get(position).getLastMessage());

        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent ChatIntent = new Intent(getContext(), ChatActivity.class);
                ChatIntent.putExtra("ContactID", chatsList.get(position).getContactID());
                ChatIntent.putExtra("ContactName", chatsList.get(position).getContactName());
                ChatIntent.putExtra("CalledBy", HomeActivity.class.getSimpleName());
                ChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(ChatIntent);
            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Are you sure you want to delete this conversation");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new DBManager(getContext()).DeleteAllChats(chatsList.get(position).getContactID());
                        HomeActivity.PopulateChats(getContext());
                        HomeActivity.adapter.notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                alert.show();
                return false;
            }
        });
        return convertView;
    }
    public static void updateText(int position)
    {
        LastText.setText(chatsList.get(position).getLastMessage());
    }

}
