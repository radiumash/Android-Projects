package com.EEEITSolutions.elearning.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.EEEITSolutions.elearning.R;
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
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //NetworkImageView
        //NetworkImageView networkImageView = new NetworkImageView(context);
        ImageView mImageView = new ImageView(context);
        //Initializing ImageLoader
//        if(GlobalVars.CategoryActivityMap.containsKey(ids.get(position))){
//            imageLoader = GlobalVars.CategoryActivityMap.get(ids.get(position));
//        }else{
//            imageLoader = CustomVolleyRequest.getInstance(context).getImageLoader();
//            imageLoader.get(images.get(position), ImageLoader.getImageListener(networkImageView, R.drawable.thumb_placeholder, android.R.drawable.ic_dialog_alert));
//            GlobalVars.CategoryActivityMap.put(ids.get(position),imageLoader);
//        }

        Picasso.with(context)
                .load(images.get(position))
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(mImageView);

        //Setting the image url to load
        //networkImageView.setImageUrl(images.get(position),imageLoader);

        //Creating a textview to show the title
        TextView textView = new TextView(context);
        textView.setTextColor(Color.parseColor("#333333"));
        textView.setPadding(0,0,0,30);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setText(names.get(position));

        //Scaling the imageview
        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageView.setPadding(0,10,0,0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(side,side);
        lp.gravity = Gravity.CENTER;
        mImageView.setLayoutParams(lp);

        //Adding views to the layout
        linearLayout.addView(mImageView);
        linearLayout.addView(textView);

        //Returnint the layout
        return linearLayout;
    }
}
