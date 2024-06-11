package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.MultipartUtility;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    TextView mSignup;
    SessionManager session;
    Button mButton;
    EditText mMobile, mPassword;
    JSONObject uObject;
    ProgressBar mDialog;
    CheckLogin cl;
    public String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());
        if(session.isLoggedIn()){
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
        mDialog = findViewById(R.id.progress_loader);
        mMobile = findViewById(R.id.mobile);
        mPassword = findViewById(R.id.password);
        mButton = findViewById(R.id.mButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMobile.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), R.string.errorBlankMobile, Toast.LENGTH_SHORT).show();
                }else if(mPassword.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), R.string.errorBlankPassword, Toast.LENGTH_SHORT).show();
                }else{
                    mButton.setEnabled(false);
                    mDialog.setVisibility(View.VISIBLE);
                    String mobile = mMobile.getText().toString();
                    String password = mPassword.getText().toString();
                    cl = new CheckLogin();
                    cl.execute(mobile, password);
                }
            }
        });

        mSignup = findViewById(R.id.signup);
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * Represents an asynchronous Save User task
     */
    public class CheckLogin extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;

            String mMobile = params[0];
            String mPass = params[1];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "check_user");
                postDataParams.put("mobile", mMobile);
                postDataParams.put("password", mPass);
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
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
                    session.createLoginSession(uObject.getString("id"), uObject.getString("city_id"),uObject.getString("mobile"),uObject.getString("name"),uObject.getString("isstar"),uObject.getString("referral_code"));
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
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
}