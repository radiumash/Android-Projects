package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.common.UserAccount;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DownlineActivity extends AppCompatActivity {

    SessionManager session;
    ListView mListView;
    ProgressBar mProgress;
    private HashMap<String,String> user;
    UserAccount[] users;
    ArrayAdapter<UserAccount> arrayAdapter;
    LinearLayout Rec, noRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downline);

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
        user = session.getUserDetails();
        this.setTitle(R.string.view_downline);

        mListView = findViewById(R.id.myListView);
        mProgress = findViewById(R.id.loading);
        Rec = findViewById(R.id.LLRec);
        noRec = findViewById(R.id.LLnoRec);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                CheckedTextView v = (CheckedTextView) view;
//                boolean currentCheck = v.isChecked();
//                UserAccount user = (UserAccount) mListView.getItemAtPosition(i);
//                user.setActive(!currentCheck);
            }
        });

        mProgress.setVisibility(View.VISIBLE);
        GetDownline gu = new GetDownline();
        gu.execute(user.get("user_id"));
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
    public class GetDownline extends AsyncTask<String, Void, Boolean> {

        private String errorMessage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;

            String mUser = params[0];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "get_downline");
                postDataParams.put("user_id", mUser);
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray downline = rootJSON.getJSONObject("results").getJSONArray("downline");
                        users = new UserAccount[downline.length()];
                        for (int i = 0; i < downline.length(); i++) {
                            users[i] = new UserAccount(downline.getJSONObject(i).getString("id"),downline.getJSONObject(i).getString("name"));
                        }
                        arrayAdapter
                                = new ArrayAdapter<UserAccount>(getApplicationContext(), android.R.layout.simple_list_item_1 , users);
                        errorMessage = message;
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
            if(success) {
                try {
                    Rec.setVisibility(View.VISIBLE);
                    mListView.setAdapter(arrayAdapter);
                    mListView.refreshDrawableState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mProgress.setVisibility(View.INVISIBLE);
            }else{
                noRec.setVisibility(View.VISIBLE);
                if (errorMessage != "") {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
