package com.rightone.SearchRight;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class OtpActivity extends AppCompatActivity {

    public static String USER_CONST = "USER";

    String errorMessage = null;
    String otp, message;
    String userType = "USER";
    SessionManager session;
    Bundle extra;
    private reSendOtpTask mResendTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }

        final EditText mOtp;
        Button mVerifyButton;
        Button mResendButton;

        extra = getIntent().getExtras();

        if(extra != null){
            session = new SessionManager(getApplicationContext());
            //Otp
            mOtp = findViewById(R.id.otp);
            otp = extra.getString("OtpForSending");
            message = extra.getString("message");
            userType = extra.getString("USER");

            //To be hide
            if(message != "" && message != null) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            //read
            session.checkLogin();
            HashMap<String, String> user = session.getUserDetails();
            // name
            final String uid = user.get(SessionManager.KEY_CID);

            if(uid == null){
                startActivity(new Intent(getApplicationContext(), CityActivity.class));
                Toast.makeText(getApplicationContext(),"Session Expired",Toast.LENGTH_SHORT).show();
            }else {
                mVerifyButton = findViewById(R.id.verify_button);
                mVerifyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOtp.getText().toString().equals("")) {
                            mOtp.setError(getString(R.string.error_field_required));
                            mOtp.requestFocus();
                        } else if (!mOtp.getText().toString().equals(otp)) {
                            mOtp.setError(getString(R.string.otp_not_match));
                            mOtp.requestFocus();
                        } else {
                            if(USER_CONST.equals(userType)){
                                String user_id = extra.getString("uid");
                                new SaveUser2().execute(user_id);
                            }else{
                                Toast.makeText(getApplicationContext(),"Listing saved! We'll contact you shortly.",Toast.LENGTH_SHORT).show();
                                //String provider_id = extra.getString("provider_id");
                                //String subscription_id = extra.getString("subscription_id");
                                //String user_id = extra.getString("user_id");
                                //new SaveProvider2().execute(provider_id, subscription_id, user_id);
                                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    }
                });
            }

            mResendButton = findViewById(R.id.mResend);
            mResendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    session = new SessionManager(getApplicationContext());
                    //read
                    session.checkLogin();
                    // get user data from session
                    HashMap<String, String> user = session.getUserDetails();
                    final String uid = user.get(SessionManager.KEY_CID);
                    final String mobile = user.get(SessionManager.KEY_MOBILE);

                    mResendTask = new reSendOtpTask(uid, mobile);
                    mResendTask.execute((Void) null);
                }
            });
        }
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
     * Represents an asynchronous Resend OTP task
     */
    public class SaveProvider2 extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String mPid = params[0];
            String mSid = params[1];
            String mEid = params[2];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "save_provider2");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("provider_id", mPid);
                postDataParams.put("subscription_id", mSid);
                postDataParams.put("user_id", mEid);
                postDataParams.put("published","0");

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");

                    if (success.equals(true)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }  catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                i.putExtra("MESSAGE", "Listing Added Successfully");
                startActivity(i);
                finish();
            }else{
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class SaveUser2 extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String mUid = params[0];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "save_user2");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("user_id", mUid);
                postDataParams.put("published","1");

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");

                    if (success.equals(true)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }  catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                i.putExtra("MESSAGE", "Welcome to Right Search");
                startActivity(i);
                finish();
            }else{
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Represents an asynchronous Resend OTP task
     */
    public class reSendOtpTask extends AsyncTask<Void, Void, Boolean> {

        private static final int OTP_REQUEST = 1;

        private final String mUid;
        private final String mMobile;
        private String errorMessage = null;

        reSendOtpTask(String uid, String mobile) {
            mUid = uid;
            mMobile = mobile;
        }

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
                postDataParams.put("action", "resend_otp");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("mobile", mMobile);
                postDataParams.put("user_id", mUid);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        otp = rootJSON.getJSONObject("results").getString("otp");
                        errorMessage = message;
                        return true;
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            mResendTask = null;

            if (success) {
                if(errorMessage != ""){
                    Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
                }
            } else {
                if(errorMessage != ""){
                    Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mResendTask = null;
        }
    }
}
