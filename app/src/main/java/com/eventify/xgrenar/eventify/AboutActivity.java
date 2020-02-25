package com.eventify.xgrenar.eventify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AboutActivity extends AppCompatActivity {

    private TextView event_time;
    private TextView event_place;
    private TextView event_contact;
    private TextView event_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("O akci");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        event_time = (TextView) findViewById(R.id.event_time);
        event_place = (TextView) findViewById(R.id.event_place);
        event_contact = (TextView) findViewById(R.id.event_contact);
        event_info = (TextView) findViewById(R.id.event_info);

        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("action").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String event = dataSnapshot.getValue(String.class);
                if (event != null) {
                    showEventInfo(event);
                    Log.d("Action: ", event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showEventInfo(final String event) {
        FirebaseDatabase.getInstance().getReference().child("actions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("Event: ", snapshot.getKey());
                    if (event.equals(snapshot.getKey())) {
                        Event e = snapshot.getValue(Event.class);
                        event_time.setText(e.getDate());
                        event_place.setText(e.getPlace());
                        event_contact.setText(e.getContact());
                        event_info.setText(e.getDescription());
                        getSupportActionBar().setTitle("O akci - " + e.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
