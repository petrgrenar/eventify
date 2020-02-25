package com.eventify.xgrenar.eventify;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AddTicketActivity extends AppCompatActivity {

    private Button scanQr, checkTicket;
    private TextView resultText;
    private EditText ticketID;
    private IntentIntegrator qrscan;
    private DatabaseReference addTicketReference;
    private FirebaseServer firebaseServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ticket);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Přidej vstupenku");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        scanQr = (Button) findViewById(R.id.scan_qr);
        checkTicket = (Button) findViewById(R.id.check_ticket);
        resultText = (TextView) findViewById(R.id.result_text);
        ticketID = (EditText) findViewById(R.id.ticket_id);

        qrscan = new IntentIntegrator(this);

        scanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrscan.setBeepEnabled(false);
                qrscan.initiateScan();
            }
        });

        checkTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findTicket(ticketID.getText().toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
            else {
                resultText.setText("Scanned: " + result.getContents());
                findTicket(result.getContents());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void findTicket(final String id) {
        final DatabaseReference actionReference = FirebaseDatabase.getInstance().getReference().child("actions");
        actionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("Action: ", snapshot.getKey());
                    actionReference.child(snapshot.getKey()).child("tickets").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot ds) {
                            for (DataSnapshot s : ds.getChildren()) {
                                Log.d("Ticket: ", s.getValue().toString());
                                Ticket t = s.getValue(Ticket.class);
                                Log.d("Ticket: ", t.getName() + " - " + t.getUrl());
                                if (id.equals(t.getName())) {
                                    Log.d("Ticket: ", "Juhuhuuu máme shodnou vstupenku");
                                    addTicketToUser(t.getUrl(), snapshot.getKey());
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError de) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addTicketToUser(String url, String action) {
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("image").setValue(url);
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("action").setValue(action);
    }
}
