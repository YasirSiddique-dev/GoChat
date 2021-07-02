package com.dev175.chat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.adapter.StatusListAdapter;
import com.dev175.chat.databinding.ActivityStatusListBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.UserStatus;

public class StatusListActivity extends AppCompatActivity {

    //For Binding
    private ActivityStatusListBinding binding;
    private UserStatus userStatus;
    private StatusListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatusListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

    }

    private void init() {
        userStatus = (UserStatus) getIntent().getSerializableExtra(Constant.STORIES);

        if (userStatus!=null)
        {
            binding.chatUserName.setText(userStatus.getName());
            Glide.with(StatusListActivity.this)
                    .load(userStatus.getProfileImage())
                    .placeholder(R.drawable.profile_avatar)
                    .into(binding.chatUserProfileImg);

        }
        adapter = new StatusListAdapter(this,userStatus.getStatuses(),userStatus.getUserId());
        binding.statusRv.setLayoutManager(new LinearLayoutManager(this));
        binding.statusRv.setAdapter(adapter);

    }
}