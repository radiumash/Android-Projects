package com.crocosauruscove.croccove;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TrailActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    Button mRanger, mDundee;
    SessionManager session;
    ProgressBar mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail);

        ConnectivityReceiver cr;
        cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        mDialog = findViewById(R.id.progress_loader);
        mDundee = findViewById(R.id.btnDundee);
        mRanger = findViewById(R.id.btnRangers);

        session = new SessionManager(getApplicationContext());
        mDundee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(session.isLoggedIn()){
                    session.setUserDetails(SessionManager.KEY_TRAIL,"Kids_");
                    mDialog.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            Intent mainIntent = new Intent(TrailActivity.this,MainActivity.class);
                            TrailActivity.this.startActivity(mainIntent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            mDialog.setVisibility(View.INVISIBLE);
                        }
                    }, SPLASH_DISPLAY_LENGTH);

                }else{
                    session.checkLogin();
                }
            }
        });

        mRanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(session.isLoggedIn()){
                    session.setUserDetails(SessionManager.KEY_TRAIL,"");
                    mDialog.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            Intent mainIntent = new Intent(TrailActivity.this,MainActivity.class);
                            TrailActivity.this.startActivity(mainIntent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            mDialog.setVisibility(View.INVISIBLE);
                        }
                    }, SPLASH_DISPLAY_LENGTH);

                }else{
                    session.checkLogin();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
