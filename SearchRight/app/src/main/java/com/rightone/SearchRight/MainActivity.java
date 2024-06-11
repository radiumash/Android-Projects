package com.rightone.SearchRight;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private String errorMessage = null;
    BottomNavigationView navigation;
    ViewPagerAdapterFragment adapter;

    private String provider_name, provider_id;
    Bundle extra;
    private ViewPager viewPager;

    MenuItem prevMenuItem;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_overview:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.navigation_photos:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.navigation_contact:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.navigation_review:
                    viewPager.setCurrentItem(3);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        extra = getIntent().getExtras();
        try {
            provider_id = extra.getString("ID");
            provider_name = extra.getString("NAME");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        this.setTitle(provider_name);

        if(!provider_id.equals("")) {
            SetHits sh = new SetHits(provider_id);
            sh.execute((Void) null);
        }

        viewPager = findViewById(R.id.viewpager);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else
                {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setOffscreenPageLimit(4);
        setupViewPager(viewPager);
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void setupViewPager(ViewPager viewPager)
    {
        List<Fragment> fragments = new Vector<Fragment>();

        Bundle bundle = new Bundle();
        bundle.putString("ID", provider_id);
        bundle.putString("NAME",provider_name);

        fragments.add(Fragment.instantiate(this, FragmentOverview.class.getName(),bundle));
        fragments.add(Fragment.instantiate(this, FragmentPhotos.class.getName(),bundle));
        fragments.add(Fragment.instantiate(this, FragmentContact.class.getName(),bundle));
        fragments.add(Fragment.instantiate(this, FragmentReview.class.getName(),bundle));
        adapter = new ViewPagerAdapterFragment(getSupportFragmentManager(), fragments);

        viewPager.setAdapter(adapter);
    }


    /**
     * Represents an asynchronous Resend OTP task
     */
    public class SetHits extends AsyncTask<Void, Void, Boolean> {

        private final String mPid;
        SetHits(String pid) {
            mPid = pid;
        }

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
                postDataParams.put("action", "set_hits");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("provider_id", mPid);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        return true;
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

            if (success) {
                //finish();
            } else {
                if(errorMessage != ""){
                    Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

}
