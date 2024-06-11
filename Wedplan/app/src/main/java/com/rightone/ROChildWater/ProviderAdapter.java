package com.rightone.ROChildWater;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ProviderAdapter extends BaseAdapter implements Filterable {

    public Context context;
    public ArrayList<ProviderDetails> providerArrayList;
    public ArrayList<ProviderDetails> orig;
    private static final int ACC_REQUEST = 1;
    private MySQLiteHelper db;

    public ProviderAdapter(Context context, ArrayList<ProviderDetails> providerArrayList) {
        super();
        this.context = context;
        this.providerArrayList = providerArrayList;
        db = new MySQLiteHelper(context);
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<ProviderDetails> results = new ArrayList<>();
                if (orig == null)
                    orig = providerArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final ProviderDetails g : orig) {
                            if (g.getBusinessName().toLowerCase()
                                    .contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                providerArrayList = (ArrayList<ProviderDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return providerArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return providerArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlaceHolder holder;
        if(convertView==null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_provider, parent, false);
            holder = new PlaceHolder();

            holder.bImage = convertView.findViewById(R.id.businessImage);
            holder.bName = convertView.findViewById(R.id.nameTextView);
            holder.bLocation = convertView.findViewById(R.id.locationTextView);
            holder.cName = convertView.findViewById(R.id.categoryTextView);
            holder.servName = convertView.findViewById(R.id.serviceTextView);
            holder.sName = convertView.findViewById(R.id.subImageView);
            holder.bRating = convertView.findViewById(R.id.ratingBar);
            holder.fTB = convertView.findViewById(R.id.favButton);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }
        holder.bImage.setOnClickListener(PopupListener);

        holder.bName.setText(providerArrayList.get(position).getBusinessName());
        holder.bName.setOnClickListener(PopupListener);

        holder.servName.setText(providerArrayList.get(position).getServiceName());
        holder.bLocation.setText(providerArrayList.get(position).getLocation());
        holder.cName.setText(providerArrayList.get(position).getSpeciality());

        if(!providerArrayList.get(position).getRating().equals("null") && !providerArrayList.get(position).getRating().isEmpty() && providerArrayList.get(position).getRating() != null) {
            holder.bRating.setRating(Float.parseFloat(providerArrayList.get(position).getRating()));
            holder.bRating.setVisibility(View.VISIBLE);
        }
        final Integer pro_id = Integer.parseInt(providerArrayList.get(position).getProvider());
        if(db.getFavourites(pro_id)) {
            holder.fTB.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
        }else{
            holder.fTB.setChecked(false);
        }
        holder.fTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
                    db.addFavourites(pro_id,true);
                }else {
                    buttonView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_favorite_gray_24dp));
                    db.removeFavourites(pro_id);
                }
            }
        });

        if (providerArrayList.get(position).getSubscription()) {
            int resId = context.getResources().getIdentifier("@drawable/premium", null, context.getPackageName());
            holder.sName.setImageResource(resId);
        } else {
            holder.sName.setImageDrawable(null);
        }
        String pid = providerArrayList.get(position).getProvider();
        String url = context.getString(R.string.image_url) + '/' + providerArrayList.get(position).getBusinessImage();
        //BitmapDownloaderTask task = new BitmapDownloaderTask(pid, url, holder.bImage);
        //task.execute((Void) null);
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.bImage);

        Integer rowPosition = position;
        holder.bImage.setTag(rowPosition);
        holder.bName.setTag(rowPosition);
        return convertView;

    }

    View.OnClickListener PopupListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Integer viewPosition = (Integer) view.getTag();
            String pid = providerArrayList.get(viewPosition).getProvider();
            String pName = providerArrayList.get(viewPosition).getBusinessName();

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("ID", pid);
            intent.putExtra("NAME",pName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
        }
    };

    private static class PlaceHolder{
        ImageView bImage;
        TextView bName;
        TextView bLocation;
        TextView cName;
        TextView servName;
        ImageView sName;
        RatingBar bRating;
        ToggleButton fTB;
    }

    class BitmapDownloaderTask extends AsyncTask<Void, Void, Bitmap> {

        String mPid, mUrl;
        ImageView mImageView;

        public BitmapDownloaderTask(String pid, String url,ImageView imageView) {
            mPid = pid;
            mUrl = url;
            mImageView = imageView;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(Void... params) {
            // params comes from the execute() call: params[0] is the url.
            Bitmap bm;
            if(GlobalVars.ProviderActivityMap.containsKey(mPid)){
                bm = GlobalVars.ProviderActivityMap.get(mPid);
            }else{
                NetworkADO networkADO = new NetworkADO();
                bm = networkADO.getBitmapFromURL(mUrl);
                GlobalVars.ProviderActivityMap.put(mPid,bm);
            }
            return bm;
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if(bitmap !=null){
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

}
