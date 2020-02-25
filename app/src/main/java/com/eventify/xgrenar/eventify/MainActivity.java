package com.eventify.xgrenar.eventify;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.getbase.floatingactionbutton.FloatingActionButton;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, NavigationView.OnNavigationItemSelectedListener  {

    private MapView mapView;
    private MapboxMap mapboxMap;

    private LocationComponent locationComponent;    //stara se o tecku na mape na zaklade aktualni polohy
    private PermissionsManager permissionsManager;  //trida starajici se o povoleni od uzivatele
    private LocationEngine locationEngine;
    private Style mapStyle;                         //styl pro to aby se vygtvorila pozice

    private List<User> users;                       //list pro nacitane uzivatele
    private List<LatLng> locations;                 //Pro ulozeni bodu na ktere se pak bude zoomovat
    private List<Marker> placeMarkers;                    //List mist na akci
    private List<Marker> meetingPoints;                    //List mist na akci

    private boolean bbflag = true;
    private boolean cctvflag = true;
    private FloatingActionButton hamburger;
    private FloatingActionButton cctv;
    private FloatingActionButton bigbrother;
    private FloatingActionButton findfriend;
    private FloatingActionButton findplace;
    private FloatingActionButton centerme;

    private Button logout;

    private DrawerLayout drawer;

    private FirebaseServer firebaseServer;
    private DatabaseReference usersReference;
    private DatabaseReference locationReference;
    private DatabaseReference friendsReference;
    private DatabaseReference lineup;
    private DatabaseReference meetingPointReference;
    private DatabaseReference currentUserReference;


    private final int THIRTY_SECONDS = 30000;
    private Handler handler;

    private BeaconUtility beaconUtility;

    //promenne k meetingu
    private EditText et;
    private TimePicker ed;
    private String txt;
    private LatLng meetingPoint;
    private String meetingPointDate;

    private User currentUser;

    private Marker markerMeetingPoint;
    private Marker friendMarker;
    private LatLng friendPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoicGV0cmdyZW5hciIsImEiOiJjanNvdXVyaTUwb2ZuNDNxbG9idWZzeWVrIn0.sPCk87-3ND2Bk3cZFYY75w");  //Klic na mapy od mapboxu
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        permissionsManager = new PermissionsManager(this);

        locations = new ArrayList<LatLng>();
        placeMarkers = new ArrayList<Marker>();
        meetingPoints = new ArrayList<Marker>();

        firebaseServer = new FirebaseServer(this);
        usersReference = firebaseServer.getDatabaseReference().child("users");
        locationReference = firebaseServer.getDatabaseReference().child("locations");
        friendsReference = usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
        getCurrentUser();

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

        handler = new Handler();

        hamburger = (FloatingActionButton) findViewById(R.id.fab_hamburger);
        cctv = (FloatingActionButton) findViewById(R.id.cctv);
        bigbrother = (FloatingActionButton) findViewById(R.id.bigbrother);
        findfriend = (FloatingActionButton) findViewById(R.id.findfriend);
        findplace = (FloatingActionButton) findViewById(R.id.findplace);
        centerme = (FloatingActionButton) findViewById(R.id.fab_centerme);

        logout = (Button) findViewById(R.id.logout_button);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        beaconUtility = new BeaconUtility(this);
        beaconUtility.addBeacon(new BeaconDefinition(34377, 5781) {
            @Override
            public void execute() {
                Log.i("iBeacon", "Vstup -> Jsem u vstupu");
                startActivity(new Intent(MainActivity.this, TicketActivity.class));
            }
        });
        beaconUtility.addBeacon(new BeaconDefinition(40848, 9215) {
            @Override
            public void execute() {
                Log.i("iBeacon", "WC -> Jsem u WC");
                showNotification("WC alert!","Ve tvé blízkosti se nachází WC -> Využij toho :)");
            }
        });
        beaconUtility.addBeacon(new BeaconDefinition(43937, 42195) {
            @Override
            public void execute() {
                Log.i("iBeacon", "Stánek -> Jsem u stánku");
                showNotification("Výhra","Právě ti nabízíme 2 + 1 pivo zdarma.");
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        hamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.START);
            }
        });

        cctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "cctv", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if (cctvflag){
                    usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cctv").setValue(0);
                    changeCctv();
                    cctvflag = false;
                }
                else if (!cctvflag){
                    usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cctv").setValue(1);
                    changeCctv();
                    cctvflag = true;
                }
            }
        });

        bigbrother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "bigbrother", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if (bbflag){
                    bigbrother.setIcon(R.drawable.eye_off_white);
                    bigbrother.setTitle(getString(R.string.bigbrother2));
                    bbflag = false;
                }
                else if (!bbflag){
                    bigbrother.setIcon(R.drawable.eye_white);
                    bigbrother.setTitle(getString(R.string.bigbrother));
                    bbflag = true;
                }
            }
        });

        findfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "find friend", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                startActivity(new Intent(MainActivity.this, FriendsActivity.class));
            }
        });

        findplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "add group", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                startActivity(new Intent(MainActivity.this, PlacesActivity.class));
            }
        });

        centerme.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                LatLng ll = new LatLng();
                ll.setLatitude(locationComponent.getLastKnownLocation().getLatitude());
                ll.setLongitude(locationComponent.getLastKnownLocation().getLongitude());
                zoomToLocation(ll);
            }
        });
        Log.d("TOAST", "Volam ShowFriends");
    }

    private void showPlaces(String event) {
        Log.d("Event", "User event: " + event);
        lineup = firebaseServer.getDatabaseReference().child("actions").child(event).child("places");
        lineup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (placeMarkers.size() > 0) {
                    Log.d("PlaceList", "Neco tam je");
                    for (Marker marker : placeMarkers) {
                        mapboxMap.removeMarker(marker);
                    }
                    placeMarkers.clear();
                }
                else {
                    Log.d("PlaceList", "Nic tam neni");
                }
                for (DataSnapshot placeDS: dataSnapshot.getChildren()){
                    Place place = placeDS.getValue(Place.class);
                    LatLng position = new LatLng();
                    position.setLatitude(place.getLatitude());
                    position.setLongitude(place.getLongitude());
                    Marker marker = MarkerManager.addMarkerToMap(MainActivity.this, mapboxMap, position, ResourceUtility.getResourceDrawableID(MainActivity.this, place.getType()), place.getType());
                    placeMarkers.add(marker);
                    Log.d("Place", "Place: " + place.getType());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showFriends(){
        friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("TOAST", "Prochazim friendy");
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final User friend = snapshot.getValue(User.class);
                    Log.d("TOAST", "Kamarad: " + friend.getEmail());
                    showFriend(friend.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showFriend(final String email) {
        Log.d("SHOWRIEND", "START");
        firebaseServer.getDatabaseReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.i("Users", dataSnapshot.toString());
                for (DataSnapshot userDS: dataSnapshot.getChildren()){
                    User user = userDS.getValue(User.class);
                    if (user.getEmail().equals(email)){
                        Log.i("Users", "Je Kamarad: " + user.getEmail() + " cctv: " + user.getCctv() + " - " + email + " KEY: " + userDS.getKey());
                        if (user.getCctv() == 1){
                            LatLng point = new LatLng();
                            point.setLatitude(user.getLatitude());
                            point.setLongitude(user.getLongitude());
                            Log.i("Users", "Je Kamarad: " + point);
                            if (friendMarker != null){
                                if (point != friendPosition){
                                    mapboxMap.removeAnnotation(friendMarker);
                                    friendMarker = MarkerManager.addMarkerToMap(MainActivity.this, mapboxMap, point, user.getEmail());
                                }

                            } else {
                                friendMarker = MarkerManager.addMarkerToMap(MainActivity.this, mapboxMap, point, user.getEmail());
                                user.setMarker(friendMarker);
                            }
                            friendPosition = point;

                        }
                        else {
                            Log.d("SHOWRIEND", "Kamarad cctv: " + user.getCctv());
                            if (user.getMarker() != null){
                                mapboxMap.removeMarker(user.getMarker());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void changeCctv(){
        usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cctv").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("CHANGECCTV", "MENIM CCTV");
                int cctvvalue = dataSnapshot.getValue(Integer.class);
                if (cctvvalue == 1){
                    cctv.setIcon(R.drawable.cctv_white);
                    cctv.setTitle(getString(R.string.cctv));
                }
                else {
                    cctv.setIcon(R.drawable.cctv_off_white);
                    cctv.setTitle(getString(R.string.cctv2));
                }
                Log.d("CCTV", "" + cctvvalue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("MissingPermission")
    public void updateMyLocation(DatabaseReference usersReference, DatabaseReference locationReference, LocationComponent locationComponent){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersReference = usersReference.child(uid);
        usersReference.child("latitude").setValue(locationComponent.getLastKnownLocation().getLatitude());
        usersReference.child("longitude").setValue(locationComponent.getLastKnownLocation().getLongitude());
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        String key = null;
        key = locationReference.push().getKey();
        LatLng ll = new LatLng();
        ll.setLatitude(locationComponent.getLastKnownLocation().getLatitude());
        ll.setLongitude(locationComponent.getLastKnownLocation().getLongitude());
        ll.setAltitude(locationComponent.getLastKnownLocation().getAltitude());
        locationReference.child(uid).child(key).setValue(ll);
        locationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void scheduleSendLocation() {
        handler.postDelayed(new Runnable() {
            public void run() {
                updateMyLocation(usersReference, locationReference, locationComponent);
                handler.postDelayed(this, THIRTY_SECONDS);
            }
        }, THIRTY_SECONDS);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.nav_1:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.nav_2:
                startActivity(new Intent(MainActivity.this, LineupActivity.class));
                break;
            case R.id.nav_3:
                startActivity(new Intent(MainActivity.this, PlacesActivity.class));
                break;
            case R.id.nav_4:
                startActivity(new Intent(MainActivity.this, FriendsActivity.class));
                break;
            case R.id.nav_5:
                startActivity(new Intent(MainActivity.this, TicketActivity.class));
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.START)){
            drawer.closeDrawer(Gravity.START);
        }
        else {
            super.onBackPressed();
        }
    }

    private User getCurrentUser() {
        currentUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return currentUser;
    }
    private void showMeetingPointsFriends() {
        friendsReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
        friendsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MeetingPoint meetingPoint = snapshot.child("meetingPoint").getValue(MeetingPoint.class);
                    User user1 = snapshot.getValue(User.class);
                    LatLng point = new LatLng();
                    if (meetingPoint != null){
                        point.setLongitude(meetingPoint.getLongitude());
                        point.setLatitude(meetingPoint.getLatitude());
                        String a = "";
                        String b = "";
                        if (meetingPoint.getHour()<10){
                            a = ("0" + String.valueOf(meetingPoint.getHour()));
                        } else {
                            a = (String.valueOf(meetingPoint.getHour()));
                        }
                        a = a.concat(":");
                        if (meetingPoint.getMin()<10){
                            a = a.concat("0" + String.valueOf(meetingPoint.getMin()));
                        } else {
                            a = a.concat(String.valueOf(meetingPoint.getMin()));
                        }
                        if (meetingPoint.getName().length() != 0){
                            a = a.concat(" - ");
                        }
                        b = a.concat(meetingPoint.getName());
                       Marker marker = MarkerManager.addMarkerToMap(MainActivity.this, mapboxMap, point,b + "\n" + user1.getEmail());
                        //Marker marker = MarkerManager.addMarkerToMap(MainActivity.this, mapboxMap, point,"akce" + "\n" + "email");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private Marker showMeetingPoints() {

        meetingPointReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("meeting_point");
        meetingPointReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MeetingPoint meetingPoint = dataSnapshot.getValue(MeetingPoint.class);
                LatLng point = new LatLng();
                if (meetingPoint != null) {
                    point.setLongitude(meetingPoint.getLongitude());
                    point.setLatitude(meetingPoint.getLatitude());
                    String a = "";
                    String b = "";
                    if (meetingPoint.getHour()<10){
                        a = ("0" + String.valueOf(meetingPoint.getHour()));
                    } else {
                        a = (String.valueOf(meetingPoint.getHour()));
                    }
                    a = a.concat(":");
                    if (meetingPoint.getMin()<10){
                        a = a.concat("0" + String.valueOf(meetingPoint.getMin()));
                    } else {
                        a = a.concat(String.valueOf(meetingPoint.getMin()));
                    }
                    if (meetingPoint.getName().length() != 0){
                        a = a.concat(" - ");
                    }
                    b = a.concat(meetingPoint.getName());

                    markerMeetingPoint = MarkerManager.addMarkerToMap(MainActivity.this, mapboxMap, point,b + "\n" + getCurrentUser().getEmail());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return markerMeetingPoint;
    }

    private void createMeetingPoint() {
        mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public boolean onMapLongClick(@NonNull final LatLng point) {
                int bo = 0; //pomocna promenna aby byl textbox nalezite vyplneny
                meetingPoint = point;

                final AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                ab.setTitle("Zadej název setkání!");
                et = new EditText(MainActivity.this);


                ab.setView(et);

                ab.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txt = et.getText().toString();

                        AlertDialog.Builder bb = new AlertDialog.Builder(MainActivity.this);
                        bb.setTitle("Zadej čas setkání!");
                        ed = new TimePicker(MainActivity.this);

                        bb.setView(ed);

                        bb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (ed.getHour()<10){
                                    meetingPointDate = ("0" + String.valueOf(ed.getHour()));
                                    int hour = Integer.parseInt(meetingPointDate);
                                } else {
                                    meetingPointDate = (String.valueOf(ed.getHour()));
                                    int hour = Integer.parseInt(meetingPointDate);
                                }
                                if (ed.getMinute()<10){
                                    meetingPointDate = meetingPointDate.concat("0" + String.valueOf(ed.getMinute()));
                                    int minute = Integer.parseInt(meetingPointDate);
                                } else {
                                    meetingPointDate = meetingPointDate.concat(String.valueOf(ed.getMinute()));
                                    int minute = Integer.parseInt(meetingPointDate);
                                }
                                if (txt.length() != 0){
                                    meetingPointDate = meetingPointDate.concat(" - ");
                                }
                                txt = meetingPointDate.concat(txt);

                                MeetingPoint pointMeeting = new MeetingPoint();
                                pointMeeting.setHour(ed.getHour());
                                pointMeeting.setMin(ed.getMinute());
                                pointMeeting.setLatitude(meetingPoint.getLatitude());
                                pointMeeting.setLongitude(meetingPoint.getLongitude());
                                pointMeeting.setName(et.getText().toString());

                                if (markerMeetingPoint != null){
                                    mapboxMap.removeAnnotation(markerMeetingPoint);
                                }
                                
                                meetingPointReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("meeting_point");
                                firebaseServer.addDataToFirebase(meetingPointReference, pointMeeting, new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        bb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                txt = "Sraz nema nazev!";
                            }
                        });

                        AlertDialog b = bb.create();
                        b.show();

                    }
                });

                ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"Akce zrušena uživatelem!", Toast.LENGTH_SHORT);
                    }
                });

                AlertDialog a = ab.create();
                a.show();

                return false;
            }
        });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        //Nepjrve musi byt nactenej styl
        mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapStyle = style;
                setUpMyLocation();
                changeCctv();
                getCurrentUser();
                updateMyLocation(usersReference, locationReference, locationComponent);
                scheduleSendLocation();
                createMeetingPoint();
            }
        });
        showFriends();
        showMeetingPoints();
        showMeetingPointsFriends();
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    //Toto musi byt vsechno kvuli mapboxu
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        beaconUtility.startMonitoring("WC", null, 40848, 9215);
        beaconUtility.startMonitoring("Stanek", null, 43937, 42195);
        beaconUtility.startMonitoring("Vstup", null, 34377, 5781);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        beaconUtility.stopMonitoring();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted){
            setUpMyLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpMyLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this)){
            LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(this)
                    .foregroundDrawable(R.drawable.cctv)
                    .backgroundDrawable(R.drawable.cctv)
	                .build();

            locationComponent = mapboxMap.getLocationComponent();

            LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                    .builder(this, mapStyle)
                    .locationComponentOptions(locationComponentOptions)
                    .build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            locationComponent.activateLocationComponent(this, mapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);   //Pri zmene lokace by se mela pohnout mapa
            enableLocationUpdates(10000);
            LatLng ll = new LatLng();
            ll.setLatitude(locationComponent.getLastKnownLocation().getLatitude());
            ll.setLongitude(locationComponent.getLastKnownLocation().getLongitude());
            zoomToLocation(ll);
        }
        else {
            permissionsManager.requestLocationPermissions(this);
        }
    }

    //Zoomuje na jeden objekt
    private void zoomToLocation(LatLng latLng){
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mapboxMap.animateCamera(cameraUpdate);
    }

    //Zoomuje na vice objektu
    private void zoomToLocation(List<LatLng> locations){
        if (locations != null && locations.size() > 1) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.includes(locations);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 30);
            mapboxMap.animateCamera(cameraUpdate);
        }
    }

    @SuppressLint("MissingPermission")
    private void enableLocationUpdates(long interval){
        if (permissionsManager.areLocationPermissionsGranted(this)){
            locationEngine = LocationEngineProvider.getBestLocationEngine(this);
            LocationEngineRequest.Builder builder = new LocationEngineRequest.Builder(interval);
            builder.setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY);
            locationEngine.requestLocationUpdates(builder.build(), new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    //Toast.makeText(MainActivity.this, "Toto je test", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            }, Looper.myLooper());
        }
        else {
            //info pro uzivatele ze nema povolenou lokaci
        }
    }
}
