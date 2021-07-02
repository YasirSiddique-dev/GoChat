package com.dev175.admin.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dev175.admin.R;
import com.dev175.admin.adapter.ReportsAdapter;
import com.dev175.admin.databinding.ActivityReportsListBinding;
import com.dev175.admin.model.Constant;
import com.dev175.admin.model.Report;
import com.dev175.admin.model.ReportDetails;
import com.dev175.admin.model.User;
import com.dev175.admin.myInterface.IOnItemClickListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportsListActivity extends AppCompatActivity implements IOnItemClickListener {

    //For Binding
    private ActivityReportsListBinding binding;

    private ArrayList<ReportDetails> reportsList;
    private ReportsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        getReports();
    }

    private void init()
    {
        getSupportActionBar().setTitle("Reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.reportsRv.setLayoutManager(new LinearLayoutManager(ReportsListActivity.this));

        adapter = new ReportsAdapter(this,this);
        reportsList = new ArrayList<>();
        adapter.setReportDetailsList(reportsList);
        binding.reportsRv.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void getReports() {

        FirebaseDatabase.getInstance().getReference().child(Constant.ROOT_REPORTS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull  DataSnapshot snapshot, @Nullable String previousChildName) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            ReportDetails reportDetails = new ReportDetails();

                            Report report = dataSnapshot.getValue(Report.class);
                            reportDetails.setReport(report);

                            FirebaseDatabase.getInstance().getReference().child(Constant.ROOT_USERS)
                                    .child(report.getReportedTo())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {

                                                User user = userSnapshot.getValue(User.class);
                                                reportDetails.setReportedTo(user);
                                                reportsList.add(reportDetails);
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(ReportsListActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull  DataSnapshot snapshot, @Nullable  String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull  DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull  DataSnapshot snapshot, @Nullable  String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });
    }

    @Override
    public void onItemClick(int position) {

        ReportDetails reportDetails = reportsList.get(position);
        Intent intent = new Intent(ReportsListActivity.this,ReportDetailsActivity.class);
        intent.putExtra(Constant.REPORT,reportDetails);
        startActivity(intent);
    }
}