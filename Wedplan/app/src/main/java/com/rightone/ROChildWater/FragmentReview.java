package com.rightone.ROChildWater;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FragmentReview extends Fragment {
    private String errorMessage = null;
    FloatingActionButton mButton;
    String pid;
    String provider_name;
    ListView mListView;
    GetReviews getR;

    ReviewDetail myReviewDetailArray[] = null;
    ReviewDetailAdapter mReviewDetailAdapter = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_review, container, false);
        pid = getArguments().getString("ID");
        provider_name = getArguments().getString("NAME");

        mButton = fragmentView.findViewById(R.id.fab);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), RatingActivity.class);
                // passing array index
                i.putExtra("ID", pid);
                i.putExtra("NAME", provider_name);
                startActivity(i);
            }
        });

        mListView = fragmentView.findViewById(R.id.myListView);

        getR = new GetReviews(pid);
        getR.execute((Void) null);

        return fragmentView;
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetReviews extends AsyncTask<Void, Void, Boolean> {

        private final String mPid;
        GetReviews(String pid) {
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
                postDataParams.put("action", "get_reviews");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("provider_id", mPid);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray reviews = rootJSON.getJSONObject("results").getJSONArray("reviews");
                        myReviewDetailArray = new ReviewDetail[reviews.length()];

                        for(int i=0;i<reviews.length();i++){
                            String bName = reviews.getJSONObject(i).getString("name");
                            Character pName = bName.charAt(0);
                            String rId = reviews.getJSONObject(i).getString("id");
                            String rTitle = reviews.getJSONObject(i).getString("review_title");
                            String rReview = reviews.getJSONObject(i).getString("review");
                            String rRating = reviews.getJSONObject(i).getString("rating");
                            String pId = reviews.getJSONObject(i).getString("provider_id");
                            myReviewDetailArray[i] = new ReviewDetail(rId,pName,bName,rTitle,rReview,rRating,pId);
                        }

                        mListView.post(new Runnable() {
                            public void run() {
                                mReviewDetailAdapter = new ReviewDetailAdapter(getActivity(),R.layout.row_review,myReviewDetailArray);

                                if(mListView != null){
                                    mListView.setAdapter(mReviewDetailAdapter);
                                    mReviewDetailAdapter.notifyDataSetChanged();
                                    mListView.refreshDrawableState();
                                }
                            }
                        });

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                ReviewDetail acc = mReviewDetailAdapter.getItem(i);
                                Intent intent = new Intent(getActivity(),ReviewActivity.class);
                                intent.putExtra("reviewId",acc.mRid);
                                intent.putExtra("reviewTitle",acc.mRTitle);
                                intent.putExtra("NAME",acc.mName);
                                intent.putExtra("review",acc.mReview);
                                intent.putExtra("rating",acc.mRating);
                                intent.putExtra("providerId",acc.mProviderId);
                                startActivity(intent);
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
            getR = null;

            if (success) {
                //finish();
            } else {
                if(errorMessage != ""){
                    Toast.makeText(getActivity(),errorMessage,Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            getR = null;
        }
    }
}
