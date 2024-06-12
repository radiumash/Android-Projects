package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class SecondaryActivity extends AppCompatActivity {

    private String subject_id, subject_name;
    private TextView imgVid, imgBook, imgAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);

        Bundle extra = getIntent().getExtras();
        try {
            subject_id = extra.getString("ID");
            subject_name = extra.getString("NAME");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if(!subject_name.isEmpty()){
            this.setTitle(subject_name);
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        imgVid = findViewById(R.id.txtVideo);
        imgBook = findViewById(R.id.txtBook);
        imgAct = findViewById(R.id.txtActivity);

        imgVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), VideosSubjectWiseActivity.class);
                // passing array index
                i.putExtra("ID", subject_id);
                i.putExtra("NAME",subject_name);
                i.putExtra("FAVOURITE","");
                startActivity(i);
            }
        });

        imgBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), EbookSubjectWiseActivity.class);
                // passing array index
                i.putExtra("ID", subject_id);
                i.putExtra("NAME",subject_name);
                i.putExtra("FAVOURITE","");
                startActivity(i);
            }
        });

        imgAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActivitySubjectWiseActivity.class);
                // passing array index
                i.putExtra("ID", subject_id);
                i.putExtra("NAME",subject_name);
                i.putExtra("FAVOURITE","");
                startActivity(i);
            }
        });
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
}
