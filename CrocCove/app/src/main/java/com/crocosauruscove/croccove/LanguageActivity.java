package com.crocosauruscove.croccove;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private long lastBackPressTime = 0;
    private Toast toast;
    Button mEnglish, mGerman, mFrench, mChinese, mItalian;
    String languageUrl;
    String langID, downloadPath;
    SessionManager session;
    ProgressBar mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        ConnectivityReceiver cr;
        cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        session = new SessionManager(getApplicationContext());
        mDialog = findViewById(R.id.progress_loader);

        mEnglish = findViewById(R.id.btnEnglish);
        mGerman = findViewById(R.id.btnGerman);
        mFrench = findViewById(R.id.btnFrench);
        mChinese = findViewById(R.id.btnChinese);
        mItalian = findViewById(R.id.btnItalian);

        mEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnabled();
                Toast.makeText(getApplicationContext(),getString(R.string.file_download),Toast.LENGTH_LONG).show();
                GetLanguageArchive gla = new GetLanguageArchive("1");
                gla.execute();
            }
        });

        mChinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnabled();
                Toast.makeText(getApplicationContext(),getString(R.string.file_download),Toast.LENGTH_LONG).show();
                GetLanguageArchive gla = new GetLanguageArchive("2");
                gla.execute();
            }
        });

        mFrench.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnabled();
                Toast.makeText(getApplicationContext(),getString(R.string.file_download),Toast.LENGTH_LONG).show();
                GetLanguageArchive gla = new GetLanguageArchive("3");
                gla.execute();
            }
        });

        mItalian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnabled();
                Toast.makeText(getApplicationContext(),getString(R.string.file_download),Toast.LENGTH_LONG).show();
                GetLanguageArchive gla = new GetLanguageArchive("4");
                gla.execute();
            }
        });

        mGerman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnabled();
                Toast.makeText(getApplicationContext(),getString(R.string.file_download),Toast.LENGTH_LONG).show();
                GetLanguageArchive gla = new GetLanguageArchive("5");
                gla.execute();
            }
        });
    }

    private void loadActivity(){
        session.createLoginSession(langID,downloadPath);
        Vibrator vibrator = (Vibrator)getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
        Toast.makeText(getApplicationContext(),getString(R.string.download_complete),Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent i = null;
                if(langID.equals("English")){
                    i = new Intent(getApplicationContext(), TrailActivity.class);
                }else {
                    i = new Intent(getApplicationContext(), MainActivity.class);
                }
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void downloadAndUnzipContent(String url){
        String filename = URLUtil.guessFileName(url, null, null);
        downloadPath = this.getFilesDir().toString();

        File file = new File(this.getFilesDir(), filename);
        if (file.exists ()){
            file.delete();
        }
        DownloadFileAsync download = new DownloadFileAsync(file.toString(), this, new DownloadFileAsync.PostDownload() {
            @Override
            public void downloadDone(File file) {
                Decompress unzip = new Decompress(LanguageActivity.this, file);
                unzip.unzip();
                loadActivity();
            }
        });
        download.execute(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDisabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setEnabled();
    }

    @Override
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG);
            toast.show();
            this.lastBackPressTime = System.currentTimeMillis();
        } else {
            if (toast != null) {
                toast.cancel();
            }
            super.onBackPressed();
        }
        setDisabled();
    }

    public void setEnabled(){
        mDialog.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void setDisabled(){
        mDialog.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    /**
     * Represents an asynchronous Get Language Archive Object task
     */
    public class GetLanguageArchive extends AsyncTask<Void, Void, Boolean> {

        private String errorMessage = null;
        String mLang;
        GetLanguageArchive(String lang){
            mLang = lang;
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
                postDataParams.put("action", "get_zip");
                postDataParams.put("lang_id", mLang);
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    if (success.equals(true)) {
                        JSONObject language = rootJSON.getJSONObject("results").getJSONObject("language");
                        String filename = language.getString("archive");
                        String url = getString(R.string.zip_url) + filename;
                        //String url = getString(R.string.zip_url) + "test.zip";
                        langID = language.getString("name");
                        languageUrl = url;
                        return true;
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
                    if(!languageUrl.equals("")){
                        downloadAndUnzipContent(languageUrl);
                    }
                } else {
                    if (errorMessage != "") {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
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


