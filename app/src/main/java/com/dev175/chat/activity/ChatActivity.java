package com.dev175.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.adapter.MessageAdapter;
import com.dev175.chat.databinding.ActivityChatBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Contact;
import com.dev175.chat.model.Data;
import com.dev175.chat.model.Inbox;
import com.dev175.chat.model.Message;
import com.dev175.chat.model.NotificationMessage;
import com.dev175.chat.model.User;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.dev175.chat.myInterface.ServiceAPI;
import com.dev175.chat.util.SpamClassifier;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vanniktech.emoji.EmojiPopup;
import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.support.label.Category;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity implements IOnItemClickListener {


    private static final String TAG = "ChatActivity";
    private static final int IMAGE_REQUEST_CODE = 124;

    private ActivityChatBinding binding;
    private boolean chat_btn_flag;

    private User receiver;

    private MessageAdapter adapter;
    private ArrayList<Message> messages;

    private String senderRoom,receiverRoom;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private ProgressDialog dialog;

    private SpamClassifier spamClassifier;
    private boolean isReceiverSpamEnable;
    private boolean isBlocked;
    private boolean iBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Init
        init();

        //initialize emoji pop-up
        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(findViewById
                (R.id.root_view))
                .build(binding.chatBox);

        //click listeners
        binding.chatBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.chatUserLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receiver!=null)
                {
                    Intent intent = new Intent(ChatActivity.this,ReceiverDetailsActivity.class);
                    intent.putExtra(Constant.USER,receiver);
                    startActivity(intent);
                }

            }
        });
        binding.emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPopup.toggle();
            }
        });

        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chat_btn_flag)
                {
                    //sending Text Msg
                    if (!isBlocked)
                    {
                        sendTextMessage();
                    }
                    else {
                       if (iBlock)
                       {
                           Toast.makeText(ChatActivity.this, "You have blocked "+receiver.getFullName(), Toast.LENGTH_SHORT).show();
                       }
                       else {
                           Toast.makeText(ChatActivity.this, receiver.getFullName()+" has blocked you", Toast.LENGTH_SHORT).show();
                       }
                    }
                }
                else
                {
                    //sending voice
                }
            }
        });

        //text change listener
        binding.chatBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                binding.chatBtn.setIconResource(R.drawable.ic_baseline_send_24);
                chat_btn_flag=true;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (binding.chatBox.getText().toString().isEmpty())
                {
                    binding.chatBtn.setIconResource(R.drawable.ic_baseline_mic_24);
                    chat_btn_flag=false;
                }
            }
            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        binding.chatBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPopup.dismiss();
            }
        });

        //Attachment Click Listener
        binding.attachmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });
    }


    private void selectImageFromGallery() {
        try
        {
            Intent objectIntent = new Intent();
            objectIntent.setType("image/*");
            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent,IMAGE_REQUEST_CODE);
        }
        catch (Exception e){
            Toast.makeText(ChatActivity.this, e.getMessage()+"", Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {

        receiver = new User();
        if (getIntent().getSerializableExtra(Constant.CONTACT)!=null)
        {
            Contact contact = (Contact) getIntent().getSerializableExtra(Constant.CONTACT);
            receiver.setUid(contact.getContactId()); //userId
            receiver.setProfileImg(contact.getProfileImg()); //userImg
            receiver.setPhone(contact.getPhoneNumber()); // phoneNumber
            receiver.setFullName(contact.getName()); // Name
            receiver.setEmail(contact.getEmail()); // Email
            checkIsUserBlocked();
            setData();
            checkReceiverSpamDetection();
        }
        else if (getIntent().getSerializableExtra(Constant.INBOX)!=null)
        {
            Inbox inbox = (Inbox) getIntent().getSerializableExtra(Constant.INBOX);
            FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(inbox.getReceiverId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            receiver = snapshot.getValue(User.class);
                            checkIsUserBlocked();
                            setData();
                            checkReceiverSpamDetection();
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Image...");
        dialog.setCancelable(false);

        spamClassifier = new SpamClassifier(this);
        spamClassifier.load();


    }

    private void checkReceiverSpamDetection() {
        database.getReference().child(Constant.SPAM)
                .child(receiver.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    isReceiverSpamEnable = (boolean) snapshot.getValue();
                }
                else {
                    isReceiverSpamEnable = false;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void checkIsUserBlocked()
    {
        if (receiver!=null)
        {
            String uid = FirebaseAuth.getInstance().getUid();
            String senderRoom = uid+"_"+receiver.getUid();

            DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Constant.BLOCK);
            database.child(senderRoom).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                  if (snapshot.exists())
                  {
                      isBlocked = snapshot.getValue(Boolean.class);
                      iBlock = isBlocked;
                  }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }
    private void checkIfReceiverHasBlockedMe()
    {
        String uid = FirebaseAuth.getInstance().getUid();
        String receiverRoom = receiver.getUid()+"_"+uid;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Constant.BLOCK);
        database.child(receiverRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot ds) {
                if (ds.exists())
                {
                    isBlocked = ds.getValue(Boolean.class);
                    iBlock = false;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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
        storage = FirebaseStorage.getInstance();

        messages = new ArrayList<>();
        adapter = new MessageAdapter(this,this,messages,senderRoom,receiverRoom,receiver);
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
    }

    private void getMessages()
    {
        database.getReference()
                .child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable  String previousChildName) {


                        Message message = snapshot.getValue(Message.class);
                        Log.d(TAG, "onChildAdded: "+message.getMessage());
                        if (!message.isSpam())
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
                        Log.d(TAG, "onChildMoved: ");
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void sendTextMessage() {

        String senderUid = FirebaseAuth.getInstance().getUid();
        String message = binding.chatBox.getText().toString();

        Date date = new Date();
        Message newMessage = new Message(message,senderUid,date.getTime());
        newMessage.setType(Constant.TEXT);

        //Check if Receiver has enable Spam Detection or not
        if (isReceiverSpamEnable)
        {

            //Classify Message as Spam or Non Spam
            List<Category> results = spamClassifier.classify(message);
            float score = results.get(1).getScore();
            newMessage.setSpamProbability(score);

            if(score>0.9){
                //Spam
                newMessage.setSpam(true);
            }
            else
            {
                //Ham
                newMessage.setSpam(false);
            }
        }

        binding.chatBox.setText("");


        String randomKey = database.getReference().push().getKey();

        HashMap<String,Object> lastMessageObj = new HashMap<>();
        lastMessageObj.put(Constant.LAST_MESSAGE,newMessage.getMessage());
        lastMessageObj.put(Constant.LAST_MESSAGE_TIME,newMessage.getTimestamp());
        database.getReference().child(Constant.CHATS).child(senderRoom).updateChildren(lastMessageObj);
        database.getReference().child(Constant.CHATS).child(receiverRoom).updateChildren(lastMessageObj);


        database.getReference().child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES)
                .child(randomKey)
                .setValue(newMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        database.getReference().child(Constant.CHATS)
                                .child(receiverRoom)
                                .child(Constant.MESSAGES)
                                .child(randomKey)
                                .setValue(newMessage)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        if (newMessage.isSpam())
                                        {
                                            sendNotification("Spam Detected");
                                        }
                                        else
                                        {
                                            sendNotification(message);
                                        }
                                    }
                                });
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            Uri selectedImage = data.getData();
            Calendar calendar = Calendar.getInstance();
            StorageReference reference = storage.getReference().child(Constant.CHATS).child(calendar.getTimeInMillis()+"");

            dialog.show();

            reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {

                    dialog.dismiss();

                    if (task.isSuccessful())
                    {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String filePath = uri.toString();

                                String senderUid = FirebaseAuth.getInstance().getUid();

                                binding.chatBox.setText("");

                                Date date = new Date();
                                Message newMessage = new Message("",senderUid,date.getTime());
                                newMessage.setImageUrl(filePath);
                                newMessage.setType(Constant.IMAGE);

                                String randomKey = database.getReference().push().getKey();

                                HashMap<String,Object> lastMessageObj = new HashMap<>();
                                lastMessageObj.put(Constant.LAST_MESSAGE,newMessage.getType());
                                lastMessageObj.put(Constant.LAST_MESSAGE_TIME,newMessage.getTimestamp());
                                database.getReference().child(Constant.CHATS).child(senderRoom).updateChildren(lastMessageObj);
                                database.getReference().child(Constant.CHATS).child(receiverRoom).updateChildren(lastMessageObj);


                                database.getReference().child(Constant.CHATS)
                                        .child(senderRoom)
                                        .child(Constant.MESSAGES)
                                        .child(randomKey)
                                        .setValue(newMessage)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                database.getReference().child(Constant.CHATS)
                                                        .child(receiverRoom)
                                                        .child(Constant.MESSAGES)
                                                        .child(randomKey)
                                                        .setValue(newMessage)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                            }
                                                        });
                                            }
                                        });





                            }
                        });
                    }
                }
            });
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constant.ACTIVE).child(uid).setValue("Online");

        if (!isBlocked)
        {
            checkIfReceiverHasBlockedMe();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constant.ACTIVE).child(uid).setValue(mydate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spamClassifier.unload();
    }

    @Override
    public void onItemClick(int position) {
        Message message = messages.get(position);


        String [] items = null;
        if (message.getSenderId().equals(receiver.getUid()))
        {
            items = new String[]{"Delete For Me", "Move to Spam", "Cancel"};

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
                                    moveMessageToSpam(message,position);
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
            items = new String[]{"Delete For Me", "Delete For Everyone", "Move to Spam", "Cancel"};

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
                                    moveMessageToSpam(message,position);
                                    break;
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

    private void moveMessageToSpam(Message message,int position) {

        database.getReference()
                .child(Constant.CHATS)
                .child(senderRoom)
                .child(Constant.MESSAGES)
                .child(message.getMessageId())
                .child(Constant.SPAM)
                .setValue(true)
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
                Toast.makeText(ChatActivity.this, "Failed to delete message!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ChatActivity.this, "Failed to Delete Message", Toast.LENGTH_SHORT).show();
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
                            .child(message.getMessageId());
                    receiverRoomMessage.removeValue();
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Failed to Delete Message", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }



    private void sendNotification(String msg) {
        Data data = new Data();
        data.setTitle("Message from "+Constant.currentUser.getFullName());
        data.setBody(msg);
        data.setClick_action("HomeActivity");

        NotificationMessage notificationTask = new NotificationMessage(receiver.getToken(), data);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")//url of FCM message server
                .addConverterFactory(GsonConverterFactory.create())//use for convert JSON file into object
                .build();

        ServiceAPI api = retrofit.create(ServiceAPI.class);

        Call<NotificationMessage> call = api.sendMessage("key="+Constant.SERVER_KEY, notificationTask);

        call.enqueue(new Callback<NotificationMessage>() {
            @Override
            public void onResponse(Call<NotificationMessage> call, Response<NotificationMessage> response) {

            }

            @Override
            public void onFailure(Call<NotificationMessage> call, Throwable t) {
                Log.e("TAG", t.getMessage());
            }
        });

    }


}