package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.adapter.OrderRowAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.OrderRowDetails;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderDetailsActivity extends AppCompatActivity {

    OrderRowDetails[] myOrderDetailArray = null;
    OrderRowAdapter mOrderDetailAdapter = null;
    private ArrayList<OrderRowDetails> orderArrayList;
    ListView mOrderListView;
    ProgressBar mProgress;
    String mOrderID, mOrderNo;
    private SessionManager session;
    private HashMap<String,String> user;
    TextView mOrderTotal, mDeliveryCharge, mNetTotal, mServiceCharge;
    JSONObject order;
    Bundle extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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
        extra = getIntent().getExtras();
        if(extra != null){
            mOrderID = extra.getString("ID");
            mOrderNo = extra.getString("NAME");
        }
        this.setTitle(mOrderNo);
        mOrderTotal = findViewById(R.id.txtOrderTotal);
        mDeliveryCharge = findViewById(R.id.txtDelievryCharge);
        mServiceCharge = findViewById(R.id.txtSanitize);
        mNetTotal = findViewById(R.id.txtNetPayableAmount);

        mOrderListView = findViewById(R.id.myListView);
        mProgress = findViewById(R.id.loading);
        mProgress.setVisibility(View.VISIBLE);

        GetOrderRows go = new GetOrderRows();
        go.execute((Void) null);
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
    public class GetOrderRows extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "get_order_products");
                postDataParams.put("order_id", mOrderID);
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        order = rootJSON.getJSONObject("results").getJSONObject("order");
                        JSONArray products = rootJSON.getJSONObject("results").getJSONArray("products");

                        myOrderDetailArray = new OrderRowDetails[products.length()];
                        orderArrayList = new ArrayList<>();

                        for (int i = 0; i < products.length(); i++) {
                            String oName = products.getJSONObject(i).getString("name");
                            String oPrice = products.getJSONObject(i).getString("price");
                            String oQty = products.getJSONObject(i).getString("quantity");
                            String oSubTotal = products.getJSONObject(i).getString("subtotal");
                            String oTax = products.getJSONObject(i).getString("tax");
                            String oNetTotal = products.getJSONObject(i).getString("order_total");
                            String oUrl = products.getJSONObject(i).getString("thumb_image");
                            orderArrayList.add(new OrderRowDetails(oName,oPrice,oQty,oSubTotal,oTax,oNetTotal,oUrl));
                        }

                        mOrderListView.post(new Runnable() {
                            public void run() {
                                mOrderDetailAdapter = new OrderRowAdapter(OrderDetailsActivity.this, orderArrayList);

                                if (mOrderListView != null) {
                                    mOrderListView.setAdapter(mOrderDetailAdapter);
                                    mOrderListView.refreshDrawableState();
                                }
                            }
                        });
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
                    String service_charge = order.getString("service_charge");
                    String net_total = order.getString("net_payable_amount");
                    String del_chg = order.getString("delivery_charge");
                    mNetTotal.setText("₹ " + net_total);
                    mDeliveryCharge.setText("₹ " + del_chg);
                    mServiceCharge.setText("₹ " + service_charge.toString());
                    Float order_total = Float.parseFloat(net_total) - Float.parseFloat(del_chg) - Float.parseFloat(service_charge);
                    mOrderTotal.setText("₹ " + order_total.toString());
                } else {
                    if (errorMessage != "") {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            mProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
