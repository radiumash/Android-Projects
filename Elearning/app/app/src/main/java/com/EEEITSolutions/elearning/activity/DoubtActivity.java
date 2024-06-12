package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.adapter.DoubtAdapter;
import com.EEEITSolutions.elearning.adapter.DoubtDetails;
import com.EEEITSolutions.elearning.common.SessionManager;
import com.EEEITSolutions.elearning.network.ConnectivityReceiver;
import com.EEEITSolutions.elearning.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DoubtActivity extends AppCompatActivity {

    SessionManager session;
    DoubtDetails[] myDoubtDetailArray = null;
    DoubtAdapter mDoubtDetailAdapter = null;
    ListView mListView;
    String mType;
    String nType, nArray, nId, nName;

    ProgressBar mProgress;
    private HashMap<String,String> user;
    String lName = "";
    String subjectID, chapterID;
    private ArrayList<DoubtDetails> doubtArrayList;
    String cid = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubt);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());
        if(!session.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        this.setTitle(getString(R.string.menu_doubt));

        user = session.getUserDetails();
        if(!user.get("language_name").equals("English")){
            lName = user.get("language_name");
        }

        Bundle extra = getIntent().getExtras();
        try {
            cid = extra.getString("ID");
            subjectID = extra.getString("SUBJECTID");
            chapterID = extra.getString("CHAPTERID");
            mType = extra.getString("TYPE");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if(cid.equals("")){
            cid = user.get(SessionManager.KEY_CID);
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mListView = findViewById(R.id.myListView);
        mProgress = findViewById(R.id.loading);
        HashMap<String, String> user = session.getUserDetails();

        GetSubject getC = new GetSubject(cid);
        getC.execute((Void) null);
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

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetSubject extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mCid;

        private String errorMessage = null;

        GetSubject(String cid) {
            mCid = cid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String url="";
            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("key", getString(R.string.api_key));
                try {
                    if(mType.equals("SUBJECT")) {
                        url = getString(R.string.server_url) + "subjectlistbyclass";
                        postDataParams.put("classid", mCid);
                        nArray = "subjects";
                        nId = "subjectid";
                        nName = "subjectname";
                        nType = "CHAPTER";
                    }else if(mType.equals("CHAPTER")){
                        url = getString(R.string.server_url) + "chapterlist";
                        postDataParams.put("classid", user.get("class"));
                        postDataParams.put("subjectid", mCid);
                        subjectID = mCid;
                        nArray = "chapters";
                        nId = "chapterid";
                        nName = "chaptername";
                        nType = "TOPIC";
                    }else if(mType.equals("TOPIC")){
                        url = getString(R.string.server_url) + "topiclistbychapter";
                        postDataParams.put("chapterid", mCid);
                        chapterID = mCid;
                        nArray = "topics";
                        nId = "topicid";
                        nName = "topicname";
                        nType = "DOUBT";
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, url);

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray accounts = rootJSON.getJSONObject("results").getJSONArray(nArray);
                        myDoubtDetailArray = new DoubtDetails[accounts.length()];
                        doubtArrayList = new ArrayList<>();
                        for (int i = 0; i < accounts.length(); i++) {
                            String id = accounts.getJSONObject(i).getString(nId);
                            String name = accounts.getJSONObject(i).getString(nName + lName);
                            String imageurl = "";
                            if(mType.equals("SUBJECT")) {
                                imageurl = accounts.getJSONObject(i).getString("imageurl");
                            }
                            doubtArrayList.add(new DoubtDetails(id,name,imageurl,nType,subjectID,chapterID));
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mDoubtDetailAdapter = new DoubtAdapter(getApplicationContext(),doubtArrayList);

                                if (mListView != null) {
                                    mListView.setAdapter(mDoubtDetailAdapter);
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
            if (success) {
                //finish();
                mListView.setTextFilterEnabled(true);
            } else {
                if (errorMessage != "") {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
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
