package com.EEEITSolutions.elearning.common;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import com.EEEITSolutions.elearning.R;

import java.util.Locale;

public class LanguageDefault {
    private Context mContent;
    private AppCompatActivity mActivity;
    private Integer mLayout;

    public void setBuilder(Context content, AppCompatActivity activity, Integer layout){
        this.mContent = content;
        this.mActivity = activity;
        this.mLayout = layout;
    }
    public void setLanguage(String languageToLoad){
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
        mContent.getResources().updateConfiguration(config,
                mContent.getResources().getDisplayMetrics());
        mActivity.setContentView(mLayout);
    }
}
