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
import com.EEEITSolutions.elearning.activity.SecondaryActivity;
import com.EEEITSolutions.elearning.common.MySQLiteHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChapterAdapter extends BaseAdapter implements Filterable {
    public Context context;
    public ArrayList<ChapterDetails> ChapterArrayList;
    public ArrayList<ChapterDetails> orig;
    private static final int ACC_REQUEST = 1;
    private MySQLiteHelper db;

    public ChapterAdapter(Context context, ArrayList<ChapterDetails> ChapterkArrayList) {
        super();
        this.context = context;
        this.ChapterArrayList = ChapterkArrayList;
        db = new MySQLiteHelper(context);
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<ChapterDetails> results = new ArrayList<>();
                if (orig == null)
                    orig = ChapterArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final ChapterDetails g : orig) {
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
                ChapterArrayList = (ArrayList<ChapterDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return ChapterArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return ChapterArrayList.get(position);
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
            holder.sId = convertView.findViewById(R.id.videoFile);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }
        holder.vThumb.setOnClickListener(PopupListener);
        holder.cId.setText(ChapterArrayList.get(position).getChapterID());
        holder.cName.setText(ChapterArrayList.get(position).getChapterName());
        holder.cName.setOnClickListener(PopupListener);

        holder.cDesc.setText(ChapterArrayList.get(position).getChapterDesc());
        holder.sId.setText(ChapterArrayList.get(position).getSubjectID());


        if(!ChapterArrayList.get(position).getRating().equals("null") && !ChapterArrayList.get(position).getRating().isEmpty() && ChapterArrayList.get(position).getRating() != null) {
            holder.bRating.setRating(Float.parseFloat(ChapterArrayList.get(position).getRating()));
            holder.bRating.setVisibility(View.VISIBLE);
        }
        final Integer pro_id = Integer.parseInt(ChapterArrayList.get(position).getChapterID());
        if(db.getFavouriteChapter(pro_id)) {
            holder.fTB.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
        }else{
            holder.fTB.setChecked(false);
        }
        holder.fTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp));
                    db.addFavouriteChapter(pro_id,true);
                }else {
                    buttonView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_gray_24dp));
                    db.removeFavouriteChapter(pro_id);
                }
            }
        });

        //BitmapDownloaderTask task = new BitmapDownloaderTask(pid, url, holder.bImage);
        //task.execute((Void) null);
        Picasso.with(context)
                .load(context.getString(R.string.image_url) + context.getString(R.string.chapter_path))
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
            String pid = ChapterArrayList.get(viewPosition).getChapterID();
            String pName = ChapterArrayList.get(viewPosition).getChapterName();
            String sid = ChapterArrayList.get(viewPosition).getSubjectID();
            Intent intent = new Intent(context, SecondaryActivity.class);
            intent.putExtra("ID", pid);
            intent.putExtra("NAME",pName);
            intent.putExtra("SUBJECTID",sid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
        }
    };

    private static class PlaceHolder{
        ImageView vThumb;
        TextView cId;
        TextView cName;
        TextView cDesc;
        TextView sId;
        RatingBar bRating;
        ToggleButton fTB;
    }
}
