package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,Current_state,sendUserId;
    private CircleImageView userProfileImage;
    private Button sendMessageRequestButton,declineMessageRequestButton;
    private TextView userProfileName,userProfileStatus;
    private DatabaseReference UserRef,chatRequestRef,ContactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("User");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef= FirebaseDatabase.getInstance().getReference().child("Notification");



        Toast.makeText(this, "User Id", Toast.LENGTH_SHORT).show();

        receiverUserId=getIntent().getExtras().get("Visit_user_id").toString();
        sendUserId=mAuth.getCurrentUser().getUid();



        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_profile_status);
        sendMessageRequestButton=(Button) findViewById(R.id.send_message_request_button);
        declineMessageRequestButton=(Button) findViewById(R.id.decline_message_request_button);


        Current_state="new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {

        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){

                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }else {

                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {

        chatRequestRef.child(sendUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)){

                            String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                Current_state="request_sent";
                                sendMessageRequestButton.setText("Cancel chat Request");
                            }else if (request_type.equals("received")){
                                Current_state="request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);

                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }else {
                            ContactsRef.child(sendUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiverUserId)){
                                        Current_state="friends";
                                        sendMessageRequestButton.setText("Remove this Contacts");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if (!sendUserId.equals(receiverUserId)){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageRequestButton.setEnabled(false);

                    if (Current_state.equals("new")){
                        sendChatRequest();
                    }if (Current_state.equals("request_sent")){
                        CancelChatRequest();
                    }if (Current_state.equals("request_received")){
                        AcceptChatRequest();
                    }if (Current_state.equals("friends")){
                        RemoveSpecificContacts();
                    }
                }
            });
        }else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void RemoveSpecificContacts() {

        ContactsRef.child(sendUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(receiverUserId).child(sendUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_state="new";
                                                sendMessageRequestButton.setText("Send message");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(sendUserId).child(receiverUserId).child("Contacts")
                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    ContactsRef.child(receiverUserId).child(sendUserId).child("Contacts")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                chatRequestRef.child(sendUserId).child(receiverUserId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    chatRequestRef.child(receiverUserId).child(sendUserId).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    Current_state="friends";
                                                                    sendMessageRequestButton.setText("Remove this contact");

                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                    declineMessageRequestButton.setEnabled(false);
                                                                }
                                                            });
                                                }
                                            }
                                        });

                            }
                        }
                    });

                }
            }
        });

    }

    private void CancelChatRequest() {
        chatRequestRef.child(sendUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(receiverUserId).child(sendUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_state="new";
                                                sendMessageRequestButton.setText("Send message");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        chatRequestRef.child(sendUserId).child(receiverUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatRequestRef.child(receiverUserId).child(sendUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        HashMap<String,String>chatNotificationMap=new HashMap<>();
                                        chatNotificationMap.put("from",sendUserId);
                                        chatNotificationMap.put("from","request");

                                        notificationRef.child(receiverUserId).push()
                                                .setValue(chatNotificationMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    sendMessageRequestButton.setEnabled(true);
                                                    Current_state="request_sent";
                                                    sendMessageRequestButton.setText("cancel chat request");

                                                }
                                            }
                                        });

                                         }
                                }
                            });
                }
            }
        });
    }
}