package com.rightone.ROChildWater;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    String mRid, mRTitle, mRName, mReview, mRating, mRProvider;
    TextView mTvTitle, mTvReview, mTvName, mBName;
    RatingBar mRbRating;
    ImageView mBImage;
    String message;
    GetProvider gPO;

    Map<String, String> mapProvider = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

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
            mRProvider = extra.getString("providerId");
            mRName = extra.getString("NAME");
            mRating = extra.getString("rating");
            mReview = extra.getString("review");
            mRTitle = extra.getString("reviewTitle");
            mRid = extra.getString("reviewId");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        mTvTitle = findViewById(R.id.tvTitle);
        mTvReview = findViewById(R.id.tvDescription);
        mTvName = findViewById(R.id.tvName);

        mRbRating = findViewById(R.id.ratingBar);

        mBName = findViewById(R.id.tvBusinessName);
        mBImage = findViewById(R.id.ivBusinessImage);

        if(!mRating.equals("")){
            mRbRating.setRating(Float.parseFloat(mRating));
        }

        mTvTitle.setText(mRTitle);
        mTvReview.setText(mReview);
        mTvName.setText(mRName);

        if(!mRProvider.equals("")){
            gPO = new GetProvider(mRProvider);
            gPO.execute((Void) null);
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

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetProvider extends AsyncTask<Void, Void, Boolean> {

        private final String mPid;

        private String errorMessage = null;

        GetProvider(String pid) {
            mPid = pid;
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
                postDataParams.put("action", "get_provider_details");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("provider_id", mPid);
                postDataParams.put("d_type", "overview");

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        mapProvider.put("business_name",rootJSON.getJSONObject("results").getJSONObject("provider").getString("business_name"));
                        mapProvider.put("business_pic",rootJSON.getJSONObject("results").getJSONObject("provider").getString("business_pic"));
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
            gPO = null;
            if (success) {
                //finish();
                mBName.setText(mapProvider.get("business_name"));
                if(!mapProvider.get("business_pic").equals("")){
                    String url = getString(R.string.image_url) + '/' + mapProvider.get("business_pic");
                    Picasso.with(getApplicationContext())
                            .load(url)
                            .placeholder(R.drawable.banner_placeholder) // optional
                            .into(mBImage);
                }
            } else {
                if (errorMessage != "") {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            gPO = null;
        }
    }
}
