package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.PicassoCircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GridViewAdapter extends BaseAdapter {
    //Context
    private Context context;

    //Array List that would contain the urls and the titles for the images
    private ArrayList<String> images;
    private ArrayList<String> names;
    private ArrayList<String> ids;

    public GridViewAdapter (Context context, ArrayList<String> images, ArrayList<String> names, ArrayList<String> ids){
        //Getting all the values
        this.context = context;
        this.images = images;
        this.names = names;
        this.ids = ids;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt(ids.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Creating a linear layout

        GridView grid = (GridView)parent;
        int side = grid.getMeasuredWidth() / 3;

        LinearLayout linearLayout = new LinearLayout(context);

        linearLayout.setGravity(Gravity.BOTTOM);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(0,0,0,35);
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(side,side);

        TextView textView = new TextView(context);
        textView.setTextColor(Color.parseColor("#333333"));
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER);
        textView.setShadowLayer(1f,-2f,-2f,Color.WHITE);
        textView.setTypeface(null, Typeface.BOLD);;
        textView.setText(names.get(position));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);

        //Scaling the imageview
        ImageView mImageView = new ImageView(context);

        Picasso.with(context)
                .load(images.get(position))
                .transform(new PicassoCircleTransformation())
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(mImageView);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        mImageView.setLayoutParams(rp);

        //Adding views to the layout
        linearLayout.addView(mImageView);
        linearLayout.addView(textView);

        //Returning the layout
        return linearLayout;
    }
}

