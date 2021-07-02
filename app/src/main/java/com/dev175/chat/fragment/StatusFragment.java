package com.dev175.chat.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dev175.chat.adapter.StatusAdapter;
import com.dev175.chat.databinding.FragmentStatusBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Status;
import com.dev175.chat.model.User;
import com.dev175.chat.model.UserStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class StatusFragment extends Fragment {
    StatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;
    FirebaseDatabase database;

    FragmentStatusBinding binding;
    public static StatusFragment getInstance()
    {
        StatusFragment statusFragment=new StatusFragment();
        return statusFragment;
    }


    public StatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= FragmentStatusBinding.inflate(inflater, container, false);
        View view=binding.getRoot();


        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);
        database = FirebaseDatabase.getInstance();

        userStatuses=new ArrayList<>();
        statusAdapter=new StatusAdapter(getContext(),userStatuses);

        binding.statusList.setAdapter(statusAdapter);

        binding.addStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 75);
            }
        });

        database.getReference().child(Constant.STORIES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot : snapshot.getChildren())
                    {
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        status.setUserId(storySnapshot.child("userId").getValue(String.class));
                        ArrayList<Status> statuses = new ArrayList<>();

                        if (storySnapshot.child("statuses").exists())
                        {
                            for(DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren())
                            {
                                Status sampleStatus = statusSnapshot.getValue(Status.class);
                                statuses.add(sampleStatus);
                            }

                            status.setStatuses(statuses);
                            userStatuses.add(status);
                        }

                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}