package com.rightone.ROChildWater;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentPhotos extends Fragment {
    String pid = null;
    Boolean loadImageSuccessful = false;
    //ArrayList for Storing image urls and titles
    private ArrayList<String> images;
    private ArrayList<String> images3d;
    private ArrayList<String> names;
    private ArrayList<String> ids;
    ProgressBar nDialog;
    Button mLeft, mRight;
    private Integer cPos = 0;
    private VrPanoramaView panoWidgetView;
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();


    GetPhotos gp;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_photos, container, false);
        pid = getArguments().getString("ID");

        mLeft = fragmentView.findViewById(R.id.btnLeft);
        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cPos.equals(0)) {
                    cPos -= 1;
                    setPanoImage(cPos);
                }else{
                    Toast.makeText(getActivity(),"First Image",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRight = fragmentView.findViewById(R.id.btnRight);
        mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cPos.equals(images3d.size()-1)) {
                    cPos += 1;
                    setPanoImage(cPos);
                }else{
                    Toast.makeText(getActivity(),"Last Image",Toast.LENGTH_SHORT).show();
                }
            }
        });

        images = new ArrayList<>();
        images3d = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();

        nDialog = fragmentView.findViewById(R.id.progress_loader);
        nDialog.setVisibility(View.VISIBLE);

        panoWidgetView = fragmentView.findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());

        gp = new GetPhotos(pid);
        gp.execute((Void) null);

        return fragmentView;
    }

    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
            panoWidgetView.loadImageFromBitmap(bitmap, panoOptions);
            GlobalVars.PhotoActivityMap.put(ids.get(cPos),bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    public Boolean setPanoImage(Integer cid) {

        nDialog.setVisibility(View.VISIBLE);
        if(GlobalVars.PhotoActivityMap.containsKey(ids.get(cid))) {
            Bitmap bm = null;
            bm = GlobalVars.PhotoActivityMap.get(ids.get(cid));
            panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
            panoWidgetView.loadImageFromBitmap(bm, panoOptions);
        }else{
            String img = images3d.get(cid);
            Context c = getActivity().getApplicationContext();
            Picasso.with(c).load(img).into(target);
        }
        return true;
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
            nDialog.setVisibility(View.INVISIBLE);
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    getActivity(), "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetPhotos extends AsyncTask<Void, Void, Boolean> {

        private final String mPid;

        private String errorMessage = null;

        GetPhotos(String pid) {
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
                postDataParams.put("d_type", "photos");

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray photos = rootJSON.getJSONObject("results").getJSONArray("photos");

                        for (int i = 0; i < photos.length(); i++) {
                            String url = getString(R.string.image_url)+'/'+photos.getJSONObject(i).getString("thumb_url");
                            images.add(url);
                            images3d.add(getString(R.string.image_url)+'/'+photos.getJSONObject(i).getString("image_url"));
                            names.add( photos.getJSONObject(i).getString("image_title"));
                            ids.add( photos.getJSONObject(i).getString("id"));
                        }
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
            try {
                if (success) {
                    setPanoImage(cPos);
                } else {
                    if (errorMessage != "") {
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
