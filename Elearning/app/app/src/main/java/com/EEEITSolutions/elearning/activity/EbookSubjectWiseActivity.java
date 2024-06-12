package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.adapter.EbookAdapter;
import com.EEEITSolutions.elearning.adapter.EbookDetails;
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

public class EbookSubjectWiseActivity extends AppCompatActivity {

    private String chapter_id, chapter_name;
    SessionManager session;
    GetBooks getP;
    EbookDetails[] myEbookDetailArray = null;
    EbookAdapter mEbookDetailAdapter = null;
    ListView mListView;
    String providerString;
    String favourite = "1";
    ProgressBar mProgress;
    private HashMap <String,String> user;
    String lName = "";

    private ArrayList<EbookDetails> eBookArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_subjectwise);

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
            chapter_id = extra.getString("ID");
            chapter_name = extra.getString("NAME");
            favourite = extra.getString("FAVOURITE");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if(favourite.equals("2")){
            this.setTitle(getString(R.string.my_favourite) + " " + getString(R.string.book));
        }else if(!chapter_name.isEmpty()){
            this.setTitle(chapter_name + ": " + getString(R.string.books));
        }

        user = session.getUserDetails();
        if(!user.get("language_name").equals("English")){
            lName = user.get("language_name");
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

        getP = new GetBooks(chapter_id);
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

    public String getFavouriteProviders(){
        MySQLiteHelper db = new MySQLiteHelper(getApplicationContext());
        return db.getFavouriteEbooks();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetBooks extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mChapterID;

        private String errorMessage = null;

        GetBooks(String chapter_id) {
            mChapterID = chapter_id;
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
                url = getString(R.string.server_url) + "chapterebooklist";
                postDataParams.put("favourite", favourite);
                try {
                    if (favourite.equals("2")) {
                        providerString = getFavouriteProviders();
                        postDataParams.put("ebookids", providerString);
                    } else {
                        postDataParams.put("chapterid", mChapterID);
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
                        JSONArray accounts = rootJSON.getJSONObject("results").getJSONArray("topics");
                        myEbookDetailArray = new EbookDetails[accounts.length()];
                        eBookArrayList = new ArrayList<>();
                        for (int i = 0; i < accounts.length(); i++) {
                            String eBookName = accounts.getJSONObject(i).getString("ebookname" + lName);
                            String chapterID = accounts.getJSONObject(i).getString("chapterid");
                            String chapterName = accounts.getJSONObject(i).getString("chaptername" + lName);
                            String topicID = accounts.getJSONObject(i).getString("topicid");
                            String topicName = accounts.getJSONObject(i).getString("topicname" + lName);
                            String eBookDesc = accounts.getJSONObject(i).getString("ebookname" + lName);
                            String eBookID = accounts.getJSONObject(i).getString("ebookid");
                            String eBookFile = accounts.getJSONObject(i).getString("ebookfile");
                            String videoRating = "5";

                            eBookArrayList.add(new EbookDetails(chapterID,chapterName,topicID,topicName,eBookName,eBookDesc,eBookFile,eBookID,videoRating));
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mEbookDetailAdapter = new EbookAdapter(getApplicationContext(),eBookArrayList);

                                if (mListView != null) {
                                    mListView.setAdapter(mEbookDetailAdapter);
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
            getP = null;

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
            getP = null;
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
