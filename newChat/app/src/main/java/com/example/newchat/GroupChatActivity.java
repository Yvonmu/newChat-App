package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView nScrollview;
    private TextView displayMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,GroupNameRef,GroupMessageKeyRef;

    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this,currentGroupName,Toast.LENGTH_SHORT).show();

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("User");
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        initializeField();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageToDatabase();

                userMessageInput.setText(" ");

                //how to see new message first
                nScrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void initializeField() {

        mToolbar=(Toolbar)findViewById(R.id.group_chat_bar_layout);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton=(ImageButton)findViewById(R.id.send_message);
        userMessageInput=(EditText)findViewById(R.id.input_group_message);
        displayMessage=(TextView)findViewById(R.id.group_chat_text_display);
        nScrollview=(ScrollView)findViewById(R.id.my_scroll_view);
    }


    private void GetUserInfo() {
        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void SaveMessageToDatabase() {
        String message=userMessageInput.getText().toString();
        String messageKey=GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"please write message",Toast.LENGTH_SHORT).show();
        }else{
            Calendar calForDate= Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("yyyy, MMM,dd");
            currentDate=currentDateFormat.format(calForDate.getTime());

            Calendar calFortIME= Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm:ss a");
            currentDate=currentTimeFormat.format(calFortIME.getTime());

            HashMap<String,Object> groupMessageKey=new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef=GroupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap=new HashMap<>();
                  messageInfoMap.put("name",currentUserName);
                  messageInfoMap.put("message",message);
                  messageInfoMap.put("date",currentDate);
                  messageInfoMap.put("time",currentTime);

             GroupMessageKeyRef.updateChildren(messageInfoMap);


        }
    }
    private void DisplayMessage(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatDate=(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String)((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();

            displayMessage.append(chatName+" :\n"+chatMessage+" :\n"+chatTime+"  "+chatDate+"\n\n\n");

            //how to see new message first
            nScrollview.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}