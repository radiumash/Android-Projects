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


public class AccountAdapter  extends ArrayAdapter<Account> {

    Context mContext;
    int mLayoutResourceId;
    Account mData[];

    public AccountAdapter(@NonNull Context context, int resource, @NonNull Account[] data) {
        super(context, resource, data);
        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mData = data;
    }

    @Nullable
    @Override
    public Account getItem(int position) {
        return mData[position];
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        PlaceHolder holder = null;

        //If we currently don't have a row view to reuse...
        if(row == null){
            //Create a new view
            //Inflate a layout for a single view
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(mLayoutResourceId,parent,false);

            holder = new PlaceHolder();

            holder.nameView = row.findViewById(R.id.nameTextView);
            holder.totalView = row.findViewById(R.id.formTextView);
            holder.imageView = row.findViewById(R.id.imageView);
            holder.nameYearView = row.findViewById(R.id.nameYearView);

            row.setTag(holder);
        }else{
            //Otherwise use an existing one
            holder = (PlaceHolder) row.getTag();
        }

        //get the data from data array
        Account place = mData[position];

        //Setup and reuse the same listener for each row
        //holder.imageView.setOnClickListener(PopupListner);

        //setting the view to reflect the data we want to display
        holder.nameView.setText(place.mMonthName);
        holder.nameYearView.setText(place.mYear);
        holder.totalView.setText(String.valueOf(place.mTotalForm));    //Always pay attention to data types.

        int resId = mContext.getResources().getIdentifier(place.mNameOfImage,"drawable",mContext.getPackageName());
        holder.imageView.setImageResource(resId);
        Integer rowPosition = position;
        holder.imageView.setTag(rowPosition);

        //returning the row view (because this is called getView after all)
        return row;

    }

    private static class PlaceHolder{
        TextView nameView;
        TextView nameYearView;
        TextView totalView;
        ImageView imageView;
    }
}
