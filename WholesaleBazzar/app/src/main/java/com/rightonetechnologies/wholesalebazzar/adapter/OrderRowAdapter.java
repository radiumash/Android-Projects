package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rightonetechnologies.wholesalebazzar.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrderRowAdapter extends BaseAdapter {
    public Context context;
    public ArrayList<OrderRowDetails> OrderRowArrayList;

    public OrderRowAdapter(Context context, ArrayList<OrderRowDetails> providerArrayList) {
        super();
        this.context = context;
        this.OrderRowArrayList = providerArrayList;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return OrderRowArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return OrderRowArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PlaceHolder holder;
        if(convertView==null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_order_detail, parent, false);
            holder = new PlaceHolder();

            holder.vThumb = convertView.findViewById(R.id.productImage);
            holder.cName = convertView.findViewById(R.id.tvName);
            holder.cPrice = convertView.findViewById(R.id.tvPrice);
            holder.cQty = convertView.findViewById(R.id.tvQty);
            holder.cSubTotal = convertView.findViewById(R.id.tvSubTotal);
            holder.cTax = convertView.findViewById(R.id.tvTax);
            holder.cNetTotal = convertView.findViewById(R.id.tvNetTotal);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }

        holder.cName.setText(OrderRowArrayList.get(position).getProductName().toString());
        holder.cPrice.setText("₹ " + OrderRowArrayList.get(position).getPrice());
        holder.cQty.setText(OrderRowArrayList.get(position).getQty());
        holder.cSubTotal.setText("₹ " + OrderRowArrayList.get(position).getSubTotal());
        holder.cTax.setText("Tax: ₹ " + OrderRowArrayList.get(position).getTax());
        holder.cNetTotal.setText("Net: ₹ " + OrderRowArrayList.get(position).getOrderTotal());
        String url = context.getString(R.string.image_url) + '/' + OrderRowArrayList.get(position).getUrl();
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.vThumb);
        return convertView;
    }

    private static class PlaceHolder{
        ImageView vThumb;
        TextView cName;
        TextView cPrice;
        TextView cQty;
        TextView cSubTotal;
        TextView cTax;
        TextView cNetTotal;
    }
}
