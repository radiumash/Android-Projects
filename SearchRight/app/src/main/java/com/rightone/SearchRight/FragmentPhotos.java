package com.rightone.SearchRight;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentPhotos extends Fragment {
    String pid = null;
    //GridView Object
    private GridView gridView;

    //ArrayList for Storing image urls and titles
    private ArrayList<String> images;
    private ArrayList<String> images3d;
    private ArrayList<String> names;
    private ArrayList<String> ids;

    GetPhotos gp;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_photos, container, false);
        pid = getArguments().getString("ID");

        gridView = fragmentView.findViewById(R.id.gridView);
        images = new ArrayList<>();
        images3d = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Sending image id to FullScreenActivity
//                Intent i = new Intent(getActivity(), PhotoActivity.class);
//                // passing array index
//                i.putExtra("ID", ids.get(position));
//                i.putExtra("URL", images3d.get(position));
//                i.putExtra("NAME",names.get(position));
//                startActivity(i);
            }
        });
        gp = new GetPhotos(pid);
        gp.execute((Void) null);

        return fragmentView;
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
                    //Creating GridViewAdapter Object
                    GridViewAdapter gridViewAdapter = new GridViewAdapter(getActivity(),images,names,ids);

                    //Adding adapter to gridview
                    gridView.setAdapter(gridViewAdapter);
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
