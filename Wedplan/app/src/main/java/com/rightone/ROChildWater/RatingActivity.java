package com.rightone.ROChildWater;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RatingActivity extends AppCompatActivity {

    String provider_id, provider_name;
    RatingBar mRatingBar;
    TextView mRatingScale;
    AutoCompleteTextView mName, mMobile, mRTitle, mReview;
    Button mSendFeedback;
    ProgressBar mDialog;

    private String errorMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        Bundle extra = getIntent().getExtras();
        try {
            provider_id = extra.getString("ID");
            provider_name = extra.getString("NAME");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        this.setTitle(provider_name);

        mRatingBar = findViewById(R.id.ratingBar);
        mRatingScale = findViewById(R.id.tvRatingScale);
        mName = findViewById(R.id.tvName);
        mRTitle = findViewById(R.id.tvTitle);
        mMobile = findViewById(R.id.tvMobile);
        mReview = findViewById(R.id.tvReview);

        mSendFeedback = findViewById(R.id.btnSubmit);

        mDialog = findViewById(R.id.progress_loader);

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                mRatingScale.setText(String.valueOf(v));
                switch ((int) ratingBar.getRating()) {
                    case 1:
                        mRatingScale.setText(getString(R.string.rating1));
                        break;
                    case 2:
                        mRatingScale.setText(getString(R.string.rating2));
                        break;
                    case 3:
                        mRatingScale.setText(getString(R.string.rating3));
                        break;
                    case 4:
                        mRatingScale.setText(getString(R.string.rating4));
                        break;
                    case 5:
                        mRatingScale.setText(getString(R.string.rating5));
                        break;
                    default:
                        mRatingScale.setText("");
                }
            }
        });

        mSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.setVisibility(View.VISIBLE);
                Boolean error = false;
                if (isOnline(RatingActivity.this)) {
                    String tvName = mName.getText().toString();
                    String tvRTitle = mRTitle.getText().toString();
                    String tvMobile = mMobile.getText().toString();
                    String tvMessage = mReview.getText().toString();

                    if(mRatingBar.getRating() == 0){
                        error = true;
                        Toast.makeText(RatingActivity.this, getString(R.string.check_rating), Toast.LENGTH_LONG).show();
                    }else if (tvName.trim().length() == 0) {
                        error = true;
                        Toast.makeText(RatingActivity.this, getString(R.string.check_name), Toast.LENGTH_LONG).show();
                    } else if (tvMobile.trim().length() == 0) {
                        error = true;
                        Toast.makeText(RatingActivity.this, getString(R.string.check_mobile), Toast.LENGTH_LONG).show();
                    } else if (tvRTitle.trim().length() == 0) {
                        error = true;
                        Toast.makeText(RatingActivity.this, getString(R.string.check_title), Toast.LENGTH_LONG).show();
                    } else if (tvMessage.trim().length() == 0) {
                        error = true;
                        Toast.makeText(RatingActivity.this, getString(R.string.check_review), Toast.LENGTH_LONG).show();
                    }else if(!isOnline(RatingActivity.this)){
                        error = true;
                        Toast.makeText(RatingActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                    } else {
                        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        String rating = String.valueOf(mRatingBar.getRating());
                        new SaveReview().execute(mName.getText().toString(), mMobile.getText().toString(), mRTitle.getText().toString(), mReview.getText().toString(), android_id, provider_id, rating);
                    }
                }else{
                    error = true;
                    Toast.makeText(RatingActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }
                if(error){
                    mDialog.setVisibility(View.INVISIBLE);
                }
            }
        });
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

    private boolean isOnline(Context mContext)
    {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }


    /**
     * Represents an asynchronous Resend OTP task
     */
    public class SaveReview extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String mName = params[0];
            String mMobile = params[1];
            String mRTitle = params[2];
            String mMessage = params[3];
            String mDeviceId = params[4];
            String mPid = params[5];
            String mRating = params[6];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "save_review");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("name", mName);
                postDataParams.put("review_title", mRTitle);
                postDataParams.put("mobile", mMobile);
                postDataParams.put("review", mMessage);
                postDataParams.put("device_id", mDeviceId);
                postDataParams.put("provider_id", mPid);
                postDataParams.put("rating", mRating);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }  catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mDialog.setVisibility(View.INVISIBLE);
            if(result){
                mName.setText("");
                mRTitle.setText("");
                mMobile.setText("");
                mReview.setText("");
                mRatingBar.setRating(0);
                Toast.makeText(RatingActivity.this, getString(R.string.review_thanks), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                // passing array index
                i.putExtra("ID", provider_id);
                i.putExtra("NAME", provider_name);
                startActivity(i);
                finish();
            }else{
                Toast.makeText(RatingActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
