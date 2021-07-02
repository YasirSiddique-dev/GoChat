package com.dev175.admin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.dev175.admin.R;
import com.dev175.admin.databinding.ActivityReportDetailsBinding;
import com.dev175.admin.model.Constant;
import com.dev175.admin.model.Report;
import com.dev175.admin.model.ReportDetails;
import com.dev175.admin.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class ReportDetailsActivity extends AppCompatActivity {

    //For Binding
    private ActivityReportDetailsBinding binding;
    private ReportDetails reportDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }
    private void init()
    {
        reportDetails = (ReportDetails) getIntent().getSerializableExtra(Constant.REPORT);
        getSupportActionBar().setTitle("Report Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String reportBy = reportDetails.getReport().getReportedBy();
        getReporterUser(reportBy);
    }

    private void getReporterUser(String reportBy) {
        FirebaseDatabase.getInstance().getReference().child(Constant.ROOT_USERS)
                .child(reportBy).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                reportDetails.setReportedBy(user);
                setReportDetails();
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void setReportDetails() {
       Report report = reportDetails.getReport();
       User uReportedTo = reportDetails.getReportedTo();
       User uReportedBy = reportDetails.getReportedBy();

        Glide.with(ReportDetailsActivity.this)
                .load(uReportedBy.getProfileImg())
                .placeholder(R.drawable.ic_user)
                .into(binding.reportByUserImage);

        binding.reportByUserName.setText(uReportedBy.getFullName());
        binding.reportByUserEmail.setText(uReportedBy.getEmail());
        binding.reportByUserPhone.setText(uReportedBy.getPhone());

        Glide.with(ReportDetailsActivity.this)
                .load(uReportedTo.getProfileImg())
                .placeholder(R.drawable.ic_user)
                .into(binding.reportToUserImage);

        binding.reportToUserName.setText(uReportedTo.getFullName());
        binding.reportToUserEmail.setText(uReportedTo.getEmail());
        binding.reportedToUserPhone.setText(uReportedTo.getPhone());

        binding.reportReason.setText(report.getReason());
    }













    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}