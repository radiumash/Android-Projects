package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.MultipartUtility;
import com.rightonetechnologies.wholesalebazzar.common.SearchableEditText;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MemberActivity extends AppCompatActivity {

    SessionManager session;
    Button mButton;
    EditText mName, mMobile, mUname, mEmail;
    TextView mUpline;
    Switch mStar;
    SearchableEditText mCity;
    String[] cityArray;
    HashMap<Integer,String> cityMap;
    ArrayAdapter<String> cityArrayAdapt;
    private HashMap<String,String> user;
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
        setContentView(R.layout.activity_member);

        this.setTitle(R.string.add_new_member);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }

        session = new SessionManager(getApplicationContext());
        if(session.isLoggedIn()){
            user = session.getUserDetails();
        }else{
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        }

        mUname = findViewById(R.id.txtUName);
        mUname.setText(user.get("name"));

        mCity = findViewById(R.id.city);
        mName = findViewById(R.id.uName);
        mMobile = findViewById(R.id.mobile);
        mEmail = findViewById(R.id.uEmail);
        mUpline = findViewById(R.id.upline);
        mUpline.setText(user.get("user_id"));

        mStar = findViewById(R.id.isstar);
        mDialog = findViewById(R.id.progress_loader);

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
                    String email = mEmail.getText().toString();
                    String isstar = mStar.isChecked() ? "1" : "0";
                    String upline = mUpline.getText().toString();
                    su = new SaveUser();
                    su.execute(name, mobile, myCity, email, isstar, upline);
                }
            }
        });

        gc = new GetCity();
        gc.execute((Void) null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
            String mEmail = params[3];
            String mStar = params[4];
            String mUpline = params[5];

            try {
                MultipartUtility multipart = new MultipartUtility(requestURL, charset);
                multipart.addHeaderField("encrtype","multipart/form-data");
                multipart.addFormField("task", "user");
                multipart.addFormField("action", "save_user");
                multipart.addFormField("key", getString(R.string.api_key));
                multipart.addFormField("name", mName);
                multipart.addFormField("mobile",mMobile);
                multipart.addFormField("city", mCity);
                multipart.addFormField("email", mEmail);
                multipart.addFormField("isstar",mStar);
                multipart.addFormField("upline", mUpline);
                multipart.addFormField("page", "member");
                multipart.addFormField("published","0");

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
            mDialog.setVisibility(View.INVISIBLE);
            try {
                if(is_new.equals("1")) {
                    Toast.makeText(getApplicationContext(),getString(R.string.user_success),Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getApplicationContext(), OtpActivity.class);
                    i.putExtra("OtpForSending", otp);
                    i.putExtra("USER", "DOWNLINE");
                    i.putExtra("uid", uObject.getString("id"));
                    startActivity(i);
                }else{
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("MESSAGE", getString(R.string.save_success));
                    startActivity(i);
                }
                finish();
            }catch (JSONException e){
                e.printStackTrace();
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
                        cityArrayAdapt = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, cityArray);
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
}
