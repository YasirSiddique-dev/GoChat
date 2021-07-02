package com.dev175.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.dev175.admin.R;


import com.dev175.admin.databinding.ItemReportBinding;
import com.dev175.admin.model.ReportDetails;
import com.dev175.admin.model.User;
import com.dev175.admin.myInterface.IOnItemClickListener;

import java.util.ArrayList;


public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportsViewHolder> {

    private Context context;
    private ArrayList<ReportDetails> reportDetailsList;
    private IOnItemClickListener clickListener;

    public ReportsAdapter(Context context, IOnItemClickListener itemClickListener) {
        this.context = context;
        this.clickListener = itemClickListener;

    }
    public void setReportDetailsList(ArrayList<ReportDetails> reportDetailsList)
    {
        this.reportDetailsList = reportDetailsList;
    }
    @NonNull
    @Override
    public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report,parent,false);
        return new ReportsViewHolder(view,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportsViewHolder holder, int position) {
        ReportDetails reportDetails = reportDetailsList.get(position);
        User user = reportDetails.getReportedTo();

        //Profile
        Glide.with(context)
                .load(user.getProfileImg())
                .placeholder(R.drawable.ic_user)
                .into(holder.binding.userImage);

        //Name
        holder.binding.userName.setText(user.getFullName());

        //Reason
        holder.binding.reportReason.setText(reportDetails.getReport().getReason());


    }

    @Override
    public int getItemCount() {
        return reportDetailsList.size();
    }

    public class ReportsViewHolder extends RecyclerView.ViewHolder {
        private ItemReportBinding binding;
        private IOnItemClickListener itemClickListener;

        public ReportsViewHolder(@NonNull View itemView,IOnItemClickListener clickListener) {
            super(itemView);
            binding = ItemReportBinding.bind(itemView);
            this.itemClickListener = clickListener;
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}


