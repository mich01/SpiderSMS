package com.mich01.spidersms.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.mich01.spidersms.Backend.BackendFunctions;
import com.mich01.spidersms.Backend.ResponseMessage;
import com.mich01.spidersms.Backend.SMSHandler;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.ContactsActivity;
import com.mich01.spidersms.UI.DataQRGenerator;
import com.mich01.spidersms.UI.HomeActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.CustomViewHolder>
{
    Context context;
    static class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView MessageStatus;


        public CustomViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textMessage);
            MessageStatus = itemView.findViewById(R.id.text_status);
        }
    }
    List<ResponseMessage> responseMessageList;
    public MessageAdapter(List<ResponseMessage> responseMessageList, Context context) {
        this.responseMessageList = responseMessageList;
        this.context =context;
    }

    @Override
    public int getItemViewType(int position)
    {
        if(responseMessageList.get(position).isSent() && responseMessageList.get(position).getMessageStatus()<3)
        {
            return R.layout.sender_bubble;
        }
        else if(!responseMessageList.get(position).isSent() && responseMessageList.get(position).getMessageStatus()<3)
        {
            return R.layout.receiver_bubble;
        }
        else
        {
            return R.layout.status_bubble;
        }
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.CustomViewHolder holder, int pos)
    {
        try {
            String Status ="-";
            if(responseMessageList.get(pos).isSent() && responseMessageList.get(pos).getMessageStatus()<1)
            {
                holder.MessageStatus.setTextColor(context.getResources().getColor(R.color.danger));
                Status =" Failed";
            }
            else if(responseMessageList.get(pos).isSent() && responseMessageList.get(pos).getMessageStatus()==1  )
            {
                holder.MessageStatus.setTextColor(context.getResources().getColor(R.color.darkblue));
                Status =" \u2713";
            }
            else if (responseMessageList.get(pos).isSent() && responseMessageList.get(pos).getMessageStatus()==2)
            {
                holder.MessageStatus.setTextColor(context.getResources().getColor(R.color.darkblue));
                Status =" \u2713"+"\u2713";
            }
            if(!responseMessageList.get(pos).isSent())
            {
                Status ="";
            }
            holder.MessageStatus.setText(new BackendFunctions().convertTime(Long.parseLong(responseMessageList.get(pos).getTimeStamp()))+Status);
            holder.textView.setText(responseMessageList.get(pos).getTextMessage());
            holder.textView.setOnLongClickListener(view -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setTitle("Are you sure you want to delete this conversation");
                alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                    new DBManager(view.getContext()).DeleteMessage(String.valueOf(pos));
                    HomeActivity.RePopulateChats(view.getContext());
                    HomeActivity.adapter.notifyDataSetChanged();
                });

                alert.setNegativeButton("Cancel",
                        (dialog, whichButton) -> {
                        });

                alert.show();
                return false;
            });
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return responseMessageList.size();
    }
}

