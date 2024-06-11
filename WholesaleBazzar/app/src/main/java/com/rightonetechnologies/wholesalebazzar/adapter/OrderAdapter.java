package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.activity.OrderDetailsActivity;
import com.rightonetechnologies.wholesalebazzar.activity.OrdersActivity;

import java.util.ArrayList;

public class OrderAdapter extends BaseAdapter {
    public Context context;
    public ArrayList<OrderDetails> OrderArrayList;

    public OrderAdapter(Context context, ArrayList<OrderDetails> providerArrayList) {
        super();
        this.context = context;
        this.OrderArrayList = providerArrayList;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return OrderArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return OrderArrayList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_order, parent, false);
            holder = new PlaceHolder();
            holder.cOID = convertView.findViewById(R.id.tvOrderID);
            holder.cONo = convertView.findViewById(R.id.tvOrderNo);
            holder.cODate = convertView.findViewById(R.id.tvOrderDate);
            holder.cOTotal = convertView.findViewById(R.id.tvOrderTotal);
            holder.cOStatus = convertView.findViewById(R.id.tvOrderStatus);
            holder.cOPaymode = convertView.findViewById(R.id.tvOrderPaymode);

            convertView.setTag(holder);
        }
        else
        {
            holder = (OrderAdapter.PlaceHolder) convertView.getTag();
        }

        holder.cOID.setText(OrderArrayList.get(position).getOrderID());
        holder.cONo.setText(OrderArrayList.get(position).getOrderNo());
        holder.cODate.setText(OrderArrayList.get(position).getOrderDate());
        holder.cOTotal.setText("₹ " + OrderArrayList.get(position).getOrderAmount());
        holder.cOStatus.setText(OrderArrayList.get(position).getOrderStatus());
        holder.cOPaymode.setText(OrderArrayList.get(position).getPaymode());
        holder.cONo.setTag(position);
        holder.cONo.setOnClickListener(PopupListener);

        return convertView;
    }

    View.OnClickListener PopupListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Integer viewPosition = Integer.parseInt(view.getTag().toString());
            String order_id = OrderArrayList.get(viewPosition).getOrderID();
            String order_no = OrderArrayList.get(viewPosition).getOrderNo();
            String order_date = OrderArrayList.get(viewPosition).getOrderDate();

            Intent intent = new Intent(context, OrderDetailsActivity.class);
            intent.putExtra("ID", order_id);
            intent.putExtra("NAME", order_no + " [" + order_date + "]");
            view.getContext().startActivity(intent);
        }
    };

    private static class PlaceHolder{
        TextView cOID;
        TextView cONo;
        TextView cODate;
        TextView cOTotal;
        TextView cOStatus;
        TextView cOPaymode;
    }
}
