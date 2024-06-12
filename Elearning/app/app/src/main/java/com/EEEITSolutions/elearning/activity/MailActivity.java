package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.common.SessionManager;
import com.EEEITSolutions.elearning.network.ConnectivityReceiver;
import com.EEEITSolutions.elearning.network.NetworkADO;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MailActivity extends AppCompatActivity {

    SessionManager session;
    private HashMap<String,String> user;
    AutoCompleteTextView mName,mEmail,mMobile,mMessage;
    String tvName,tvEmail,tvMobile,tvMessage;
    ConnectivityReceiver cr;
    Button mSend;
    ProgressBar mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());
        if(!session.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        this.setTitle(getString(R.string.contact_us));
        user = session.getUserDetails();

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ImageView imgMail = findViewById(R.id.imgMail);
        Picasso.with(getApplicationContext())
                .load(getString(R.string.image_url) + getString(R.string.mail_path))
                .placeholder(R.drawable.thumb_placeholder)
                .into(imgMail);

        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mMobile = findViewById(R.id.mobile);
        mMessage = findViewById(R.id.message);

        mName.setText(user.get("name"));

        mSend = findViewById(R.id.btnSend);
        mDialog = findViewById(R.id.progress_loader);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setVisibility(View.VISIBLE);
                if (cr.isConnected()){
                    tvName = mName.getText().toString();
                    tvEmail = mEmail.getText().toString();
                    tvMobile = mMobile.getText().toString();
                    tvMessage = mMessage.getText().toString();

                    if (tvMessage.trim().length() == 0){
                        Toast.makeText(getApplicationContext(), getString(R.string.check_message), Toast.LENGTH_SHORT).show();
                        mDialog.setVisibility(View.INVISIBLE);
                    }else if(!cr.isConnected()){
                        Toast.makeText(getApplicationContext(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                        mDialog.setVisibility(View.INVISIBLE);
                    }else {
                        new SaveContact().execute(tvName, tvEmail, tvMobile, tvMessage);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
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

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class SaveContact extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String mName = params[0];
            String mEmail = params[1];
            String mMobile = params[2];
            String mMessage = params[3];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("emailid", mEmail);
                postDataParams.put("mobileno", mMobile);
                postDataParams.put("query", mMessage);
                postDataParams.put("studentid", user.get("user_id"));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url) + "contactus");
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");

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
                mEmail.setText("");
                mMobile.setText("");
                mMessage.setText("");
                Toast.makeText(getApplicationContext(), getString(R.string.message_saved), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.message_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
