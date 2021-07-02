package com.dev175.chat.fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.dev175.chat.R;
import com.dev175.chat.activity.HomeActivity;
import com.dev175.chat.databinding.EditAboutmeDialogBinding;
import com.dev175.chat.databinding.EditNameDialogBinding;
import com.dev175.chat.databinding.FragmentProfileBinding;
import com.dev175.chat.model.Constant;
import com.dev175.chat.model.User;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class MyProfileFragment extends Fragment {

    private static final int IMAGE_REQUEST_CODE = 842;
    private FragmentProfileBinding binding;

    //Selected Image Uri
    private Uri imageUri;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        binding.profileName.bringToFront();
        binding.usernameEdit.bringToFront();
        binding.profilePicture.bringToFront();

        setUserProfile();
        binding.usernameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNameDialog();
            }
        });

        binding.aboutEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               editAboutMeDialog();
            }
        });

        binding.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });
        return root;
    }

    private void editNameDialog() {

        //Open Dialog
        Dialog dialog = new Dialog(getContext());
        EditNameDialogBinding dialogBinding = EditNameDialogBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialogBinding.name.setText(Constant.currentUser.getFullName());
        dialogBinding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialogBinding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogBinding.name.getText().toString().isEmpty())
                {
                    dialogBinding.nameLyt.setError("\u2022 Name is Required!");
                    return;
                }
                else {
                    dialogBinding.nameLyt.setErrorEnabled(false);
                }


                FirebaseDatabase.getInstance().getReference().child(Constant.USERS)
                        .child(Constant.currentUser.getUid())
                        .child("fullName")
                        .setValue(dialogBinding.name.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                binding.profileName.setText(dialogBinding.name.getText().toString());
                                HomeActivity.name.setText(dialogBinding.name.getText().toString());
                                Constant.currentUser.setFullName(dialogBinding.name.getText().toString());
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getContext(), "Failed to Update your name!", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.dismiss();
            }
        });
    }

    private void editAboutMeDialog() {

        //Open Dialog
        Dialog dialog = new Dialog(getContext());
        EditAboutmeDialogBinding dialogBinding = EditAboutmeDialogBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialogBinding.aboutme.setText(Constant.currentUser.getAboutMe());
        dialogBinding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialogBinding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogBinding.aboutme.getText().toString().isEmpty())
                {
                    dialogBinding.aboutmeLyt.setError("\u2022 About me is Required!");
                    return;
                }
                else {
                    dialogBinding.aboutmeLyt.setErrorEnabled(false);
                }


                FirebaseDatabase.getInstance().getReference().child(Constant.USERS)
                        .child(Constant.currentUser.getUid())
                        .child("aboutMe")
                        .setValue(dialogBinding.aboutme.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                binding.aboutContent.setText(dialogBinding.aboutme.getText().toString());
                                Constant.currentUser.setAboutMe(dialogBinding.aboutme.getText().toString());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(getContext(), "Failed to Update your about about me!", Toast.LENGTH_SHORT).show();
                            }
                        });

                dialog.dismiss();
            }
        });
    }

    private void setUserProfile() {

        if (Constant.currentUser!=null)
        {
            //Set Image
            Glide.with(getContext())
                    .load(Constant.currentUser.getProfileImg())
                    .placeholder(R.drawable.profile_avatar)
                    .into(binding.profilePicture);

            //Set Name
            binding.profileName.setText(Constant.currentUser.getFullName());

            //About Me
            binding.aboutContent.setText(Constant.currentUser.getAboutMe());

            //Set Phone
            binding.phoneContent.setText(Constant.currentUser.getPhone());

            //Set Email
            binding.emailContent.setText(Constant.currentUser.getEmail());



        }
    }


    private void selectImageFromGallery() {
        try
        {
            Intent objectIntent = new Intent();
            objectIntent.setType("image/*");
            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent,IMAGE_REQUEST_CODE);
        }
        catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            imageUri=data.getData();
            Glide.with(getContext()).load(imageUri).into(binding.profilePicture);
            updateProfileImg();
        }
    }

    private void updateProfileImg() {
       StorageReference userImagesRef= FirebaseStorage.getInstance().getReference().child(Constant.PROFILES)
               .child(Constant.currentUser.getUid()+".jpg");

        ExecutorService executors = Executors.newSingleThreadExecutor();
        executors.execute(new Runnable() {
            @Override
            public void run() {

                if (imageUri!=null)
                {
                    Bitmap bitmap = null;
                    try {

                        if (Build.VERSION.SDK_INT < 28)
                        {
                            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        }
                        else
                        {
                            ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), imageUri);
                            bitmap = ImageDecoder.decodeBitmap(source);
                        }

                    } catch (Exception e) {
                        Log.e("TAG", "onActivityResult: " + e.getMessage());
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    }
                    byte[] data = baos.toByteArray();

                     userImagesRef.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                userImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        saveUserProfile(imageUrl);
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(getContext(), ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }

        });
    }

    private void saveUserProfile(String imageUrl) {
        FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Constant.currentUser.getUid())
        .child(Constant.PROFILE_IMG).setValue(imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Constant.currentUser.setProfileImg(imageUrl);
                Glide.with(getContext())
                        .load(Constant.currentUser.getProfileImg())
                        .placeholder(R.drawable.profile_avatar)
                        .into(HomeActivity.profile);

                Toast.makeText(getContext(), "Profile Image Updated", Toast.LENGTH_SHORT).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}