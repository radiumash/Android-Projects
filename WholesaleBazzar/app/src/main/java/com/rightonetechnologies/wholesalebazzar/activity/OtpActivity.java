package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonIOException;
import com.rightonetechnologies.wholesalebazzar.R;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class OtpActivity extends AppCompatActivity {

    public static String USER_CONST = "USER";

    String errorMessage = null;
    String otp, message;
    String userType = "USER";
    String user_id;
    SessionManager session;
    ProgressBar mProgress;
    JSONObject uObject;
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
        mProgress = findViewById(R.id.progress_loader);
        session = new SessionManager(getApplicationContext());
        extra = getIntent().getExtras();

        if(extra != null){
            //Otp
            mOtp = findViewById(R.id.otp);
            otp = extra.getString("OtpForSending");
            message = extra.getString("message");
            userType = extra.getString("USER");
            user_id = extra.getString("uid");

            //To be hide
            if(message != "" && message != null) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            mVerifyButton = findViewById(R.id.verify_button);
            mVerifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProgress.setVisibility(View.VISIBLE);
                    if (mOtp.getText().toString().equals("")) {
                        mOtp.setError(getString(R.string.error_field_required));
                        mOtp.requestFocus();
                    } else if (!mOtp.getText().toString().equals(otp)) {
                        mOtp.setError(getString(R.string.otp_not_match));
                        mOtp.requestFocus();
                    } else {
                        if(USER_CONST.equals(userType)){
                            new SaveUser2().execute(user_id);
                        }else{
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.putExtra("MESSAGE", getString(R.string.save_success));
                            startActivity(i);
                            finish();
                        }
                    }
                    mProgress.setVisibility(View.INVISIBLE);
                }
            });

            mResendButton = findViewById(R.id.mResend);
            mResendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mProgress.setVisibility(View.VISIBLE);

                    mResendTask = new reSendOtpTask(user_id);
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
                    uObject = rootJSON.getJSONObject("results").getJSONObject("user");
                    message = rootJSON.getString("message");

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
                try {
                    session.createLoginSession(uObject.getString("id"), uObject.getString("city_id"), uObject.getString("mobile"), uObject.getString("name"), uObject.getString("isstar"), uObject.getString("referral_code"));
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("MESSAGE", message);
                    startActivity(i);
                    finish();
                }catch (JSONException e){
                    e.printStackTrace();
                }
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
        private String errorMessage = null;

        reSendOtpTask(String uid) {
            mUid = uid;
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
                postDataParams.put("task", "user");
                postDataParams.put("action", "resend_otp");
                postDataParams.put("key", getString(R.string.api_key));
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
            mProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mResendTask = null;
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
