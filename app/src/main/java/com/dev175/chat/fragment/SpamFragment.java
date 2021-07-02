package com.dev175.chat.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dev175.chat.R;
import com.dev175.chat.activity.ChatActivity;
import com.dev175.chat.activity.SpamChatActivity;
import com.dev175.chat.adapter.InboxAdapter;
import com.dev175.chat.adapter.SpamInboxAdapter;
import com.dev175.chat.databinding.FragmentSpamBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Inbox;
import com.dev175.chat.model.Message;
import com.dev175.chat.model.SpamInbox;
import com.dev175.chat.model.User;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpamFragment extends Fragment implements IOnItemClickListener {

    //For Logs
    private static final String TAG = "SpamFragment";

    //For Binding
    private FragmentSpamBinding binding;
    private SpamInboxAdapter inboxAdapter;
    private ArrayList<SpamInbox> inboxList;
    private DatabaseReference database;

    public static SpamFragment getInstance()
    {
        return new SpamFragment();
    }

    public SpamFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSpamBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        init();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        getInboxList();
    }

    private void getInboxList() {
        inboxList.clear();
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference inboxDb = database.child(Constant.CHATS);
        inboxDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Log.d(TAG, "onDataChange: "+snapshot.getKey());
                String[] keys = snapshot.getKey().split("_");
                if (keys[0].equals(uid))
                {

                    SpamInbox inbox = new SpamInbox();
                    inbox.setReceiverId(keys[1]);
                    inbox.setLastMessageTime(snapshot.child("lastMessageTime").getValue(long.class));

                    boolean isSpamUser = false;

                    //Check Spam Messages
                    for (DataSnapshot ds : snapshot.child("messages").getChildren())
                    {
                        Log.d(TAG, "onChildAdded: "+ds.getValue(Message.class).getMessage());
                        //If Message is Spam
                        if (ds.getValue(Message.class).isSpam())
                        {
                            Log.d(TAG, "onChildAdded: Break "+inbox.getReceiverId());
                            isSpamUser = true;
                            break;
                        }
                    }
                    if (isSpamUser)
                    {
                        database.child(Constant.USERS).child(keys[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot ds) {
                                User user = ds.getValue(User.class);
                                inbox.setReceiverName(user.getFullName());
                                inbox.setReceiverProfile(user.getProfileImg());
                                inboxList.add(inbox);
                                inboxAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildChanged: ");
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                Log.d(TAG, "onChildRemoved: ");
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.d(TAG, "onChildMoved: ");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, "onCancelled: ");
            }
        });

 /*       inboxDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {

                int i=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    inboxList = new ArrayList<>();

                    i++;
                    Log.d(TAG, "onDataChange: "+snapshot.getKey());
                    String[] keys = snapshot.getKey().split("_");
                    if (keys[0].equals(uid))
                    {

                        Inbox inbox = new Inbox();
                        inbox.setReceiverId(keys[1]);
                        inbox.setLastMessage(snapshot.child("lastMessage").getValue(String.class));
                        inbox.setLastMessageTime(snapshot.child("lastMessageTime").getValue(long.class));

                        database.child(Constant.USERS).child(keys[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot ds) {
                                User user = ds.getValue(User.class);
                                inbox.setReceiverName(user.getFullName());
                                inbox.setReceiverProfile(user.getProfileImg());
                                inboxList.add(inbox);

                                inboxAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }

                }


                if (i==dataSnapshot.getChildrenCount())
                {
                    inboxAdapter = new InboxAdapter(getContext(),inboxList);
                    binding.inboxRv.setAdapter(inboxAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });



*/

    }

    private void init() {

        binding.spamInboxRv.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseDatabase.getInstance().getReference();
        inboxList = new ArrayList<>();
        inboxAdapter = new SpamInboxAdapter(getContext(),this,inboxList);
        binding.spamInboxRv.setAdapter(inboxAdapter);
    }

    @Override
    public void onItemClick(int position) {
        if (!inboxList.isEmpty())
        {
            SpamInbox inbox = inboxList.get(position);
            Intent intent = new Intent(getContext(), SpamChatActivity.class);
            intent.putExtra(Constant.INBOX,inbox);
            startActivity(intent);
        }
    }
}