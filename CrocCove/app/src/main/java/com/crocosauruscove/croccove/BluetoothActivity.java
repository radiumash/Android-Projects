package com.crocosauruscove.croccove;

import android.accounts.AccountManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class BluetoothActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    public static final int REQUEST_ENABLE_BT = 1;

    int i = 1;
    Button mNext;
    ProgressBar mDialog;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ConnectivityReceiver cr;
        cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }

        mNext = findViewById(R.id.btnNext);
        mDialog = findViewById(R.id.progress_loader);
        enableBluetooth();
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNext.setEnabled(false);
                mDialog.setVisibility(View.VISIBLE);
                if(isBTEnabled()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent mainIntent = new Intent(BluetoothActivity.this, LanguageActivity.class);
                            BluetoothActivity.this.startActivity(mainIntent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            mDialog.setVisibility(View.INVISIBLE);
                            BluetoothActivity.this.finish();
                        }
                    }, SPLASH_DISPLAY_LENGTH);
                }else{
                    enableBluetooth();
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

    private void enableBluetooth(){
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private Boolean isBTEnabled(){
        if (!mBluetoothAdapter.isEnabled()){
            return false;
        }else{
            return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            return;
        }else{
            if(requestCode == REQUEST_ENABLE_BT){
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "You need Bluetooth to run this application", Toast.LENGTH_LONG).show();
                }
            }
        }
        mNext.setEnabled(true);
        mDialog.setVisibility(View.INVISIBLE);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
