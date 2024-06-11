package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.adapter.CartAdapter;
import com.rightonetechnologies.wholesalebazzar.adapter.CartDetails;
import com.rightonetechnologies.wholesalebazzar.common.MySQLiteHelper;
import com.rightonetechnologies.wholesalebazzar.common.SessionManager;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class CartActivity extends AppCompatActivity {

    private MySQLiteHelper db;
    CartDetails[] myCartDetailArray = null;
    CartAdapter mCartDetailAdapter = null;
    private ArrayList<CartDetails> cartArrayList;
    ListView mCartListView;
    ProgressBar mProgress;
    private SessionManager session;
    private HashMap<String,String> user;
    RelativeLayout rl;
    LinearLayout ll;
    TextView mTotal;
    Button mCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

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

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        this.setTitle(getString(R.string.view_cart));

        mTotal = findViewById(R.id.tvTotal);
        mCheckout = findViewById(R.id.btnCheckout);
        mCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AddressActivity.class);
                startActivity(i);
            }
        });
        db = new MySQLiteHelper(this);
        mCartListView = findViewById(R.id.myListView);
        mProgress = findViewById(R.id.loading);
        checkCart();
    }

    public void checkCart(){
        rl = findViewById(R.id.checkOut);
        ll = findViewById(R.id.cartEmpty);
        if(getCartTotal() > 0){
            rl.setVisibility(View.VISIBLE);
            ll.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(View.VISIBLE);
        }else {
            rl.setVisibility(View.INVISIBLE);
            ll.setVisibility(View.VISIBLE);
        }
        showCart();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(getCartTotal() > 0) {
            getMenuInflater().inflate(R.menu.cart, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_empty_cart) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
            builder.setTitle(getString(R.string.action_empty_cart));
            builder.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
            builder.setMessage(getString(R.string.alert_empty_cart));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    db.deleteCart();
                    checkCart();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateTotal(){
        mTotal.setText(getString(R.string.total_rupee) + db.getCartTotal());
    }

    public Float getCartTotal(){
        return Float.parseFloat(db.getCartTotal());
    }

    private void showCart(){

        try {
            JSONArray exDeals = db.getCart();

            myCartDetailArray = new CartDetails[exDeals.length()];
            cartArrayList = new ArrayList<>();

            for (int i = 0; i < exDeals.length(); i++) {
                String sqlid = exDeals.getJSONObject(i).getString("id");
                String dealid = exDeals.getJSONObject(i).getString("Did");
                String company = exDeals.getJSONObject(i).getString("Company");
                String pid = exDeals.getJSONObject(i).getString("Pid");
                String packid = exDeals.getJSONObject(i).getString("PackID");
                String name = exDeals.getJSONObject(i).getString("DName");
                String mrp = exDeals.getJSONObject(i).getString("Pmrp");
                String sp = exDeals.getJSONObject(i).getString("Psp");
                String tax = exDeals.getJSONObject(i).getString("PTax");
                String unit_id = exDeals.getJSONObject(i).getString("PunitID");
                String unit_name = exDeals.getJSONObject(i).getString("Punit");
                String image_url = exDeals.getJSONObject(i).getString("Image");
                String qty = exDeals.getJSONObject(i).getString("Pqty");

                cartArrayList.add(new CartDetails(sqlid, dealid, company, pid, packid, name, unit_id, unit_name, mrp, sp, tax, qty, image_url));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        mCartListView.post(new Runnable() {
            public void run() {
                mCartDetailAdapter = new CartAdapter(CartActivity.this, cartArrayList);

                if (mCartListView != null) {
                    mCartListView.setAdapter(mCartDetailAdapter);
                    mCartListView.refreshDrawableState();
                }
                mProgress.setVisibility(View.GONE);
            }
        });
        String total = getString(R.string.total_rupee) + db.getCartTotal();
        mTotal.setText(total);
    }
}
