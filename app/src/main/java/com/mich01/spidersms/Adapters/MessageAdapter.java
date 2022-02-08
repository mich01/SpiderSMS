package com.mich01.spidersms.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.mich01.spidersms.Backend.ResponseMessage;
import com.mich01.spidersms.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.CustomViewHolder>
{

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        public CustomViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            //textView = itemView.findViewById(R.id.textMessage);
            //imageView =(ImageView) itemView.findViewById(R.id.img_bubble);
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
        return  new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.CustomViewHolder holder, int position)
    {
        if(responseMessageList.get(position).getMessageType()==1 || !responseMessageList.isEmpty()) {
            //holder.textView.setText(responseMessageList.get(position).getTextMessage());
        }
        else
        {
            //holder.textView.setText(responseMessageList.get(position).getTextMessage());
        }
    }

    @Override
    public int getItemCount() {
        return responseMessageList.size();
    }
}

