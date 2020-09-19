package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        InitializeField();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToMainActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"please enter email..",Toast.LENGTH_SHORT).show();
        }if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"please enter password..",Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Create New Account");
            loadingBar.setMessage("Please wait,while creating new account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                        String currentUserId=mAuth.getCurrentUser().getUid();
                        RootRef.child("User").child(currentUserId).setValue("");

                        RootRef.child("User").child(currentUserId).child("device_token")
                                .setValue(deviceToken);

                        SendUserToMainActivity();

                        Toast.makeText(RegisterActivity.this,"Account created Successful",Toast.LENGTH_SHORT).show();

                        loadingBar.dismiss();
                    }else {
                        String message=task.getException().toString();
                        Toast.makeText(RegisterActivity.this,"Account already taken:"+message,Toast.LENGTH_SHORT).show();

                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void InitializeField() {
        CreateAccountButton = (Button) findViewById(R.id.register_button);

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);

        loadingBar=new ProgressDialog(this);
    }
    private void SendUserToMainActivity() {

        Intent mainIntent= new Intent(RegisterActivity.this,LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}