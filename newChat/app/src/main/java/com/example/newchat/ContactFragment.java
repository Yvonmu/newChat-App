package com.example.newchat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


public class ContactFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactList;

    private DatabaseReference ContactsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;



    public ContactFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView= inflater.inflate(R.layout.fragment_contact, container, false);

        myContactList=(RecyclerView) ContactsView.findViewById(R.id.Contacts_Lists);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

       ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
       userRef=FirebaseDatabase.getInstance().getReference().child("User");
        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, contactsViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contactsViewHolder holder, int i, @NonNull Contacts contacts) {
                String usersIds=getRef(i).getKey();

                userRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){


                            if (dataSnapshot.child("userState").hasChild("state")){
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")){
                                    holder.onlineIcon.setVisibility(View.VISIBLE);

                                }else if (state.equals("offline")){
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);

                                }


                            }else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);

                            }


                            if (dataSnapshot.hasChild("image")){
                                String userImage=dataSnapshot.child("image").getValue().toString();
                                String profileName=dataSnapshot.child("name").getValue().toString();
                                String profileStatus=dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder. profileImage);
                            }else {
                                String profileName=dataSnapshot.child("name").getValue().toString();
                                String profileStatus=dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
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
            public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                contactsViewHolder viewHolder=new contactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class contactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;

        ImageView onlineIcon;

        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.user_profile_image);
            onlineIcon=(ImageView) itemView.findViewById(R.id.user_online_status);
        }
    }
}