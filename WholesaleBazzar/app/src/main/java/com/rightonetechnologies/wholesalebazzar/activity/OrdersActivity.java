package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.adapter.OrderAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.OrderDetails;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class OrdersActivity extends AppCompatActivity {

    OrderDetails[] myOrderDetailArray = null;
    OrderAdapter mOrderDetailAdapter = null;
    private ArrayList<OrderDetails> orderArrayList;
    ListView mOrderListView;
    ProgressBar mProgress;
    private SessionManager session;
    private HashMap<String,String> user;
    LinearLayout Rec, noRec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

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
        this.setTitle(getString(R.string.view_orders));

        mOrderListView = findViewById(R.id.myListView);
        mProgress = findViewById(R.id.loading);
        mProgress.setVisibility(View.VISIBLE);
        Rec = findViewById(R.id.LLRec);
        noRec = findViewById(R.id.LLnoRec);

        GetOrders go = new GetOrders();
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

    public String getDateReadable(String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date sourceDate = null;
        try {
            sourceDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        String targetdatevalue = targetFormat.format(sourceDate);
        return targetdatevalue;
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetOrders extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "get_orders");
                postDataParams.put("user_id", user.get("user_id"));
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray orders = rootJSON.getJSONObject("results").getJSONArray("orders");

                        myOrderDetailArray = new OrderDetails[orders.length()];
                        orderArrayList = new ArrayList<>();
                        if(orders.length() > 0) {
                            for (int i = 0; i < orders.length(); i++) {
                                String oid = orders.getJSONObject(i).getString("id");
                                String ono = orders.getJSONObject(i).getString("order_number");
                                String odate = getDateReadable(orders.getJSONObject(i).getString("created"));
                                String ototal = orders.getJSONObject(i).getString("net_payable_amount");
                                String ostatus = orders.getJSONObject(i).getString("payment_status") == "1" ? "Completed" : "Pending";
                                String opaymode = orders.getJSONObject(i).getString("payment_mode");
                                orderArrayList.add(new OrderDetails(oid, ono, odate, ototal, ostatus, opaymode));
                            }

                            mOrderListView.post(new Runnable() {
                                public void run() {
                                    mOrderDetailAdapter = new OrderAdapter(OrdersActivity.this, orderArrayList);

                                    if (mOrderListView != null) {
                                        mOrderListView.setAdapter(mOrderDetailAdapter);
                                        mOrderListView.refreshDrawableState();
                                    }
                                }
                            });
                            return true;
                        }else{
                            return false;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if (success) {
                    Rec.setVisibility(View.VISIBLE);
                } else {
                    noRec.setVisibility(View.VISIBLE);
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
