package com.dev175.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.adapter.SpamMessageAdapter;
import com.dev175.chat.databinding.ActivitySpamChatBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Message;
import com.dev175.chat.model.SpamInbox;
import com.dev175.chat.model.User;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Calendar;

public class SpamChatActivity extends AppCompatActivity implements IOnItemClickListener {

    private static final String TAG = "SpamChatActivity";

    private ActivitySpamChatBinding binding;
    private User receiver;
    private SpamMessageAdapter adapter;
    private ArrayList<Message> messages;
    private String senderRoom,receiverRoom;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivitySpamChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        //click listeners
        binding.chatBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = new String[]{"Clear Spam Chat","Cancel"};

                new MaterialAlertDialogBuilder(SpamChatActivity.this)
                        .setTitle("Select from below")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which)
                                {
                                    case 0:
                                    {
                                        clearSpamChat(messages);
                                        break;
                                    }
                                    case 1:
                                    {
                                        dialog.dismiss();
                                        break;
                                    }
                                }
                            }
                        }).show();
            }
        });
    }

    private void clearSpamChat(ArrayList<Message> messages) {
        DatabaseReference messagesDb = database.getReference().child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES);

        for (Message message : messages)
        {
            messagesDb.child(message.getMessageId())
                    .removeValue();
        }

        messages.clear();
        adapter.notifyDataSetChanged();
    }


    private void init() {

        receiver = new User();
      if (getIntent().getSerializableExtra(Constant.INBOX)!=null){
            SpamInbox inbox = (SpamInbox) getIntent().getSerializableExtra(Constant.INBOX);
            FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(inbox.getReceiverId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            receiver = snapshot.getValue(User.class);
                            setData();
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
        }

    }
    private void setData()
    {
        //Set Name
        binding.chatUserName.setText(receiver.getFullName());

        //Set Profile Image
        Glide.with(this)
                .load(receiver.getProfileImg())
                .placeholder(R.drawable.profile_avatar)
                .into(binding.chatProfileImg);

        senderRoom = FirebaseAuth.getInstance().getUid()+"_"+receiver.getUid();
        receiverRoom = receiver.getUid()+"_"+FirebaseAuth.getInstance().getUid();

        database = FirebaseDatabase.getInstance();

        messages = new ArrayList<>();
        adapter = new SpamMessageAdapter(this,this,messages,senderRoom,receiverRoom,receiver);
        binding.messagesRv.setLayoutManager(new LinearLayoutManager(this));
        binding.messagesRv.setAdapter(adapter);


        //Set Status of Receiver
        database.getReference()
                .child(Constant.ACTIVE)
                .child(receiver.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            String status = snapshot.getValue(String.class);
                            if (!status.isEmpty())
                            {
                                if (status.equals("Online"))
                                {
                                    binding.chatUserStatus.setText(status);
                                }
                                else {
                                    binding.chatUserStatus.setText("Last seen at "+status);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        getMessages();

        binding.progressCircular.setVisibility(View.GONE);
    }

    private void getMessages()
    {
        database.getReference()
                .child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {


                        Message message = snapshot.getValue(Message.class);
                        if (message.isSpam())
                        {
                            message.setMessageId(snapshot.getKey());
                            messages.add(message);
                            binding.messagesRv.scrollToPosition(messages.size()-1);
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        binding.progressCircular.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constant.ACTIVE).child(uid).setValue("Online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constant.ACTIVE).child(uid).setValue(mydate);
    }

    @Override
    public void onItemClick(int position) {
        Message message = messages.get(position);

        String [] items = null;
        if (message.getSenderId().equals(receiver.getUid()))
        {
            items = new String[]{"Delete For Me","Move to Chat", "Cancel"};

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Select from below")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case 0:
                                {
                                    deleteMessageForMe(message,position);
                                    break;
                                }
                                case 1:
                                {
                                    moveMessageToChat(message,position);
                                    break;
                                }
                                case 2:
                                {
                                    dialog.dismiss();
                                    break;
                                }
                            }
                        }
                    }).show();
        }
        else
        {
            items = new String[]{"Delete For Me", "Delete For Everyone","Move to Chat", "Cancel"};

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Select from below")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case 0:
                                {
                                    deleteMessageForMe(message,position);
                                    break;
                                }
                                case 1:
                                {
                                    deleteMessageForEveryone(message,position);
                                    break;
                                }
                                case 2:
                                {
                                    moveMessageToChat(message,position);
                                }
                                case 3:
                                {
                                    dialog.dismiss();
                                    break;
                                }

                            }
                        }
                    }).show();
        }

    }

    private void moveMessageToChat(Message message, int position) {

        database.getReference()
                .child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES)
                .child(message.getMessageId())
                .child(Constant.SPAM)
                .setValue(false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        messages.remove(message);
                        binding.messagesRv.scrollToPosition(position-1);
                        adapter.notifyDataSetChanged();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(SpamChatActivity.this, "Failed to move message!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void deleteMessageForMe(Message message, int position) {
        DatabaseReference messagesDb = FirebaseDatabase.getInstance().getReference().child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES)
                .child(message.getMessageId());

        messagesDb.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    messages.remove(message);
                    binding.messagesRv.scrollToPosition(position-1);
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    Toast.makeText(SpamChatActivity.this, "Failed to Delete Message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(Message message, int position) {
        DatabaseReference senderRoomMessage = FirebaseDatabase.getInstance().getReference()
                .child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES)
                .child(message.getMessageId());
        senderRoomMessage.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    messages.remove(message);
                    binding.messagesRv.scrollToPosition(position-1);
                    adapter.notifyDataSetChanged();

                    //Delete From Receiver Room
                    DatabaseReference receiverRoomMessage = FirebaseDatabase.getInstance().getReference()
                            .child(Constant.CHATS)
                            .child(receiverRoom)
                            .child(Constant.MESSAGES)
                            .child(message.getMessageId())
                            .child("message");

                    receiverRoomMessage.setValue("Message Deleted !");
                }
                else
                {
                    Toast.makeText(SpamChatActivity.this, "Failed to Delete Message", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}