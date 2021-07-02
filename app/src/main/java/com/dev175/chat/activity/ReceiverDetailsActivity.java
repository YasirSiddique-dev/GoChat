package com.dev175.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.databinding.ActivityReceiverDetailsBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Report;
import com.dev175.chat.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class ReceiverDetailsActivity extends AppCompatActivity {

    //For Binding
    private ActivityReceiverDetailsBinding binding;
    private User receiver;
    private boolean isBlocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiverDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }

    private void init() {
        receiver = (User) getIntent().getSerializableExtra(Constant.USER);

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
                        isBlocked= snapshot.getValue(Boolean.class);
                      if (isBlocked)
                      {
                          binding.blockUser.setVisibility(View.GONE);
                          binding.unBlockUser.setVisibility(View.VISIBLE);

                      }
                      else {
                          binding.unBlockUser.setVisibility(View.GONE);
                          binding.blockUser.setVisibility(View.VISIBLE);
                      }
                  }
                  else {
                      binding.blockUser.setVisibility(View.VISIBLE);
                  }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
        if (receiver!=null)
        {
            binding.chatUserName.setText(receiver.getFullName());
            Glide.with(ReceiverDetailsActivity.this)
                    .load(receiver.getProfileImg())
                    .placeholder(R.drawable.profile_avatar)
                    .into(binding.profilePicture);
            binding.userEmail.setText(receiver.getEmail());
            binding.userPhone.setText(receiver.getPhone());
            binding.userAboutMe.setText(receiver.getAboutMe());
        }

        binding.chatBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSupportNavigateUp();
            }
        });


        binding.blockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockUser();
            }
        });

        binding.unBlockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unBlockUser();
            }
        });
        binding.reportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportUser();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void unBlockUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        String senderRoom = uid+"_"+receiver.getUid();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Constant.BLOCK);
        database.child(senderRoom).setValue(false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ReceiverDetailsActivity.this, receiver.getFullName()+" is Unblocked", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void blockUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        String senderRoom = uid+"_"+receiver.getUid();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Constant.BLOCK);
        database.child(senderRoom).setValue(true)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ReceiverDetailsActivity.this, receiver.getFullName()+" is Blocked", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void reportUser() {
        String[] items = new String[]
                {"Pretending to be Someone",
                "Fake Account",
                "Posting Inappropriate things",
                "Spreading Spam Content",
                "Cancel"};
        Report report = new Report();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select from below")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                            {
                                report.setReason(items[0]);
                                break;
                            }
                            case 1:
                            {
                                report.setReason(items[1]);
                                break;
                            }
                            case 2:
                            {
                                report.setReason(items[2]);
                                break;
                            }
                            case 3:
                            {
                                report.setReason(items[3]);
                                break;
                            }
                            case 4:
                            {
                                report.setReason("");
                                dialog.dismiss();
                                break;

                            }
                        }
                        if (!report.getReason().equals(""))
                        {
                            report.setReportedTo(receiver.getUid());
                            String uid = FirebaseAuth.getInstance().getUid();
                            report.setReportedBy(uid);
                            DatabaseReference database =FirebaseDatabase.getInstance().getReference();
                            String randomKey = database.push().getKey();
                            report.setId(randomKey);

                            database.child(Constant.REPORTS)
                                    .child(receiver.getUid())
                                    .child(randomKey)
                                    .setValue(report)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(ReceiverDetailsActivity.this, "User has reported successfully!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).show();
    }
}