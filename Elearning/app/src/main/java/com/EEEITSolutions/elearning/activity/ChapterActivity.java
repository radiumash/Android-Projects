package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.adapter.ChapterAdapter;
import com.EEEITSolutions.elearning.adapter.ChapterDetails;
import com.EEEITSolutions.elearning.common.MySQLiteHelper;
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

public class ChapterActivity extends AppCompatActivity {

    private String subject_id, subject_name = "", sType = "";
    SessionManager session;
    GetChapters getC;
    ChapterDetails[] myChapterDetailArray = null;
    ChapterAdapter mChapterDetailAdapter = null;
    ListView mListView;
    String providerString;
    Boolean favourite = false;
    ProgressBar mProgress;

    private ArrayList<ChapterDetails> chapterArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

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

        Bundle extra = getIntent().getExtras();
        try {
            subject_id = extra.getString("ID");
            subject_name = extra.getString("NAME");
            sType = extra.getString("TYPE");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if(!subject_name.isEmpty()){
            this.setTitle(subject_name + ": " + getString(R.string.chapter));
        }else{
            this.setTitle(getString(R.string.chapter));
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mListView = findViewById(R.id.myListView);
        mProgress = findViewById(R.id.loading);
        HashMap<String, String> user = session.getUserDetails();
        // name
        final String cid = user.get(SessionManager.KEY_CID);
        if(sType.equals("")) {
            getC = new GetChapters(cid, subject_id);
            getC.execute((Void) null);
        }else{
            GetChaptersByBookID getCB = new GetChaptersByBookID(subject_id);
            getCB.execute((Void) null);
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public String getFavouriteProviders(){
        MySQLiteHelper db = new MySQLiteHelper(getApplicationContext());
        return db.getFavouriteProviders();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetChapters extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mCid;
        private final String mSubject;

        private String errorMessage = null;

        GetChapters(String cid, String service_id) {
            mCid = cid;
            mSubject = service_id;
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
                    if (favourite.equals(true)) {
                        providerString = getFavouriteProviders();
                        url = getString(R.string.server_url) + "";
                        postDataParams.put("providers", providerString);
                    } else {
                        url = getString(R.string.server_url) + "chapterlistbyclass";
                        postDataParams.put("classid", mCid);
                        postDataParams.put("subjecttid", mSubject);
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
                        JSONArray accounts = rootJSON.getJSONObject("results").getJSONArray("chapters");
                        myChapterDetailArray = new ChapterDetails[accounts.length()];
                        chapterArrayList = new ArrayList<>();
                        for (int i = 0; i < accounts.length(); i++) {
                            String chapterName = accounts.getJSONObject(i).getString("chaptername");
                            String chapterID = accounts.getJSONObject(i).getString("chapterid");
                            String chapterDesc = accounts.getJSONObject(i).getString("chapterdescription");
                            String subjectId = accounts.getJSONObject(i).getString("subjectid");
                            String subjectName = accounts.getJSONObject(i).getString("subjectname");
                            String videoRating = "5";

                            chapterArrayList.add(new ChapterDetails(chapterID,subjectId,chapterName,chapterDesc,subjectName,videoRating));
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mChapterDetailAdapter = new ChapterAdapter(getApplicationContext(),chapterArrayList);

                                if (mListView != null) {
                                    mListView.setAdapter(mChapterDetailAdapter);
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
            getC = null;

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
            getC = null;
            mProgress.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetChaptersByBookID extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mBid;

        private String errorMessage = null;

        GetChaptersByBookID(String cid) {
            mBid = cid;
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
                    if (favourite.equals(true)) {
                        providerString = getFavouriteProviders();
                        url = getString(R.string.server_url) + "";
                        postDataParams.put("providers", providerString);
                    } else {
                        url = getString(R.string.server_url) + "booklistbyqrcode";
                        postDataParams.put("isbnno", mBid);
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
                        JSONArray accounts = rootJSON.getJSONObject("results").getJSONArray("books");
                        myChapterDetailArray = new ChapterDetails[accounts.length()];
                        chapterArrayList = new ArrayList<>();
                        for (int i = 0; i < accounts.length(); i++) {
                            String chapterName = accounts.getJSONObject(i).getString("chaptername");
                            String chapterID = accounts.getJSONObject(i).getString("chapterid");
                            String chapterDesc = accounts.getJSONObject(i).getString("chapterdescription");
                            String subjectId = accounts.getJSONObject(i).getString("subjectid");
                            String subjectName = accounts.getJSONObject(i).getString("subjectname");
                            String videoRating = "5";

                            chapterArrayList.add(new ChapterDetails(chapterID,subjectId,chapterName,chapterDesc,subjectName,videoRating));
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mChapterDetailAdapter = new ChapterAdapter(getApplicationContext(),chapterArrayList);

                                if (mListView != null) {
                                    mListView.setAdapter(mChapterDetailAdapter);
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
            getC = null;

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
            getC = null;
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
