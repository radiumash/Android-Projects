package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.AddToCart;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ProductDetailActivity extends AppCompatActivity implements AddToCart.OnQuantityChangeListener {

    private HashMap<String,String> user;
    SessionManager session;
    ProgressBar mProgressBar;
    TextView mCompany, mName, mMrp, mPrice, mUnit, mDescription;
    ImageView mImage;
    AddToCart mAddToCart;
    Bundle extra;
    String product_id, product_name;
    JSONObject product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

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

        extra = getIntent().getExtras();
        if(extra != null){
            product_id = extra.getString("ID");
            product_name = extra.getString("NAME");
        }
        mImage = findViewById(R.id.imgProduct);
        mCompany = findViewById(R.id.tvCompanyName);
        mName = findViewById(R.id.tvProductName);
        mMrp = findViewById(R.id.tvProductMRP);
        mMrp.setPaintFlags(mMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mPrice = findViewById(R.id.tvProductPrice);
        mUnit = findViewById(R.id.tvProductUnit);
        mDescription = findViewById(R.id.tvProductDescription);
        mAddToCart = findViewById(R.id.productQty);
        mAddToCart.setOnQuantityChangeListener(this);

        mProgressBar = findViewById(R.id.loading);
        mProgressBar.setVisibility(View.VISIBLE);
        getProduct gp = new getProduct();
        gp.execute((Void) null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {

    }

    @Override
    public void onLimitReached() {
        Log.d(getClass().getSimpleName(), "Limit reached");
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class getProduct extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "get_product");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("product_id",product_id);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        try {
                            product = rootJSON.getJSONObject("results").getJSONObject("product");
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
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
                    mCompany.setText(product.getString("company_name"));
                    mName.setText(product.getString("product_name"));
                    mMrp.setText("₹ " + product.getString("mrp"));
                    mPrice.setText(product.getString("sell_price"));
                    mUnit.setText(product.getString("quantity") + " " + product.getString("unit"));
                    mDescription.setText(product.getString("product_desc"));
                    String url = getString(R.string.image_url) + '/' + product.getString("image_url");
                    Picasso.with(getApplicationContext())
                            .load(url)
                            .placeholder(R.drawable.thumb_placeholder) // optional
                            .into(mImage);
                    mAddToCart.setPid(product.getString("product_id"));
                    mAddToCart.setPackID(product.getString("id"));
                    mAddToCart.setUid(product.getString("unit_id"));
                    mAddToCart.setDid("0");
                    mAddToCart.setMRP(product.getString("mrp"));
                    mAddToCart.setSP(product.getString("sell_price"));
                    mAddToCart.setTax(product.getString("tax"));
                    mAddToCart.setCompany(product.getString("company_name"));
                    mAddToCart.setUnit(product.getString("quantity") + " " + product.getString("unit"));
                    mAddToCart.setImage(url);
                    mAddToCart.setName(product.getString("product_name"));
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