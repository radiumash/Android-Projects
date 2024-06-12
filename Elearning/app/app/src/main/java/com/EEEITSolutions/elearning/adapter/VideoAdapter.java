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
import com.EEEITSolutions.elearning.activity.VideoActivity;
import com.EEEITSolutions.elearning.common.MySQLiteHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VideoAdapter extends BaseAdapter implements Filterable{
    public Context context;
    public ArrayList<VideoDetails> VideoArrayList;
    public ArrayList<VideoDetails> orig;
    private static final int ACC_REQUEST = 1;
    private MySQLiteHelper db;

    public VideoAdapter(Context context, ArrayList<VideoDetails> providerArrayList) {
        super();
        this.context = context;
        this.VideoArrayList = providerArrayList;
        db = new MySQLiteHelper(context);
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<VideoDetails> results = new ArrayList<>();
                if (orig == null)
                    orig = VideoArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final VideoDetails g : orig) {
                            if (g.getChapterName().toLowerCase()
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
                VideoArrayList = (ArrayList<VideoDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return VideoArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return VideoArrayList.get(position);
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
            holder.cName = convertView.findViewById(R.id.subjectName);
            holder.tName = convertView.findViewById(R.id.chapterDesc);
            holder.bRating = convertView.findViewById(R.id.ratingBar);
            holder.fTB = convertView.findViewById(R.id.favButton);
            holder.cId = convertView.findViewById(R.id.chapterId);
            holder.vFile = convertView.findViewById(R.id.videoFile);
            holder.vName = convertView.findViewById(R.id.chapterName);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }
        holder.vThumb.setOnClickListener(PopupListener);
        holder.cId.setText(VideoArrayList.get(position).getTopicID());
        holder.cName.setText(VideoArrayList.get(position).getChapterName());
        holder.vName.setText(VideoArrayList.get(position).getVideoName());
        holder.vName.setOnClickListener(PopupListener);

        holder.tName.setText(VideoArrayList.get(position).getTopicName());
        holder.vFile.setText(VideoArrayList.get(position).getVideoFile());

        if(!VideoArrayList.get(position).getRating().equals("null") && !VideoArrayList.get(position).getRating().isEmpty() && VideoArrayList.get(position).getRating() != null) {
            holder.bRating.setRating(Float.parseFloat(VideoArrayList.get(position).getRating()));
            holder.bRating.setVisibility(View.VISIBLE);
        }
        final Integer pro_id = Integer.parseInt(VideoArrayList.get(position).getVideoID());
        if(db.getFavouriteVideo(pro_id)) {
            holder.fTB.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
        }else{
            holder.fTB.setChecked(false);
        }
        holder.fTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
                    db.addFavouriteVideo(pro_id,true);
                }else {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_gray_24dp));
                    db.removeFavouriteVideo(pro_id);
                }
            }
        });

        String url = context.getString(R.string.image_url) + '/' + VideoArrayList.get(position).getVideoThumb();

        Picasso.with(context)
                .load(url)
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
            String pid = VideoArrayList.get(viewPosition).getVideoID();
            String pName = VideoArrayList.get(viewPosition).getChapterName();
            String pUrl = VideoArrayList.get(viewPosition).getVideoFile();
            Intent intent = new Intent(context, VideoActivity.class);
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
        TextView tName;
        TextView tId;
        TextView vFile;
        TextView vId;
        TextView vName;
        TextView cId;
        RatingBar bRating;
        ToggleButton fTB;
    }
}