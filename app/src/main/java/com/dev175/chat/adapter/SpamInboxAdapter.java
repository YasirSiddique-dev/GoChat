package com.dev175.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.databinding.ItemSpamInboxBinding;
import com.dev175.chat.model.SpamInbox;
import com.dev175.chat.myInterface.IOnItemClickListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SpamInboxAdapter extends RecyclerView.Adapter<SpamInboxAdapter.SpamInboxViewHolder> {

    private Context context;
    private ArrayList<SpamInbox> inboxes;
    private IOnItemClickListener clickListener;

    public SpamInboxAdapter(Context context, IOnItemClickListener itemClickListener,ArrayList<SpamInbox> inboxes) {
        this.context = context;
        this.clickListener = itemClickListener;
        this.inboxes = inboxes;
    }

    @NonNull
    @Override
    public SpamInboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spam_inbox,parent,false);
        return new SpamInboxViewHolder(view,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SpamInboxViewHolder holder, int position) {
        SpamInbox inbox = inboxes.get(position);

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

    }

    @Override
    public int getItemCount() {
        return inboxes.size();
    }

    public class SpamInboxViewHolder extends RecyclerView.ViewHolder {
        private ItemSpamInboxBinding binding;
        private IOnItemClickListener itemClickListener;

        public SpamInboxViewHolder(@NonNull View itemView,IOnItemClickListener clickListener) {
            super(itemView);
            binding = ItemSpamInboxBinding.bind(itemView);
            this.itemClickListener = clickListener;
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}


