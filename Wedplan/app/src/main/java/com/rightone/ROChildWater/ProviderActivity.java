package com.rightone.ROChildWater;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ProviderActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SessionManager session;

    GetProviders getP;
    ProviderDetails myAccountDetailArray[] = null;
    ProviderAdapter mAccountDetailAdapter = null;

    private ArrayList<ProviderDetails> providerArrayList;
    private SwipeRefreshLayout swipeRefreshLayout;
    ListView mListView;
    String service_id, service_name, favourite = "", providerString = "", queryString="";
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        mSearchView = findViewById(R.id.searchView1);
        session = new SessionManager(getApplicationContext());
        if(!session.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), CityActivity.class));
            finish();
        }
        Bundle extra = getIntent().getExtras();
        try {
            service_id = extra.getString("ID");
            service_name = extra.getString("NAME");
            favourite = extra.getString("FAVOURITE");
            queryString = extra.getString("QUERY");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        if(!service_name.equals("")) {
            this.setTitle(service_name);
        }else if(!queryString.equals("")){
            this.setTitle(queryString);
        }

        mListView = findViewById(R.id.myListView);

        HashMap<String, String> user = session.getUserDetails();
        // name
        final String cid = user.get(SessionManager.KEY_CID);

        getP = new GetProviders(cid, service_id);
        getP.execute((Void) null);
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

    @Override
    public void onRefresh() {
        HashMap<String, String> user = session.getUserDetails();
        final String cid = user.get(SessionManager.KEY_CID);
        getP = new GetProviders(cid, service_id);
        getP.execute((Void) null);
    }

    private void setupSearchView(){
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    mListView.clearTextFilter();
                } else {
                    mListView.setFilterText(newText);
                }
                return true;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    mSearchView.setQuery("", false);
                }
            }
        });
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search Here");
    }

    public String getFavouriteProviders(){
        MySQLiteHelper db = new MySQLiteHelper(getApplicationContext());
        return db.getFavouriteProviders();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetProviders extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mCid;
        private final String mService;

        private String errorMessage = null;

        GetProviders(String cid, String service_id) {
            mCid = cid;
            mService = service_id;
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
                postDataParams.put("key", getString(R.string.api_key));
                try {
                    if (favourite.equals("1")) {
                        providerString = getFavouriteProviders();
                        postDataParams.put("action", "get_providers_by_ids");
                        postDataParams.put("providers", providerString);
                    } else if (!queryString.equals("")) {
                        postDataParams.put("action", "get_providers_by_query");
                        postDataParams.put("cat_id", service_id);
                        postDataParams.put("queryString", queryString);
                    } else {
                        postDataParams.put("action", "get_providers");
                        postDataParams.put("city_id", mCid);
                        postDataParams.put("service_id", mService);
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray accounts = rootJSON.getJSONObject("results").getJSONArray("providers");
                        myAccountDetailArray = new ProviderDetails[accounts.length()];
                        service_name = accounts.getJSONObject(0).getString("service_name");
                        providerArrayList = new ArrayList<>();
                        for (int i = 0; i < accounts.length(); i++) {
                            String bName = accounts.getJSONObject(i).getString("business_name");
                            String bImage = accounts.getJSONObject(i).getString("business_pic");
                            String lName = accounts.getJSONObject(i).getString("location_name");
                            String spName = accounts.getJSONObject(i).getString("specialities");
                            String iSp = accounts.getJSONObject(i).getString("is_premium");
                            String bRating = accounts.getJSONObject(i).getString("rating");
                            String bDesc = accounts.getJSONObject(i).getString("description");
                            String bServ = accounts.getJSONObject(i).getString("service_name");
                            Boolean sName = false;
                            if(iSp.equals("1")){
                                sName = true;
                            }
                            String pId = accounts.getJSONObject(i).getString("id");

                            providerArrayList.add(new ProviderDetails(bImage, bName, lName, spName, sName, pId, bRating, bDesc, bServ));
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mAccountDetailAdapter = new ProviderAdapter(getApplicationContext(),providerArrayList);

                                if (mListView != null) {
                                    mListView.setAdapter(mAccountDetailAdapter);
                                    mListView.refreshDrawableState();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            swipeRefreshLayout.setRefreshing(false);
            getP = null;

            if (success) {
                //finish();
                mListView.setTextFilterEnabled(true);
                setupSearchView();
            } else {
                if (errorMessage != "") {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            swipeRefreshLayout.setRefreshing(false);
            getP = null;
        }
    }

}
