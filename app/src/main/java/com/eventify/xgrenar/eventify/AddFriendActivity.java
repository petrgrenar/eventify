package com.eventify.xgrenar.eventify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.Marker;

public class AddFriendActivity extends AppCompatActivity {

    private EditText searchField;
    private ImageButton searchBtn;
    private RecyclerView resultList;

    FirebaseServer firebaseServer;

    private DatabaseReference usersReference;
    private DatabaseReference friendsReference;
    private boolean isfriend = false;
    private DatabaseReference meetingPointReference;

    private MeetingPoint meetingPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Přidat přítele");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        firebaseServer = new FirebaseServer(this);

        usersReference = FirebaseDatabase.getInstance().getReference("users");
        friendsReference = usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
        meetingPointReference = usersReference;

        searchField = (EditText) findViewById(R.id.search_field);
        searchBtn = (ImageButton) findViewById(R.id.search_btn);
        resultList = (RecyclerView) findViewById(R.id.result_list);
        resultList.setHasFixedSize(true);
        resultList.setLayoutManager(new LinearLayoutManager(this));

        firebaseUserSearch("");

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchText = searchField.getText().toString();
                firebaseUserSearch(searchText);
            }
        });
    }


    private void firebaseUserSearch(String searchText) {

        final Query firebaseSearchQuery = usersReference.orderByChild("email").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(firebaseSearchQuery,User.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<User, UsersViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new UsersViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull User model) {
                holder.setDetails(model.getEmail());
                final String ue = model.getEmail();
                final User user = new User();
                user.setEmail(model.getEmail());
                user.setLatitude(model.getLongitude());
                meetingPointReference = FirebaseDatabase.getInstance().getReference().child("users");
                meetingPointReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user1 = snapshot.getValue(User.class);
                            if (user1.getEmail().equals(ue)){
                                meetingPoint = snapshot.child("meeting_point").getValue(MeetingPoint.class);
                                Log.i("Shoda", "shodaaa");
                                if (meetingPoint != null){
                                    user.setMeetingPoint(meetingPoint);

                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.view.setOnClickListener(new View.OnClickListener() {
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
                            if (isfriend == false){
                                firebaseServer.addDataToFirebase(friendsReference, null, user, new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        finish();
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
                    }
                });
            }
        };
        resultList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        View view;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDetails(String userEmail){
            TextView user_email = (TextView) view.findViewById(R.id.email_view);

            user_email.setText(userEmail);
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
