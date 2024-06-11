package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.common.AddToCart;
import com.rightonetechnologies.wholesalebazzar.common.PicassoCircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartAdapter extends BaseAdapter implements AddToCart.OnQuantityChangeListener{
    public Context context;
    public ArrayList<CartDetails> CartArrayList;
    private PlaceHolder holder;

    public CartAdapter(Context context, ArrayList<CartDetails> providerArrayList) {
        super();
        this.context = context;
        this.CartArrayList = providerArrayList;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return CartArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return CartArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(R.layout.row_cart, parent, false);
        holder = new PlaceHolder();
        holder.vThumb = convertView.findViewById(R.id.productImage);
        holder.mId = convertView.findViewById(R.id.sqlID);
        holder.pId = convertView.findViewById(R.id.productID);
        holder.dId = convertView.findViewById(R.id.dealID);
        holder.cName = convertView.findViewById(R.id.companyName);
        holder.pName = convertView.findViewById(R.id.productName);
        holder.uType = convertView.findViewById(R.id.uName);
        holder.pSp = convertView.findViewById(R.id.spProduct);
        holder.pMrp = convertView.findViewById(R.id.mrpProduct);
        holder.pQty = convertView.findViewById(R.id.qtyProduct);
        holder.pTotal = convertView.findViewById(R.id.productTotal);
        holder.rCart = convertView.findViewById(R.id.removeCart);

        holder.pId.setText(CartArrayList.get(position).getProductID());
        holder.mId.setText(CartArrayList.get(position).getSqlID());
        holder.dId.setText(CartArrayList.get(position).getDealID());
        holder.cName.setText(CartArrayList.get(position).getCompanyName());
        holder.pName.setText(CartArrayList.get(position).getProductName());
        holder.uType.setText(CartArrayList.get(position).getUnit());
        holder.uType.setTag(CartArrayList.get(position).getUnitID());
        holder.pMrp.setText(CartArrayList.get(position).getProductMRP());
        holder.pMrp.setPaintFlags(holder.pMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.pMrp.setTag(CartArrayList.get(position).getProductMRP());
        holder.pSp.setText(CartArrayList.get(position).getProductSP());
        holder.pQty.setOnQuantityChangeListener(this);
        holder.pQty.setPid(CartArrayList.get(position).getProductID());
        holder.pQty.setPackID(CartArrayList.get(position).getPackageID());
        holder.pQty.setUid(holder.uType.getTag().toString());
        holder.pQty.setDid(CartArrayList.get(position).getDealID());
        holder.pQty.setMRP(holder.pMrp.getTag().toString());
        holder.pQty.setSP(CartArrayList.get(position).getProductSP());
        holder.pQty.setTax(CartArrayList.get(position).getTax());
        holder.pQty.setCompany(CartArrayList.get(position).getCompanyName());
        holder.pQty.setUnit(CartArrayList.get(position).getUnit());
        holder.pQty.setImage(CartArrayList.get(position).getProductImage());
        holder.pQty.setName(CartArrayList.get(position).getProductName());
        holder.pQty.showQty(Integer.parseInt(CartArrayList.get(position).getQty()));

        Float total = Integer.parseInt(CartArrayList.get(position).getQty()) * Float.parseFloat(CartArrayList.get(position).getProductSP());
        holder.pTotal.setText(context.getString(R.string.rupee) + " " + total.toString());

        holder.pQty.setSqlID(CartArrayList.get(position).getSqlID());
        holder.rCart.setTag(position);
        holder.rCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.pQty.removeCart(holder.mId.getText().toString());
                Integer index = (Integer) view.getTag();
                CartArrayList.remove(index.intValue());
                notifyDataSetChanged();
            }
        });
        String url = context.getString(R.string.image_url) + '/' + CartArrayList.get(position).getProductImage();
        Picasso.with(context)
                .load(url)
                .transform(new PicassoCircleTransformation())
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.vThumb);
        return convertView;
    }

    @Override
    public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {
        Integer position = Integer.parseInt(holder.rCart.getTag().toString());
        Float total = newQuantity * Float.parseFloat(CartArrayList.get(position).getProductSP());
        holder.pTotal.setText(context.getString(R.string.rupee) + " " + total.toString());
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
        TextView pSp;
        TextView pMrp;
        AddToCart pQty;
        TextView dId;
        TextView pId;
        TextView mId;
        TextView pTotal;
        Button rCart;
    }
}
