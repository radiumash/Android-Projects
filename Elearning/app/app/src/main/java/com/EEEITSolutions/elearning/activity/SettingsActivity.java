package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.common.SearchableEditText;
import com.EEEITSolutions.elearning.common.SessionManager;
import com.EEEITSolutions.elearning.network.ConnectivityReceiver;
import com.EEEITSolutions.elearning.network.NetworkADO;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private String studentId, oldPassword, studentLanguage, studentName, imageUrl;
    private String newPassword, newLanguageID;
    Button mButton;
    EditText mPassword, mRePassword;
    SearchableEditText mLanguage;
    ImageView mProfilePic;
    TextView mName;
    String[] languageArray;
    HashMap<Integer,String> languageMap;
    ArrayAdapter<String> languageArrayAdapt;
    GetUserData gu;
    SaveUserData su;
    ProgressBar mProgress;
    SessionManager session;
    HashMap<String,String> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            user = session.getUserDetails();
            studentId = user.get("user_id");
        }
        setLanguage(user.get("language_name"));
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mProfilePic = findViewById(R.id.profilePic);
        mName = findViewById(R.id.studentName);
        mPassword = findViewById(R.id.password);
        mRePassword = findViewById(R.id.rePassword);
        mLanguage = findViewById(R.id.languageID);
        mButton = findViewById(R.id.submit);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateUserData()){
                    mProgress.setVisibility(View.VISIBLE);
                    su = new SaveUserData();
                    su.execute(studentId,newLanguageID,oldPassword,newPassword);
                }

            }
        });
        mProgress = findViewById(R.id.loading);
        mProgress.setVisibility(View.VISIBLE);

        gu = new GetUserData();
        gu.execute((Void) null);
    }

    private Boolean validateUserData(){
        boolean result = true;
        String messag = "";
        newLanguageID = String.valueOf(languageMap.get(languageArrayAdapt.getPosition(mLanguage.getText().toString())));
        if(!mPassword.getText().toString().isEmpty() && !mRePassword.getText().toString().isEmpty()){
            if(mPassword.getText().toString().equals(mRePassword.getText().toString())){
                newPassword = mPassword.getText().toString();
            }else{
                Toast.makeText(getApplicationContext(), R.string.errorPasswordNotMatch, Toast.LENGTH_SHORT).show();
                result = false;
            }
        } else if(newLanguageID.equals("null")){
            Toast.makeText(getApplicationContext(), R.string.errorLanguage, Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static Object getKeyFromValue(HashMap hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetUserData extends AsyncTask<Void, Void, Boolean> {

        private String errorMessage = null;
        ProgressBar progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;
            progress = findViewById(R.id.progress);
            try {

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("studentid",studentId);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url) + "studentdetail");

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONObject student = rootJSON.getJSONObject("results").getJSONObject("student");
                        JSONArray language = rootJSON.getJSONObject("results").getJSONArray("language");


                        oldPassword = student.getString("password");
                        studentLanguage = student.getString("languageid");
                        studentName = student.getString("studentname");
                        imageUrl = student.getString("imageurl");

                        languageArray = new String[language.length()+1];
                        languageArray[0] = "Select Language";
                        languageMap = new HashMap<>();

                        for (int i = 0; i < language.length(); i++) {
                            languageMap.put(i+1, language.getJSONObject(i).getString("languageid"));
                            languageArray[i+1] = language.getJSONObject(i).getString("languagename");
                        }
                        languageArrayAdapt = new ArrayAdapter(SettingsActivity.this, android.R.layout.simple_list_item_1, languageArray);
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
                    if (languageArrayAdapt != null) {
                        mLanguage.setAdapter(languageArrayAdapt);
                        Picasso.with(getApplicationContext())
                                .load(getString(R.string.image_url) + "/" + imageUrl)
                                .networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE)
                                .placeholder(R.drawable.thumb_placeholder) // optional
                                .into(mProfilePic);
                        mName.setText(studentName);
                        Object sValue = Object.class.cast(studentLanguage);
                        Object sKey = getKeyFromValue(languageMap, sValue);
                        mLanguage.setSelectedItemPosition(languageArrayAdapt.getItem(Integer.parseInt(sKey.toString())));
                    }
                } else {
                    if (errorMessage != "") {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            mProgress.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgress.setVisibility(View.GONE);
        }
    }

    private void setLanguage(String languageToLoad){
        String default_lang = "hi";
        switch (languageToLoad){
            case "null":
            case "":
            case "Hindi":
                default_lang = "hi";
                break;

            case "English":
                default_lang = "en";
                break;
        }
        Locale locale = new Locale(default_lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_settings);
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class SaveUserData extends AsyncTask<String, Void, Boolean> {

        private String errorMessage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String mStudentID = params[0];
            String mLanguageID = params[1];
            String mOldPassword = params[2];
            String mNewPassword = params[3];

            NetworkADO networkADO;
            String jsonResponse;

            try {

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("studentid",mStudentID);
                postDataParams.put("languageid",mLanguageID);
                postDataParams.put("oldpassword",mOldPassword);
                postDataParams.put("newpassword",mNewPassword);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url) + "studentupdate");

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");
                    if (success.equals(true)) {
                        return true;
                    }else{
                        errorMessage = message;
                        return false;
                    }

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
                    setLanguage(mLanguage.getText().toString());
                    Toast.makeText(getApplicationContext(), getString(R.string.dataupdate), Toast.LENGTH_LONG).show();
                    session.logoutUser();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.unabletofetchdata), Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            mProgress.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgress.setVisibility(View.GONE);
        }
    }
}
