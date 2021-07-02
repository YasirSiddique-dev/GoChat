package com.dev175.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.dev175.chat.R;
import com.dev175.chat.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    //For Binding
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init()
    {

        binding.forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = validateFields();

                if (isValid)
                {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(binding.emailForgot.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //                         progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(ForgotPasswordActivity.this, "Password has sent to your email", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this,SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;


        //Email
        if (binding.emailForgot.getText().toString().isEmpty())
        {
            binding.emailForgotLyt.setError("\u2022 Email is required!");
            binding.emailForgot.setBackgroundResource(R.drawable.edit_text_warning);
            isValid = false;
        }
        else
        {
            binding.emailForgotLyt.setErrorEnabled(false);
            binding.emailForgot.setBackgroundResource(R.drawable.edit_text_style);
        }


        return isValid;

    }

}