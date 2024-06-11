package com.rightonetechnologies.wholesalebazzar.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.rightonetechnologies.wholesalebazzar.R;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import com.rightonetechnologies.wholesalebazzar.adapter.DealAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.DealDetails;
import com.rightonetechnologies.wholesalebazzar.adapter.GridViewAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.OffersAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.OffersDetails;
import com.rightonetechnologies.wholesalebazzar.adapter.ProductsAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.ProductsDetails;
import com.rightonetechnologies.wholesalebazzar.adapter.ViewPagerAdapter;
import com.rightonetechnologies.wholesalebazzar.common.Converter;
import com.rightonetechnologies.wholesalebazzar.common.ExpandableGridView;
import com.rightonetechnologies.wholesalebazzar.common.MySQLiteHelper;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.common.SliderUtils;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;
import com.rightonetechnologies.wholesalebazzar.network.NetworkADO;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private long lastBackPressTime = 0;
    private Toast toast;
    private SwipeRefreshLayout swipeRefreshLayout;
    JSONObject rootJSON;
    private MySQLiteHelper db;
    ProgressBar mProgress;
    DealDetails[] myExDealDetailArray = null;
    DealAdapter mExDealDetailAdapter = null;
    private ArrayList<DealDetails> ExDealArrayList;
    ListView mExDealListView;

    ProductsDetails[] myTopProductDetailArray = null;
    ProductsAdapter mTopProductDetailAdapter = null;
    private ArrayList<ProductsDetails> TopProductArrayList;
    ListView mTopProductListView;

    OffersDetails[] myOffersDetailArray = null;
    private ArrayList<OffersDetails> OffersArrayList;
    private ExpandableGridView gridViewOffers;

    ViewPager viewPager;
    private ImageView[] dots;

    ArrayList<SliderUtils> sliderImg;
    ViewPagerAdapter viewPagerAdapter;

    //GridView Object
    private ExpandableGridView gridView;

    //ArrayList for Storing image urls and titles
    private ArrayList<String> images;
    private ArrayList<String> names;
    private ArrayList<String> ids;

    private ArrayList<String> bIds;
    private ArrayList<String> bName;
    private SessionManager session;
    private HashMap<String,String> user;
    GetInitHome gs;
    Bundle extra;
    String message;
    TextView mName, mReferral, mStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        message = "";
        if(extra != null){
            message = extra.getString("MESSAGE");
        }
        if(message != ""){
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), "Dear " + user.get("name") + ",\nWelcome to " + getString(R.string.app_name), Toast.LENGTH_LONG).show();
        }
        db = new MySQLiteHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                if (id == R.id.nav_deal) {
                    Intent intent = new Intent(getApplicationContext(), DealActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_referral) {
                    Intent intent = new Intent(getApplicationContext(), MemberActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_orders) {
                    Intent intent = new Intent(getApplicationContext(), OrdersActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_downline){
                    Intent intent = new Intent(getApplicationContext(), DownlineActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_faqs) {
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("URL", getString(R.string.url) + "faq.html");
                    intent.putExtra("NAME", "FAQs");
                    startActivity(intent);
                } else if (id == R.id.nav_privacy) {
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("URL", getString(R.string.url) + "privacy.html");
                    intent.putExtra("NAME", "Privacy Policy");
                    startActivity(intent);
                } else if (id == R.id.nav_terms) {
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("URL", getString(R.string.url) + "terms.html");
                    intent.putExtra("NAME", "Terms & Conditions");
                    startActivity(intent);
                } else if (id == R.id.nav_share) {
                    String message = "This is testing.";
                    Intent shareText = new Intent(Intent.ACTION_SEND);
                    shareText .setType("text/plain");
                    shareText .putExtra(Intent.EXTRA_TEXT, message);
                    startActivity(Intent.createChooser(shareText , "Share Wholesale Bazzar"));
                } else if (id == R.id.nav_send) {
                    Intent intent = new Intent(getApplicationContext(), SendActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    session.logoutUser();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        mName = navigationView.getHeaderView(0).findViewById(R.id.txtName);
        mReferral = navigationView.getHeaderView(0).findViewById(R.id.txtReferral);
        mStar = navigationView.getHeaderView(0).findViewById(R.id.txtStar);

        mName.setText(user.get("name"));
        mReferral.setText(user.get("referral_code"));
        if(user.get("isstar").equals("1")){
            mStar.setText(getString(R.string.star_memeber));
        }else{
            mStar.setText(getString(R.string.member));
            nav_Menu.findItem(R.id.nav_referral).setVisible(false);
            nav_Menu.findItem(R.id.nav_downline).setVisible(false);
            nav_Menu.findItem(R.id.nav_send).setVisible(false);
        }

        mProgress = findViewById(R.id.loading);
        mProgress.setVisibility(View.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        mExDealListView = findViewById(R.id.exDealLV);
        mTopProductListView = findViewById(R.id.topProductLV);

        sliderImg = new ArrayList<>();
        viewPager = findViewById(R.id.viewPager);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);

        gridView = findViewById(R.id.gridView);
        images = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();

        gridViewOffers = findViewById(R.id.gridViewOffers);

        bIds = new ArrayList<>();
        bName = new ArrayList<>();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Sending image id to FullScreenActivity
                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                // passing array index
                i.putExtra("ID", Long.toString(id));
                i.putExtra("NAME",names.get(position));
                i.putExtra("FAVOURITE","");
                startActivity(i);
            }
        });
        gs = new GetInitHome();
        gs.execute((Void) null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //refresh();
        this.invalidateOptionsMenu();
    }

    @Override
    public void onRefresh() {
        mProgress.setVisibility(View.VISIBLE);
        refresh();
    }

    private void refresh(){
        mProgress.setVisibility(View.VISIBLE);
        images = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();

        gs = new GetInitHome();
        gs.execute((Void) null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Integer count = Integer.parseInt(db.getCartCount());
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem3 = menu.findItem(R.id.search_action);
        menuItem3.setIcon(Converter.convertLayoutToImage(MainActivity.this,0,R.drawable.ic_baseline_search_24));
        MenuItem menuItem = menu.findItem(R.id.cart_action);
        menuItem.setIcon(Converter.convertLayoutToImage(MainActivity.this,count,R.drawable.ic_shopping_cart_black_24dp));
        MenuItem menuItem2 = menu.findItem(R.id.notification_action);
        menuItem2.setIcon(Converter.convertLayoutToImage(MainActivity.this,0,R.drawable.ic_notifications_black_24dp));


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
        }else if(id == R.id.notification_action){
            Intent i = new Intent(getApplicationContext(), NotificationActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private class SliderTimer extends TimerTask {

        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (viewPager.getCurrentItem() < sliderImg.size() - 1) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                    } else {
                        viewPager.setCurrentItem(0);
                    }
                }
            });
        }
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetInitHome extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "user_init_home");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("city_id",user.get("city_id"));
                postDataParams.put("cid","1");

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray categories = rootJSON.getJSONObject("results").getJSONArray("categories");

                        for (int i = 0; i < categories.length(); i++) {
                            String url = getString(R.string.image_url)+'/'+categories.getJSONObject(i).getString("image_url");
                            images.add(url);
                            names.add( categories.getJSONObject(i).getString("name"));
                            ids.add( categories.getJSONObject(i).getString("id"));
                        }

                        JSONArray banners = rootJSON.getJSONObject("results").getJSONArray("banners");

                        for (int i = 0; i < banners.length(); i++) {
                            SliderUtils sliderUtils = new SliderUtils();
                            String url = getString(R.string.image_url)+'/'+banners.getJSONObject(i).getString("img_url");
                            sliderUtils.setSliderImageUrl(url);
                            sliderImg.add(sliderUtils);
                            bIds.add(banners.getJSONObject(i).getString("product_id"));
                            bName.add(banners.getJSONObject(i).getString("name"));
                        }

                        JSONArray exDeals = rootJSON.getJSONObject("results").getJSONArray("exclusive");
                        myExDealDetailArray = new DealDetails[exDeals.length()];
                        ExDealArrayList = new ArrayList<>();

                        for (int i = 0; i < exDeals.length(); i++) {

                            String dealid = exDeals.getJSONObject(i).getString("id");
                            String company = exDeals.getJSONObject(i).getString("company_name");
                            String pid = exDeals.getJSONObject(i).getString("product_id");
                            String product_name = exDeals.getJSONObject(i).getString("product_name");
                            String deal_name = exDeals.getJSONObject(i).getString("deal_name");
                            String mrp = exDeals.getJSONObject(i).getString("mrp");
                            String tax = exDeals.getJSONObject(i).getString("tax");
                            String image_url = exDeals.getJSONObject(i).getString("image_url");
                            JSONArray units = exDeals.getJSONObject(i).getJSONArray("unit");

                            HashMap<Integer, String> unitMap = new HashMap<>();
                            HashMap<Integer, String> mrpMap = new HashMap<>();
                            HashMap<Integer, String> spMap = new HashMap<>();
                            String[] unitArray = new String[units.length()+1];
                            unitArray[0] = "Select Pack";
                            for (int j = 0; j < units.length(); j++) {
                                mrpMap.put(j+1, units.getJSONObject(j).getString("mrp"));
                                spMap.put(j+1, units.getJSONObject(j).getString("sell_price"));
                                unitMap.put(j+1, units.getJSONObject(j).getString("id"));
                                unitArray[j+1] = units.getJSONObject(j).getString("qty") + ' ' + units.getJSONObject(j).getString("short_code");
                            }
                            ArrayAdapter<String> unitArrayAdapt = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, unitArray);
                            ExDealArrayList.add(new DealDetails(dealid, deal_name, company, pid, product_name, unitArrayAdapt, unitMap, mrp, "", tax, image_url, spMap, mrpMap));
                        }

                        mExDealListView.post(new Runnable() {
                            public void run() {
                                mExDealDetailAdapter = new DealAdapter(MainActivity.this, ExDealArrayList);

                                if (mExDealListView != null) {
                                    mExDealListView.setAdapter(mExDealDetailAdapter);
                                    mExDealListView.refreshDrawableState();
                                }
                            }
                        });

                        JSONArray topProducts = rootJSON.getJSONObject("results").getJSONArray("top_products");
                        myTopProductDetailArray = new ProductsDetails[topProducts.length()];
                        TopProductArrayList = new ArrayList<>();

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

                            TopProductArrayList.add(new ProductsDetails(id,company,pid,product_name,uid,unit_name,qty,mrp,sp,tax,image_url));
                        }

                        mTopProductListView.post(new Runnable() {
                            public void run() {
                                mTopProductDetailAdapter = new ProductsAdapter(MainActivity.this, TopProductArrayList);

                                if (mTopProductListView != null) {
                                    mTopProductListView.setAdapter(mTopProductDetailAdapter);
                                    mTopProductListView.refreshDrawableState();
                                }
                            }
                        });


                        JSONArray offers = rootJSON.getJSONObject("results").getJSONArray("offers");
                        myOffersDetailArray = new OffersDetails[offers.length()];
                        OffersArrayList = new ArrayList<>();

                        for (int i = 0; i < offers.length(); i++) {

                            String id = offers.getJSONObject(i).getString("id");
                            String company = offers.getJSONObject(i).getString("company_name");
                            String pid = offers.getJSONObject(i).getString("product_id");
                            String product_name = offers.getJSONObject(i).getString("product_name");
                            String unit_name = offers.getJSONObject(i).getString("unit");
                            String qty = offers.getJSONObject(i).getString("quantity");
                            String mrp = offers.getJSONObject(i).getString("mrp");
                            String tax = offers.getJSONObject(i).getString("tax");
                            String sp = offers.getJSONObject(i).getString("sell_price");
                            String image_url = offers.getJSONObject(i).getString("image_url");

                            OffersArrayList.add(new OffersDetails(id,company,pid,product_name,unit_name,qty,mrp,sp,tax,image_url));
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
            swipeRefreshLayout.setRefreshing(false);
            mProgress.setVisibility(View.GONE);
            try {
                if (success) {

                    //Creating GridViewAdapter Object for Categories
                    GridViewAdapter gridViewAdapter = new GridViewAdapter(getApplicationContext(),images,names,ids);
                    gridView.setAdapter(gridViewAdapter);

                    //Banners
                    viewPagerAdapter = new ViewPagerAdapter(sliderImg,MainActivity.this, bIds, bName);
                    viewPager.setAdapter(viewPagerAdapter);


                    OffersAdapter offersAdapter = new OffersAdapter(getApplicationContext(), OffersArrayList);
                    gridViewOffers.setAdapter(offersAdapter);

                } else {
                    if (errorMessage != "") {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                swipeRefreshLayout.setRefreshing(false);
                mProgress.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            swipeRefreshLayout.setRefreshing(false);
            mProgress.setVisibility(View.GONE);
        }
    }
}
