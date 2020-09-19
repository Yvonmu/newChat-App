package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private Toolbar SettingsToolbar;

    private static final int GalleryPick=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });



        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick&& resultCode==RESULT_OK && data!=null){
            Uri ImageUri=data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){

                loadingBar.setTitle("Upload image");
                loadingBar.setMessage("Please wait,your image to be uploaded");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri=result.getUri();

                StorageReference filePath=UserProfileImageRef.child(currentUserId +".jpg");

               /* filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(SettingActivity.this, "Profile image uploaded", Toast.LENGTH_SHORT).show();

                            final String downloadUrl=task.getResult().getDownloadUrl().toString();

                            RootRef.child("User").child(currentUserId).child("image")
                                    .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(SettingActivity.this, "image save in database", Toast.LENGTH_SHORT).show();

                                        loadingBar.dismiss();
                                    }else {
                                        String message=task.getException().toString();
                                        Toast.makeText(SettingActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();

                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }else {
                            String message=task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();

                            loadingBar.dismiss();
                        }
                    }
                });*/
               filePath.putFile(resultUri)
                       .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onSuccess(final UploadTask.TaskSnapshot task) {
                               final Task<Uri>firebaseUri=task.getStorage().getDownloadUrl();
                               firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                       final String downloadUrl=uri.toString();

                                       RootRef.child("User").child(currentUserId).child("image")
                                               .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()){
                                                   Toast.makeText(SettingActivity.this, "image save in database", Toast.LENGTH_SHORT).show();

                                                   loadingBar.dismiss();
                                               }else {
                                                   String message=task.getException().toString();
                                                   Toast.makeText(SettingActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();

                                                   loadingBar.dismiss();
                                               }
                                           }
                                       });
                                   }
                               });
                           }
                       });
            }
        }


    }

    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"please Write Your User fName..",Toast.LENGTH_SHORT).show();
        }if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(this,"please Write Your Status..",Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String,Object>profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            RootRef.child("User").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this,"profile Update success",Toast.LENGTH_SHORT).show();

                            }else {
                                userName.setVisibility(View.VISIBLE);
                                String message=task.getException().toString();
                                Toast.makeText(SettingActivity.this,"Error: "+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        UpdateAccountSettings=(Button)findViewById(R.id.update_sstting_button);
        userName=(EditText)findViewById(R.id.set_User_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);

        SettingsToolbar=(Toolbar)findViewById(R.id.setting_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Setting");
    }



    private void RetrieveUserInfo() {
        RootRef.child("User").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists())&& (dataSnapshot.hasChild("name")&& (dataSnapshot.hasChild("image")))){
                            String retrieveUserName= dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus= dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage= dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);


                        }else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                            String retrieveUserName= dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus= dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }else {
                            Toast.makeText(SettingActivity.this,"set&Update your profile info",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void SendUserToMainActivity() {

        Intent mainIntent= new Intent(SettingActivity.this,LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}