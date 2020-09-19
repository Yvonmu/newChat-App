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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccountLink,ForgetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference().child("User");

        InitializeField();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();

            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }

            private void AllowUserToLogin() {
                String email=UserEmail.getText().toString();
                String password=UserPassword.getText().toString();

                if (TextUtils.isEmpty(email)){
                   // Toast.makeText(this,"please enter email..",Toast.LENGTH_SHORT).show();
                }if (TextUtils.isEmpty(password)){
                    //Toast.makeText(this,"please enter password..",Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.setTitle("Sign In");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    mAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){

                                        String currentUserId=mAuth.getCurrentUser().getUid();
                                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                                        usersRef.child(currentUserId).child("device_token").setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            SendUserToMainActivity();
                                                            Toast.makeText(LoginActivity.this,"Logged in Successful",Toast.LENGTH_SHORT).show();

                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });


                                    }else {
                                        String message=task.getException().toString();
                                        Toast.makeText(LoginActivity.this,"Account already taken:"+message,Toast.LENGTH_SHORT).show();

                                        loadingBar.dismiss();
                                    }
                                }
                            });

                }
            }

        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
    }

    private void InitializeField() {
        LoginButton=(Button) findViewById(R.id.login_button);
        PhoneLoginButton=(Button) findViewById(R.id.login_on_phone_button);
        UserEmail=(EditText)findViewById(R.id.login_email);
        UserPassword=(EditText)findViewById(R.id.login_password);
        NeedNewAccountLink=(TextView)findViewById(R.id.need_new_account_link);
        ForgetPasswordLink=(TextView)findViewById(R.id.forget_password);

        loadingBar=new ProgressDialog(this);
    }



    private void SendUserToMainActivity() {

        Intent mainIntent= new Intent(LoginActivity.this,LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {

        Intent registerIntent= new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }
}