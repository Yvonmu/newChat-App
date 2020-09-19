package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderId;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private Toolbar ChatToolbar;
    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;
    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;
    private ProgressDialog loadingBar;

    private Uri fileUri;

    private String saveCurrentTime,saveCurrentDate,checker="",myUri=" ";
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId=getIntent().getExtras().get("Visit_User_id").toString();
        messageReceiverName=getIntent().getExtras().get("Visit_User_name").toString();
        messageReceiverImage=getIntent().getExtras().get("Visit_User_image").toString();

        InitializeControliers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });


        DisplayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]{
                        "Images",
                        "PDF Files",
                        "Ms Word Files"
                };

                final AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the Files");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                            checker="image";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);

                        }if (i==1){
                            checker="pdf";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF Files"),438);

                        }if (i==2){
                            checker="docx";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/m.s word");
                            startActivityForResult(intent.createChooser(intent,"Select Ms Word Files"),438);
                        }


                    }

                });
                builder.show();
            }
        });
    }

    private void InitializeControliers() {

        ChatToolbar=(Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView) findViewById(R.id.custom_profile_image);
        userName=(TextView) findViewById(R.id.user_profile_name);
        userLastSeen=(TextView) findViewById(R.id.custom_user_last_seen);

        sendMessageButton=(ImageButton) findViewById(R.id.send_message_button);
        sendFilesButton=(ImageButton) findViewById(R.id.send_files_button);
        messageInputText=(EditText) findViewById(R.id.input_message);

        messageAdapter=new MessageAdapter(messagesList);
        userMessageList=(RecyclerView)findViewById(R.id.private_message_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        loadingBar=new ProgressDialog(this);

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MM dd,yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());


        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK&&data!=null && data.getData()!=null){
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, to be uploaded");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri=data.getData();

            if (!checker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef="Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef="Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef= RootRef.child("Messages").child(messageSenderId)
                        .child(messageSenderId).child(messageReceiverId).push();

                final String messagePushId=userMessageKeyRef.getKey();

                final StorageReference filePath=storageReference.child(messagePushId+ "."+checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){


                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",task.getResult().toString());//.getDownloadUrl() after getResult()
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverId);
                            messageTextBody.put("messageId",messagePushId);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);

                            Map messageBodyDetail=new HashMap();
                            messageBodyDetail.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageBodyDetail.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

                            RootRef.updateChildren(messageBodyDetail);
                            loadingBar.dismiss();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int)p+"% Uploading...");
                    }
                });


            }else if (checker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef="Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef="Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef= RootRef.child("Messages").child(messageSenderId)
                        .child(messageSenderId).child(messageReceiverId).push();

                final String messagePushId=userMessageKeyRef.getKey();

                final StorageReference filePath=storageReference.child(messagePushId+ "."+"jpg");

                uploadTask=filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri=task.getResult();
                            myUri=downloadUri.toString();


                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",myUri);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverId);
                            messageTextBody.put("messageId",messagePushId);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);

                            Map messageBodyDetail=new HashMap();
                            messageBodyDetail.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageBodyDetail.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

                            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){

                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                    }else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Unsent", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText(" ");
                                }
                            });

                        }
                    }
                });


            }else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen(){
        RootRef.child("User").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("userState").hasChild("state")){
                    String state=dataSnapshot.child("userState").child("state").getValue().toString();
                    String date=dataSnapshot.child("userState").child("date").getValue().toString();
                    String time=dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")){
                        userLastSeen.setText("online ");

                    }else if (state.equals("offline")){
                        userLastSeen.setText("Last seen:"+date+" "+time);

                    }


                }else {
                    userLastSeen.setText("offline ");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages=dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    private void SendMessage(){
        String messageText=messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "First write Your Message", Toast.LENGTH_SHORT).show();
        }else {
            String messageSenderRef="Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef="Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef= RootRef.child("Messages").child(messageSenderId)
                    .child(messageSenderId).child(messageReceiverId).push();

            String messagePushId=userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("to",messageReceiverId);
            messageTextBody.put("messageId",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetail=new HashMap();
            messageBodyDetail.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetail.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(ChatActivity.this, "Unsent", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText(" ");
                }
            });


        }
    }
}