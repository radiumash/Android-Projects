package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.adapter.DealAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.DealDetails;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DealActivity extends AppCompatActivity {

    ProgressBar mProgress;
    DealDetails[] myExDealDetailArray = null;
    DealAdapter mExDealDetailAdapter = null;
    private ArrayList<DealDetails> ExDealArrayList;
    ListView mExDealListView;

    private SessionManager session;
    private HashMap<String,String> user;
    LinearLayout Rec, noRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

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
        this.setTitle(getString(R.string.view_deals));

        mProgress = findViewById(R.id.loading);
        mProgress.setVisibility(View.VISIBLE);

        mExDealListView = findViewById(R.id.myListView);
        Rec = findViewById(R.id.LLRec);
        noRec = findViewById(R.id.LLnoRec);

        GetDeals gd = new GetDeals();
        gd.execute((Void) null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetDeals extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "get_deals");
                postDataParams.put("city_id", user.get("city_id"));
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray exDeals = rootJSON.getJSONObject("results").getJSONArray("deals");
                        myExDealDetailArray = new DealDetails[exDeals.length()];
                        ExDealArrayList = new ArrayList<>();

                        for (int i = 0; i < exDeals.length(); i++) {

                            String dealid = exDeals.getJSONObject(i).getString("id");
                            String company = exDeals.getJSONObject(i).getString("company_name");
                            String pid = exDeals.getJSONObject(i).getString("product_id");
                            String product_name = exDeals.getJSONObject(i).getString("product_name");
                            String deal_name = exDeals.getJSONObject(i).getString("deal_name");
                            String mrp = exDeals.getJSONObject(i).getString("mrp");
                            String tax = exDeals.getJSONObject(i).getString("tax");
                            String image_url = exDeals.getJSONObject(i).getString("image_url");
                            JSONArray units = exDeals.getJSONObject(i).getJSONArray("unit");

                            HashMap<Integer, String> unitMap = new HashMap<>();
                            HashMap<Integer, String> mrpMap = new HashMap<>();
                            HashMap<Integer, String> spMap = new HashMap<>();
                            String[] unitArray = new String[units.length()+1];
                            unitArray[0] = "Select Pack";
                            for (int j = 0; j < units.length(); j++) {
                                mrpMap.put(j+1, units.getJSONObject(j).getString("mrp"));
                                spMap.put(j+1, units.getJSONObject(j).getString("sell_price"));
                                unitMap.put(j+1, units.getJSONObject(j).getString("id"));
                                unitArray[j+1] = units.getJSONObject(j).getString("name");
                            }
                            ArrayAdapter<String> unitArrayAdapt = new ArrayAdapter(DealActivity.this, android.R.layout.simple_list_item_1, unitArray);
                            ExDealArrayList.add(new DealDetails(dealid, deal_name, company, pid, product_name, unitArrayAdapt, unitMap, mrp, "", tax, image_url, spMap, mrpMap));
                        }

                        mExDealListView.post(new Runnable() {
                            public void run() {
                                mExDealDetailAdapter = new DealAdapter(DealActivity.this, ExDealArrayList);

                                if (mExDealListView != null) {
                                    mExDealListView.setAdapter(mExDealDetailAdapter);
                                    mExDealListView.refreshDrawableState();
                                }
                            }
                        });
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
                    Rec.setVisibility(View.VISIBLE);
                } else {
                    noRec.setVisibility(View.VISIBLE);
                    if (errorMessage != "") {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            mProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
