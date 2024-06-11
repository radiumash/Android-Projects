package com.rightone.ROChildWater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 1000;
    SaveDeviceID ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,CityActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

        if(checkFirstRun()){
            String device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            ssid = new SaveDeviceID(device_id,"Android", "USER");
            ssid.execute((Void) null);
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
