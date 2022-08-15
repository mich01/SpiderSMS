package com.mich01.spidersms.Adapters;

import static com.mich01.spidersms.Data.StringsConstants.ContactID;
import static com.mich01.spidersms.Data.StringsConstants.CalledBy;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateUtils;
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
import com.mich01.spidersms.UI.TextDrawable.TextDrawable;

import java.util.Calendar;
import java.util.List;

public class ChatsAdapter extends ArrayAdapter<LastChat>
{
    List<LastChat> chatsList;
    TextView lastText;
    Context context;

    public ChatsAdapter(@NonNull Context c, int resource, List<LastChat> chatsList)
    {
        super(c, resource, chatsList);
        this.chatsList = chatsList;
        this.context =c;
    }

    @Nullable
    @Override
    public LastChat getItem(int position) {
        return super.getItem(position);
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView ==null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_item,parent,false);
        }
        TextDrawable drawable = TextDrawable.builder().buildRect(String.valueOf(chatsList.get(position).getContactName().charAt(0)), R.id.last_contact_img);
        ImageView images = convertView.findViewById(R.id.last_contact_img);
        TextView contactID = convertView.findViewById(R.id.last_contact_id);
        TextView timeStamp = convertView.findViewById(R.id.time_stamp);
        lastText = convertView.findViewById(R.id.last_chat_text);
        images.setImageDrawable(drawable);
        contactID.setText(chatsList.get(position).getContactName());
        String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(Long.parseLong(chatsList.get(position).getTimestamp()), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
        timeStamp.setText(niceDateStr);
        if(chatsList.get(position).getContactID().equalsIgnoreCase(chatsList.get(position).getContactName()))
        {
            contactID.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        else
        {
            contactID.setTypeface(null, Typeface.BOLD);
        }
        if(chatsList.get(position).getStatus()==0)
        {
            lastText.setTypeface(null, Typeface.BOLD);
            lastText.setTextColor(Color.BLACK);
            timeStamp.setTextColor(Color.BLACK);
            convertView.setBackgroundResource(R.color.gold);
        }
        else
        {
            lastText.setTypeface(null, Typeface.NORMAL);
            contactID.setTextColor(Color.WHITE);
            lastText.setTextColor(Color.WHITE);
            timeStamp.setTextColor(Color.WHITE);
            convertView.setBackgroundResource(R.color.darkblue);
        }
        lastText.setText(chatsList.get(position).getLastMessage());

        convertView.setOnClickListener(v -> {
            Intent  chatIntent  = new Intent(v.getRootView().getContext(), ChatActivity.class);
             chatIntent .putExtra(ContactID, chatsList.get(position).getContactID());
             chatIntent .putExtra(ContactName, chatsList.get(position).getContactName());
             chatIntent .putExtra(CalledBy , HomeActivity.class.getSimpleName());
             chatIntent .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity( chatIntent );
        });
        convertView.setOnLongClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(view.getRootView().getContext());
            alert.setTitle(R.string.do_you_want_to_delete);
            alert.setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                new DBManager(getContext()).DeleteAllChats(chatsList.get(position).getContactID());
                HomeActivity.rePopulateChats(view.getRootView().getContext());
                HomeActivity.adapter.notifyDataSetChanged();
            });

            alert.setNegativeButton(R.string.cancel,
                    (dialog, whichButton) -> dialog.cancel());
                alert.show();
            return false;
        });
        return convertView;
    }
}
