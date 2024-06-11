package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.logging.type.HttpRequest;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PaymentActivity extends AppCompatActivity {

    private HashMap<String,String> user;
    SessionManager session;
    ProgressBar mProgressBar;
    String order_id, chksum, order_total;
    PaymentActivity activity;
    final int requestCode = 2;
    String bodyData = "";
    float value;
    InitiatePayment payTM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        this.activity = this;

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());
        if(!session.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        }
        user = session.getUserDetails();
        Bundle extra = getIntent().getExtras();
        try {
            order_id = extra.getString("ID");
            order_total = extra.getString("BILL");
            chksum = extra.getString("CHKSUM");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        this.setTitle(getString(R.string.process_payment));
        mProgressBar = findViewById(R.id.mProgress);

        if (ContextCompat.checkSelfPermission(PaymentActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PaymentActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        startPayment();
    }

    void startPayment() {
        bodyData = getPaytmParams();
        mProgressBar.setVisibility(View.VISIBLE);

        payTM = new InitiatePayment();
        payTM.execute();
    }

    String getPaytmParams () {
        JSONObject paytmParams;
        try {
            JSONObject body = new JSONObject();
            body.put("requestType", "Payment");
            body.put("mid", R.string.paytm_merchant_key);
            body.put("websiteName", R.string.app_name);
            body.put("orderId", order_id);
            body.put("callbackUrl",R.string.callback_url + order_id );

            JSONObject txnAmount = new JSONObject();
            try{
                value = Float.parseFloat(order_total);
            } catch (Exception e) {
                value = 0f;
            }
            txnAmount.put("value", String.format(Locale.getDefault(), "%.2f", value));
            txnAmount.put("currency", "INR");

            JSONObject userInfo = new JSONObject();
            userInfo.put("custId", user.get("user_id"));

            body.put("txnAmount", txnAmount);
            body.put("userInfo", userInfo);

            /*
             * Generate checksum by parameters we have in body
             * You can get Checksum JAR from https://developer.paytm.com/docs/checksum/
             * Find your Merchant Key in your Paytm Dashboard at https://dashboard.paytm.com/next/apikeys
             */

            paytmParams = body;

        } catch (Exception e) {
            e.printStackTrace();
            paytmParams = new JSONObject();
        }
        return paytmParams.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Represents an asynchronous Resend OTP task
     */
    public class InitiatePayment extends AsyncTask<Void, Void, Boolean> {

        private String errorMessage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String url = String.format(String.valueOf(R.string.trans_url), R.string.paytm_merchant_key, order_id);


            NetworkADO networkADO;
            String jsonResponse;
            try {
                JSONObject paytmParams = new JSONObject();

                JSONObject head = new JSONObject();
                head.put("signature", chksum);

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("head", head);
                paytmParams.put("head", head);
                paytmParams.put("body", new JSONObject(bodyData));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(paytmParams, url);

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    processPaytmTransaction(rootJSON);
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
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.requestCode && data != null) {
            String nsdk = data.getStringExtra("nativeSdkForMerchantMessage");
            String response = data.getStringExtra("response");
            Toast.makeText(this, nsdk + response, Toast.LENGTH_SHORT).show();
        }
    }

    void processPaytmTransaction(JSONObject data) {
        try {
            Log.i("CHECKSUM", data.getJSONObject("body").toString());
            Log.i("CHECKSUM", data.getJSONObject("head").getString("signature"));
            Log.e("TXN_TOKEN", data.getJSONObject("body").getString("txnToken"));



            PaytmOrder paytmOrder = new PaytmOrder(order_id, String.valueOf(R.string.paytm_merchant_key), data.getJSONObject("body").getString("txnToken"),
                    String.format(Locale.getDefault(), "%.2f", value), String.valueOf((R.string.callback_url)));

            TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
                @Override
                public void onTransactionResponse(Bundle bundle) {
                    Toast.makeText(getApplicationContext(), "Payment Transaction response " + bundle.toString(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void networkNotAvailable() {
                    Log.e("RESPONSE", "network not available");
                }

                @Override
                public void onErrorProceed(String s) {
                    Log.e("RESPONSE", "error proceed: " + s);

                }

                @Override
                public void clientAuthenticationFailed(String s) {
                    Log.e("RESPONSE", "client auth failed: " + s);

                }

                @Override
                public void someUIErrorOccurred(String s) {
                    Log.e("RESPONSE", "UI error occured: " + s);

                }

                @Override
                public void onErrorLoadingWebPage(int i, String s, String s1) {
                    Log.e("RESPONSE", "error loading webpage: " + s + "--" + s1);

                }

                @Override
                public void onBackPressedCancelTransaction() {
                    Log.e("RESPONSE", "back pressed");

                }

                @Override
                public void onTransactionCancel(String s, Bundle bundle) {
                    Log.e("RESPONSE", "transaction cancel: " + s);

                }
            });
            transactionManager.startTransaction(this.activity, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Represents an asynchronous Resend OTP task
     */
    public class cancelOrder extends AsyncTask<Void, Void, Boolean> {

        private String errorMessage = null;

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
                postDataParams.put("action", "cancel_order");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("order_id",order_id);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        return true;
                    }else{
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
                    Intent i = new Intent(getApplicationContext(), CartActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    if (errorMessage != "") {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
