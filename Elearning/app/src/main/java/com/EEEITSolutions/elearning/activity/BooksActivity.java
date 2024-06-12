package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import android.os.Bundle;

public class BooksActivity extends AppCompatActivity {

    private String subject_id, subject_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        Bundle extra = getIntent().getExtras();
        try {
            subject_id = extra.getString("ID");
            subject_name = extra.getString("NAME");
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if(!subject_name.isEmpty()){
            this.setTitle(subject_name + ": " + getString(R.string.books));
        }
    }
}
