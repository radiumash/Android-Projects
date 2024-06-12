package com.EEEITSolutions.elearning.adapter;

import android.content.Context;
import android.content.Intent;
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

import androidx.core.content.ContextCompat;

import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.activity.EbookActivity;
import com.EEEITSolutions.elearning.common.MySQLiteHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EbookAdapter extends BaseAdapter implements Filterable {
    public Context context;
    public ArrayList<EbookDetails> EbookArrayList;
    public ArrayList<EbookDetails> orig;
    private static final int ACC_REQUEST = 1;
    private MySQLiteHelper db;

    public EbookAdapter(Context context, ArrayList<EbookDetails> eBookArrayList) {
        super();
        this.context = context;
        this.EbookArrayList = eBookArrayList;
        db = new MySQLiteHelper(context);
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<EbookDetails> results = new ArrayList<>();
                if (orig == null)
                    orig = EbookArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final EbookDetails g : orig) {
                            if (g.getEbookName().toLowerCase()
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
                EbookArrayList = (ArrayList<EbookDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return EbookArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return EbookArrayList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);
            holder = new PlaceHolder();

            holder.vThumb = convertView.findViewById(R.id.videoThumb);
            holder.cName = convertView.findViewById(R.id.chapterName);
            holder.cDesc = convertView.findViewById(R.id.chapterDesc);
            holder.bRating = convertView.findViewById(R.id.ratingBar);
            holder.fTB = convertView.findViewById(R.id.favButton);
            holder.cId = convertView.findViewById(R.id.chapterId);
            holder.vFile = convertView.findViewById(R.id.videoFile);
            holder.sName = convertView.findViewById(R.id.subjectName);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }
        holder.vThumb.setOnClickListener(PopupListener);
        holder.cId.setText(EbookArrayList.get(position).getChapterID());
        holder.cName.setText(EbookArrayList.get(position).getEbookName());
        holder.cName.setOnClickListener(PopupListener);

        holder.cDesc.setText(EbookArrayList.get(position).getTopicName());
        holder.sName.setText(EbookArrayList.get(position).getChapterName());
        holder.vFile.setText(EbookArrayList.get(position).getEbookFile());


        if(!EbookArrayList.get(position).getRating().equals("null") && !EbookArrayList.get(position).getRating().isEmpty() && EbookArrayList.get(position).getRating() != null) {
            holder.bRating.setRating(Float.parseFloat(EbookArrayList.get(position).getRating()));
            holder.bRating.setVisibility(View.VISIBLE);
        }
        final Integer pro_id = Integer.parseInt(EbookArrayList.get(position).getmEbookID());
        if(db.getFavouriteEbook(pro_id)) {
            holder.fTB.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
        }else{
            holder.fTB.setChecked(false);
        }
        holder.fTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
                    db.addFavouriteEbook(pro_id,true);
                }else {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_gray_24dp));
                    db.removeFavouriteEbook(pro_id);
                }
            }
        });

        //BitmapDownloaderTask task = new BitmapDownloaderTask(pid, url, holder.bImage);
        //task.execute((Void) null);
        Picasso.with(context)
                .load(context.getString(R.string.image_url) + context.getString(R.string.ebook_path))
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.vThumb);

        Integer rowPosition = position;
        holder.vThumb.setTag(rowPosition);
        holder.cName.setTag(rowPosition);
        return convertView;

    }



    View.OnClickListener PopupListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Integer viewPosition = (Integer) view.getTag();
            String pid = EbookArrayList.get(viewPosition).getmEbookID();
            String pName = EbookArrayList.get(viewPosition).getEbookName();
            String pUrl = EbookArrayList.get(viewPosition).getEbookFile();
            Intent intent = new Intent(context, EbookActivity.class);
            intent.putExtra("ID", pid);
            intent.putExtra("NAME",pName);
            intent.putExtra("URL",pUrl);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
        }
    };

    private static class PlaceHolder{
        ImageView vThumb;
        TextView cName;
        TextView cDesc;
        TextView vFile;
        TextView vId;
        TextView cId;
        TextView sName;
        RatingBar bRating;
        ToggleButton fTB;
    }

}
