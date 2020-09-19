package com.example.newchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class CheckFragment extends Fragment {

    private View chatView;
    private RecyclerView chatList;
    private DatabaseReference chatsRef,userRef;
    private FirebaseAuth mAuth;

    private String currentUserId;




    public CheckFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatView= inflater.inflate(R.layout.fragment_check, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef=FirebaseDatabase.getInstance().getReference().child("User");
        chatList=(RecyclerView) chatView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return chatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,chatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, chatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int i, @NonNull Contacts contacts) {
                        final String userIds=getRef(i).getKey();
                        final String[] retrieveImage = {"default_image"};

                        userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               //for the user that don't what to set profile image
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){
                                        retrieveImage[0] =dataSnapshot.child("image").getValue().toString();

                                        Picasso.get().load(retrieveImage[0]).into(holder.profileImage);
                                    }

                                    final String retrieveName=dataSnapshot.child("name").getValue().toString();
                                    final String retrieveStatus=dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retrieveName);



                                    if (dataSnapshot.child("userState").hasChild("state")){
                                        String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")){
                                            holder.userStatus.setText("online ");

                                        }else if (state.equals("offline")){
                                            holder.userStatus.setText("Last seen:"+date+" "+time);

                                        }


                                    }else {
                                        holder.userStatus.setText("offline ");

                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("Visit_User_id", userIds);
                                            chatIntent.putExtra("Visit_User_name", retrieveName);
                                            chatIntent.putExtra("Visit_User_image", retrieveName);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);

                        return new chatsViewHolder(view);
                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userStatus,userName;

        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.user_profile_image);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
        }
    }
}