package com.dev175.chat.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.databinding.ItemReceiveBinding;
import com.dev175.chat.databinding.ItemSendBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Message;
import com.dev175.chat.model.User;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageAdapter extends RecyclerView.Adapter{

    private Context context;
    private ArrayList<Message> messages;
    private int ITEM_SENT = 1;
    private int ITEM_RECEIVE = 2;
    private String senderRoom;
    private String receiverRoom;
    private User receiver;
    private IOnItemClickListener clickListener;

    public MessageAdapter(Context context, IOnItemClickListener listener, ArrayList<Message> messages, String senderRoom, String receiverRoom, User receiver) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        this.receiver = receiver;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==ITEM_SENT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
            return new SendViewHolder(view,clickListener);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive,parent,false);
            return new ReceiveViewHolder(view,clickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        //Setting Reactions
        int[] reactions = new int[] { R.drawable.ic_fb_like, R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh, R.drawable.ic_fb_wow, R.drawable.ic_fb_sad, R.drawable.ic_fb_angry};

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (pos!=-1)
            {
                if (holder.getClass() ==  SendViewHolder.class){
                    SendViewHolder viewHolder = (SendViewHolder) holder;
                    viewHolder.binding.feeling.setImageResource(reactions[pos]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                    Log.d("TAG", "onBindViewHolder: "+pos);
                }
                else
                {
                    ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
                    viewHolder.binding.feeling.setImageResource(reactions[pos]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                    Log.d("TAG2", "onBindViewHolder: "+pos);
                }

                message.setFeeling(pos);
                FirebaseDatabase.getInstance().getReference().child(Constant.CHATS)
                        .child(senderRoom).child(Constant.MESSAGES)
                        .child(message.getMessageId()).setValue(message);
                FirebaseDatabase.getInstance().getReference().child(Constant.CHATS)
                        .child(receiverRoom).child(Constant.MESSAGES)
                        .child(message.getMessageId()).setValue(message);

            }
            return true; // true is closing popup, false is requesting a new selection

        });

        //Set Data
        if (holder.getClass() == SendViewHolder.class){

            SendViewHolder viewHolder = (SendViewHolder) holder;
            if (message.getType().equals(Constant.TEXT))
            {
                viewHolder.binding.sendMessage.setText(message.getMessage());
                viewHolder.binding.messageImgCv.setVisibility(View.GONE);
                viewHolder.binding.sendMessage.setVisibility(View.VISIBLE);
            }
            else if (message.getType().equals(Constant.IMAGE)){
                viewHolder.binding.sendMessage.setVisibility(View.GONE);
                viewHolder.binding.messageImgCv.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(viewHolder.binding.messageImg);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            viewHolder.binding.messageTime.setText(dateFormat.format(new Date(message.getTimestamp())));


            if (message.getFeeling()>=0)
            {

                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            //Reaction on Text Message
            viewHolder.binding.sendMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return true;
                }
            });


            //Reaction on Image Message
            viewHolder.binding.messageImg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }
        else
        {
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
            if (message.getType().equals(Constant.TEXT))
            {
                viewHolder.binding.messageImgCv.setVisibility(View.GONE);
                viewHolder.binding.receiveMessage.setVisibility(View.VISIBLE);
                viewHolder.binding.receiveMessage.setText(message.getMessage());
            }
            else if (message.getType().equals(Constant.IMAGE)){
                viewHolder.binding.receiveMessage.setVisibility(View.GONE);
                viewHolder.binding.messageImgCv.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(viewHolder.binding.messageImg);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            viewHolder.binding.messageTime.setText(dateFormat.format(new Date(message.getTimestamp())));
            Glide.with(context)
                    .load(receiver.getProfileImg())
                    .placeholder(R.drawable.profile_avatar)
                    .into(viewHolder.binding.receiverProfileImg);

            if (message.getFeeling()>=0)
            {
//                message.setFeeling(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }


            //Reaction on Text Message
            viewHolder.binding.receiveMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            //Reaction on Image Message
            viewHolder.binding.messageImg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId()))
            {
                return ITEM_SENT;
            }
        else {
            return ITEM_RECEIVE;
        }
    }

    public class SendViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private ItemSendBinding binding;
        private IOnItemClickListener itemClickListener;

        public SendViewHolder(@NonNull View itemView, IOnItemClickListener listener)
        {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
            this.itemClickListener = listener;
            itemView.setOnLongClickListener(this);

        }

        @Override
        public boolean onLongClick(View v)
        {
            itemClickListener.onItemClick(getAdapterPosition());
            return true;
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
    {

        private ItemReceiveBinding binding;
        private IOnItemClickListener itemClickListener;

        public ReceiveViewHolder(@NonNull View itemView,IOnItemClickListener clickListener)
        {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
            this.itemClickListener = clickListener;
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v)
        {
            itemClickListener.onItemClick(getAdapterPosition());
            return true;
        }
    }
}
