package com.rightone.SearchRight;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    private Integer dotscount;
    private ImageView[] dots;

    List<SliderUtils> sliderImg;
    ViewPagerAdapter viewPagerAdapter;
    GetBanner gb;

    Button mButtonMap;

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
    GetService gs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle extra = getIntent().getExtras();
        try {
            category_id = extra.getString("ID");
            category_name = extra.getString("NAME");
            favourite = extra.getString("FAVOURITE");
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

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

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
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
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
