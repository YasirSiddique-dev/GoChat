package com.dev175.chat.fragment;

import android.content.DialogInterface;
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
import com.dev175.chat.adapter.InboxAdapter;
import com.dev175.chat.databinding.FragmentChatsBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Inbox;
import com.dev175.chat.model.User;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.dev175.chat.myInterface.IOnItemLongClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
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

public class ChatsFragment extends Fragment implements IOnItemClickListener , IOnItemLongClickListener {

    //For Logs
    private static final String TAG = "ChatsFragment";

    //For Binding
    private FragmentChatsBinding binding;
    private InboxAdapter inboxAdapter;
    private ArrayList<Inbox> inboxList;
    private DatabaseReference database;

    public static ChatsFragment getInstance()
    {
        ChatsFragment chatsFragment=new ChatsFragment();
        return  chatsFragment;
    }


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       binding = FragmentChatsBinding.inflate(inflater,container,false);
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
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

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
                            if (!inboxList.contains(inbox))
                            {
                                inboxList.add(inbox);
                            }

                            inboxAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
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

        binding.inboxRv.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseDatabase.getInstance().getReference();



        inboxList = new ArrayList<>();
        inboxAdapter = new InboxAdapter(getContext(),this,this,inboxList);
        binding.inboxRv.setAdapter(inboxAdapter);
    }

    @Override
    public void onItemClick(int position) {
        //OnClick Listener
        Inbox inbox = inboxList.get(position);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constant.INBOX,inbox);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {
        Inbox inbox = inboxList.get(position);

        String[] items = new String[]{"Delete Chat","Cancel"};

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Select from below")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                            {
                                deleteInbox(inbox,position);
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

    private void deleteInbox(Inbox inbox, int position) {
        String senderRoom =FirebaseAuth.getInstance().getUid()+"_"+inbox.getReceiverId();
        database.child(Constant.CHATS)
                .child(senderRoom)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            inboxList.remove(inbox);
                            binding.inboxRv.scrollToPosition(position-1);
                            inboxAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getContext(), ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}