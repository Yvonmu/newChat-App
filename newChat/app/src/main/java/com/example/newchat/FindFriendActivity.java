package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView findfriendRecyclerList;
    private DatabaseReference UserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        UserRef= FirebaseDatabase.getInstance().getReference().child("User");

        findfriendRecyclerList=(RecyclerView)findViewById(R.id.find_people_recycler_list);
        findfriendRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolBar=(Toolbar)findViewById(R.id.find_people_toolbar);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find People");
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, FiendFriendViewHolder>adapter =
                new FirebaseRecyclerAdapter<Contacts, FiendFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FiendFriendViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id=getRef(position).getKey();

                                Intent profileIntent=new Intent(FindFriendActivity.this,ProfileActivity.class);
                                profileIntent.putExtra("Visit_user_id",visit_user_id);
                                startActivity(profileIntent);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FiendFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                        FiendFriendViewHolder viewHolder=new FiendFriendViewHolder(view);

                        return viewHolder;
                    }
                };

        findfriendRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    public static class FiendFriendViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;
        public FiendFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.user_profile_image);


        }
    }
}