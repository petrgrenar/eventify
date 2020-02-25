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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class PlacesActivity extends AppCompatActivity {

    private DatabaseReference places;
    private FirebaseServer firebaseServer;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Místa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.places_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseServer = new FirebaseServer(this);
        firebaseServer.getDatabaseReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("action").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String event = dataSnapshot.getValue(String.class);
                if (event != null) {
                    showPlaces(event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showPlaces(String event) {
        places = firebaseServer.getDatabaseReference().child("actions").child(event).child("places");
        Log.d("PLACES", "ref: " + places);

        FirebaseRecyclerOptions<Place> options = new FirebaseRecyclerOptions.Builder<Place>()
                .setQuery(places,Place.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Place, PlacesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Place, PlacesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PlacesViewHolder holder, int position, @NonNull Place model) {
                holder.setDetails(getApplicationContext(), model.getType());
            }

            @NonNull
            @Override
            public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new PlacesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.places_layout, viewGroup, false));
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
