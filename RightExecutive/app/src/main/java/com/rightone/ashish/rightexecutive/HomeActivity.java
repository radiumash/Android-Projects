package com.rightone.ashish.rightexecutive;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String errorMessage = null;
    private long lastBackPressTime = 0;
    private Toast toast;
    private SwipeRefreshLayout swipeRefreshLayout;
    SessionManager session;
    GetExecutiveAccount getEA;
    private Account[] myAccountArray = null;

    ListView mListView;
    String uid, executiveName;

    AccountAdapter mAccountAdapter = null;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }

        fab = findViewById(R.id.fab);

        mListView = findViewById(R.id.myListView);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        uid = user.get(SessionManager.KEY_UID);
        executiveName = user.get(SessionManager.KEY_NAME);
        this.setTitle("Welcome, " + executiveName);

        Bundle extra = getIntent().getExtras();

        if(extra != null) {
            session = new SessionManager(getApplicationContext());
            String message = extra.getString("MESSAGE");
            if(message != "") {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ProviderActivity.class);
                // passing array index
                i.putExtra("ExecutiveID", uid);
                i.putExtra("NAME",executiveName);
                i.putExtra("ProviderID","");
                i.putExtra("ServiceID","");
                i.putExtra("IS_NEW",true);
                startActivity(i);
            }
        });

        if(uid == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            Toast.makeText(getApplicationContext(),"Session Expired",Toast.LENGTH_SHORT).show();
        }else {
            getEA = new GetExecutiveAccount(uid);
            getEA.execute((Void) null);
        }
    }

    @Override
    public void onRefresh() {
        if(uid == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            Toast.makeText(getApplicationContext(),"Session Expired",Toast.LENGTH_SHORT).show();
        }else {
            getEA = new GetExecutiveAccount(uid);
            getEA.execute((Void) null);
        }
    }

    @Override
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG);
            toast.show();
            this.lastBackPressTime = System.currentTimeMillis();
        } else {
            if (toast != null) {
                toast.cancel();
            }
            super.onBackPressed();
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
    public class GetExecutiveAccount extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;
        private final String mUid;

        GetExecutiveAccount(String uid) {
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
                postDataParams.put("task", "executive");
                postDataParams.put("action", "accounts");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("executive_id", mUid);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");

                    if (success.equals(true)) {
                        JSONArray accounts = rootJSON.getJSONObject("results").getJSONArray("accounts");
                        myAccountArray = new Account[accounts.length()];

                        final String exeName = rootJSON.getJSONObject("results").getJSONObject("executive").getString("name");
                        for(int i=0;i<accounts.length();i++){
                            String mDate = accounts.getJSONObject(i).getString("created");
                            String date1 = parseDate(mDate,"dd/MM/yyyy");
                            String dArr[] = date1.split("-");
                            String imgDayName = "d" + String.valueOf(Integer.parseInt(dArr[0]));
                            String cc = dArr[2] + '/' + dArr[1] + '/' + dArr[0];
                            Integer mForm = Integer.valueOf(accounts.getJSONObject(i).getString("total_providers"));

                            myAccountArray[i] = new Account(dArr[1],mForm,imgDayName,cc,dArr[2]);
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mAccountAdapter = new AccountAdapter(getApplicationContext(),R.layout.row,myAccountArray);

                                if(mListView != null){
                                    mListView.setAdapter(mAccountAdapter);
                                    mAccountAdapter.notifyDataSetChanged();
                                    mListView.refreshDrawableState();
                                }
                            }
                        });

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Account acc = mAccountAdapter.getItem(i);
                                Intent intent = new Intent(getApplicationContext(),AccountActivity.class);
                                intent.putExtra("NAME",executiveName);
                                intent.putExtra("CreateDate",acc.getCreateDate());
                                startActivity(intent);
                            }
                        });
                    }else{
                        errorMessage = rootJSON.getString("message");
                        return false;
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
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
            getEA = null;
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
            getEA = null;
        }

        public String parseDate(String dateString, String format)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            SimpleDateFormat formatterOut = new SimpleDateFormat("dd-MM-yyyy");
            Date date = null;
            try {
                date = formatter.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return formatterOut.format(date);
        }
    }
}
