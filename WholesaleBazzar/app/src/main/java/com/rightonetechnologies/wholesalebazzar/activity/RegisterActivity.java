package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rightonetechnologies.wholesalebazzar.BuildConfig;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.*;
import com.rightonetechnologies.wholesalebazzar.network.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    SessionManager session;
    Button mButton;
    EditText mName, mMobile, mPassword, mReferral;
    TextView mUname, mLogin;
    SearchableEditText mCity;
    String[] cityArray;
    HashMap<Integer,String> cityMap;
    ArrayAdapter<String> cityArrayAdapt;
    private HashMap<String,String> user;
    GetCity gc;
    public String message;
    JSONObject uObject;
    String otp, referral_name;
    ProgressBar mDialog;
    SaveUser su;
    String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        checkPermissionWrapper();
        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());

        mUname = findViewById(R.id.txtUName);
        mLogin = findViewById(R.id.login);
        mName = findViewById(R.id.uName);
        mMobile = findViewById(R.id.mobile);
        mPassword = findViewById(R.id.password);
        mReferral = findViewById(R.id.referral);
        mDialog = findViewById(R.id.progress_loader);

        mCity = findViewById(R.id.city);

        mReferral.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if(length == 10){
                    hideKeyboard();
                    mDialog.setVisibility(View.VISIBLE);
                    GetUplineName gu = new GetUplineName();
                    gu.execute(editable.toString());
                }
            }
        });
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
                }else if(mPassword.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), R.string.errorBlankPassword, Toast.LENGTH_SHORT).show();
                }else{
                    mButton.setEnabled(false);
                    mDialog.setVisibility(View.VISIBLE);
                    String name = mName.getText().toString();
                    String mobile = mMobile.getText().toString();
                    String password = mPassword.getText().toString();
                    String referral = mReferral.getText().toString();
                    su = new SaveUser();
                    su.execute(name, mobile, password, myCity, referral);
                }
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        if(checkFirstRun()){
//            FirebaseMessaging.getInstance().subscribeToTopic("user");
//            FirebaseInstallations.getInstance().getToken(true)
//                .addOnCompleteListener(new OnCompleteListener<>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            //Log.w(TAG, "getInstanceId failed", task.getException());
//                            return;
//                        }
//
//                        // Get new Instance ID token
//                        token = task.getResult().getToken();
//                    }
//                });
        }

        gc = new GetCity();
        gc.execute((Void) null);
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
                                ActivityCompat.requestPermissions(RegisterActivity.this,permissionsList.toArray(new String[permissionsList.size()]),
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
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    Toast.makeText(RegisterActivity.this, "All permission granted", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Permission Denied
                    Toast.makeText(RegisterActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private Boolean checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;
        Boolean status = false;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run

        } else if (savedVersionCode == DOESNT_EXIST) {

            // TODO This is a new install (or the user cleared the shared preferences)
            status = true;

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
        return status;
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
            String mPass = params[2];
            String mCity = params[3];
            String mReferral = params[4];

            try {
                MultipartUtility multipart = new MultipartUtility(requestURL, charset);
                multipart.addHeaderField("encrtype","multipart/form-data");
                multipart.addFormField("task", "user");
                multipart.addFormField("action", "save_user");
                multipart.addFormField("key", getString(R.string.api_key));
                multipart.addFormField("name", mName);
                multipart.addFormField("mobile",mMobile);
                multipart.addFormField("password",mPass);
                multipart.addFormField("city", mCity);
                multipart.addFormField("upline_referral", mReferral);
                multipart.addFormField("isstar","0");
                multipart.addFormField("upline", "0");
                multipart.addFormField("page", "register");
                multipart.addFormField("device_id", token);
                multipart.addFormField("published","0");

                String jsonResponse = multipart.finish(); // response from server.
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        otp = rootJSON.getJSONObject("results").getString("otp");
                        uObject = rootJSON.getJSONObject("results").getJSONObject("user");
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
                    //session.createLoginSession(uObject.getString("id"), uObject.getString("city_id"),uObject.getString("mobile"),uObject.getString("name"),uObject.getString("isstar"),uObject.getString("referral_code"));
                    Toast.makeText(getApplicationContext(),getString(R.string.user_success),Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getApplicationContext(), OtpActivity.class);
                    i.putExtra("OtpForSending", otp);
                    i.putExtra("USER", "USER");
                    i.putExtra("uid", uObject.getString("id"));
                    startActivity(i);
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
                            cityArray[i+1] = cities.getJSONObject(i).getString("name");
                        }
                        cityArrayAdapt = new ArrayAdapter(RegisterActivity.this, android.R.layout.simple_list_item_1, cityArray);
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

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetUplineName extends AsyncTask<String, Void, Boolean> {

        private String errorMessage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;

            String mReferral = params[0];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "get_upline");
                postDataParams.put("referral_code", mReferral);
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        referral_name = rootJSON.getJSONObject("results").getString("referral_name");
                        errorMessage = message;
                    }else{
                        referral_name = "Wrong Referral Code";
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
                mUname.setText(referral_name);
            }catch (Exception e){
                e.printStackTrace();
            }
            mDialog.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDialog.setVisibility(View.INVISIBLE);
        }
    }

    public void hideKeyboard(){
        View view = this.getCurrentFocus();
        try {
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}
