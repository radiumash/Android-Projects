package com.rightone.ROChildWater;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FragmentOverview extends Fragment{

    String pid = null;
    GetProviderOverview gPO;
    private GoogleMap mMap;
    MapView mMapView;
    String provider_name;

    TextView mBusinessName, mDesc, mPhone, mAddress, mEmail, mWebsite, mSpeciality, mHits;
    ImageView mBusinessImage;
    RatingBar mRatingBar;
    Button mDrive;

    Map<String, String> mapProvider = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_overview, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapView = fragmentView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                // Add a marker in Sydney and move the camera
            }
        });
        pid = getArguments().getString("ID");

        mBusinessName = fragmentView.findViewById(R.id.tvBusinessName);
        mDesc = fragmentView.findViewById(R.id.tvDescription);
        mPhone = fragmentView.findViewById(R.id.tvPhone);
        mAddress = fragmentView.findViewById(R.id.tvAddress);
        mEmail = fragmentView.findViewById(R.id.tvEmail);
        mWebsite = fragmentView.findViewById(R.id.tvWebsite);
        mHits = fragmentView.findViewById(R.id.tvHits);
        mSpeciality = fragmentView.findViewById(R.id.tvSpeciality);
        mRatingBar = fragmentView.findViewById(R.id.ratingBar);

        mBusinessImage = fragmentView.findViewById(R.id.ivBusinessImage);

        mDrive = fragmentView.findViewById(R.id.btnDrive);
        gPO = new GetProviderOverview(pid);
        gPO.execute((Void) null);

        return fragmentView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState); mMapView.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetProviderOverview extends AsyncTask<Void, Void, Boolean> {

        private final String mPid;

        private String errorMessage = null;

        GetProviderOverview(String pid) {
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
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        mapProvider.put("business_name",rootJSON.getJSONObject("results").getJSONObject("provider").getString("business_name"));
                        mapProvider.put("business_pic",rootJSON.getJSONObject("results").getJSONObject("provider").getString("business_pic"));
                        mapProvider.put("description",html2text(rootJSON.getJSONObject("results").getJSONObject("provider").getString("description")));
                        mapProvider.put("mobile",rootJSON.getJSONObject("results").getJSONObject("provider").getString("mobile"));
                        mapProvider.put("address",rootJSON.getJSONObject("results").getJSONObject("provider").getString("address"));
                        mapProvider.put("email",rootJSON.getJSONObject("results").getJSONObject("provider").getString("email"));
                        mapProvider.put("website",rootJSON.getJSONObject("results").getJSONObject("provider").getString("website"));
                        mapProvider.put("hits",rootJSON.getJSONObject("results").getJSONObject("provider").getString("hits"));
                        mapProvider.put("specialities",rootJSON.getJSONObject("results").getJSONObject("provider").getString("specialities"));
                        mapProvider.put("latitude",rootJSON.getJSONObject("results").getJSONObject("provider").getString("latitude"));
                        mapProvider.put("longitude",rootJSON.getJSONObject("results").getJSONObject("provider").getString("longitude"));
                        mapProvider.put("rating",rootJSON.getJSONObject("results").getJSONObject("provider").getString("rating"));

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
                if(mapProvider != null) {
                    mBusinessName.setText(mapProvider.get("business_name"));
                    if (!mapProvider.get("business_pic").equals("")) {
                        String url = getActivity().getString(R.string.image_url) + '/' + mapProvider.get("business_pic");
                        //BitmapDownloaderTask task = new BitmapDownloaderTask(url, mBusinessImage);
                        //task.execute((Void) null);
                        Picasso.with(getActivity())
                                .load(url)
                                .placeholder(R.drawable.banner_placeholder) // optional
                                .into(mBusinessImage);
                    }
                    mDesc.setText(mapProvider.get("description"));
                    mPhone.setText(mapProvider.get("mobile"));
                    mPhone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + mapProvider.get("mobile")));
                            startActivity(intent);
                        }
                    });
                    mAddress.setText(mapProvider.get("address"));

                    if (!mapProvider.get("email").equals("")) {
                        mEmail.setText(mapProvider.get("email"));
                        mEmail.setVisibility(View.VISIBLE);
                    }
                    if (!mapProvider.get("website").equals("")) {
                        mWebsite.setText(mapProvider.get("website"));
                        mWebsite.setVisibility(View.VISIBLE);
                        mWebsite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                                intent.putExtra("URL", mapProvider.get("website"));
                                intent.putExtra("NAME", mapProvider.get("business_name"));
                                startActivity(intent);
                            }
                        });
                    }
                    String v = mapProvider.get("hits") + " Views";
                    mHits.setText(v);
                    if (!mapProvider.get("specialities").equals("")) {
                        mSpeciality.setText(mapProvider.get("specialities"));
                        mSpeciality.setVisibility(View.VISIBLE);
                    }

                    LatLng usrMarker = new LatLng(Double.parseDouble(mapProvider.get("latitude")), Double.parseDouble(mapProvider.get("longitude")));
                    mMap.addMarker(new MarkerOptions().position(usrMarker).title(provider_name));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usrMarker, 13.0f));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(usrMarker));

                    mDrive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + mapProvider.get("latitude") + "," + mapProvider.get("longitude") + "&travelmode=driving");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            getActivity().startActivity(intent);
                        }
                    });

                    if (!mapProvider.get("rating").equals("null") && !mapProvider.get("rating").isEmpty() && mapProvider.get("rating") != null) {
                        mRatingBar.setRating(Float.parseFloat(mapProvider.get("rating")));
                        mRatingBar.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (errorMessage != "") {
                    //Toast.makeText(getActivity().getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }

        public String html2text(String html) {
            return android.text.Html.fromHtml(html).toString();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            gPO = null;
        }
    }

    class BitmapDownloaderTask extends AsyncTask<Void, Void, Bitmap> {

        String mUrl;
        ImageView mImageView;

        public BitmapDownloaderTask(String url,ImageView imageView) {
            mUrl = url;
            mImageView = imageView;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(Void... params) {
            // params comes from the execute() call: params[0] is the url.
            Bitmap bm;
            NetworkADO networkADO = new NetworkADO();
            bm = networkADO.getBitmapFromURL(mUrl);
            return bm;
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if(bitmap !=null){
                mImageView.setImageBitmap(bitmap);
            }
        }
    }
}
