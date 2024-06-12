package com.EEEITSolutions.elearning.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.EEEITSolutions.elearning.R;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.EEEITSolutions.elearning.activity.VideoActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<SliderUtils> sliderImg;
    private ArrayList<String> ids, names, urls;


    public ViewPagerAdapter(List sliderImg,Context context,ArrayList<String> ids,ArrayList<String> names, ArrayList<String> urls) {
        this.sliderImg = sliderImg;
        this.context = context;
        this.ids = ids;
        this.names = names;
        this.urls = urls;
    }

    @Override
    public int getCount() {
        return sliderImg.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, null);
        SliderUtils utils = sliderImg.get(position);
        ImageView imageView = view.findViewById(R.id.imageView);
        Picasso.with(context)
                .load(utils.getSliderImageUrl())
                .placeholder(R.drawable.banner_placeholder) // optional
                .into(imageView);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoActivity.class);
                intent.putExtra("ID", ids.get(position));
                intent.putExtra("NAME", names.get(position));
                intent.putExtra("URL", urls.get(position));
                context.startActivity(intent);
            }
        });

        TextView tv = view.findViewById(R.id.txtTitle);
        tv.setText(names.get(position));

        TextView tvD = view.findViewById(R.id.txtDesc);
        tvD.setText(names.get(position));

        ViewPager vp = (ViewPager) container;
        int side = vp.getMeasuredWidth();
        double height = side * .71;
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setGravity(Gravity.BOTTOM);
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(side,(int)height);
        vp.setLayoutParams(rp);
        vp.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}
