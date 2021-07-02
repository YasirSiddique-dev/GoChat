package com.dev175.chat.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.dev175.chat.R;
import com.dev175.chat.model.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SettingFragment extends PreferenceFragmentCompat implements PreferenceManager.OnPreferenceTreeClickListener {

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        getActivity().setTitle("Settings");
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                if (key.equals(Constant.SPAM_DETECTION))
                {
                    boolean val = sharedPreferences.getBoolean(key, false);

                    String uid = FirebaseAuth.getInstance().getUid();
                    DatabaseReference spamDb = FirebaseDatabase.getInstance().getReference()
                            .child(Constant.SPAM)
                            .child(uid);
                    spamDb.setValue(val);

                }
            }
        };
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
}