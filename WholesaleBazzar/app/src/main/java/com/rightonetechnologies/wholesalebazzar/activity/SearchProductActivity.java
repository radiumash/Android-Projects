package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.adapter.ProductsAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.ProductsDetails;
import com.rightonetechnologies.wholesalebazzar.common.Converter;
import com.rightonetechnologies.wholesalebazzar.common.MySQLiteHelper;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchProductActivity extends AppCompatActivity {

    SessionManager session;
    private HashMap<String,String> user;

    String search;
    ProgressBar mProgressBar;
    private MySQLiteHelper db;
    private ArrayList<String> images;
    private ArrayList<String> names;
    private ArrayList<String> ids;

    ProductsDetails[] myProductDetailArray = null;
    ProductsAdapter mProductDetailAdapter = null;
    private ArrayList<ProductsDetails> ProductArrayList;
    ListView mProductListView;
    LinearLayout Rec, noRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

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
            search = extra.getString("QUERY");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(!search.isEmpty()){
            this.setTitle(getString(R.string.search_result) + search);
        }

        Rec = findViewById(R.id.LLRec);
        noRec = findViewById(R.id.LLnoRec);

        db = new MySQLiteHelper(this);
        mProgressBar = findViewById(R.id.mProgress);
        mProductListView = findViewById(R.id.ProductLV);
        images = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();
        mProgressBar.setVisibility(View.VISIBLE);
        GetProducts getP = new GetProducts(search);
        getP.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Integer count = Integer.parseInt(db.getCartCount());
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem3 = menu.findItem(R.id.search_action);
        menuItem3.setIcon(Converter.convertLayoutToImage(SearchProductActivity.this,0,R.drawable.ic_baseline_search_24));
        MenuItem menuItem = menu.findItem(R.id.cart_action);
        menuItem.setIcon(Converter.convertLayoutToImage(SearchProductActivity.this,count,R.drawable.ic_shopping_cart_black_24dp));
        MenuItem menuItem2 = menu.findItem(R.id.notification_action);
        menuItem2.setIcon(Converter.convertLayoutToImage(SearchProductActivity.this,0,R.drawable.ic_notifications_black_24dp));

        final SearchView searchView = (SearchView) menuItem3.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.equals("")) {
                    searchView.clearFocus();
                    Intent i = new Intent(getApplicationContext(), SearchProductActivity.class);
                    i.putExtra("QUERY", query);
                    startActivity(i);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.cart_action) {
            Intent i = new Intent(getApplicationContext(), CartActivity.class);
            startActivity(i);
        }
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
    protected void onRestart() {
        super.onRestart();
        this.invalidateOptionsMenu();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetProducts extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mSearch;

        private String errorMessage = null;

        GetProducts(String search) {
            mSearch = search;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String url="";
            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "search_product");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("city_id",user.get("city_id"));
                postDataParams.put("keyword", mSearch);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray topProducts = rootJSON.getJSONObject("results").getJSONArray("products");
                        myProductDetailArray = new ProductsDetails[topProducts.length()];
                        ProductArrayList = new ArrayList<>();

                        for (int i = 0; i < topProducts.length(); i++) {

                            String id = topProducts.getJSONObject(i).getString("id");
                            String company = topProducts.getJSONObject(i).getString("company_name");
                            String pid = topProducts.getJSONObject(i).getString("product_id");
                            String product_name = topProducts.getJSONObject(i).getString("product_name");
                            String uid = topProducts.getJSONObject(i).getString("unit_id");
                            String unit_name = topProducts.getJSONObject(i).getString("unit");
                            String qty = topProducts.getJSONObject(i).getString("quantity");
                            String mrp = topProducts.getJSONObject(i).getString("mrp");
                            String tax = topProducts.getJSONObject(i).getString("tax");
                            String sp = topProducts.getJSONObject(i).getString("sell_price");
                            String image_url = topProducts.getJSONObject(i).getString("image_url");

                            ProductArrayList.add(new ProductsDetails(id,company,pid,product_name,uid,unit_name,qty,mrp,sp,tax,image_url));
                        }

                        mProductListView.post(new Runnable() {
                            public void run() {
                                mProductDetailAdapter = new ProductsAdapter(SearchProductActivity.this, ProductArrayList);

                                if (mProductListView != null) {
                                    mProductListView.setAdapter(mProductDetailAdapter);
                                    mProductListView.refreshDrawableState();
                                }
                            }
                        });

                        return true;
                    }else{
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
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if (success) {
                try {
                    Rec.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                noRec.setVisibility(View.VISIBLE);
                if (errorMessage != "") {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
