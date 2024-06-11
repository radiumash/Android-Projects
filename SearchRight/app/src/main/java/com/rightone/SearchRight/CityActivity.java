package com.rightone.SearchRight;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityActivity extends Activity {
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    SessionManager session;
    Button mButton;
    EditText mName, mMobile;
    SearchableEditText mCity;
    String[] cityArray;
    HashMap<Integer,String> cityMap;
    ArrayAdapter<String> cityArrayAdapt;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    GetCity gc;
    public String message;
    JSONObject uObject;
    String otp;
    String is_new;
    ProgressBar mDialog;
    SaveUser su;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        checkPermissionWrapper();
        displayLocationSettingsRequest(this);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());
        if(session.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), CategoryActivity.class));
            finish();
        }
        mName = findViewById(R.id.uName);
        mMobile = findViewById(R.id.mobile);
        mDialog = findViewById(R.id.progress_loader);

        mCity = findViewById(R.id.city);
        mButton = findViewById(R.id.mButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myCity = String.valueOf(cityMap.get(cityArrayAdapt.getPosition(mCity.getText().toString())));
                if(myCity.equals("null")) {
                    Toast.makeText(getApplicationContext(), R.string.errorBlankCity, Toast.LENGTH_SHORT).show();
                }else if(mName.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.errorBlankName, Toast.LENGTH_SHORT).show();
                }else if(mMobile.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), R.string.errorBlankMobile, Toast.LENGTH_SHORT).show();
                }else{
                    mButton.setEnabled(false);
                    mDialog.setVisibility(View.VISIBLE);
                    String name = mName.getText().toString();
                    String mobile = mMobile.getText().toString();

                    su = new SaveUser();
                    su.execute(name, mobile, myCity);
                }
            }
        });

        gc = new GetCity();
        gc.execute((Void) null);
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(CityActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            //Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
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


    private void checkPermissionWrapper() {
        List<String> permissionsNeeded = new ArrayList();

        final List<String> permissionsList = new ArrayList();
        if (!addPermission(permissionsList, Manifest.permission.INTERNET))
            permissionsNeeded.add("Internet");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_NETWORK_STATE))
            permissionsNeeded.add("Network");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Location");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Coarse Location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(CityActivity.this,permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(this,permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        //insertDummyContact();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap();
                // Initial
                perms.put(Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_NETWORK_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    Toast.makeText(CityActivity.this, "All permission granted", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Permission Denied
                    Toast.makeText(CityActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Represents an asynchronous Save User task
     */
    public class SaveUser extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String charset = "UTF-8";
            String requestURL = getString(R.string.server_url);

            String mName = params[0];
            String mMobile = params[1];
            String mCity = params[2];

            try {
                MultipartUtility multipart = new MultipartUtility(requestURL, charset);
                multipart.addHeaderField("encrtype","multipart/form-data");
                multipart.addFormField("task", "user");
                multipart.addFormField("action", "save_user");
                multipart.addFormField("key", getString(R.string.api_key));
                multipart.addFormField("name", mName);
                multipart.addFormField("mobile",mMobile);
                multipart.addFormField("city", mCity);
                multipart.addFormField("published","1");

                String jsonResponse = multipart.finish(); // response from server.
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        otp = rootJSON.getJSONObject("results").getString("otp");
                        uObject = rootJSON.getJSONObject("results").getJSONObject("user");
                        is_new = rootJSON.getJSONObject("results").getString("is_new");
                        return true;
                    } else {
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
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                mDialog.setVisibility(View.INVISIBLE);
                try {
                    session.createLoginSession(uObject.getString("id"), uObject.getString("city_id"),uObject.getString("mobile"),uObject.getString("name"));
                    if(is_new.equals("1")) {
                        Toast.makeText(getApplicationContext(),getString(R.string.user_success),Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), OtpActivity.class);
                        i.putExtra("OtpForSending", otp);
                        i.putExtra("USER", "USER");
                        i.putExtra("uid", uObject.getString("id"));
                        startActivity(i);
                    }else{
                        Toast.makeText(getApplicationContext(),"Dear " + uObject.getString("name") + ",\nWelcome to Right Search",Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                        i.putExtra("MESSAGE", "");
                        startActivity(i);
                    }
                    finish();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else{
                mDialog.setVisibility(View.INVISIBLE);
                mButton.setEnabled(true);
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetCity extends AsyncTask<Void, Void, Boolean> {

        private String errorMessage = null;

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
                postDataParams.put("task", "user");
                postDataParams.put("action", "get_cities");
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray cities = rootJSON.getJSONObject("results").getJSONArray("cities");

                        cityArray = new String[cities.length()+1];
                        cityArray[0] = "Select City";
                        cityMap = new HashMap<>();

                        for (int i = 0; i < cities.length(); i++) {
                            cityMap.put(i+1, cities.getJSONObject(i).getString("id"));
                            cityArray[i+1] = cities.getJSONObject(i).getString("city_name");
                        }
                        cityArrayAdapt = new ArrayAdapter(CityActivity.this, android.R.layout.simple_list_item_1, cityArray);
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    if (cityArrayAdapt != null) {
                        mCity.setAdapter(cityArrayAdapt);
                        //myState.setSelection(cityArrayAdapt.getPosition(stateMap.get(providerObj.getString("province"))));
                    }
                } else {
                    if (errorMessage != "") {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
