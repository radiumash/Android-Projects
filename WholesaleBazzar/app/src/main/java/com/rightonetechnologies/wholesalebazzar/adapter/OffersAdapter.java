package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.activity.ProductDetailActivity;
import com.rightonetechnologies.wholesalebazzar.common.PicassoCircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OffersAdapter extends BaseAdapter {
    //Context
    private Context context;
    private ArrayList<OffersDetails> offersDetailsArrayList;

    public OffersAdapter (Context context, ArrayList<OffersDetails> oAL){
        //Getting all the values
        super();
        this.context = context;
        this.offersDetailsArrayList = oAL;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return offersDetailsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return offersDetailsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt(offersDetailsArrayList.get(position).getProductID());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PlaceHolder holder;
        GridView grid = (GridView)parent;
        int side = grid.getMeasuredWidth() / 3;
        if(convertView==null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_offer, parent, false);
            convertView.setLayoutParams(new FrameLayout.LayoutParams(side,side));
            holder = new PlaceHolder();

            holder.vThumb = convertView.findViewById(R.id.productImage);
            holder.pId = convertView.findViewById(R.id.productID);
            holder.Id = convertView.findViewById(R.id.rowID);
            holder.pName = convertView.findViewById(R.id.productName);
            holder.uName = convertView.findViewById(R.id.unitName);
            holder.pMrp = convertView.findViewById(R.id.mrpProduct);
            holder.pSP = convertView.findViewById(R.id.spProduct);
            holder.pQty = convertView.findViewById(R.id.qtyProduct);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }

        holder.Id.setText(offersDetailsArrayList.get(position).getID());
        holder.pId.setText(offersDetailsArrayList.get(position).getProductID());
        holder.pName.setText(offersDetailsArrayList.get(position).getProductName());
        holder.uName.setText(offersDetailsArrayList.get(position).getUnitName());
        holder.pMrp.setText(offersDetailsArrayList.get(position).getProductMRP());
        holder.pSP.setText(offersDetailsArrayList.get(position).getProductSP());
        holder.pQty.setText(offersDetailsArrayList.get(position).getQty());
        holder.pMrp.setPaintFlags(holder.pMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.vThumb.setTag(position);

        String url = context.getString(R.string.image_url) + '/' + offersDetailsArrayList.get(position).getProductImage();
        Picasso.with(context)
                .load(url)
                .transform(new PicassoCircleTransformation())
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.vThumb);
        holder.pName.setTag(position);
        holder.pName.setOnClickListener(PopupListener);
        holder.vThumb.setOnClickListener(PopupListener);


        return convertView;
    }

    View.OnClickListener PopupListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Integer viewPosition = Integer.parseInt(view.getTag().toString());
            String pid = offersDetailsArrayList.get(viewPosition).getID();
            String pName = offersDetailsArrayList.get(viewPosition).getProductName();

            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("ID", pid);
            intent.putExtra("NAME",pName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
        }
    };

    private static class PlaceHolder{
        ImageView vThumb;
        TextView pName;
        TextView uName;
        TextView pMrp;
        TextView pSP;
        TextView pQty;
        TextView Id;
        TextView pId;
    }
}
