package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.common.SessionManager;
import com.EEEITSolutions.elearning.network.ConnectivityReceiver;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.MediaController;
import android.widget.Toast;

public class VideoActivity extends AppCompatActivity {
    SessionManager session;
    private String video_id, video_name, video_url;
    MediaController mc;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

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
            video_id = extra.getString("ID");
            video_name = extra.getString("NAME");
            video_url = extra.getString("URL");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if(!video_name.isEmpty()) {
            webView = (WebView) findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(getString(R.string.video_url) + video_url);
        }else{
            Toast.makeText(getApplicationContext(),"Unable to play video",Toast.LENGTH_LONG).show();
        }
    }
}
