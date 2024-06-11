package com.rightone.ROChildWater;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;

public class PhotoActivity extends AppCompatActivity {

    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();

    String photo_id, photo_url, photo_name;
    GetPhoto gp;
    ProgressBar nDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        nDialog = findViewById(R.id.progress_loader);
        nDialog.setVisibility(View.VISIBLE);

        Bundle extra = getIntent().getExtras();
        try {
            photo_id = extra.getString("ID");
            photo_url = extra.getString("URL");
            photo_name = extra.getString("NAME");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        this.setTitle(photo_name);

        panoWidgetView = findViewById(R.id.pano_view);

        gp = new GetPhoto(photo_url, panoWidgetView);
        gp.execute((Void) null);

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
    protected void onPause() {
        panoWidgetView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoWidgetView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (gp != null) {
            gp = null;
        }
        super.onDestroy();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetPhoto extends AsyncTask<Void, Void, Bitmap> {

            String mUrl;
            VrPanoramaView mImageView;

            public GetPhoto(String url, VrPanoramaView imageView) {
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
                if(GlobalVars.PhotoActivityMap.containsKey(photo_id)){
                    bm = GlobalVars.PhotoActivityMap.get(photo_id);
                }else{
                    NetworkADO networkADO = new NetworkADO();
                    bm = networkADO.getBitmapFromURL(mUrl);
                    GlobalVars.PhotoActivityMap.put(photo_id,bm);
                }

                return bm;
            }

            @Override
            // Once the image is downloaded, associates it to the imageView
            protected void onPostExecute(Bitmap bitmap) {
                if (isCancelled()) {
                    bitmap = null;
                }
                if(bitmap !=null){
                    panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
                    mImageView.loadImageFromBitmap(bitmap, panoOptions);
                    nDialog.setVisibility(View.INVISIBLE);
                }
            }
        }
}
