package com.example.newchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    public MessageAdapter( List<Messages> userMessagesList){
        this.userMessagesList=userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);

        mAuth=FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();

        usersRef= FirebaseDatabase.getInstance().getReference().child("User").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiverImage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text")){

            if (fromUserId.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage()+"\n\n"+messages.getTime()+" -"+messages.getDate());
            }else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage()+"\n\n"+messages.getTime()+" -"+messages.getDate());

            }
        }else if(fromMessageType.equals("image")){
            if (fromUserId.equals(messageSenderId)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            }else {

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);

            }
        }else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx")){
            if (fromUserId.equals(messageSenderId)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
//copy the link from firebase
               Picasso.get()
                       .load("")//copy the link from firebase
                       .into(holder.messageSenderPicture);


            }else {
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load("")//copy the link from firebase
                        .into(holder.messageReceiverPicture);



            }
        }

        if (fromUserId.equals(messageSenderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete for me",
                                "Download and view This Document",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    deleteSentMessage(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i==1){

                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (i==3){
                                    deleteMessageForEveryOne(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    deleteSentMessage(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i==2){
                                    deleteMessageForEveryOne(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (userMessagesList.get(position).getType().equals("image") ){
                        CharSequence options[]=new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    deleteSentMessage(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (i==1){
                                    Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);


                                }else if (i==3){
                                    deleteMessageForEveryOne(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }else if (fromUserId.equals(messageSenderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete for me",
                                "Download and view This Document",
                                "Cancel"

                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    deleteReceivedMessage(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i==1){

                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete for me",
                                "Cancel"

                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    deleteReceivedMessage(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if (userMessagesList.get(position).getType().equals("image") ){
                        CharSequence options[]=new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel",

                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    deleteReceivedMessage(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (i==1){
                                    Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private  void deleteSentMessage(final int position,final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }else {

                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    private  void deleteReceivedMessage(final int position,final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }else {

                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private  void deleteMessageForEveryOne(final int position,final MessageViewHolder holder){
        final DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Deleted Success", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                      }else {

                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public  class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText=(TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=(TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_file);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_file);

        }
    }

}
