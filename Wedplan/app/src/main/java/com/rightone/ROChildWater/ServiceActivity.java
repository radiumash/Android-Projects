package com.rightone.ROChildWater;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private long lastBackPressTime = 0;
    private Toast toast;

    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    private Integer dotscount;
    private ImageView[] dots;

    List<SliderUtils> sliderImg;
    ViewPagerAdapter viewPagerAdapter;
    private DrawerLayout mDrawerLayout;

    GetBanner gb;

    private SwipeRefreshLayout swipeRefreshLayout;

    //GridView Object
    private GridView gridView;

    //ArrayList for Storing image urls and titles
    private ArrayList<String> images;
    private ArrayList<String> names;
    private ArrayList<String> ids;

    private ArrayList<String> bIds;
    private ArrayList<String> bName;
    String category_id, category_name, favourite = "";
    GetService gs; Button mButtonMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Bundle extra = getIntent().getExtras();
        try {
            //category_id = extra.getString("ID");
            category_id = "1";
            //category_name = extra.getString("NAME");
            category_name = getString(R.string.app_name);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        if(!category_name.equals("")) {
            this.setTitle(category_name);
        }
        mButtonMap = findViewById(R.id.btMapRadius);
        mButtonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                // passing array index
                i.putExtra("ID", category_id);
                i.putExtra("NAME",category_name);
                i.putExtra("FAVOURITE","");
                startActivity(i);
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(
            new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    // Respond when the drawer's position changes
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    // Respond when the drawer is opened
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    // Respond when the drawer is closed
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    // Respond when the drawer motion state changes
                }
            }
        );

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    // set item as selected to persist highlight
                    menuItem.setChecked(true);
                    // close drawer when item is tapped
                    mDrawerLayout.closeDrawers();
                    // Add code here to update the UI based on the item selected
                    // For example, swap UI fragments here
                    if(menuItem.toString().equals("My Favourites")){
                        Intent i = new Intent(getApplicationContext(), ProviderActivity.class);
                        // passing array index
                        i.putExtra("ID", "");
                        i.putExtra("NAME","My Favourites");
                        i.putExtra("FAVOURITE","1");
                        startActivity(i);
                    }else if(menuItem.toString().equals("Change City")){
                        startActivity(new Intent(getApplicationContext(), ChangeCityActivity.class));
                        finish();
                    }else if(menuItem.toString().equals("Add New Listing")){
                        startActivity(new Intent(getApplicationContext(), NewProviderActivity.class));
                    }else if(menuItem.toString().equals("Privacy Policy")){
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra("URL", "http://www.rightonetechnologies.com/privacy.html");
                        intent.putExtra("NAME", "Privacy Policy");
                        startActivity(intent);
                    }else if(menuItem.toString().equals("Terms And Conditions")){
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra("URL", "http://www.rightonetechnologies.com/terms.html");
                        intent.putExtra("NAME", "Terms & Conditions");
                        startActivity(intent);
                    }else if(menuItem.toString().equals("Disclaimer")){
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra("URL", "http://www.rightonetechnologies.com/disclaimer.html");
                        intent.putExtra("NAME", "Disclaimer");
                        startActivity(intent);
                    }else if(menuItem.toString().equals("Refund")){
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra("URL", "http://www.rightonetechnologies.com/refund.html");
                        intent.putExtra("NAME", "Refund Policy");
                        startActivity(intent);
                    }
                    return true;
                }
            });

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }

        sliderImg = new ArrayList<>();
        viewPager = findViewById(R.id.viewPager);
        sliderDotspanel = findViewById(R.id.SliderDots);

        gb = new GetBanner();
        gb.execute((Void) null);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i = 0; i< dotscount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);

        gridView = findViewById(R.id.gridView);
        images = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();

        bIds = new ArrayList<>();
        bName = new ArrayList<>();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Sending image id to FullScreenActivity
                Intent i = new Intent(getApplicationContext(), ProviderActivity.class);
                // passing array index
                i.putExtra("ID", Long.toString(id));
                i.putExtra("NAME",names.get(position));
                i.putExtra("FAVOURITE","");
                i.putExtra("QUERY", "");
                startActivity(i);
            }
        });
        gs = new GetService();
        gs.execute((Void) null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_main, menu);
        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) searchViewItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.equals("")) {
                    searchView.clearFocus();
                    Intent i = new Intent(getApplicationContext(), ProviderActivity.class);
                    // passing array index
                    i.putExtra("ID", category_id);
                    i.putExtra("NAME", "");
                    i.putExtra("FAVOURITE", "");
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRefresh() {
        images = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();

        gs = new GetService();
        gs.execute((Void) null);
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
    }

    private class SliderTimer extends TimerTask {

        @Override
        public void run() {
            ServiceActivity.this.runOnUiThread(new Runnable() {
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
    public class GetBanner extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "get_banners");
                postDataParams.put("key", getString(R.string.api_key));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray banners = rootJSON.getJSONObject("results").getJSONArray("banners");

                        for (int i = 0; i < banners.length(); i++) {
                            SliderUtils sliderUtils = new SliderUtils();
                            String pms = banners.getJSONObject(i).getString("params");
                            JSONObject jsonObject = new JSONObject(pms);
                            String url = getString(R.string.image_url)+'/'+jsonObject.getString("imageurl");
                            sliderUtils.setSliderImageUrl(url);
                            sliderImg.add(sliderUtils);
                            String currentString =  banners.getJSONObject(i).getString("clickurl");
                            if(!currentString.equals("")) {
                                String[] separated = currentString.split("\\|");
                                bIds.add(separated[0]);
                                bName.add(separated[1]);
                            }else{
                                bIds.add("");
                                bName.add("");
                            }
                        }
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
                    viewPagerAdapter = new ViewPagerAdapter(sliderImg,ServiceActivity.this, bIds, bName);
                    viewPager.setAdapter(viewPagerAdapter);
                    dotscount = viewPagerAdapter.getCount();
                    dots = new ImageView[dotscount];

                    for(int i = 0; i < dotscount; i++){
                        dots[i] = new ImageView(ServiceActivity.this);
                        dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));
                        LinearLayout.LayoutParams paramsL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        paramsL.setMargins(8, 0, 8, 0);
                        sliderDotspanel.addView(dots[i], paramsL);
                    }
                    dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
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



    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetService extends AsyncTask<Void, Void, Boolean> {

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
                postDataParams.put("action", "get_services");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("cat_id", category_id);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray services = rootJSON.getJSONObject("results").getJSONArray("services");

                        for (int i = 0; i < services.length(); i++) {
                            String url = getString(R.string.image_url)+'/'+services.getJSONObject(i).getString("service_pic");
                            images.add(url);
                            names.add( services.getJSONObject(i).getString("service_name"));
                            ids.add( services.getJSONObject(i).getString("id"));
                        }
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
            swipeRefreshLayout.setRefreshing(false);
            try {
                if (success) {
                    //Creating GridViewAdapter Object
                    GridViewAdapter gridViewAdapter = new GridViewAdapter(getApplicationContext(),images,names,ids);

                    //Adding adapter to gridview
                    gridView.setAdapter(gridViewAdapter);
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
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
