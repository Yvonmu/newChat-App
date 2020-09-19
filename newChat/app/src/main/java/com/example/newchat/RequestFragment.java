package com.example.newchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference chatRequestRef,usersRef,contactRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView= inflater.inflate(R.layout.fragment_request, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("User");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef=FirebaseDatabase.getInstance().getReference().child("Contacts");


        myRequestList=(RecyclerView) RequestsFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int i, @NonNull Contacts contacts) {
                        holder.itemView.findViewById(R.id.request_accept_Button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_Button).setVisibility(View.VISIBLE);

                        final String list_user_id=getRef(i).getKey();

                        DatabaseReference getTypeRef=getRef(i).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String type=dataSnapshot.getValue().toString();

                                    if (type.equals("received")){
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")){
                                                   final String requestProfileImage=dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();
                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("you have sent a request to "+ requestUserName);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[]=new CharSequence[]{
                                                                "Accept",
                                                                "Cancel"
                                                        };

                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName+"Chat Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if (i==0){
                                                                    contactRef.child(currentUserId).child(list_user_id).child("Contact").setValue("Saved")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        contactRef.child(list_user_id).child(currentUserId).child("Contact").setValue("Saved")
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){
                                                                                                            chatRequestRef.child(currentUserId).child(list_user_id)
                                                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()){
                                                                                                                        chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()){
                                                                                                                                    Toast.makeText(getContext(), "Contact saved", Toast.LENGTH_SHORT).show();
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
                                                                            });
                                                                }if (i==1){
                                                                    chatRequestRef.child(currentUserId).child(list_user_id)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }else if(type.equals("sent")){
                                        Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_Button);
                                        request_sent_btn.setText("Request sent");

                                        holder.itemView.findViewById(R.id.request_cancel_Button).setVisibility(View.INVISIBLE);

                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")){
                                                    final String requestProfileImage=dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();
                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(requestUserStatus);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[]=new CharSequence[]{

                                                                "Cancel Chat Request"
                                                        };

                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already sent request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if (i==1){
                                                                    chatRequestRef.child(currentUserId).child(list_user_id)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            Toast.makeText(getContext(), "You have cancelled the chat request.", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                        RequestsViewHolder holder=new RequestsViewHolder(view);
                        return holder;
                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_image);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.user_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_Button);
            CancelButton=itemView.findViewById(R.id.request_cancel_Button);
        }
    }
}