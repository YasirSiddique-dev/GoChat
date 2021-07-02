package com.dev175.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.dev175.chat.databinding.ActivityContactBinding;
import com.dev175.chat.model.User;
import com.dev175.chat.myInterface.IOnItemClickListener;
import com.dev175.chat.R;
import com.dev175.chat.adapter.ContactAdapter;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ContactActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, IOnItemClickListener {

    private static final String TAG = "ContactActivity";

    //User Contacts List
    private ArrayList<Contact> contactsList;

    //Db Contacts List
    private ArrayList<User> usersList;
    private ActivityContactBinding binding;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        getUserContactsFromDb();


    }

    private void init() {
        try {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Contacts");
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, "init: ",e.getCause());
        }
        contactsList = new ArrayList<>();
        usersList= new ArrayList<>();

        adapter = new ContactAdapter(this,this);
        binding.contactsRv.setLayoutManager(new LinearLayoutManager(this));

    }

    //Get Phone Contacts
    private void getContactList() {

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        while (cursor.moveToNext())
        {
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            if (phone.length()>5)
            {
                phone = phone.replace("-","");
                phone = phone.replace("(","");
                phone = phone.replace(")","");
                phone = phone.replace(" ","");
                phone = phone.replace("+","");

                //Converting 03 to 923
                if (phone.startsWith("03"))
                {
                    phone= "92"+phone.substring(1);
                }

                Contact contact = new Contact();
                contact.setPhoneNumber(phone);
                contact.setName(name);

                for (int i=0 ; i<usersList.size(); i++)
                {
                    if(PhoneNumberUtils.compare(usersList.get(i).getPhone(), contact.getPhoneNumber()))
                    {
                        contact.setAvailability(Constant.AVAILABLE);
                        contact.setProfileImg(usersList.get(i).getProfileImg());
                        contact.setContactId(usersList.get(i).getUid());
                        contact.setName(usersList.get(i).getFullName());
                        contact.setEmail(usersList.get(i).getEmail());
                        break;
                    }
                    else
                    {
                        contact.setContactId("");
                        contact.setProfileImg("");
                        contact.setAvailability(Constant.INVITE_YOUR_FRIEND);
                    }

                }

                if (!contactsList.contains(contact))
                {
                    contactsList.add(contact);
                }

            }
        }

        Collections.sort(contactsList,Contact.contactListSortByName);
        Collections.sort(contactsList,Contact.contactListSortByStatus);

        ArrayList<Contact> noRepeat = new ArrayList<>();

        //Removing Repeated names
        for (Contact contact : contactsList) {
            boolean isFound = false;
            // check if the contact  exists in noRepeat
            for (Contact c : noRepeat)
            {
                if (c.getPhoneNumber().equals(contact.getPhoneNumber()) || (c.equals(contact)))
                {
                    isFound = true;
                    break;
                }
            }
            if (!isFound)
            {
                noRepeat.add(contact);
            }
        }

        contactsList=noRepeat;
        cursor.close();

       //SetAdapter
        Log.d(TAG, "getContactList: "+contactsList.size());
        adapter.setContactList(contactsList);
        binding.contactsRv.setAdapter(adapter);

    }

    //Get Contacts From Database
    private void getUserContactsFromDb() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseReference usersDb = FirebaseDatabase.getInstance().getReference().child(Constant.USERS);
                usersDb.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            User user = dataSnapshot.getValue(User.class);
                            usersList.add(user);
                        }
                        //Get Contacts From Phone Storage and Check Which Contact is also an user & Set Status Accordingle
                        if (usersList.size()==snapshot.getChildrenCount())
                        {
                            getContactList();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {
                        Toast.makeText(ContactActivity.this, error+"", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    //////////////////// On Query Text Changed: Filter Results in Adapter ////////////////////
    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return false;
    }


    ////////////////////////////// Inflating Search Menu /////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search,menu);

        //Find Menu Item
        MenuItem searchItem = menu.findItem(R.id.action_search);

        //Search View
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Contact");
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(false);

        return true;
    }

    @Override
    public void onItemClick(int position) {

        Contact contact = contactsList.get(position);
        if (contact.getAvailability().equals(Constant.AVAILABLE))
        {
            Intent intent = new Intent(ContactActivity.this,ChatActivity.class);
            intent.putExtra(Constant.CONTACT,contact);
            startActivity(intent);
        }
    }

}