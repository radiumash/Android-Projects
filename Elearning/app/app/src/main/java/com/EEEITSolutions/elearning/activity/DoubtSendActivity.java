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

public class DoubtSendActivity extends AppCompatActivity {

    ConnectivityReceiver cr;
    SessionManager session;
    ProgressBar mProgress;
    private HashMap<String,String> user;
    AutoCompleteTextView mSubject, mMessage;
    String subjectID, chapterID;
    String cid = "";
    Button mSend;
    ProgressBar mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubt_send);

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

        this.setTitle(getString(R.string.menu_doubt));

        user = session.getUserDetails();
        Bundle extra = getIntent().getExtras();
        try {
            cid = extra.getString("ID");
            subjectID = extra.getString("SUBJECTID");
            chapterID = extra.getString("CHAPTERID");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ImageView imgMail = findViewById(R.id.imgMail);
        Picasso.with(getApplicationContext())
                .load(getString(R.string.image_url) + getString(R.string.mail_path))
                .placeholder(R.drawable.thumb_placeholder)
                .into(imgMail);
        mSend = findViewById(R.id.btnSend);
        mDialog = findViewById(R.id.progress_loader);
        mSubject = findViewById(R.id.subject);
        mMessage = findViewById(R.id.message);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setVisibility(View.VISIBLE);
                if (cr.isConnected()){
                    String tvSubject = mSubject.getText().toString();
                    String tvMessage = mMessage.getText().toString();
                    if (tvSubject.trim().length() == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.check_subject), Toast.LENGTH_SHORT).show();
                        mDialog.setVisibility(View.INVISIBLE);
                    }else if (tvMessage.trim().length() == 0){
                        Toast.makeText(getApplicationContext(), getString(R.string.check_message), Toast.LENGTH_SHORT).show();
                        mDialog.setVisibility(View.INVISIBLE);
                    }else if(!cr.isConnected()){
                        Toast.makeText(getApplicationContext(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                        mDialog.setVisibility(View.INVISIBLE);
                    }else {
                        new SaveDoubt().execute(tvSubject, tvMessage);
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
    public class SaveDoubt extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String mSub = params[0];
            String mMess = params[1];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("key", getString(R.string.api_key));

                postDataParams.put("studentid", user.get("user_id"));
                postDataParams.put("subjectid", subjectID);
                postDataParams.put("chaptertid", chapterID);
                postDataParams.put("topictid", cid);
                postDataParams.put("doubtsubject", mSub);
                postDataParams.put("doubt", mMess );

                networkADO = new NetworkADO();
                 jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url) + "studentdoubtsave");
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
                mSubject.setText("");
                mMessage.setText("");
                Toast.makeText(getApplicationContext(), getString(R.string.message_saved), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.message_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
