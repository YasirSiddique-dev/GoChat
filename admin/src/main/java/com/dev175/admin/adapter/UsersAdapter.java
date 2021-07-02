package com.dev175.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.dev175.admin.R;
import com.dev175.admin.databinding.ItemUserBinding;
import com.dev175.admin.model.User;
import com.dev175.admin.myInterface.IOnItemClickListener;
import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private Context context;
    private ArrayList<User> users;
    private IOnItemClickListener clickListener;

    public UsersAdapter(Context context, IOnItemClickListener itemClickListener) {
        this.context = context;
        this.clickListener = itemClickListener;

    }
    public void setUsers(ArrayList<User> usersList)
    {
        this.users = usersList;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user,parent,false);
        return new UserViewHolder(view,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        //Profile
        Glide.with(context)
                .load(user.getProfileImg())
                .placeholder(R.drawable.ic_user)
                .into(holder.binding.userImage);

        //Name
        holder.binding.userName.setText(user.getFullName());

        //Email
        holder.binding.userEmail.setText(user.getEmail());

        //Phone
        holder.binding.userPhone.setText(user.getPhone());

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private ItemUserBinding binding;
        private IOnItemClickListener itemClickListener;

        public UserViewHolder(@NonNull View itemView,IOnItemClickListener clickListener) {
            super(itemView);
            binding = ItemUserBinding.bind(itemView);
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


