package com.EEEITSolutions.elearning.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.activity.DoubtActivity;
import com.EEEITSolutions.elearning.activity.DoubtSendActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DoubtAdapter extends BaseAdapter implements Filterable {
    public Context context;
    public ArrayList<DoubtDetails> DoubtArrayList;
    public ArrayList<DoubtDetails> orig;
    private static final int ACC_REQUEST = 1;

    public DoubtAdapter(Context context, ArrayList<DoubtDetails> providerArrayList) {
        super();
        this.context = context;
        this.DoubtArrayList = providerArrayList;
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<DoubtDetails> results = new ArrayList<>();
                if (orig == null)
                    orig = DoubtArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final DoubtDetails g : orig) {
                            if (g.getName().toLowerCase()
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
                DoubtArrayList = (ArrayList<DoubtDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return DoubtArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return DoubtArrayList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_doubt, parent, false);
            holder = new PlaceHolder();

            holder.vThumb = convertView.findViewById(R.id.imageThumb);
            holder.cName = convertView.findViewById(R.id.tvName);
            holder.cId = convertView.findViewById(R.id.tvID);
            holder.cType = convertView.findViewById(R.id.tvType);
            holder.cS = convertView.findViewById(R.id.tvS);
            holder.cC = convertView.findViewById(R.id.tvC);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }
        holder.vThumb.setOnClickListener(PopupListener);
        holder.cId.setText(DoubtArrayList.get(position).getID());
        holder.cName.setText(DoubtArrayList.get(position).getName());
        holder.cType.setText(DoubtArrayList.get(position).getType());
        holder.cS.setText(DoubtArrayList.get(position).getSubject());
        holder.cC.setText(DoubtArrayList.get(position).getChapter());
        holder.cName.setOnClickListener(PopupListener);

        if(DoubtArrayList.get(position).getType().equals("CHAPTER")) {
            String url = context.getString(R.string.image_url) + '/' + DoubtArrayList.get(position).getUrl();
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.thumb_placeholder) // optional
                    .into(holder.vThumb);
        }else if(DoubtArrayList.get(position).getType().equals("TOPIC")){
            Picasso.with(context)
                    .load(context.getString(R.string.image_url) + context.getString(R.string.chapter_path))
                    .placeholder(R.drawable.thumb_placeholder) // optional
                    .into(holder.vThumb);
        }else if(DoubtArrayList.get(position).getType().equals("DOUBT")){
            Picasso.with(context)
                    .load(context.getString(R.string.image_url) + context.getString(R.string.activity1_path))
                    .placeholder(R.drawable.thumb_placeholder) // optional
                    .into(holder.vThumb);
        }
        Integer rowPosition = position;
        holder.vThumb.setTag(rowPosition);
        holder.cName.setTag(rowPosition);
        return convertView;
    }

    View.OnClickListener PopupListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Integer viewPosition = (Integer) view.getTag();
            String pid = DoubtArrayList.get(viewPosition).getID();
            String pType = DoubtArrayList.get(viewPosition).getType();
            Intent intent;
            if(pType.equals("DOUBT")){
                intent = new Intent(context, DoubtSendActivity.class);
            }else{
                intent = new Intent(context, DoubtActivity.class);
            }
            intent.putExtra("ID",pid);
            intent.putExtra("SUBJECTID",DoubtArrayList.get(viewPosition).getSubject());
            intent.putExtra("CHAPTERID",DoubtArrayList.get(viewPosition).getChapter());
            intent.putExtra("TYPE",pType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
        }
    };

    private static class PlaceHolder{
        ImageView vThumb;
        TextView cName;
        TextView cId;
        TextView cType;
        TextView cS, cC;
    }
}
