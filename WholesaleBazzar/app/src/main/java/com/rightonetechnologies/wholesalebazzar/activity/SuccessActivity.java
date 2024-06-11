package com.rightonetechnologies.wholesalebazzar.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.network.ConnectivityReceiver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SuccessActivity extends AppCompatActivity {

    Bundle extra;
    JSONObject obj;
    String json;
    TextView mOrder;
    Button mHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        mOrder = findViewById(R.id.tvOrder);
        mHome = findViewById(R.id.btnHome);
        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
        extra = getIntent().getExtras();

        if(extra != null){
            json = extra.getString("ORDER");
        }

        if(!json.equals("")) {
            try {
                obj = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String orderStr = "";
        if(obj != null){
            try {
                orderStr = "Order Number: " + obj.getString("order_number") + "\n";
                orderStr += "Order Date: " + getDateReadable(obj.getString("created")) + "\n";
                orderStr += "Net Payable Amount: " + obj.getString("net_payable_amount") + "\n";
                orderStr += "Order Status: " + obj.getString("order_status") + "\n";
                orderStr += "Payment Mode: " + obj.getString("payment_mode") + "\n";
                mOrder.setText(orderStr);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
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
}
