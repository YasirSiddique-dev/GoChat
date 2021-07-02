package com.dev175.admin.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.dev175.admin.R;
import com.dev175.admin.databinding.ActivityUserDetailsBinding;
import com.dev175.admin.model.Constant;
import com.dev175.admin.model.User;

public class UserDetailsActivity extends AppCompatActivity {

    private User user;
    private ActivityUserDetailsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }
    private void init()
    {
        user = (User) getIntent().getSerializableExtra(Constant.USER);
        getSupportActionBar().setTitle("User Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Glide.with(UserDetailsActivity.this)
                .load(user.getProfileImg())
                .placeholder(R.drawable.ic_user)
                .into(binding.userImg);

        binding.userName.setText(user.getFullName());
        binding.userEmail.setText(user.getEmail());
        binding.userPhone.setText(user.getPhone());
        binding.userUid.setText(user.getUid());
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}