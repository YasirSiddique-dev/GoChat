package com.dev175.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dev175.chat.R;
import com.dev175.chat.databinding.ActivitySignInBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.util.CheckSettingPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();


        //Check Login details are saved in preferences or not
        checkLoginDetails();

        //Click Listener
        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });

        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean checkFields = validateFields();
                if (checkFields)
                {
                    signInUser();
                }
            }
        });

        binding.forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,ForgotPasswordActivity.class));
            }
        });
    }

    private void checkLoginDetails() {
        SharedPreferences loginPreferences = getSharedPreferences(getPackageName()+Constant.PREF_USER_LOGIN_DETAILS, Context.MODE_PRIVATE);
        String uEmail = loginPreferences.getString(Constant.KEY_EMAIL,"N/A");
        String uPassword = loginPreferences.getString(Constant.KEY_PASSWORD,"N/A");
        if (!uEmail.equals("N/A"))
        {
            binding.emailSignin.setText(uEmail);
            binding.passwordSignin.setText(uPassword);
            binding.checkboxRemember.setChecked(true);
        }
    }

    private void signInUser() {
        String uEmail = binding.emailSignin.getText().toString().trim();
        String uPassword = binding.passwordSignin.getText().toString().trim();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //Show Progress & Hide Button
                        binding.signInBtn.setVisibility(View.GONE);
                        binding.progressCircular.setVisibility(View.VISIBLE);
                    }
                });

                firebaseAuth.signInWithEmailAndPassword(uEmail,uPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                //Show Btn & Hide Progress
                                binding.progressCircular.setVisibility(View.GONE);
                                binding.signInBtn.setVisibility(View.VISIBLE);


                                if (task.isSuccessful())
                                {

                                    //If email is verified then continue to login
                                    if (firebaseAuth.getCurrentUser().isEmailVerified())
                                    {
                                        saveLoginDetailsToPreferences();
                                        saveSpamDetails();
                                        updateToken();

                                        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        Toast.makeText(SignInActivity.this, "Please Verify your email..!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    Log.d(TAG, "onComplete: "+task.getException());
                                    Toast.makeText(SignInActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void saveSpamDetails() {
        boolean isEnable = CheckSettingPreferences.getSpamDetectionMode(this);
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constant.SPAM)
                .child(uid)
                .setValue(isEnable);
    }

    private void saveLoginDetailsToPreferences() {

        /*
         * getPreferences = for Activity level
         * getSharedPreferences = for Application level */

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName()+ Constant.PREF_USER_LOGIN_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.KEY_EMAIL,binding.emailSignin.getText().toString());
        editor.putString(Constant.KEY_PASSWORD,binding.passwordSignin.getText().toString());
        editor.apply();


        /*
         * editor.apply():
         * It saves changes Asynchronously and returns nothing.
         *
         * editor.commit():
         * It saves changes Synchronously and returns boolean value
         *
         */
    }

    private boolean validateFields() {
        boolean isValid = true;

        //Email
        if (binding.emailSignin.getText().toString().isEmpty())
        {
            binding.emailSigninLyt.setError("\u2022 Email is required!");
            binding.emailSignin.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else
        {
            binding.emailSigninLyt.setErrorEnabled(false);
            binding.emailSignin.setBackgroundResource(R.drawable.edit_text_style);
        }

        //Password
        if (binding.passwordSignin.getText().toString().isEmpty())
        {
            binding.passwordSigninLyt.setError("\u2022 Password is required!");
            binding.passwordSignin.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else
        {
            binding.passwordSigninLyt.setErrorEnabled(false);
            binding.passwordSignin.setBackgroundResource(R.drawable.edit_text_style);
        }

        return isValid;

    }


    private void updateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful())
                        {
                            String token = task.getResult();

                            String uid = FirebaseAuth.getInstance().getUid();
                            DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child(Constant.USERS);
                            userDb.child(uid).child("token").setValue(token)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}