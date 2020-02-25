package com.eventify.xgrenar.eventify;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseServer {

    private DatabaseReference firebaseDatabase;
    private Context context;

    public FirebaseServer(Context context) {
        this.context = context;
        FirebaseApp.initializeApp(context);
        firebaseDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://eventify-eventapp.firebaseio.com/");
    }

    public DatabaseReference getDatabaseReference(){
        return firebaseDatabase;
    }

    public void addDataToFirebase(DatabaseReference databaseReference, String key, Object data, ValueEventListener valueEventListener){
        String firebaseKey = null;
        if (key != null){
            firebaseKey = key;
        } else {
            firebaseKey = databaseReference.push().getKey();
        }
        databaseReference.child(firebaseKey).setValue(data);    //zapisuju do child + poduroven klicem definovana..takze je to napr users->klic->data
        databaseReference.addValueEventListener(valueEventListener);
    }

    public void addDataToFirebase(DatabaseReference databaseReference, Object data, ValueEventListener valueEventListener){

        databaseReference.setValue(data);    //zapisuju do child + poduroven klicem definovana..takze je to napr users->klic->data
        databaseReference.addValueEventListener(valueEventListener);
    }
}
