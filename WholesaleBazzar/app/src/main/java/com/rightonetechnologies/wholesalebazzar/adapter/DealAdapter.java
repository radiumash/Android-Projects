package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.AddToCart;
import com.rightonetechnologies.wholesalebazzar.common.PicassoCircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class DealAdapter extends BaseAdapter implements AddToCart.OnQuantityChangeListener {
    public Context context;
    public ArrayList<DealDetails> DealArrayList;

    public DealAdapter(Context context, ArrayList<DealDetails> providerArrayList) {
        super();
        this.context = context;
        this.DealArrayList = providerArrayList;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return DealArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return DealArrayList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_deal, parent, false);
            holder = new PlaceHolder();

            holder.vThumb = convertView.findViewById(R.id.productImage);
            holder.pId = convertView.findViewById(R.id.productID);
            holder.dId = convertView.findViewById(R.id.dealID);
            holder.cName = convertView.findViewById(R.id.companyName);
            holder.pName = convertView.findViewById(R.id.productName);
            holder.uType = convertView.findViewById(R.id.uName);
            holder.pMrp = convertView.findViewById(R.id.mrpProduct);
            holder.pQty = convertView.findViewById(R.id.qtyProduct);
            convertView.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder) convertView.getTag();
        }

        holder.pId.setText(DealArrayList.get(position).getProductID().toString());
        holder.dId.setText(DealArrayList.get(position).getDealID().toString());
        holder.cName.setText(DealArrayList.get(position).getCompanyName());
        holder.pName.setText(DealArrayList.get(position).getDealName());
        holder.uType.setText("Select");
        holder.uType.setTag("0");
        holder.uType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select Package");
                builder.setAdapter(DealArrayList.get(position).getProductUnit(), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        holder.uType.setText(DealArrayList.get(position).getProductUnit().getItem(which));
                        holder.uType.setTag(which);
                        holder.pQty.reset();
                        holder.pQty.setUid(String.valueOf(which));
                        HashMap<Integer, String> unitMap = DealArrayList.get(position).getUnitMap();
                        HashMap<Integer, String> spMap = DealArrayList.get(position).getSPMap();
                        HashMap<Integer, String> mrpMap = DealArrayList.get(position).getMRPMap();
                        try {
                            holder.pQty.setPackID(unitMap.get(which));
                            holder.pQty.setSP(spMap.get(which));
                            holder.pQty.setMRP(mrpMap.get(which));
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                        holder.pQty.setUnit(DealArrayList.get(position).getProductUnit().getItem(which));
                    }
                });
                builder.show();
            }
        });

        holder.pMrp.setText(context.getString(R.string.mrp) + " " + DealArrayList.get(position).getProductMRP().toString());
        holder.pMrp.setTag(DealArrayList.get(position).getProductMRP());
        holder.pQty.setSP(DealArrayList.get(position).getProductSP());
        holder.pQty.setOnQuantityChangeListener(this);
        holder.pQty.setPid(DealArrayList.get(position).getProductID());
        holder.pQty.setUid(holder.uType.getTag().toString());
        holder.pQty.setDid(DealArrayList.get(position).getDealID());
        holder.pQty.setMRP(holder.pMrp.getTag().toString());
        holder.pQty.setTax(DealArrayList.get(position).getTax());
        holder.pQty.setCompany(DealArrayList.get(position).getCompanyName());
        holder.pQty.setImage(DealArrayList.get(position).getProductImage());
        holder.pQty.setName(DealArrayList.get(position).getProductName());

        String url = context.getString(R.string.image_url) + '/' + DealArrayList.get(position).getProductImage();
        Picasso.with(context)
                .load(url)
                .transform(new PicassoCircleTransformation())
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.vThumb);
        return convertView;
    }

    @Override
    public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {

    }

    @Override
    public void onLimitReached() {
        Log.d(getClass().getSimpleName(), "Limit reached");
    }

    private static class PlaceHolder{
        ImageView vThumb;
        TextView cName;
        TextView pName;
        TextView uType;
        TextView pMrp;
        AddToCart pQty;
        TextView dId;
        TextView pId;
    }
}
