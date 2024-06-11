package com.rightone.ashish.rightexecutive;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AccountDetailAdapter extends ArrayAdapter<AccountDetail> {

    Context mContext;
    int mLayoutResourceId;
    AccountDetail mData[];

    public AccountDetailAdapter(@NonNull Context context, int resource, @NonNull AccountDetail[] data) {
        super(context, resource, data);
        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mData = data;
    }

    @Nullable
    @Override
    public AccountDetail getItem(int position) {
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

            holder.bName = row.findViewById(R.id.nameTextView);
            holder.sName = row.findViewById(R.id.serviceTextView);
            holder.sTitle = row.findViewById(R.id.subscriptionTextView);
            holder.locName = row.findViewById(R.id.locationTextView);
            holder.bPic = row.findViewById(R.id.businessImage);
            holder.pPic = row.findViewById(R.id.subImageView);

            row.setTag(holder);
        }else{
            //Otherwise use an existing one
            holder = (PlaceHolder) row.getTag();
        }

        //get the data from data array
        AccountDetail place = mData[position];

        //setting the view to reflect the data we want to display
        holder.bName.setText(place.mBusinessName);
        holder.sName.setText(place.mServiceName);
        holder.locName.setText(place.mLocCityState);
        String cCon = place.mSubscription + '(' + place.mAmount + ')';
        holder.sTitle.setText(cCon);
        if(place.mIsPremium.equals("1")){
            holder.pPic.setVisibility(View.VISIBLE);
        }

        String url = mContext.getString(R.string.image_url) + '/' + place.mBusinessPic;
        Picasso.with(mContext)
                .load(url)
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.bPic);

        Integer rowPosition = position;
        holder.bPic.setTag(rowPosition);

        //returning the row view (because this is called getView after all)
        return row;
    }

    private static class PlaceHolder{
        TextView bName;
        TextView sName;
        TextView locName;
        TextView sTitle;
        ImageView bPic;
        ImageView pPic;
    }
}
