package com.shiv.learnify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.qhutch.bottomsheetlayout.BottomSheetLayout;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap map;
    private double latitude;
    private double longitude;
    private ImageButton currentLocationButton;

    private Button beaconSelect; //TODO: replace with actual beacons

    private BottomSheetLayout bottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentLocationButton = findViewById(R.id.currentLocation);
        beaconSelect = findViewById(R.id.beaconSelect);
        bottomSheet = findViewById(R.id.bottomSheetLayout);
        bottomSheet.setVisibility(View.GONE);
        currentLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bottomSheet.collapse();
                getLocation();
            }
        });

        beaconSelect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bottomSheet.collapse();
                //TODO: replace this with function to get Beacon from latlng if exists
                ArrayList<String> courses = new ArrayList<>();
                courses.add("CS 250");
                courses.add("CS 251");
                courses.add("CS 291");
                courses.add("MA 261");
                courses.add("ECON 251");
                courses.add("HIST 104");
                Student student = new Student("Shiv", "shiv@shiv.com", 123456789, courses, null);
                Beacon beacon = new Beacon(student, "CS 250", new LatLng(40.0, 80.0), "Midterm 2", "Help me please");

                //TODO: populate bottom sheet layout with values

                openBottomSheet();
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
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                closeBottomSheet();
                System.out.println("map click");
            }
        });
        setMarker();
    }

    /**
     * Gets location of the user, taking care of all the permissions
     */
    void getLocation()
    {
        while(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        LocationListener locationListener = new LocationListener()
        {
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s)
            {

            }

            @Override
            public void onProviderDisabled(String s)
            {

            }

            @Override
            public void onLocationChanged(Location location)
            {
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

    void closeBottomSheet()
    {
        bottomSheet.setVisibility(View.GONE);
        bottomSheet.animate().scaleY(0);
        bottomSheet.collapse();
    }

    void openBottomSheet()
    {
        bottomSheet.collapse();
        bottomSheet.animate().scaleY(1);
        bottomSheet.setVisibility(View.VISIBLE);
    }

    /**
     * OnClick for R.id.currentLocationButton button
     */
    public void setMarker()
    {
        try
        {
            LatLng place = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(place).title("Current Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 18));
        }
        catch(NullPointerException e)
        {
            Log.i("MapView", "Map not ready yet");
        }
    }
}
