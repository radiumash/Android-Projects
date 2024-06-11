package com.rightone.SearchRight;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class NewMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    String lat, longi, bName;

    private GoogleMap mMap;
    LocationManager locationManager;
    String locationProvider;
    LatLng myLocation;
    Button done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            lat = extra.getString("LAT");
            longi = extra.getString("LONG");
            bName = extra.getString("LOCATION");
        }

        done = findViewById(R.id.send);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("LAT", String.valueOf(myLocation.latitude));
                intent.putExtra("LONG", String.valueOf(myLocation.longitude));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(!lat.equals("") && !longi.equals("")) {
            setMap(lat,longi,bName);
        }else{
            this.initializeLocationManager();
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                myLocation = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("Current Position").draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(myLocation,16);
                mMap.animateCamera(zoom);
            }
        });


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.e("DRAG11", (String.valueOf(marker.getPosition().latitude)));
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Log.e("DRAG12", (String.valueOf(marker.getPosition().latitude)));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.e("DRAG13", (String.valueOf(marker.getPosition().latitude)));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void initializeLocationManager() {
        try {
            //get the location manager
            this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //define the location manager criteria
            Criteria criteria = new Criteria();

            this.locationProvider = locationManager.getBestProvider(criteria, false);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //mMap.setMyLocationEnabled(true);
                //Location location = locationManager.getLastKnownLocation(locationProvider);
                Location location = getLastKnownLocation();
                //initialize the location
                if (location != null) {
                    onLocationChanged(location);
                }else{
                    showSettingsAlert("GPS");
                }
            } else {
                // Show rationale and request permission.
                Log.i("PERMISSION", "Unable to get permission.");
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = null;
        try {
            providers = mLocationManager.getProviders(true);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        Location bestLocation = null;
        Location l = null;
        for (String provider : providers) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                l = mLocationManager.getLastKnownLocation(provider);
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public void showSettingsAlert(String provider) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                NewMapsActivity.this);

        alertDialog.setTitle(provider + " SETTINGS");

        alertDialog
                .setMessage(provider + " is not enabled! Want to go to settings menu?");

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        NewMapsActivity.this.startActivity(intent);
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public void onLocationChanged(Location location) {
        Log.i("called", "onLocationChanged");

        //when the location changes, update the map by zooming to the location
        myLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Choose your marker").draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(myLocation,16);
        mMap.animateCamera(zoom);
    }

    public void setMap(String Lat, String Long, String BName){
        myLocation = new LatLng(Double.parseDouble(Lat),Double.parseDouble(Long));
        mMap.addMarker(new MarkerOptions().position(myLocation).title(BName).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(myLocation,16);
        mMap.animateCamera(zoom);
    }
}
