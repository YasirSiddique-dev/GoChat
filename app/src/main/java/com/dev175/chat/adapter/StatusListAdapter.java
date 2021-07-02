package com.dev175.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.activity.HomeActivity;
import com.dev175.chat.activity.StatusListActivity;
import com.dev175.chat.databinding.ItemStatusBinding;
import com.dev175.chat.databinding.ItemStatusSingleBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Status;
import com.dev175.chat.model.UserStatus;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusListAdapter extends RecyclerView.Adapter<StatusListAdapter.StatusViewHolder> {

    Context context;
    ArrayList<Status> userStatuses;
    private String userId;

    public StatusListAdapter(Context context, ArrayList<Status> userStatuses,String uid) {
        this.context = context;
        this.userStatuses = userStatuses;
        this.userId = uid;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status_single, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {

        Status userStatus = userStatuses.get(position);

        Glide.with(context).load(userStatus.getImageUrl()).into(holder.binding.image);

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        String dateStr = sdf.format(userStatus.getTimeStamp());

        holder.binding.timestamp.setText(dateStr);

        holder.binding.statusItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> myStories = new ArrayList<>();
                myStories.add(new MyStory(userStatus.getImageUrl()));


                new StoryView.Builder(((StatusListActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                        .setSubtitleText("") // Default is Hidden
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before calling show method
                        .show();
            }
        });

        holder.binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child(Constant.STORIES)
                        .child(userId).child("statuses").child(userStatus.getStatusId())
                        .removeValue();
                //Remove from storage

                FirebaseStorage.getInstance().getReferenceFromUrl(userStatus.getImageUrl())
                        .delete();

                userStatuses.remove(userStatus);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {

        ItemStatusSingleBinding binding;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStatusSingleBinding.bind(itemView);
        }
    }
}