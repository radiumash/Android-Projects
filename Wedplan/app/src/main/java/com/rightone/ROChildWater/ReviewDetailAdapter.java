package com.rightone.ROChildWater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReviewDetailAdapter extends ArrayAdapter<ReviewDetail> {
    Context mContext;
    int mLayoutResourceId;
    ReviewDetail mData[];

    public ReviewDetailAdapter(@NonNull Context context, int resource, @NonNull ReviewDetail[] data) {
        super(context, resource, data);
        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mData = data;
    }

    @Nullable
    @Override
    public ReviewDetail getItem(int position) {
        return mData[position];
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        PlaceHolder holder;

        //If we currently don't have a row view to reuse...
        if(row == null){
            //Create a new view
            //Inflate a layout for a single view
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(mLayoutResourceId,parent,false);

            holder = new PlaceHolder();

            holder.rChar = row.findViewById(R.id.alphaTextView);
            holder.bName = row.findViewById(R.id.nameTextView);
            holder.bRTitle = row.findViewById(R.id.titleTextView);
            holder.bReview = row.findViewById(R.id.reviewTextView);
            holder.bRating = row.findViewById(R.id.ratingBar);

            row.setTag(holder);
        }else{
            //Otherwise use an existing one
            holder = (PlaceHolder) row.getTag();
        }

        //get the data from data array
        ReviewDetail place = mData[position];

        //setting the view to reflect the data we want to display
        holder.rChar.setText(String.valueOf(place.mRChar).toUpperCase());
        holder.bName.setText(place.mName);
        holder.bRTitle.setText(place.mRTitle);
        String subStr;
        if(place.mReview.length() > 70){
            subStr = place.mReview.substring(0,70) + " ...";
        }else{
            subStr = place.mReview;
        }
        holder.bReview.setText(subStr);
        if(!place.mRating.equals("null") && !place.mRating.isEmpty() && place.mRating != null){
            holder.bRating.setRating(Float.parseFloat(place.mRating));
            holder.bRating.setVisibility(View.VISIBLE);
        }
        //returning the row view (because this is called getView after all)
        return row;
    }

    private static class PlaceHolder{
        TextView rChar;
        TextView bName;
        TextView bRTitle;
        TextView bReview;
        RatingBar bRating;
    }
}
