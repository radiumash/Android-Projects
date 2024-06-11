package com.rightone.ROChildWater;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    String locationProvider;
    LatLng myLocation;

    SessionManager session;
    JSONArray providers;
    String message="";
    SearchableEditText mService;
    String[] serviceArray;
    HashMap<Integer,String> serviceMap;
    ArrayAdapter<String> serviceArrayAdapter;

    String category_id, category_name, favourite = "";

    String city_id = "";
    Button mButtonCategory;
    GetProviders gp;
    PageInit pInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle extra = getIntent().getExtras();
        try {
            category_id = extra.getString("ID");
            category_name = extra.getString("NAME");
            favourite = extra.getString("FAVOURITE");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        session = new SessionManager(this);
        HashMap<String, String> user = session.getUserDetails();
        // name
        city_id = user.get(SessionManager.KEY_CID);

        mButtonCategory = findViewById(R.id.btCategory);
        mButtonCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ServiceActivity.class);
                // passing array index
                i.putExtra("ID", category_id);
                i.putExtra("NAME",category_name);
                i.putExtra("FAVOURITE","");
                startActivity(i);
            }
        });
        mService = findViewById(R.id.serviceName);
        mService.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("Select Service") && !s.toString().equals("")) {
                    setHomeMarker();
                    String service_id = String.valueOf(serviceMap.get(serviceArrayAdapter.getPosition(s.toString())));
                    gp = new GetProviders(city_id,service_id);
                    gp.execute((Void) null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        pInit = new PageInit();
        pInit.execute((Void) null);
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
        this.initializeLocationManager();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                myLocation = latLng;
                setHomeMarker();
                if(!mService.getText().toString().equals("Select Service") && !mService.getText().toString().equals("")) {
                    String service_id = String.valueOf(serviceMap.get(serviceArrayAdapter.getPosition(mService.getText().toString())));
                    gp = new GetProviders(city_id,service_id);
                    gp.execute((Void) null);
                }
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                try {
                    if (!marker.getTag().equals("")) {
                        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                        intent.putExtra("ID", marker.getTag().toString());
                        intent.putExtra("NAME", marker.getTitle());
                        startActivity(intent);
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
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
                MapsActivity.this);

        alertDialog.setTitle(provider + " SETTINGS");

        alertDialog
                .setMessage(provider + " is not enabled! Want to go to settings menu?");

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MapsActivity.this.startActivity(intent);
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
        //when the location changes, update the map by zooming to the location
        myLocation = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(myLocation).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.you_are_here));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(myLocation,16);
        mMap.animateCamera(zoom);
    }

    public void setHomeMarker(){
        mMap.clear();
        MarkerOptions marker = new MarkerOptions().position(myLocation).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.you_are_here));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(myLocation,16);
        mMap.animateCamera(zoom);
    }
    /**
     * Represents an asynchronous Resend OTP task
     */
    public class PageInit extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "executive");
                postDataParams.put("action", "get_services");
                postDataParams.put("cat_id", category_id);
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray services = rootJSON.getJSONObject("results").getJSONArray("services");

                        serviceArray = new String[services.length()+1];
                        serviceArray[0] = "Select Service";
                        serviceMap = new HashMap<>();

                        for(int i=0;i<services.length();i++){
                            serviceMap.put(i+1,services.getJSONObject(i).getString("id"));
                            serviceArray[i+1] = services.getJSONObject(i).getString("service_name");
                        }
                        serviceArrayAdapter = new ArrayAdapter(MapsActivity.this, android.R.layout.simple_list_item_1,serviceArray);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    if (serviceArrayAdapter != null) {
                        mService.setAdapter(serviceArrayAdapter);
                    }
                } else {
                    if (message != "") {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetProviders extends AsyncTask<Void, Void, Boolean> {

        String mCity, mService;
        GetProviders(String city, String service){
            mCity = city;
            mService = service;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;

            try{
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "get_providers_map");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("service_id", mService);
                postDataParams.put("city_id", mCity);
                postDataParams.put("lat", myLocation.latitude);
                postDataParams.put("lon", myLocation.longitude);


                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean successP = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (successP.equals(true)) {
                        providers = rootJSON.getJSONObject("results").getJSONArray("providers");
                    }else{
                        Toast.makeText(getApplicationContext(),"Something went wrong", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    if(providers != null){
                        for(int i=0;i<providers.length();i++){
                            Double lat = Double.parseDouble(providers.getJSONObject(i).get("latitude").toString());
                            Double lon = Double.parseDouble(providers.getJSONObject(i).get("longitude").toString());
                            String bName = providers.getJSONObject(i).get("business_name").toString();

                            if(!lat.toString().equals("") && !lon.toString().equals("")){
                                MarkerOptions marker;
                                LatLng location = new LatLng(lat,lon);
                                if(providers.getJSONObject(i).get("is_premium").toString().equals("1")){
                                    marker = new MarkerOptions().position(location).title(bName).icon(BitmapDescriptorFactory.fromResource(R.drawable.premium_marker));
                                }else{
                                    marker = new MarkerOptions().position(location).title(bName).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                }
                                Marker mM = mMap.addMarker(marker);
                                mM.setTag(providers.getJSONObject(i).get("id").toString());
                            }
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(myLocation,16);
                        mMap.animateCamera(zoom);
                    }
                } else {
                    if (message != "") {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}

