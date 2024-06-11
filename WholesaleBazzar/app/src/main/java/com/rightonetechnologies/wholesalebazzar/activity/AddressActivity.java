package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.MySQLiteHelper;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddressActivity extends AppCompatActivity {

    private static final int SANITIZATION_CHARGES = 10;
    private static final int MINIMUM_FREE_DELIVERY_AMOUNT = 1000;
    private HashMap<String,String> user;
    SessionManager session;
    ProgressBar mProgressBar;
    private MySQLiteHelper db;
    RadioGroup addressGrp, paymodeGrp, slotGrp;
    Integer addressChk = 0;
    Integer slotChk = 1;
    String paymodeChk = "";
    Button mAddNew, mConfirm;
    JSONArray addresses, slots, slotCounts;
    JSONObject order;
    String chksum;
    Double del_chg = 0.0;
    Double netChg;
    TextView mOrderTotal, mDeliveryCharge, mNetPayable, mDeliveryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

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

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        this.setTitle(getString(R.string.choose_address));
        db = new MySQLiteHelper(this);
        mProgressBar = findViewById(R.id.mProgress);
        mOrderTotal = findViewById(R.id.txtOrderTotal);
        mDeliveryCharge = findViewById(R.id.txtDelievryCharge);
        mNetPayable = findViewById(R.id.txtNetPayableAmount);
        mDeliveryDate = findViewById(R.id.txtSlotDate);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String tomorrowAsString = dateFormat.format(tomorrow);
        mDeliveryDate.setText(tomorrowAsString);

        mOrderTotal.setText("₹ " + db.getCartTotal());
        if(Float.parseFloat(db.getCartTotal()) < MINIMUM_FREE_DELIVERY_AMOUNT){
            del_chg = 30.0;
        }
        mDeliveryCharge.setText("₹ " + del_chg.toString());
        netChg = Float.parseFloat(db.getCartTotal()) + del_chg + SANITIZATION_CHARGES;
        mNetPayable.setText("₹ " + netChg.toString());

        addressGrp = findViewById(R.id.rgAddress);
        addressGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = addressGrp.getCheckedRadioButtonId();
                RadioButton radioBtn = findViewById(checkedRadioButtonId);
                addressChk = radioBtn.getId();
                Toast.makeText(AddressActivity.this, radioBtn.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        paymodeGrp = findViewById(R.id.rgPayment);
        paymodeGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int checkedRadioButtonId = paymodeGrp.getCheckedRadioButtonId();
                RadioButton radioBtn = findViewById(checkedRadioButtonId);
                paymodeChk = radioBtn.getTag().toString();
            }
        });

        slotGrp = findViewById(R.id.rgSlot);
        slotGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = slotGrp.getCheckedRadioButtonId();
                RadioButton radioBtn = findViewById(checkedRadioButtonId);
                slotChk = radioBtn.getId();
                Toast.makeText(AddressActivity.this, radioBtn.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        mAddNew = findViewById(R.id.btnAddNew);
        mAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), NewAddressActivity.class);
                startActivity(i);
            }
        });
        mConfirm = findViewById(R.id.btnConfirm);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(!addressChk.equals(0)){
                if(!paymodeChk.equals("")){
                    mProgressBar.setVisibility(View.VISIBLE);
                    saveOrder sA = new saveOrder();
                    sA.execute((Void) null);
                }else{
                    Toast.makeText(AddressActivity.this, getString(R.string.err_payment_mode), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(AddressActivity.this, getString(R.string.err_address), Toast.LENGTH_SHORT).show();
            }
            }
        });
        getAddresses gA = new getAddresses();
        gA.execute((Void) null);

        getSlots gS = new getSlots();
        gS.execute((Void) null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Represents an asynchronous Get Addresses task
     */
    public class getAddresses extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "user_address");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("user_id",user.get("user_id"));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        addresses = rootJSON.getJSONObject("results").getJSONArray("address");
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
                    for (int i = 0; i < addresses.length(); i++) {
                        RadioButton radioButton = new RadioButton(AddressActivity.this);
                        String addr = addresses.getJSONObject(i).getString("description") + '\n' + addresses.getJSONObject(i).getString("address");
                        radioButton.setText(addr);
                        radioButton.setId(Integer.parseInt(addresses.getJSONObject(i).getString("id")));
                        //radioButton.setBackgroundColor(Color.parseColor("#CCCCCC"));
                        radioButton.setPadding(20,40,20,40);
                        addressGrp.addView(radioButton);
                    }
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

    /**
     * Represents an asynchronous Get Delivery Slots task
     */
    public class getSlots extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "get_slots");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("city_id",user.get("city_id"));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        slots = rootJSON.getJSONObject("results").getJSONArray("slots");
                        slotCounts = rootJSON.getJSONObject("results").getJSONArray("slots_today");
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
                    for (int i = 0; i < slots.length(); i++) {
                        RadioButton radioButton = new RadioButton(AddressActivity.this);
                        String slt = slots.getJSONObject(i).getString("name");
                        radioButton.setText(slt);
                        radioButton.setId(Integer.parseInt(slots.getJSONObject(i).getString("id")));
                        //radioButton.setBackgroundColor(Color.parseColor("#CCCCCC"));
                        radioButton.setPadding(20,20,20,20);
                        slotGrp.addView(radioButton);
                    }
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

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class saveOrder extends AsyncTask<Void, Void, Boolean> {

        private String errorMessage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;
            JSONArray cart = db.getCart();
            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "save_order");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("user_id",user.get("user_id"));
                postDataParams.put("address_id", addressChk);
                postDataParams.put("payment_mode", paymodeChk);
                postDataParams.put("total", db.getCartTotal());
                postDataParams.put("mrp_total", db.getCartMRPTotal());
                postDataParams.put("tax", db.getCartTaxTotal());
                postDataParams.put("delivery_charge", del_chg);
                postDataParams.put("service_charge", SANITIZATION_CHARGES);
                postDataParams.put("slot_id", slotChk);
                postDataParams.put("cart",cart);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                         order = rootJSON.getJSONObject("results").getJSONObject("order");
                         chksum = rootJSON.getJSONObject("results").getString("chksum");
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
                    if(order.length() > 0){
                        if(order.getString("payment_mode").equals("POD")){
                            db.deleteCart();
                            Intent i = new Intent(getApplicationContext(), SuccessActivity.class);
                            i.putExtra("ORDER", order.toString());
                            startActivity(i);
                        }else {
                            //Float total = Float.parseFloat(order.getString("net_payable_amount"));
                            Intent i = new Intent(getApplicationContext(), PaymentActivity.class);
                            // passing array index
                            i.putExtra("ID", order.getString("id"));
                            //i.putExtra("BILL", String.format("%.2f", total));
                            i.putExtra("BILL", order.getString("net_payable_amount"));
                            i.putExtra("CHKSUM", chksum);
                            startActivity(i);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
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
