package com.dev175.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.activity.ChatActivity;
import com.dev175.chat.databinding.ItemInboxBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Inbox;
import com.dev175.chat.model.User;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.dev175.chat.myInterface.IOnItemLongClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {
    private Context context;
    private ArrayList<Inbox> inboxes;
    private IOnItemClickListener clickListener;
    private IOnItemLongClickListener longClickListener;

    public InboxAdapter(Context context, IOnItemClickListener itemClickListener, IOnItemLongClickListener itemLongClick, ArrayList<Inbox> inboxes) {
        this.context = context;
        this.clickListener = itemClickListener;
        this.longClickListener = itemLongClick;
        this.inboxes = inboxes;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inbox,parent,false);
        return new InboxViewHolder(view,clickListener,longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        Inbox inbox = inboxes.get(position);

        //Name
        holder.binding.userName.setText(inbox.getReceiverName());

        //Profile
        Glide.with(context)
                .load(inbox.getReceiverProfile())
                .placeholder(R.drawable.profile_avatar)
                .into(holder.binding.userImg);


        //Last Message Time
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        holder.binding.lastMessageTime.setText(dateFormat.format(new Date(inbox.getLastMessageTime())));

        //Last Message
        if (!inbox.getLastMessage().equals(""))
        {
            holder.binding.lastMessage.setText(inbox.getLastMessage());
        }
        else {
            holder.binding.lastMessage.setText("Tap to chat");
        }


    }

    @Override
    public int getItemCount() {
        return inboxes.size();
    }

    public class InboxViewHolder extends RecyclerView.ViewHolder {
        private ItemInboxBinding binding;
        private IOnItemClickListener itemClickListener;
        private IOnItemLongClickListener longClickListener;

        public InboxViewHolder(@NonNull View itemView,IOnItemClickListener clickListener,IOnItemLongClickListener longClickListener) {
            super(itemView);
            binding = ItemInboxBinding.bind(itemView);
            this.itemClickListener = clickListener;
            this.longClickListener = longClickListener;

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(getAdapterPosition());
                }
            });

            this.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener.onItemLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }
}

