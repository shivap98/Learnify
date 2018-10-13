package com.shiv.learnify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qhutch.bottomsheetlayout.BottomSheetLayout;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private double latitude;
    private double longitude;
    private ImageButton currentLocationButton;

    private Button beaconSelect; //TODO: replace with actual beacons

    private BottomSheetLayout bottomSheet;
    private Switch beaconSwitch;
    private ConstraintLayout beaconLayout;
    private TextView beaconStatus;
    public HashSet<Marker> markersList;
    private Marker currentMarker;
    private String beaconKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentLocationButton = findViewById(R.id.currentLocation);
        beaconSelect = findViewById(R.id.beaconSelect);
        bottomSheet = findViewById(R.id.bottomSheetLayout);
        beaconSwitch = findViewById(R.id.beaconSwitch);
        beaconLayout = findViewById(R.id.beaconLayout);
        beaconStatus = findViewById(R.id.beaconStatus);
        markersList = new HashSet<>();


        bottomSheet.setVisibility(View.GONE);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheet.collapse();
                getLocation();
            }
        });

        beaconSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheet.collapse();
                //TODO: replace this with function to get Beacon from latlng if exists


                //TODO: populate bottom sheet layout with values

                openBottomSheet();
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
                    //TODO:add the pop up where the student info is entered
                    //TODO: also add the pop up for the beacon specific info
                    //this is temporary right now
                    ArrayList<String> courses = new ArrayList<>();
                    courses.add("CS 250");
                    courses.add("CS 251");
                    courses.add("CS 291");
                    courses.add("MA 261");
                    courses.add("ECON 251");
                    courses.add("HIST 104");
                    Student student = new Student("Kartik", "kk@mm.com", 123456789, courses, null);
                    Beacon beacon = new Beacon(student, "CS 251", new LatLng(latitude, longitude), "Midterm 2", "Help me please");

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
                    currentMarker.remove();
                    markersList.remove(currentMarker);
                    currentMarker = null;
                    DatabaseReference dr = FirebaseDatabase.getInstance().getReference();

                    //TODO: remove the hard core course name and add it from the pop up for beacon info thing
                    dr.child("universities").child("michigan").child("CS 251").child(beaconKey).removeValue();


                }
            }
        });

        getLocation();




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
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 18));
        } catch (NullPointerException e) {
            Log.i("MapView", "Map not ready yet");
        }
    }
}
