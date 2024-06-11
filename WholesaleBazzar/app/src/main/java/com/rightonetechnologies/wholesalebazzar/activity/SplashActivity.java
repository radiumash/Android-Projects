package com.rightonetechnologies.wholesalebazzar.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import org.json.JSONException;
import org.json.JSONObject;
public class SplashActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 1000;
    //SaveDeviceID ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
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
    public class SaveDeviceID extends AsyncTask<Void, Void, Boolean> {

        String mDeviceId;
        String mPlatform;
        String mUserType;

        SaveDeviceID(String device_id, String platform, String user_type){
            mDeviceId = device_id;
            mPlatform = platform;
            mUserType = user_type;
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
                postDataParams.put("action", "save_device");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("device_id", mDeviceId);
                postDataParams.put("platform", mPlatform);
                postDataParams.put("description", mUserType);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }  catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
}
