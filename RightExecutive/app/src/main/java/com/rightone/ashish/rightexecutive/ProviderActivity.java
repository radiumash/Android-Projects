package com.rightone.ashish.rightexecutive;

import android.content.Context;
import android.net.Uri;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class ProviderActivity extends AppCompatActivity {

    Boolean isNew = true;
    String message = null;
    SessionManager session;
    String executiveId, otp;
    String providerId, serviceId;
    JSONObject providerObj;
    JSONObject pObject;
    Boolean isProPicChg = false;
    Boolean isBusiPicChg = false;

    TextView mProName, mSpeciality, mBusinessName, mAddress, mPhone, mMobile, mZip, mLatitude, mLongitude, mEmail, mWebsite, mDescription;
    SearchableEditText myState, myCategory, myService, mySubscription;
    HashMap<Integer,String> stateMap, categoryMap, serviceMap, subscriptionMap;
    ArrayAdapter<String> stateArrayAdapt, categoryArrayAdapter, serviceArrayAdapter, subscriptionArrayAdapter;

    ImageView mProPic, mBusinessPic, mGmap;
    String[] stateArray, categoryArray, serviceArray, subscriptionArray;

    SearchableEditText mCity, mLocality;
    String[] cityArray, localityArray;
    HashMap<Integer,String> cityMap, localityMap;
    ArrayAdapter<String> cityArrayAdapt, localityArrayAdapter;
    ProgressBar mDialog;
    Button mSave;
    ProviderInit pInit;
    PageInit pageInit;
    GetService gs;
    GetCity gc;
    GetLocality gl;

    String vLatitude = "";
    String vLongitude = "";
    String vBusinessName = "";

    ConnectivityReceiver cr;

    private static int RESULT_PROFILE_IMAGE = 1;
    private static int RESULT_BG_IMAGE = 2;
    private static int RESULT_LAT_LONG = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        mDialog = findViewById(R.id.progress_loader);
        mSave = findViewById(R.id.btnSave);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setVisibility(View.VISIBLE);
                saveProvider();

            }
        });

        mProName = findViewById(R.id.providerName);
        mSpeciality = findViewById(R.id.specility);
        mBusinessName = findViewById(R.id.businessName);
        mAddress = findViewById(R.id.address);
        mPhone = findViewById(R.id.otherNumber);
        mMobile = findViewById(R.id.mobile);
        mZip = findViewById(R.id.zipCode);
        mGmap = findViewById(R.id.map);
        mLatitude = findViewById(R.id.latitude);
        mLongitude = findViewById(R.id.longitude);
        mEmail = findViewById(R.id.email);
        mWebsite = findViewById(R.id.website);
        mDescription = findViewById(R.id.description);

        mProPic = findViewById(R.id.mImagePerson);
        mBusinessPic = findViewById(R.id.mImageBg);

        mProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_PROFILE_IMAGE);
            }
        });

        mBusinessPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_BG_IMAGE);
            }
        });

        myState = findViewById(R.id.stateName);
        myCategory = findViewById(R.id.categoryName);
        myService = findViewById(R.id.serviceName);
        mySubscription = findViewById(R.id.subscriptionName);
        mCity = findViewById(R.id.cityName);
        mLocality = findViewById(R.id.locationName);

        Bundle extra = getIntent().getExtras();

        if(extra != null) {
            session = new SessionManager(getApplicationContext());
            executiveId = extra.getString("ExecutiveID");
            providerId = extra.getString("ProviderID");
            serviceId = extra.getString("ServiceID");
            isNew = extra.getBoolean("IS_NEW");
        }

        if(isNew){
            this.setTitle("Add New Business/Provider");
        }else{
            this.setTitle("Update Business/Provider");
        }
        myState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("Select State") && !s.toString().equals("")) {
                    String state_id = String.valueOf(stateMap.get(stateArrayAdapt.getPosition(s.toString())));
                    gc = new GetCity(state_id);
                    gc.execute((Void) null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        mCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("Select City") && !s.toString().equals("")) {
                    String city_id = String.valueOf(cityMap.get(cityArrayAdapt.getPosition(s.toString())));
                    gl = new GetLocality(city_id);
                    gl.execute((Void) null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        myCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("Select Category") && !s.toString().equals("")) {
                    String category_id = String.valueOf(categoryMap.get(categoryArrayAdapter.getPosition(s.toString())));
                    gs = new GetService(category_id);
                    gs.execute((Void) null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        mGmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapActivity();
            }
        });
        if(isNew){
            pageInit = new PageInit();
            pageInit.execute((Void) null);

        }else{
            pageInit = new PageInit();
            pageInit.execute((Void) null);

            pInit = new ProviderInit();
            pInit.execute((Void) null);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveProvider() {
        if (!cr.isConnected()) {
            Toast.makeText(getApplicationContext(), getString(R.string.check_internet), Toast.LENGTH_LONG).show();
            mDialog.setVisibility(View.INVISIBLE);
        } else {
            String mProNameS = mProName.getText().toString();
            String mSpecialityS = mSpeciality.getText().toString();
            String mBusinessNameS = mBusinessName.getText().toString();
            String mAddressS = mAddress.getText().toString();
            String mPhoneS = mPhone.getText().toString();
            String mMobileS = mMobile.getText().toString();
            String mZipS = mZip.getText().toString();
            String mLatitudeS = mLatitude.getText().toString();
            String mLongitudeS = mLongitude.getText().toString();

            String myStateS = String.valueOf(stateMap.get(stateArrayAdapt.getPosition(myState.getText().toString())));
            String myCategoryS = String.valueOf(categoryMap.get(categoryArrayAdapter.getPosition(myCategory.getText().toString())));
            String myServiceS = "";
            if(serviceArrayAdapter != null) {
                myServiceS = String.valueOf(serviceMap.get(serviceArrayAdapter.getPosition(myService.getText().toString())));
            }
            String mySubscriptionS = String.valueOf(subscriptionMap.get(subscriptionArrayAdapter.getPosition(mySubscription.getText().toString())));
            String mCityS = "";
            if(cityArrayAdapt != null) {
                mCityS = String.valueOf(cityMap.get(cityArrayAdapt.getPosition(mCity.getText().toString())));
            }
            String mLocalityS = "";
            if(localityArrayAdapter != null) {
                mLocalityS = String.valueOf(localityMap.get(localityArrayAdapter.getPosition(mLocality.getText().toString())));
            }
            String mDescriptionS = mDescription.getText().toString();
            String mEmailS = mEmail.getText().toString();
            String mWebsiteS = mWebsite.getText().toString();

            String mProPicS = "";
            if(mProPic.getTag() != null) {
                mProPicS = mProPic.getTag().toString();
            }
            String mBusinessPicS = "";
            if(mProPic.getTag() != null) {
                mBusinessPicS = mBusinessPic.getTag().toString();
            }

            Boolean error = false;
            if (mProPicS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_profile_pic), Toast.LENGTH_SHORT).show();
            }else if (mBusinessPicS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_business_pic), Toast.LENGTH_SHORT).show();
            }else if (mProNameS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_owner_name), Toast.LENGTH_SHORT).show();
            } else if (mSpecialityS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_speciality), Toast.LENGTH_SHORT).show();
            } else if (mBusinessNameS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_business_name), Toast.LENGTH_SHORT).show();
            } else if (mAddressS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_address), Toast.LENGTH_SHORT).show();
            } else if (mMobileS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_mobile_number), Toast.LENGTH_SHORT).show();
            } else if (mZipS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_zip_code), Toast.LENGTH_SHORT).show();
            } else if (mLatitudeS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_latitude), Toast.LENGTH_SHORT).show();
            } else if (mLongitudeS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_longitude), Toast.LENGTH_SHORT).show();
            } else if (myStateS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_state_name), Toast.LENGTH_SHORT).show();
            } else if (myCategoryS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_category_name), Toast.LENGTH_SHORT).show();
            } else if (myServiceS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_service_name), Toast.LENGTH_SHORT).show();
            } else if (mySubscriptionS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_subscription), Toast.LENGTH_SHORT).show();
            } else if (mCityS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_city_name), Toast.LENGTH_SHORT).show();
            } else if (mLocalityS.trim().length() == 0) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_location_name), Toast.LENGTH_SHORT).show();
            } else if (executiveId.equals("")) {
                error = true;
                Toast.makeText(this, getString(R.string.chk_executive), Toast.LENGTH_SHORT).show();
            } else if (!cr.isConnected()) {
                error = true;
                Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            } else {
                SaveProvider sp = new SaveProvider();
                sp.execute(mProNameS, mSpecialityS, mBusinessNameS, mAddressS, mPhoneS, mMobileS, mZipS, mLatitudeS, mLongitudeS, myStateS, myServiceS, mySubscriptionS, mCityS, mLocalityS, mProPicS, mBusinessPicS, executiveId, mDescriptionS, mEmailS, mWebsiteS);
            }
             if(error){
                 mDialog.setVisibility(View.INVISIBLE);
             }
        }
    }

    /**
     * Represents an asynchronous Save Provider task
     */
    public class SaveProvider extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String charset = "UTF-8";
            String requestURL = getString(R.string.server_url);
            String mIdS = "";
            if (!isNew && providerObj != null) {
                try {
                    mIdS = providerObj.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String mProNameS = params[0];
            String mSpecialityS = params[1];
            String mBusinessNameS = params[2];
            String mAddressS = params[3];
            String mPhoneS = params[4];
            String mMobileS = params[5];
            String mZipS = params[6];
            String mLatitudeS = params[7];
            String mLongitudeS = params[8];
            String myStateS = params[9];
            String myServiceS = params[10];
            String mySubscriptionS = params[11];
            String mCityS = params[12];
            String mLocalityS = params[13];
            String mProPicS = "";
            if (isProPicChg) {
                mProPicS = scaleDown(params[14], 480, 320, false);
            }
            String mBusinessPicS = "";
            if (isBusiPicChg){
                mBusinessPicS = scaleDown(params[15], 480, 320, false);
            }
            String mExecutiveS = params[16];
            String mDescriptionS = params[17];
            String mEmailS = params[18];
            String mWebsiteS = params[19];

            try {
                MultipartUtility multipart = new MultipartUtility(requestURL, charset);

                multipart.addHeaderField("encrtype","multipart/form-data");

                multipart.addFormField("task", "executive");
                multipart.addFormField("action", "save_provider");
                multipart.addFormField("key", getString(R.string.api_key));
                if(mIdS != "") {
                    multipart.addFormField("pro_id", mIdS);
                }
                multipart.addFormField("pro_name", mProNameS);
                multipart.addFormField("pro_name_visible","1");
                multipart.addFormField("specialities", mSpecialityS);
                multipart.addFormField("business_name", mBusinessNameS);
                multipart.addFormField("address", mAddressS);
                multipart.addFormField("phone", mPhoneS);
                multipart.addFormField("mobile", mMobileS);
                multipart.addFormField("mobile_visible","1");
                multipart.addFormField("zip", mZipS);
                multipart.addFormField("latitude", mLatitudeS);
                multipart.addFormField("longitude", mLongitudeS);
                multipart.addFormField("province_id", myStateS);
                multipart.addFormField("service_id", myServiceS);
                multipart.addFormField("subscription_id", mySubscriptionS);
                multipart.addFormField("city_id", mCityS);
                multipart.addFormField("location_id", mLocalityS);
                if(isProPicChg) {
                    multipart.addFilePart("pro_pic", new File(mProPicS));
                }
                if(isBusiPicChg) {
                    multipart.addFilePart("business_pic", new File(mBusinessPicS));
                }
                multipart.addFormField("executive_id", mExecutiveS);
                multipart.addFormField("description", mDescriptionS);
                multipart.addFormField("email", mEmailS);
                multipart.addFormField("website", mWebsiteS);
                multipart.addFormField("published","0");

                String jsonResponse = multipart.finish(); // response from server.
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        otp = rootJSON.getJSONObject("results").getString("otp");
                        pObject = rootJSON.getJSONObject("results").getJSONObject("provider");
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
                mDialog.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),getString(R.string.provider_success),Toast.LENGTH_LONG).show();
                try {
                    Intent i = new Intent(getApplicationContext(), OtpActivity.class);
                    i.putExtra("OtpForSending", otp);
                    i.putExtra("USER", "PROVIDER");
                    i.putExtra("provider_id", pObject.getString("id"));
                    i.putExtra("subscription_id", pObject.getString("subscription_id"));
                    i.putExtra("executive_id", pObject.getString("executive_id"));
                    startActivity(i);
                    finish();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else{
                mDialog.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }
        }
    }

    public String scaleDown(String url, int width, int height,
                                   boolean filter) {
        Uri imageUri = Uri.parse(url);
        //String selectedImageUri = getRealPath(imageUri);
        //String dir = new File(selectedImageUri).getParent();
        //File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e){
            e.printStackTrace();
        }
        Bitmap out = getScaledBitmap(b,width);
        //Bitmap out = Bitmap.createScaledBitmap(b, width, height, filter);
        String filename = getSaltString() + ".jpg";
        File file = new File(this.getFilesDir(), filename);
        if (file.exists ()) file.delete ();
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            outputStream.write(bytes.toByteArray());
            outputStream.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    protected Bitmap getScaledBitmap(Bitmap bm, Integer maxSize){
        int outWidth;
        int outHeight;
        int inWidth = bm.getWidth();
        int inHeight = bm.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
        return resizedBitmap;
    }

    protected String getSaltString() {
        String saltCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * saltCHARS.length());
            salt.append(saltCHARS.charAt(index));
        }
        return salt.toString();

    }

    public void showMapActivity(){
        if(!isNew) {
            try {
                vLatitude = providerObj.getString("latitude");
                vLongitude = providerObj.getString("longitude");
                vBusinessName = providerObj.getString("business_name");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        Intent i = new Intent(this, MapsActivity.class);

        i.putExtra("LAT", vLatitude);
        i.putExtra("LONG",vLongitude);
        i.putExtra("LOCATION",vBusinessName);
        //startActivityForResult(Intent.createChooser(i, "Select Lat/Long"), RESULT_LAT_LONG);
        startActivityForResult(i,RESULT_LAT_LONG);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            if(requestCode == RESULT_PROFILE_IMAGE){
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    isProPicChg = true;
                    ImageView imageView = findViewById(R.id.mImagePerson);
                    imageView.setImageURI(selectedImageUri);
                    imageView.setTag(selectedImageUri);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }else if(requestCode == RESULT_BG_IMAGE){
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    isBusiPicChg = true;
                    ImageView imageView = findViewById(R.id.mImageBg);
                    imageView.setImageURI(selectedImageUri);
                    imageView.setTag(selectedImageUri);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }else if(requestCode == RESULT_LAT_LONG){
                String latitude = data.getStringExtra("LAT");
                String longitude = data.getStringExtra("LONG");
                mLatitude.setText(latitude);
                mLongitude.setText(longitude);
            }
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class PageInit extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("task", "executive");
                postDataParams.put("action", "provider_init_data");
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray categories = rootJSON.getJSONObject("results").getJSONArray("categories");
                        JSONArray subscriptions = rootJSON.getJSONObject("results").getJSONArray("subscription");
                        JSONArray states = rootJSON.getJSONObject("results").getJSONArray("states");

                        stateArray = new String[states.length()+1];
                        stateArray[0] = "Select State";
                        stateMap = new HashMap<>();

                        for(int i=0;i<states.length();i++){
                            stateMap.put(i+1,states.getJSONObject(i).getString("id"));
                            stateArray[i+1] = states.getJSONObject(i).getString("state_name");
                        }
                        stateArrayAdapt = new ArrayAdapter(ProviderActivity.this, android.R.layout.simple_list_item_1,stateArray);

                        categoryArray = new String[categories.length()+1];
                        categoryArray[0] = "Select Category";
                        categoryMap = new HashMap<>();

                        for(int i=0;i<categories.length();i++){
                            categoryMap.put(i+1,categories.getJSONObject(i).getString("id"));
                            categoryArray[i+1] = categories.getJSONObject(i).getString("category_name");
                        }
                        categoryArrayAdapter = new ArrayAdapter(ProviderActivity.this, android.R.layout.simple_list_item_1,categoryArray);

                        subscriptionArray = new String[subscriptions.length()+1];
                        subscriptionArray[0] = "Select Subscription";
                        subscriptionMap = new HashMap<>();

                        for(int i=0;i<subscriptions.length();i++){
                            subscriptionMap.put(i+1,subscriptions.getJSONObject(i).getString("id"));
                            subscriptionArray[i+1] = subscriptions.getJSONObject(i).getString("subscription_title");
                        }

                        subscriptionArrayAdapter = new ArrayAdapter(ProviderActivity.this, android.R.layout.simple_list_item_1,subscriptionArray);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    if (stateArrayAdapt != null) {
                        myState.setAdapter(stateArrayAdapt);
                    }

                    if (categoryArrayAdapter != null) {
                        myCategory.setAdapter(categoryArrayAdapter);
                    }

                    if (subscriptionArrayAdapter != null) {
                        mySubscription.setAdapter(subscriptionArrayAdapter);
                    }
                } else {
                    if (message != "") {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class ProviderInit extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;

            try{
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "executive");
                postDataParams.put("action", "provider");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("provider_id", providerId);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean successP = rootJSON.getBoolean("success");
                    message = rootJSON.getString("message");

                    if (successP.equals(true)) {
                        providerObj = rootJSON.getJSONObject("results").getJSONObject("provider");
                    }else{
                        Toast.makeText(getApplicationContext(),"Something went wrong", Toast.LENGTH_LONG).show();
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
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    if (providerObj.getString("province_id") != null) {
                        //myState.setSelectedItemPosition(stateMap.get(Integer.parseInt(providerObj.getString("province_id"))));
                        Object sValue = Object.class.cast(providerObj.getString("province_id"));
                        Object sKey = getKeyFromValue(stateMap, sValue);
                        myState.setSelectedItemPosition(stateArrayAdapt.getItem(Integer.parseInt(sKey.toString())));
                    }
                    if (providerObj.getString("category_id") != null) {
                        Object sValue = Object.class.cast(providerObj.getString("category_id"));
                        Object sKey = getKeyFromValue(categoryMap, sValue);
                        myCategory.setSelectedItemPosition(categoryArrayAdapter.getItem(Integer.parseInt(sKey.toString())));
                    }

                    if (providerObj.getString("subscription_id") != null) {
                        Object sValue = Object.class.cast(providerObj.getString("subscription_id"));
                        Object sKey = getKeyFromValue(subscriptionMap, sValue);
                        mySubscription.setSelectedItemPosition(subscriptionArrayAdapter.getItem(Integer.parseInt(sKey.toString())));
                    }
                    executiveId = providerObj.getString("executive_id");
                    mProName.setText(providerObj.getString("pro_name"));
                    mBusinessName.setText(providerObj.getString("business_name"));
                    mAddress.setText(providerObj.getString("address"));
                    mMobile.setText(providerObj.getString("mobile"));
                    mPhone.setText(providerObj.getString("phone"));
                    mZip.setText(providerObj.getString("zip"));
                    mSpeciality.setText(providerObj.getString("specialities"));
                    mLatitude.setText(providerObj.getString("latitude"));
                    mLongitude.setText(providerObj.getString("longitude"));
                    mEmail.setText(providerObj.getString("email"));
                    mWebsite.setText(providerObj.getString("website"));
                    mDescription.setText(providerObj.getString("description"));

                    if(providerObj.getString("pro_pic") != ""){
                        String mpPic = getString(R.string.image_url) + '/' + providerObj.getString("pro_pic");
                        mProPic.setTag(Uri.parse(mpPic));
                        Picasso.with(getApplicationContext())
                                .load(mpPic)
                                .placeholder(R.drawable.thumb_placeholder) // optional
                                .into(mProPic);
                    }

                    if(providerObj.getString("business_pic") != ""){
                        String mbPic = getString(R.string.image_url) + '/' + providerObj.getString("business_pic");
                        mBusinessPic.setTag(Uri.parse(mbPic));
                        Picasso.with(getApplicationContext())
                                .load(mbPic)
                                .placeholder(R.drawable.banner_placeholder) // optional
                                .into(mBusinessPic);
                    }

                } else {
                    if (message != "") {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
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
    public class GetService extends AsyncTask<Void, Void, Boolean> {

        String mCategory;

        GetService(String cat){
            mCategory = cat;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(!mCategory.equals("")) {
                NetworkADO networkADO;
                String jsonResponse;

                try {
                    JSONObject postDataParams = new JSONObject();
                    postDataParams.put("task", "executive");
                    postDataParams.put("action", "get_services");
                    postDataParams.put("key", getString(R.string.api_key));
                    postDataParams.put("cat_id", mCategory);

                    networkADO = new NetworkADO();
                    jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                    try {
                        JSONObject rootJSON = new JSONObject(jsonResponse);
                        Boolean success = rootJSON.getBoolean("success");
                        message = rootJSON.getString("message");

                        if (success.equals(true)) {
                            JSONArray services = rootJSON.getJSONObject("results").getJSONArray("services");

                            serviceArray = new String[services.length() + 1];
                            serviceArray[0] = "Select Service";
                            serviceMap = new HashMap<>();

                            for (int i = 0; i < services.length(); i++) {
                                serviceMap.put(i + 1, services.getJSONObject(i).getString("id"));
                                serviceArray[i + 1] = services.getJSONObject(i).getString("service_name");
                            }
                            serviceArrayAdapter = new ArrayAdapter(ProviderActivity.this, android.R.layout.simple_spinner_item, serviceArray);
                        }
                        return true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    if (serviceArrayAdapter != null) {
                        myService.setAdapter(serviceArrayAdapter);
                        if(providerObj != null) {
                            if(!providerObj.getString("service_id").equals("")) {
                                Object sValue = Object.class.cast(providerObj.getString("service_id"));
                                Object sKey = getKeyFromValue(serviceMap, sValue);
                                myService.setSelectedItemPosition(serviceArrayAdapter.getItem(Integer.parseInt(sKey.toString())));
                            }
                        }
                    }
                } else {
                    if (message != "") {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetCity extends AsyncTask<Void, Void, Boolean> {

        String mState;

        GetCity(String state){
            mState = state;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(!mState.equals("")) {
                NetworkADO networkADO;
                String jsonResponse;

                try {
                    JSONObject postDataParams = new JSONObject();
                    postDataParams.put("task", "executive");
                    postDataParams.put("action", "get_cities");
                    postDataParams.put("key", getString(R.string.api_key));
                    postDataParams.put("state_id", mState);

                    networkADO = new NetworkADO();
                    jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                    try {
                        JSONObject rootJSON = new JSONObject(jsonResponse);
                        Boolean success = rootJSON.getBoolean("success");
                        message = rootJSON.getString("message");

                        if (success.equals(true)) {
                            JSONArray cities = rootJSON.getJSONObject("results").getJSONArray("cities");

                            cityArray = new String[cities.length() + 1];
                            cityArray[0] = "Select City";
                            cityMap = new HashMap<>();

                            for (int i = 0; i < cities.length(); i++) {
                                cityMap.put(i + 1, cities.getJSONObject(i).getString("id"));
                                cityArray[i + 1] = cities.getJSONObject(i).getString("city_name");
                            }
                            cityArrayAdapt = new ArrayAdapter(ProviderActivity.this, android.R.layout.simple_spinner_item, cityArray);
                        }
                        return true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    if (cityArrayAdapt != null) {
                        mCity.setAdapter(cityArrayAdapt);
                        if(providerObj != null) {
                            if(!providerObj.getString("city_id").equals("")) {
                                Object sValue = Object.class.cast(providerObj.getString("city_id"));
                                Object sKey = getKeyFromValue(cityMap, sValue);
                                mCity.setSelectedItemPosition(cityArrayAdapt.getItem(Integer.parseInt(sKey.toString())));
                            }
                        }
                    }
                } else {
                    if (message != "") {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetLocality extends AsyncTask<Void, Void, Boolean> {

        String mCity = null;

        GetLocality(String city){
            mCity = city;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(!mCity.equals("")) {
                NetworkADO networkADO;
                String jsonResponse;

                try {
                    JSONObject postDataParams = new JSONObject();
                    postDataParams.put("task", "executive");
                    postDataParams.put("action", "get_location");
                    postDataParams.put("key", getString(R.string.api_key));
                    postDataParams.put("city_id", mCity);

                    networkADO = new NetworkADO();
                    jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                    try {
                        JSONObject rootJSON = new JSONObject(jsonResponse);
                        Boolean success = rootJSON.getBoolean("success");
                        final String message = rootJSON.getString("message");

                        if (success.equals(true)) {
                            JSONArray locality = rootJSON.getJSONObject("results").getJSONArray("locations");

                            localityArray = new String[locality.length() + 1];
                            localityArray[0] = "Select Locality";
                            localityMap = new HashMap<>();

                            for (int i = 0; i < locality.length(); i++) {
                                localityMap.put(i + 1, locality.getJSONObject(i).getString("id"));
                                localityArray[i + 1] = locality.getJSONObject(i).getString("location_name");
                            }
                            localityArrayAdapter = new ArrayAdapter(ProviderActivity.this, android.R.layout.simple_list_item_1, localityArray);
                        }
                        return true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if (success) {
                try {
                    if (localityArrayAdapter != null) {
                        mLocality.setAdapter(localityArrayAdapter);
                        if(providerObj != null) {
                            if(!providerObj.getString("location_id").equals("")) {
                                Object sValue = Object.class.cast(providerObj.getString("location_id"));
                                Object sKey = getKeyFromValue(localityMap, sValue);
                                mLocality.setSelectedItemPosition(localityArrayAdapter.getItem(Integer.parseInt(sKey.toString())));
                            }
                        }
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            } else {
                if (message != "") {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
