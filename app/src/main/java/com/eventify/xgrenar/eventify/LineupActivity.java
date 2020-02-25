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

public class LineupActivity extends AppCompatActivity {

    private DatabaseReference lineup;
    private FirebaseServer firebaseServer;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lineup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.lineup_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseServer = new FirebaseServer(this);
        firebaseServer.getDatabaseReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("action").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String event = dataSnapshot.getValue(String.class);
                if (event != null) {
                    showLineup(event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showLineup(String event) {
        lineup = firebaseServer.getDatabaseReference().child("actions").child(event).child("lineup");
        Log.d("LINEUP", "ref: " + lineup);

        FirebaseRecyclerOptions<Lineup> options = new FirebaseRecyclerOptions.Builder<Lineup>()
                .setQuery(lineup,Lineup.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Lineup, LineupViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Lineup, LineupViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LineupViewHolder holder, int position, @NonNull Lineup model) {
                holder.setDetails(getApplicationContext(), model.getDsc(), model.getTime());
            }

            @NonNull
            @Override
            public LineupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new LineupViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.lineup_layout, viewGroup, false));
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
