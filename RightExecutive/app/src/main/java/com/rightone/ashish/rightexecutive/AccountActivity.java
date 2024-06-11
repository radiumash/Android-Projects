package com.rightone.ashish.rightexecutive;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AccountActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String errorMessage = null;

    SessionManager session;
    String executiveId, executive_name;
    String createDate;

    GetAccountDetail getAD;
    AccountDetail myAccountDetailArray[] = null;
    AccountDetailAdapter mAccountDetailAdapter = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    ListView mListView;
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

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

        Bundle extra = getIntent().getExtras();

        if(extra != null) {
            session = new SessionManager(getApplicationContext());
            executive_name = extra.getString("NAME");
            createDate = extra.getString("CreateDate");
        }

        this.setTitle(executive_name);
        mTextView = findViewById(R.id.mCreateDate);
        mTextView.setText(createDate);

        mListView = findViewById(R.id.myListView);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        final String uid = user.get(SessionManager.KEY_UID);
        executiveId = uid;
        if(uid == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            Toast.makeText(getApplicationContext(),"Session Expired",Toast.LENGTH_SHORT).show();
        }else {
            getAD = new GetAccountDetail(uid,createDate);
            getAD.execute((Void) null);
        }
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
    public void onRefresh() {
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        final String uid = user.get(SessionManager.KEY_UID);
        if(uid == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            Toast.makeText(getApplicationContext(),"Session Expired",Toast.LENGTH_SHORT).show();
        }else {
            getAD = new GetAccountDetail(uid,createDate);
            getAD.execute((Void) null);
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
    public class GetAccountDetail extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mUid;
        private final String mCreateDate;

        GetAccountDetail(String uid, String createDate) {
            mUid = uid;
            mCreateDate = createDate;
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
                postDataParams.put("action", "accounts_bydate");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("executive_id", mUid);
                postDataParams.put("created", mCreateDate);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray accounts = rootJSON.getJSONObject("results").getJSONArray("accounts");
                        myAccountDetailArray = new AccountDetail[accounts.length()];

                        for(int i=0;i<accounts.length();i++){
                            String bName = accounts.getJSONObject(i).getString("business_name");
                            String sName = accounts.getJSONObject(i).getString("service_name");
                            String sTitle = accounts.getJSONObject(i).getString("subscription_title");
                            String isP = accounts.getJSONObject(i).getString("is_premium");
                            String lcState = accounts.getJSONObject(i).getString("location_name") +
                                    ',' + accounts.getJSONObject(i).getString("city_name") +
                                    ',' + accounts.getJSONObject(i).getString("state_name");
                            String pId = accounts.getJSONObject(i).getString("provider_id");
                            String bPic = accounts.getJSONObject(i).getString("business_pic");
                            String bAmount = accounts.getJSONObject(i).getString("amount");
                            myAccountDetailArray[i] = new AccountDetail(bName,sName,lcState,sTitle,isP,pId,bPic,bAmount);
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mAccountDetailAdapter = new AccountDetailAdapter(getApplicationContext(),R.layout.row_account,myAccountDetailArray);

                                if(mListView != null){
                                    mListView.setAdapter(mAccountDetailAdapter);
                                    mAccountDetailAdapter.notifyDataSetChanged();
                                    mListView.refreshDrawableState();
                                }
                            }
                        });

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                AccountDetail acc = mAccountDetailAdapter.getItem(i);
                                Intent intent = new Intent(getApplicationContext(),ProviderActivity.class);

                                intent.putExtra("ExecutiveID",executiveId);
                                intent.putExtra("ProviderID",acc.mProviderId);
                                intent.putExtra("ServiceID",acc.mServiceName);
                                intent.putExtra("IS_NEW",false);

                                startActivityForResult(intent,ACC_REQUEST);
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
            getAD = null;
            swipeRefreshLayout.setRefreshing(false);
            if (success) {
                //finish();
            } else {
                if(errorMessage != ""){
                    Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            swipeRefreshLayout.setRefreshing(false);
            getAD = null;
        }
    }
}
