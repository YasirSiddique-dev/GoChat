package com.dev175.admin.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.dev175.admin.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    //Permissions Request Code
    private static final int PERMISSIONS_REQUEST_CODE = 175;

    //Permissions
    String [] appPermissions = {
            Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For Binding
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        if (checkAndRequestPermissions()) {

            int splashTimeOut = 1000;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    //If user have an active session
                    if (firebaseUser != null) {

                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    }
                    else
                    {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                    finish();
                }
            }, splashTimeOut);
        }
    }

    private boolean checkAndRequestPermissions() {

        //Check which permissions are granted
        List<String> listPermissionNeeded = new ArrayList<>();
        for (String perm : appPermissions)
        {
            if (ContextCompat.checkSelfPermission(this,perm) != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionNeeded.add(perm);
            }
        }

        //Ask for non-granted permissions

        if (!listPermissionNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,
                    listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE
            );
            return false;
        }

        //App has all permissions, process ahead
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE)
        {
            HashMap<String,Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            //Gather permissions grant results
            for (int i=0 ;i<grantResults.length;i++)
            {
                //Add only permissions which are denied
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                {
                    permissionResults.put(permissions[i],grantResults[i]);
                    deniedCount++;
                }
            }

            //Check if all permissions are granted
            if (deniedCount==0)
            {
                //process ahead
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                //If user have an active session
                if (firebaseUser != null) {

                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));

                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }

            }
            //at least on or all permissions are denied
            else
            {

                for (Map.Entry<String,Integer> entry : permissionResults.entrySet())
                {

                    String perName = entry.getKey();


                    //Permission is denied (this is the first time, when "never ask again " is not checked)
                    //so ask again explaning the usage of permission
                    //shouldShowRequestPermissionRationale will return true
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,perName))
                    {
                        showDialog("", "This app needs Contacts,Storage & Internet Permissions to work properly.",
                                "Yes, Grant Permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        checkAndRequestPermissions();
                                    }
                                },
                                "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                },false);


                    }

                    //Permission is denied (and never ask again is checked)
                    //shouldShowRequestPermissionRationale will return false
                    else {

                        //Ask user togo settings and manually allow permissions

                        showDialog("",
                                "You have denied some permissions. Allow all permissions at [Setting] > [Permissions]",
                                "Go to Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        //Goto App Settings
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    }
                                },
                                "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                },false);
                        break;

                    }

                }
            }

        }
    }

    public void showDialog(String title,
                           String msg,
                           String positiveLabel,
                           DialogInterface.OnClickListener positiveOnClick,
                           String negativeLabel, DialogInterface.OnClickListener negativeOnClick,
                           boolean isCancelAble)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(isCancelAble);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel,positiveOnClick);
        builder.setNegativeButton(negativeLabel,negativeOnClick);

        AlertDialog alert = builder.create();
        alert.show();
    }
}