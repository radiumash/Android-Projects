package com.EEEITSolutions.elearning.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import com.EEEITSolutions.elearning.R;

import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.EEEITSolutions.elearning.adapter.DoubtAdapter;
import com.google.android.material.navigation.NavigationView;
import com.EEEITSolutions.elearning.adapter.GridViewAdapter;
import com.EEEITSolutions.elearning.adapter.SliderUtils;
import com.EEEITSolutions.elearning.adapter.ViewPagerAdapter;
import com.EEEITSolutions.elearning.common.SessionManager;
import com.EEEITSolutions.elearning.network.ConnectivityReceiver;
import com.EEEITSolutions.elearning.network.NetworkADO;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private long lastBackPressTime = 0;
    private Toast toast;
    private ImageView mImage;
    private TextView mName, mClass;
    private GridView gridView;
    private SessionManager session;
    //ArrayList for Storing image urls and titles
    private ArrayList<String> images;
    private ArrayList<String> names;
    private ArrayList<String> ids;
    ProgressBar mProgress;
    private HashMap <String,String> user;
    private String student_name, student_class, student_image = "";

    private ViewPager viewPager, viewPager1, viewPager2;
    private List<SliderUtils> sliderImg, sliderImg1, sliderImg2;
    private ArrayList<String> bIds, bIds1, bIds2;
    private ArrayList<String> bName, bName1, bName2;
    private ArrayList<String> bUrl, bUrl1, bUrl2;
    ViewPagerAdapter viewPagerAdapter, viewPagerAdapter1, viewPagerAdapter2;
    String lName = "";
    GetInitHome gh;

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
        if (session.isLoggedIn()) {
            user = session.getUserDetails();
            student_name = user.get("name");
            student_class = user.get("class_name");
            student_image = user.get("profile_pic");
        }else{
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }
        if(!user.get("language_name").equals("English")){
            lName = user.get("language_name");
            setLanguage(lName);
        }
        String wm = getString(R.string.welcome_message);
        Toast.makeText(getApplicationContext(),wm.replace("%n",student_name),Toast.LENGTH_LONG).show();
        mProgress = findViewById(R.id.progress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                try {
                    if (!student_name.equals("")) {
                        mName = findViewById(R.id.txtUserName);
                        mName.setText(student_name);
                    }
                    if (!student_class.equals("")) {
                        mClass = findViewById(R.id.txtUserClass);
                        mClass.setText(student_class);
                    }
                    if (!student_image.equals("")) {
                        mImage = findViewById(R.id.imgUserProfile);
                        Picasso.with(getApplicationContext())
                                .load(getString(R.string.image_url) + "/" + student_image)
                                .placeholder(R.drawable.user) // optional
                                .into(mImage);
                    }
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        gridView = findViewById(R.id.gridView);
        images = new ArrayList<>();
        names = new ArrayList<>();
        ids = new ArrayList<>();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), ChapterActivity.class);
                // passing array index
                i.putExtra("ID", Long.toString(id));
                i.putExtra("NAME",names.get(position));
                i.putExtra("TYPE","");
                i.putExtra("FAVOURITE","1");
                startActivity(i);
            }
        });

        viewPager = findViewById(R.id.viewPager);
        sliderImg = new ArrayList<>();
        bIds = new ArrayList<>();
        bName = new ArrayList<>();
        bUrl = new ArrayList<>();

        viewPager1 = findViewById(R.id.viewPager1);
        sliderImg1 = new ArrayList<>();
        bIds1 = new ArrayList<>();
        bName1 = new ArrayList<>();
        bUrl1 = new ArrayList<>();

        viewPager2 = findViewById(R.id.viewPager2);
        sliderImg2 = new ArrayList<>();
        bIds2 = new ArrayList<>();
        bName2 = new ArrayList<>();
        bUrl2 = new ArrayList<>();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);

        gh = new GetInitHome();
        gh.execute((Void) null);
    }

    private void setLanguage(String languageToLoad){
        String default_lang = "hi";
        switch (languageToLoad){
            case "null":
            case "":
            case "Hindi":
                default_lang = "hi";
                break;

            case "English":
                default_lang = "en";
                break;
        }
        Locale locale = new Locale(default_lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_main);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_video) {
            Intent i = new Intent(getApplicationContext(), VideoAllActivity.class);
            startActivity(i);
        } else if(id == R.id.nav_fet_video) {
            Intent i = new Intent(getApplicationContext(), VideoFeaturedActivity.class);
            i.putExtra("FAVOURITE", "1");
            startActivity(i);
        } else if (id == R.id.nav_ask_doubt) {
            Intent i = new Intent(getApplicationContext(), DoubtActivity.class);
            i.putExtra("ID", "");
            i.putExtra("SUBJECTID", "");
            i.putExtra("CHAPTERID", "");
            i.putExtra("TOPICID", "");
            i.putExtra("TYPE", "SUBJECT");
            startActivity(i);
        //} else if (id == R.id.nav_activity) {

        } else if(id == R.id.nav_scan_book){
            Intent i = new Intent(getApplicationContext(), ScanBookActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_contact) {
            Intent i = new Intent(getApplicationContext(), ContactActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_fav_chapter) {
            Intent i = new Intent(getApplicationContext(), ChapterActivity.class);
            i.putExtra("FAVOURITE", "2");
            startActivity(i);
        } else if (id == R.id.nav_fav_video) {
            Intent i = new Intent(getApplicationContext(), VideosSubjectWiseActivity.class);
            i.putExtra("FAVOURITE", "2");
            startActivity(i);
        } else if (id == R.id.nav_fav_ebook) {
            Intent i = new Intent(getApplicationContext(), EbookSubjectWiseActivity.class);
            i.putExtra("FAVOURITE", "2");
            startActivity(i);
        } else if (id == R.id.nav_fav_activity) {
            Intent i = new Intent(getApplicationContext(), ActivitySubjectWiseActivity.class);
            i.putExtra("FAVOURITE", "2");
            startActivity(i);
        } else if (id == R.id.nav_fav_feat_video) {
            Intent i = new Intent(getApplicationContext(), VideoFeaturedActivity.class);
            i.putExtra("FAVOURITE", "2");
            startActivity(i);
        //} else if (id == R.id.nav_download) {

        } else if (id == R.id.nav_logout) {
            session.logoutUser();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

                    if (viewPager1.getCurrentItem() < sliderImg1.size() - 1) {
                        viewPager1.setCurrentItem(viewPager1.getCurrentItem() + 1);
                    } else {
                        viewPager1.setCurrentItem(0);
                    }

                    if (viewPager2.getCurrentItem() < sliderImg2.size() - 1) {
                        viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
                    } else {
                        viewPager2.setCurrentItem(0);
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
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("studentid",user.get("user_id"));
                postDataParams.put("classid",user.get("class"));
                postDataParams.put("languageid",user.get("language"));

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url) + "inithome");

                try {
                     JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    final String message = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONArray subjects = rootJSON.getJSONObject("results").getJSONArray("subjects");

                        for (int i = 0; i < subjects.length(); i++) {
                            String url = getString(R.string.image_url)+'/' + subjects.getJSONObject(i).getString("imageurl");
                            images.add(url);
                            names.add( subjects.getJSONObject(i).getString("subjectname" + lName));
                            ids.add( subjects.getJSONObject(i).getString("subjectid"));
                        }

                        JSONArray recentV = rootJSON.getJSONObject("results").getJSONArray("recentvideos");

                        for (int i = 0; i < recentV.length(); i++) {
                            SliderUtils sliderUtils = new SliderUtils();
                            String url = getString(R.string.image_url)+'/'+recentV.getJSONObject(i).getString("thumbnail");
                            sliderUtils.setSliderImageUrl(url);
                            sliderImg.add(sliderUtils);
                            bIds.add(recentV.getJSONObject(i).getString("videoid"));
                            bName.add(recentV.getJSONObject(i).getString("videoname" + lName));
                            bUrl.add(recentV.getJSONObject(i).getString("videofile"));
                        }

                        JSONArray topV = rootJSON.getJSONObject("results").getJSONArray("topvideos");

                        for (int i = 0; i < topV.length(); i++) {
                            SliderUtils sliderUtils = new SliderUtils();
                            String url = getString(R.string.image_url)+'/'+topV.getJSONObject(i).getString("thumbnail");
                            sliderUtils.setSliderImageUrl(url);
                            sliderImg1.add(sliderUtils);
                            bIds1.add(topV.getJSONObject(i).getString("videoid"));
                            bName1.add(topV.getJSONObject(i).getString("videoname" + lName));
                            bUrl1.add(topV.getJSONObject(i).getString("videofile"));
                        }

                        JSONArray latestV = rootJSON.getJSONObject("results").getJSONArray("latestvideos");

                        for (int i = 0; i < latestV.length(); i++) {
                            SliderUtils sliderUtils = new SliderUtils();
                            String url = getString(R.string.image_url)+'/'+latestV.getJSONObject(i).getString("thumbnail");
                            sliderUtils.setSliderImageUrl(url);
                            sliderImg2.add(sliderUtils);
                            bIds2.add(latestV.getJSONObject(i).getString("videoid"));
                            bName2.add(latestV.getJSONObject(i).getString("videoname" + lName));
                            bUrl2.add(latestV.getJSONObject(i).getString("videofile"));
                        }
                        return true;
                    }else{
                        errorMessage = message;
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
                    //Creating GridViewAdapter Object for Categories
                    GridViewAdapter gridViewAdapter = new GridViewAdapter(getApplicationContext(),images,names,ids);
                    gridView.setAdapter(gridViewAdapter);

                    //Recent
                    viewPagerAdapter = new ViewPagerAdapter(sliderImg,MainActivity.this, bIds, bName, bUrl);
                    viewPager.setAdapter(viewPagerAdapter);

                    //Top
                    viewPagerAdapter1 = new ViewPagerAdapter(sliderImg1,MainActivity.this, bIds1, bName1, bUrl1);
                    viewPager1.setAdapter(viewPagerAdapter1);

                    //Latest
                    viewPagerAdapter2 = new ViewPagerAdapter(sliderImg2,MainActivity.this, bIds2, bName2, bUrl2);
                    viewPager2.setAdapter(viewPagerAdapter2);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.unabletofetchdata), Toast.LENGTH_LONG).show();
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
