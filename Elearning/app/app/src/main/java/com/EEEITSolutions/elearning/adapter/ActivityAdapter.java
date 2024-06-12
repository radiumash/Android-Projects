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
import com.EEEITSolutions.elearning.activity.ActivityActivity;
import com.EEEITSolutions.elearning.common.MySQLiteHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ActivityAdapter extends BaseAdapter implements Filterable {
    public Context context;
    public ArrayList<ActivityDetails>ActivityArrayList;
    public ArrayList<ActivityDetails> orig;
    private static final int ACC_REQUEST = 1;
    private MySQLiteHelper db;

    public ActivityAdapter(Context context, ArrayList<ActivityDetails> activityArrayList) {
        super();
        this.context = context;
        this.ActivityArrayList = activityArrayList;
        db = new MySQLiteHelper(context);
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<ActivityDetails> results = new ArrayList<>();
                if (orig == null)
                    orig = ActivityArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final ActivityDetails g : orig) {
                            if (g.getActivityName().toLowerCase()
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
                ActivityArrayList = (ArrayList<ActivityDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return ActivityArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return ActivityArrayList.get(position);
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
            holder.aName = convertView.findViewById(R.id.chapterName);
            holder.tName = convertView.findViewById(R.id.chapterDesc);
            holder.bRating = convertView.findViewById(R.id.ratingBar);
            holder.fTB = convertView.findViewById(R.id.favButton);
            holder.cId = convertView.findViewById(R.id.chapterId);
            holder.tId = convertView.findViewById(R.id.videoFile);
            holder.sName = convertView.findViewById(R.id.subjectName);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }
        holder.vThumb.setOnClickListener(PopupListener);
        holder.cId.setText(ActivityArrayList.get(position).getChapterID());
        holder.aName.setText(ActivityArrayList.get(position).getActivityName());
        holder.aName.setOnClickListener(PopupListener);

        holder.tName.setText(ActivityArrayList.get(position).getTopicName());
        String sub = context.getString(R.string.activity_type) + ActivityArrayList.get(position).getActivityType() + "\n" + context.getString(R.string.total_marks)
                +  ActivityArrayList.get(position).getTotalMarks() + "\n" + context.getString(R.string.total_questions) + ActivityArrayList.get(position).getTotalQuestions()
                + "\n" + context.getString(R.string.time_reequired) + ActivityArrayList.get(position).getTimeRequired();
        holder.sName.setText(sub);
        holder.tId.setText(ActivityArrayList.get(position).getTopicID());


        if(!ActivityArrayList.get(position).getRating().equals("null") && !ActivityArrayList.get(position).getRating().isEmpty() && ActivityArrayList.get(position).getRating() != null) {
            holder.bRating.setRating(Float.parseFloat(ActivityArrayList.get(position).getRating()));
            holder.bRating.setVisibility(View.VISIBLE);
        }
        final Integer pro_id = Integer.parseInt(ActivityArrayList.get(position).getActivityID());
        if(db.getFavouriteActivity(pro_id)) {
            holder.fTB.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
        }else{
            holder.fTB.setChecked(false);
        }
        holder.fTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
                    db.addFavouriteActivity(pro_id,true);
                }else {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_gray_24dp));
                    db.removeFavouriteActivity(pro_id);
                }
            }
        });

        //BitmapDownloaderTask task = new BitmapDownloaderTask(pid, url, holder.bImage);
        //task.execute((Void) null);
        Picasso.with(context)
                .load(context.getString(R.string.image_url) + context.getString(R.string.activity1_path))
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.vThumb);

        Integer rowPosition = position;
        holder.vThumb.setTag(rowPosition);
        holder.aName.setTag(rowPosition);
        return convertView;

    }



    View.OnClickListener PopupListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Integer viewPosition = (Integer) view.getTag();
            String pid = ActivityArrayList.get(viewPosition).getActivityID();
            String pName = ActivityArrayList.get(viewPosition).getActivityName();
            Intent intent = new Intent(context, ActivityActivity.class);
            intent.putExtra("ID", pid);
            intent.putExtra("NAME",pName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
        }
    };

    private static class PlaceHolder{
        ImageView vThumb;
        TextView cId;
        TextView tId;
        TextView tName;
        TextView aId;
        TextView aName;
        TextView sName;
        RatingBar bRating;
        ToggleButton fTB;
    }
}
