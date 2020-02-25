package com.eventify.xgrenar.eventify;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView resultList;

    FirebaseServer firebaseServer;
    private DatabaseReference friendsReference;
    private boolean isfriend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Přátele");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        resultList = (RecyclerView) findViewById(R.id.result_list);
        resultList.setHasFixedSize(true);
        resultList.setLayoutManager(new LinearLayoutManager(this));

        firebaseServer = new FirebaseServer(this);
        friendsReference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FriendsActivity.this, AddFriendActivity.class));
            }
        });

        firebaseUserSearch();
    }

    private void firebaseUserSearch() {

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(friendsReference,User.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<User, FriendsActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, FriendsActivity.UsersViewHolder>(options) {

            @NonNull
            @Override
            public FriendsActivity.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FriendsActivity.UsersViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsActivity.UsersViewHolder holder, int position, @NonNull User model) {
                holder.setDetails(model.getEmail());
                final String ue = model.getEmail();
                final User user = new User();
                user.setEmail(model.getEmail());
                holder.view.findViewById(R.id.delete_friend).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    //Snackbar.make(v, "Friend: " + ue, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String key = null;
                            User u;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                u = snapshot.getValue(User.class);
                                key = snapshot.getKey();
                                Log.d("TOAST", u.getEmail() + " - " + ue);
                                if (u.getEmail().equals(ue)){
                                    Log.d("TOAST", "Je tam, tak nepridavam");
                                    isfriend = true;
                                }
                            }
                            if (isfriend == true){
                                friendsReference.child(key).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    }
                });
            }
        };
        resultList.setAdapter(firebaseRecyclerAdapter);
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        View view;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDetails(String userEmail){
            TextView user_email = (TextView) view.findViewById(R.id.email_view);
            ImageView delete = (ImageView) view.findViewById(R.id.delete_friend);

            user_email.setText(userEmail);
            delete.setImageResource(R.drawable.delete);
        }

        public void setImage(boolean f) {
            ImageView friend_box = (ImageView) view.findViewById(R.id.delete_friend);
            Log.d("TOAST:::SETIMAGE", "Zobrazuju: " + f);
            if (f == true) {
                friend_box.setImageResource(R.drawable.checkbox_marked_circle_outline);
            }
            else {
                friend_box.setImageResource(R.drawable.checkbox_blank_circl_outline);
            }
        }
    }
}


