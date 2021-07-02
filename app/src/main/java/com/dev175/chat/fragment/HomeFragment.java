package com.dev175.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.dev175.chat.R;
import com.dev175.chat.adapter.FragmentAdapter;
import com.dev175.chat.activity.ContactActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton contactBtn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager=root.findViewById(R.id.main_view_pager);
        tabLayout=root.findViewById(R.id.main_tabs);
        contactBtn=root.findViewById(R.id.fab_contact);

        FragmentAdapter fragmentAdpaters=new FragmentAdapter(getActivity().getSupportFragmentManager());
        fragmentAdpaters.addfragments(ChatsFragment.getInstance(), "Chats");
        fragmentAdpaters.addfragments(StatusFragment.getInstance(), "Status");
        fragmentAdpaters.addfragments(SpamFragment.getInstance(),"Spam");

        viewPager.setAdapter(fragmentAdpaters);
        tabLayout.setupWithViewPager(viewPager);

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ContactActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }
}