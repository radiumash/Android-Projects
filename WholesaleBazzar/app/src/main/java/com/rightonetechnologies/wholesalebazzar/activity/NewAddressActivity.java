package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.MultipartUtility;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NewAddressActivity extends AppCompatActivity {

    ProgressBar mProgress;
    private SessionManager session;
    private EditText mTitle, mAddress, mPhone;
    private Button mSubmit;
    private CheckBox mCheck;
    private String message;
    private HashMap<String,String> user;

    private SaveAddress su;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_address);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }

        session = new SessionManager(getApplicationContext());
        if(session.isLoggedIn()){
            user = session.getUserDetails();
        }else{
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        this.setTitle(getString(R.string.activity_newaddress));

        mProgress = findViewById(R.id.mProgress);
        mTitle = findViewById(R.id.txtTitle);
        mAddress = findViewById(R.id.txtAddress);
        mPhone = findViewById(R.id.txtAltPhone);
        mSubmit = findViewById(R.id.mButton);
        mCheck = findViewById(R.id.chkDefaultAddress);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTitle.equals("null")) {
                    Toast.makeText(getApplicationContext(), R.string.errorBlankCity, Toast.LENGTH_SHORT).show();
                }else if(mAddress.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.errorBlankName, Toast.LENGTH_SHORT).show();
                }else{
                    mSubmit.setEnabled(false);
                    mProgress.setVisibility(View.VISIBLE);
                    String title = mTitle.getText().toString();
                    String address = mAddress.getText().toString();
                    String phone = mPhone.getText().toString();
                    String defC = "0";
                    String userid = user.get("user_id");
                    if(mCheck.isChecked()){
                        defC = "1";
                    }
                    su = new SaveAddress();
                    su.execute(title, address, phone, defC, userid);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Represents an asynchronous Save User task
     */
    public class SaveAddress extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String charset = "UTF-8";
            String requestURL = getString(R.string.server_url);

            String mTitle = params[0];
            String mAddress = params[1];
            String mPhone = params[2];
            String mDef = params[3];
            String mUser = params[4];

            try {
                MultipartUtility multipart = new MultipartUtility(requestURL, charset);
                multipart.addHeaderField("encrtype","multipart/form-data");
                multipart.addFormField("task", "user");
                multipart.addFormField("action", "save_address");
                multipart.addFormField("key", getString(R.string.api_key));
                multipart.addFormField("user_id", mUser);
                multipart.addFormField("title", mTitle);
                multipart.addFormField("address",mAddress);
                multipart.addFormField("phone", mPhone);
                multipart.addFormField("is_default", mDef);
                multipart.addFormField("published","1");

                String jsonResponse = multipart.finish(); // response from server.
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                mProgress.setVisibility(View.INVISIBLE);
                Intent i = new Intent(getApplicationContext(), AddressActivity.class);
                i.putExtra("MESSAGE", "");
                startActivity(i);
                finish();
            }else{
                mProgress.setVisibility(View.INVISIBLE);
                mSubmit.setEnabled(true);
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }
        }
    }
}
