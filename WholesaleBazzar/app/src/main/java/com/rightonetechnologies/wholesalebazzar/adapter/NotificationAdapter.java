package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rightonetechnologies.wholesalebazzar.R;
import java.util.ArrayList;

public class NotificationAdapter extends BaseAdapter {
    public Context context;
    public ArrayList<NotificationDetails> NotificationArrayList;

    public NotificationAdapter(Context context, ArrayList<NotificationDetails> providerArrayList) {
        super();
        this.context = context;
        this.NotificationArrayList = providerArrayList;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return NotificationArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return NotificationArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PlaceHolder holder;
        if(convertView==null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);
            holder = new PlaceHolder();
            holder.cNID = convertView.findViewById(R.id.tvNotificationID);
            holder.cNTitle = convertView.findViewById(R.id.tvNotificationTitle);
            holder.cNDate = convertView.findViewById(R.id.tvNotificationDate);
            holder.cNMessage = convertView.findViewById(R.id.tvNotificationMessage);
            holder.cNSender = convertView.findViewById(R.id.tvNotificationSendBy);
            holder.cNType = convertView.findViewById(R.id.tvNotificationType);

            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }

        holder.cNID.setText(NotificationArrayList.get(position).getNotificationID());
        holder.cNTitle.setText(NotificationArrayList.get(position).getNotificationTitle());
        holder.cNDate.setText(NotificationArrayList.get(position).getNotificationCreateDate());
        holder.cNMessage.setText(NotificationArrayList.get(position).getNotificationMessage());
        holder.cNSender.setText(NotificationArrayList.get(position).getNotificationSender());
        holder.cNType.setText(NotificationArrayList.get(position).getNotificationType());
        return convertView;
    }

    private static class PlaceHolder{
        TextView cNID;
        TextView cNTitle;
        TextView cNDate;
        TextView cNMessage;
        TextView cNSender;
        TextView cNType;
        ImageView COImg;
    }
}
