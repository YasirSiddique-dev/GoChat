package com.dev175.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.dev175.chat.R;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    private List<Contact> contactList;
    private final Context context;
    private List<Contact> copyList;
    private IOnItemClickListener iOnItemClickListener;

    public ContactAdapter(Context context,IOnItemClickListener clickListener) {
        this.context = context;
        this.iOnItemClickListener = clickListener;
    }

    public void setContactList(ArrayList<Contact> list)
    {
        this.contactList = list;
        this.copyList = new ArrayList<>();
        copyList.addAll(contactList);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_contact,parent,false);
        return new MyViewHolder(view,iOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ImageView userImg = holder.userImg;
        TextView userName = holder.userName;
        TextView userNumber = holder.userNumber;
        TextView userAvailability = holder.userAvailability;

        //Setting data
        userName.setText(contactList.get(position).getName());
        userNumber.setText(contactList.get(position).getPhoneNumber());
        if (contactList.get(position).getAvailability().equals(Constant.AVAILABLE))
        {
            //Make TextView Green
            userAvailability.setTextColor(Color.parseColor("#34A138"));
        }
        else {
            //Make TextView Red
            userAvailability.setTextColor(Color.RED);

        }
        Glide.with(context)
                .load(contactList.get(position).getProfileImg())
                .placeholder(R.drawable.profile_avatar)
                .into(userImg);

        userAvailability.setText(contactList.get(position).getAvailability());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView userImg;
        private TextView userName;
        private TextView userNumber;
        private TextView userAvailability;

        //Click Listener
        private IOnItemClickListener iOnItemClickListener;

        public MyViewHolder(@NonNull View itemView,IOnItemClickListener iOnItemClickListener) {
            super(itemView);

            userImg = itemView.findViewById(R.id.userImg);
            userName = itemView.findViewById(R.id.userName);
            userNumber = itemView.findViewById(R.id.userNumber);
            userAvailability = itemView.findViewById(R.id.userAvailability);
            this.iOnItemClickListener = iOnItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iOnItemClickListener.onItemClick(getAdapterPosition());
        }
    }

    public void filter(String queryText)
    {
        contactList.clear();

        if(queryText.isEmpty())
        {
            contactList.addAll(copyList);
        }
        else
        {
            for (int i=0;i<copyList.size();i++)
            {
                String name = copyList.get(i).getName();
                if(name.toLowerCase().contains(queryText.toLowerCase()))
                {
                    contactList.add(copyList.get(i));
                }
            }
        }

        notifyDataSetChanged();
    }
}
