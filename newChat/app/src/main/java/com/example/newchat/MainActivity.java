package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar nToolBar;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        RootRef= FirebaseDatabase.getInstance().getReference();

        nToolBar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(nToolBar);
        getSupportActionBar().setTitle("New Chat");

        myViewPager=findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter= new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout.findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if (currentUser==null){
            SendUserToLoginActivity();
        }else{

            updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if (currentUser!=null){
            updateUserStatus("offline");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if (currentUser!=null){
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserId=mAuth.getCurrentUser().getUid();

        RootRef.child("User").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();

                }else{
                    SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent findFriendIntent= new Intent(MainActivity.this,LoginActivity.class);
        findFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(findFriendIntent);
        finish();
    }
    private void SendUserToSettingActivity() {
        Intent settingIntent= new Intent(MainActivity.this,SettingActivity.class);

        startActivity(settingIntent);

    }

    private void SendUserToFindFriendActivity() {
        Intent settingIntent= new Intent(MainActivity.this,FindFriendActivity.class);


        startActivity(settingIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId()==R.id.main_find_people_option){

            SendUserToFindFriendActivity();

        }if (item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();

        }if (item.getItemId()==R.id.main_setting_option){
            SendUserToSettingActivity();

        }if (item.getItemId()==R.id.main_logOut_option){

            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();

        }

        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group name: ");

        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("e.g Siblings");
        builder.setView(groupNameField);

        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName= groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this,"please write group name",Toast.LENGTH_SHORT).show();
                }else{
                    CreateNewGroup(groupName);

                }
            }
        });
        builder.setNegativeButton("exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue(" ")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName+"group is created successful",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void updateUserStatus(String state){
        String saveCurrentTime,saveCurrentDate;

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MM dd,yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());


        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineState=new HashMap<>();
        onlineState.put("time",saveCurrentTime);
        onlineState.put("date",saveCurrentDate);
        onlineState.put("state",state);

        currentUserId=mAuth.getCurrentUser().getUid();

        RootRef.child("User").child(currentUserId).child("userState")
                .updateChildren(onlineState);

    }
    }
