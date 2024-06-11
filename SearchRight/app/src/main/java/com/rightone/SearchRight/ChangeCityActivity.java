package com.rightone.SearchRight;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ChangeCityActivity extends AppCompatActivity {
    SessionManager session;
    Button mButton;
    SearchableEditText mCity;
    String[] cityArray;
    HashMap<Integer,String> cityMap;
    ArrayAdapter<String> cityArrayAdapt;
    ProgressBar mDialog;
    GetCity gc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_city);
        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        mDialog = findViewById(R.id.progress_loader);

        mCity = findViewById(R.id.city);
        mButton = findViewById(R.id.mButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myCity = String.valueOf(cityMap.get(cityArrayAdapt.getPosition(mCity.getText().toString())));
                if(myCity.equals("null")) {
                    Toast.makeText(getApplicationContext(), R.string.errorBlankCity, Toast.LENGTH_SHORT).show();
                }else{
                    session = new SessionManager(getApplicationContext());
                    session.checkLogin();
                    HashMap<String, String> user = session.getUserDetails();
                    // name
                    final String uid = user.get(SessionManager.KEY_UID);
                    final String name = user.get(SessionManager.KEY_NAME);
                    final String mobile = user.get(SessionManager.KEY_MOBILE);
                    session.createLoginSession(uid, myCity,mobile,name);
                    Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                    startActivity(i);
                }
            }
        });

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
                        cityArrayAdapt = new ArrayAdapter(ChangeCityActivity.this, android.R.layout.simple_list_item_1, cityArray);
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
