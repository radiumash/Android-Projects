package com.rightonetechnologies.wholesalebazzar.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.activity.MainActivity;
import com.rightonetechnologies.wholesalebazzar.activity.ProductDetailActivity;
import com.rightonetechnologies.wholesalebazzar.common.AddToCart;
import com.rightonetechnologies.wholesalebazzar.common.MySQLiteHelper;
import com.rightonetechnologies.wholesalebazzar.common.PicassoCircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductsAdapter extends BaseAdapter implements AddToCart.OnQuantityChangeListener{
    public Context context;
    public ArrayList<ProductsDetails> TPArrayList;
    private MySQLiteHelper db;
    PlaceHolder holder;
    private static final String TAG = "Pos :";


    public ProductsAdapter(Context context, ArrayList<ProductsDetails> providerArrayList) {
        super();
        this.context = context;
        this.TPArrayList = providerArrayList;
        db = new MySQLiteHelper(context);
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return TPArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return TPArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.row_product, parent, false);
        holder = new PlaceHolder();
        holder.vThumb = convertView.findViewById(R.id.productImage);
        holder.pId = convertView.findViewById(R.id.productID);
        holder.Id = convertView.findViewById(R.id.rowID);
        holder.cName = convertView.findViewById(R.id.companyName);
        holder.pName = convertView.findViewById(R.id.productName);
        holder.uName = convertView.findViewById(R.id.unitName);
        holder.pMrp = convertView.findViewById(R.id.mrpProduct);
        holder.pSP = convertView.findViewById(R.id.spProduct);
        holder.pQty = convertView.findViewById(R.id.qtyProduct);
        holder.packQty = convertView.findViewById(R.id.qtyPack);

        holder.Id.setText(TPArrayList.get(position).getID());
        holder.pId.setText(TPArrayList.get(position).getProductID());
        holder.cName.setText(TPArrayList.get(position).getCompanyName());
        holder.pName.setText(TPArrayList.get(position).getProductName());
        holder.uName.setText(TPArrayList.get(position).getUnitName());
        holder.pMrp.setText(TPArrayList.get(position).getProductMRP());
        holder.pSP.setText(TPArrayList.get(position).getProductSP());
        holder.packQty.setText(TPArrayList.get(position).getPackQty());
        holder.pMrp.setPaintFlags(holder.pMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        String url = context.getString(R.string.image_url) + '/' + TPArrayList.get(position).getProductImage();

        Picasso.with(context)
                .load(url)
                .transform(new PicassoCircleTransformation())
                .placeholder(R.drawable.thumb_placeholder) // optional
                .into(holder.vThumb);
        holder.pName.setTag(position);
        holder.pName.setOnClickListener(PopupListener);
        holder.vThumb.setTag(position);
        holder.vThumb.setOnClickListener(PopupListener);
        Log.i(TAG, "Position - " + position + " - " + TPArrayList.get(position).getID());
        holder.pQty.setTag(position);
        holder.pQty.setOnQuantityChangeListener(this);
        holder.pQty.hideQty(0);
        holder.pQty.setPid(TPArrayList.get(position).getProductID());
        holder.pQty.setPackID(TPArrayList.get(position).getID());
        holder.pQty.setUid(TPArrayList.get(position).getUnitID());
        holder.pQty.setDid("0");
        holder.pQty.showQty(db.getQtyByPackID(TPArrayList.get(position).getID()));
        holder.pQty.setMRP(TPArrayList.get(position).getProductMRP());
        holder.pQty.setSP(TPArrayList.get(position).getProductSP());
        holder.pQty.setTax(TPArrayList.get(position).getTax());
        holder.pQty.setCompany(TPArrayList.get(position).getCompanyName());
        holder.pQty.setUnit(TPArrayList.get(position).getPackQty() + " " + TPArrayList.get(position).getUnitName());
        holder.pQty.setImage(TPArrayList.get(position).getProductImage());
        holder.pQty.setName(TPArrayList.get(position).getProductName());

        return convertView;
    }

    View.OnClickListener PopupListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Integer viewPosition = Integer.parseInt(view.getTag().toString());
            String pid = TPArrayList.get(viewPosition).getID();
            String pName = TPArrayList.get(viewPosition).getProductName();

            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("ID", pid);
            intent.putExtra("NAME",pName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
        }
    };

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
        TextView uName;
        TextView pMrp;
        TextView pSP;
        TextView packQty;
        AddToCart pQty;
        TextView Id;
        TextView pId;
    }
}
