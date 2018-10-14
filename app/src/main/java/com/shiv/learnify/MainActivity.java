package com.shiv.learnify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qhutch.bottomsheetlayout.BottomSheetLayout;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    public ArrayList<Marker> markersList;
    Student currentStudent;
    //bottom sheet fields
    ImageView profilePic;
    TextView studentName;
    TextView courseName;
    TextView beaconTitle;
    TextView descriptionText;
    private GoogleMap map;
    private double latitude;
    private double longitude;
    private ImageButton currentLocationButton;
    private BottomSheetLayout bottomSheet;
    private Switch beaconSwitch;
    private ConstraintLayout beaconLayout;
    private TextView beaconStatus;
    private ArrayList<Beacon> beaconsList;
    private Marker currentMarker;
    private String beaconKey;
    private String uid;
    private String currentBeaconCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentLocationButton = findViewById(R.id.currentLocation);
        bottomSheet = findViewById(R.id.bottomSheetLayout);
        beaconSwitch = findViewById(R.id.beaconSwitch);
        beaconLayout = findViewById(R.id.beaconLayout);
        beaconStatus = findViewById(R.id.beaconStatus);
        markersList = new ArrayList<>();
        beaconsList = new ArrayList<>();

        uid = getIntent().getExtras().getString("uid");

        bottomSheet.setVisibility(View.GONE);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheet.collapse();
                getLocation();
            }
        });

        profilePic = findViewById(R.id.profilePic);
        studentName = findViewById(R.id.studentName);
        courseName = findViewById(R.id.courseName);
        beaconTitle = findViewById(R.id.beaconTitle);
        descriptionText = findViewById(R.id.descriptionText);

        //getting the student from database using the uid

        DatabaseReference studentReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentStudent = dataSnapshot.getValue(Student.class);
                System.out.println(currentStudent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        beaconLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beaconSwitch.toggle();
            }
        });

        beaconSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    beaconLayout.setBackground(getDrawable(R.drawable.beacon_stroke_green));
                    beaconStatus.setText("On");

                    //adding the beacon for the current location and sending it to the database
                    closeBottomSheet();
                    getLocation();
                    //TODO: also add the pop up for the beacon specific info
                    //TODO: update currentBeaconCourse name as well
                    //this is temporary right now

                    currentBeaconCourse = "CS 252";

                    Beacon beacon = new Beacon(currentStudent, "CS 252", new CustomLatLng(latitude, longitude), "Lab03", "Raspberry PI set up");

                    beaconsList.add(beacon);
                    LatLng place = new LatLng(latitude, longitude);
//                    markersList.add(map.addMarker(new MarkerOptions().position(place).title("Current Location")));
                    currentMarker = map.addMarker(new MarkerOptions().position(place).title("Your Beacon"));
                    markersList.add(currentMarker);

                    DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
                    beaconKey = dr.push().getKey();


                    dr.child("universities").child("michigan").child(beacon.course).child(beaconKey).setValue(beacon);


                } else {
                    beaconLayout.setBackground(getDrawable(R.drawable.beacon_stroke_red));
                    beaconStatus.setText("Off");

                    DatabaseReference dr = FirebaseDatabase.getInstance().getReference();

                    beaconsList.clear();
                    dr.child("universities").child("michigan").child(currentBeaconCourse).child(beaconKey).removeValue();
                    currentMarker.remove();
                    markersList.remove(currentMarker);
                    currentMarker = null;
                    closeBottomSheet();

                }
            }
        });

        getLocation();

        //Database reference for listening to change in courses for particular uni only
        DatabaseReference courseReference = FirebaseDatabase.getInstance().getReference().child("universities")
                .child("michigan");

        ValueEventListener postListner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot courseBeacon : snapshot.getChildren()) {
                        Beacon b = courseBeacon.getValue(Beacon.class);
                        beaconsList.add(b);
                    }
                }

                showBeacons();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //linking the listener to reference
        courseReference.addValueEventListener(postListner);


    }

    /**
     * show beacons method
     * called everytime when there is a change in list of beacons in firebase
     */

    private void showBeacons() {
        for (int i = 0; i < markersList.size(); i++) {
            markersList.get(i).remove();
        }
        markersList.clear();
        for (int i = 0; i < beaconsList.size(); i++) {
            LatLng latLng = new LatLng(beaconsList.get(i).location.latitude, beaconsList.get(i).location.longitude);
            markersList.add(map.addMarker(new MarkerOptions().position(latLng).title(beaconsList.get(i).course)));
        }
    }


    /**
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                closeBottomSheet();
                System.out.println("map click");
            }
        });
        setMarker();

        //set on marker click for the selected marker and compare the title with the list
        //TODO: add the info related to that marker in the bottom sheet

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Beacon beacon = beaconsList.get(markersList.indexOf(marker));
                System.out.println(beacon.toString());

                populateBeaconBottomSheet(beacon);

                openBottomSheet();

                return false;
            }
        });
    }

    /**
     * Displays the beacon values in the bottom sheet when beacon is selected
     *
     * @param beacon, beacon selected
     */
    void populateBeaconBottomSheet(Beacon beacon) {
        //TODO: set photo in profile pic
        profilePic.setImageResource(R.mipmap.ic_launcher_round);
        studentName.setText(beacon.student.name);
        courseName.setText(beacon.course);
        beaconTitle.setText(beacon.title);
        descriptionText.setText(beacon.description);
    }

    /**
     * Gets location of the user, taking care of all the permissions
     */
    void getLocation() {
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

            @Override
            public void onLocationChanged(Location location) {
                // Previously mock location is cleared.
                // getLastKnownLocation(LocationManager.GPS_PROVIDER); will not return mock location.

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                setMarker();
            }

        };

        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    void closeBottomSheet() {
        bottomSheet.setVisibility(View.GONE);
        bottomSheet.animate().scaleY(0);
        bottomSheet.collapse();
    }

    void openBottomSheet() {
        bottomSheet.collapse();
        bottomSheet.animate().scaleY(1);
        bottomSheet.setVisibility(View.VISIBLE);
    }

    /**
     * OnClick for R.id.currentLocationButton button
     */
    public void setMarker() {
        try {
            LatLng place = new LatLng(latitude, longitude);
//            map.addMarker(new MarkerOptions().position(place).title("Current Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 15));
        } catch (NullPointerException e) {
            Log.i("MapView", "Map not ready yet");
        }
    }
}
