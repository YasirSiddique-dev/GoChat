package com.dev175.admin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.dev175.admin.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //Firebase Authentication
    private FirebaseAuth mAuth;

    //For Binding
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Login Btn Click Listener
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {

        boolean checkFields = validateLoginFields();

        if (checkFields)
        {
            //Set Progress bar visible and hide button
            binding.loginBtn.setVisibility(View.GONE);
            binding.progressCircular.setVisibility(View.VISIBLE);

            //Login with email and password
            mAuth.signInWithEmailAndPassword(binding.email.getText().toString(),binding.password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful() && mAuth.getCurrentUser()!=null)
                            {
                                //Set Progress bar hide and show button
                                binding.loginBtn.setVisibility(View.VISIBLE);
                                binding.progressCircular.setVisibility(View.GONE);

                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }
                            else
                            {
                                //Set Progress bar hide and show button
                                binding.loginBtn.setVisibility(View.VISIBLE);
                                binding.progressCircular.setVisibility(View.GONE);

                                Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }


    private boolean validateLoginFields() {
        boolean isValid = true;

        //Email
        if (binding.email.getText().toString().isEmpty())
        {
            binding.emailLyt.setError("\u2022 Email is Required!");
            isValid = false;
        }
        else
        {
            binding.emailLyt.setErrorEnabled(false);
        }

        //Password
        if (binding.password.getText().toString().isEmpty())
        {
            binding.passwordLyt.setError("\u2022 Password is Required!");
            isValid = false;
        }
        else if (binding.password.getText().toString().length()<8)
        {
            binding.passwordLyt.setError("\u2022 Password Should be at least 8 chars long!");
            isValid=false;
        }
        else if (binding.password.getText().toString().length()>18)
        {
            binding.passwordLyt.setError("\u2022 Password Should not be greater than 18 chars!");
            isValid=false;
        }
        else
        {
            binding.passwordLyt.setErrorEnabled(false);
        }

        return isValid;

    }

}