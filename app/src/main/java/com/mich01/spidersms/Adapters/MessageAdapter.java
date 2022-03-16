package com.mich01.spidersms.Adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mich01.spidersms.Backend.ResponseMessage;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.HomeActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.CustomViewHolder>
{

    static class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public CustomViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textMessage);
        }
    }
    List<ResponseMessage> responseMessageList;
    public MessageAdapter(List<ResponseMessage> responseMessageList) {
        this.responseMessageList = responseMessageList;
    }

    @Override
    public int getItemViewType(int position)
    {
        if(responseMessageList.get(position).isSent())
        {
            return R.layout.sender_bubble;
        }
        else
        {
            return R.layout.receiver_bubble;
        }
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.CustomViewHolder holder, int pos)
    {
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

    }

    @Override
    public int getItemCount() {
        return responseMessageList.size();
    }
}

