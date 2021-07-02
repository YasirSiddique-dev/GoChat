package com.dev175.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.dev175.chat.R;
import com.dev175.chat.databinding.ActivitySignUpBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clickListener();

    }

    private void clickListener() {

        //Signup Click Listener
        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checkFields = validateFields();
                if (checkFields)
                {
                    binding.signupBtn.setVisibility(View.GONE);
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    String uName = binding.signUpFullname.getText().toString().trim();
                    String uEmail = binding.signUpEmail.getText().toString().trim();
                    String uPassword = binding.signUpPassword.getText().toString().trim();
                    String uPhone = binding.signUpPhone.getText().toString().trim();

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(uEmail,uPassword)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (task.isSuccessful()) //If user sign up successfully
                                            {
                                                FirebaseAuth.getInstance()
                                                        .getCurrentUser()
                                                        .sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    String uid = FirebaseAuth.getInstance().getUid();
                                                                    User user = new User();
                                                                    user.setFullName(uName);
                                                                    user.setEmail(uEmail);
                                                                    user.setPhone(uPhone);
                                                                    user.setProfileImg("");
                                                                    user.setUid(uid);
                                                                    user.setAboutMe("Welcome to my profile");
                                                                    user.setToken("");

                                                                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                                                                            .getReference().child(Constant.USERS);
                                                                    userRef.child(uid).setValue(user)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Toast.makeText(SignUpActivity.this, "Sign up Successful.\nPlease verify your email to Login.", Toast.LENGTH_SHORT).show();
                                                                                    resetFields();

                                                                                    //Show btn and Hide progress
                                                                                    binding.progressCircular.setVisibility(View.GONE);
                                                                                    binding.signupBtn.setVisibility(View.VISIBLE);

                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(SignUpActivity.this, e.getMessage()+"", Toast.LENGTH_SHORT).show();

                                                                                    //Show Progress and Hide Button
                                                                                    binding.progressCircular.setVisibility(View.GONE);
                                                                                    binding.signupBtn.setVisibility(View.VISIBLE);

                                                                                }
                                                                            });

                                                                }
                                                                else
                                                                    {
                                                                        binding.progressCircular.setVisibility(View.GONE);
                                                                        binding.signupBtn.setVisibility(View.VISIBLE);

                                                                    Log.d(TAG, "onComplete: "+task.getException());
                                                                    Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });

                                            } else //if any error occurs
                                            {
                                                //Show Progress and Hide Button
                                                binding.progressCircular.setVisibility(View.GONE);
                                                binding.signupBtn.setVisibility(View.VISIBLE);

                                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });

                }
            }
        });

        //Sign in Click Listener
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });

    }

    private void resetFields() {
        binding.signUpFullname.setText("");
        binding.signUpEmail.setText("");
        binding.signUpPassword.setText("");
        binding.signUpPhone.setText("");
    }


    private boolean validateFields() {
        boolean isValid = true;

        //Name
        if (binding.signUpFullname.getText().toString().isEmpty())
        {
            binding.signUpFullnameLyt.setError("\u2022 Full name is required!");
            binding.signUpFullname.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else
        {
            binding.signUpFullnameLyt.setErrorEnabled(false);
            binding.signUpFullname.setBackgroundResource(R.drawable.edit_text_style);
        }

        //Email
        if (binding.signUpEmail.getText().toString().isEmpty())
        {
            binding.signUpEmailLyt.setError("\u2022 Email is required!");
            binding.signUpEmail.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else
        {
            binding.signUpEmailLyt.setErrorEnabled(false);
            binding.signUpEmail.setBackgroundResource(R.drawable.edit_text_style);
        }

        //Password
        if (binding.signUpPassword.getText().toString().isEmpty())
        {
            binding.signUpPasswordLyt.setError("\u2022 Password is required!");
            binding.signUpPassword.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else
        {
            binding.signUpPasswordLyt.setErrorEnabled(false);
            binding.signUpPassword.setBackgroundResource(R.drawable.edit_text_style);
        }

        //Phone Number
        if (binding.signUpPhone.getText().toString().isEmpty())
        {
            binding.signUpPhoneLyt.setError("\u2022 Phone number is required!");
            binding.signUpPhone.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else if (!binding.signUpPhone.getText().toString().startsWith("92"))
        {
            binding.signUpPhoneLyt.setError("\u2022 Phone number must starts with 92!");
            binding.signUpPhone.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else
        {
            binding.signUpPhoneLyt.setErrorEnabled(false);
            binding.signUpPhone.setBackgroundResource(R.drawable.edit_text_style);
        }

        return isValid;

    }
}












