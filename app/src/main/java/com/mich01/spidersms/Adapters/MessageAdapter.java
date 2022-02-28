package com.mich01.spidersms.Adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
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
        return  new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.CustomViewHolder holder, int pos)
    {
        if(responseMessageList.get(pos).getMessageType()==1)
        {
            holder.textView.setText(responseMessageList.get(pos).getTextMessage());
        }
        else
        {
            holder.textView.setText(responseMessageList.get(pos).getTextMessage());
        }
        holder.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setTitle("Are you sure you want to delete this conversation");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new DBManager(view.getContext()).DeleteMessage(String.valueOf(pos));
                        HomeActivity.PopulateChats(view.getContext());
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

    }

    @Override
    public int getItemCount() {
        return responseMessageList.size();
    }
}

