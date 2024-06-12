package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.adapter.VideoAdapter;
import com.EEEITSolutions.elearning.adapter.VideoDetails;
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

public class VideoAllActivity extends AppCompatActivity {

    SessionManager session;
    GetVideos getP;
    VideoDetails[] myVideoDetailArray = null;
    VideoAdapter mVideoDetailAdapter = null;
    private ArrayList<VideoDetails> videoArrayList;
    ListView mListView;
    String providerString;
    String favourite = "1";
    ProgressBar mProgress;

    private HashMap <String,String> user;
    String lName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoall);

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
            favourite = extra.getString("FAVOURITE");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        if(favourite.equals("2")){
            this.setTitle(getString(R.string.my_favourite) + " " + getString(R.string.video));
        }else {
            this.setTitle(getString(R.string.videosAllSubject));
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

        getP = new GetVideos(cid);
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
        return db.getFavouriteVideos();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetVideos extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mCid;

        private String errorMessage = null;

        GetVideos(String cid) {
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
                url = getString(R.string.server_url) + "allvideolistbyclass";
                postDataParams.put("favourite", favourite);
                try {
                    if (favourite.equals("2")) {
                        providerString = getFavouriteProviders();
                        postDataParams.put("videoids", providerString);
                    } else {
                        postDataParams.put("classid", mCid);
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
                        myVideoDetailArray = new VideoDetails[accounts.length()];
                        videoArrayList = new ArrayList<>();
                        for (int i = 0; i < accounts.length(); i++) {
                            String topicname = accounts.getJSONObject(i).getString("topicname" + lName);
                            String topicid = accounts.getJSONObject(i).getString("topicid");
                            String chapterName = accounts.getJSONObject(i).getString("chaptername" + lName);
                            String chapterID = accounts.getJSONObject(i).getString("chapterid");
                            String videoID = accounts.getJSONObject(i).getString("videoid");
                            String videoName = accounts.getJSONObject(i).getString("videoname" + lName);
                            String videoThumb = accounts.getJSONObject(i).getString("videothumbnail");
                            String videoFile = accounts.getJSONObject(i).getString("videofile");
                            String videoRating = "5";

                            videoArrayList.add(new VideoDetails(chapterID,chapterName,topicid,topicname,videoFile,videoThumb,videoID,videoName,videoRating));
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mVideoDetailAdapter = new VideoAdapter(getApplicationContext(),videoArrayList);

                                if (mListView != null) {
                                    mListView.setAdapter(mVideoDetailAdapter);
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
