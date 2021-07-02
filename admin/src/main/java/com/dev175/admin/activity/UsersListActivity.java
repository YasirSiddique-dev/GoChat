package com.dev175.admin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dev175.admin.R;
import com.dev175.admin.adapter.UsersAdapter;
import com.dev175.admin.databinding.ActivityUsersListBinding;
import com.dev175.admin.model.Constant;
import com.dev175.admin.model.User;
import com.dev175.admin.myInterface.IOnItemClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersListActivity extends AppCompatActivity implements IOnItemClickListener {

    //For Binding
    private ActivityUsersListBinding binding;
    private UsersAdapter adapter;
    private ArrayList<User> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        getUsers();
    }

    private void getUsers() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child(Constant.ROOT_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        usersList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            User user = dataSnapshot.getValue(User.class);
                            usersList.add(user);
                        }

                        if (usersList.size()==snapshot.getChildrenCount())
                        {
                            binding.progressCircular.setVisibility(View.GONE);
                            adapter.setUsers(usersList);
                            binding.usersRv.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {
                        binding.progressCircular.setVisibility(View.GONE);
                        Toast.makeText(UsersListActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void init()
    {
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new UsersAdapter(this,this);
        binding.usersRv.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();;
        return true;
    }

    @Override
    public void onItemClick(int position) {
        User user = usersList.get(position);
        Intent intent = new Intent(UsersListActivity.this,UserDetailsActivity.class);
        intent.putExtra(Constant.USER,user);
        startActivity(intent);
    }
}