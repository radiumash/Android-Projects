package com.rightonetechnologies.wholesalebazzar.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;

import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rightonetechnologies.wholesalebazzar.R;
import com.rightonetechnologies.wholesalebazzar.activity.CartActivity;
import com.rightonetechnologies.wholesalebazzar.activity.CategoryActivity;
import com.rightonetechnologies.wholesalebazzar.activity.MainActivity;
import com.rightonetechnologies.wholesalebazzar.activity.SearchProductActivity;

public class AddToCart extends LinearLayout implements View.OnClickListener {
    private int quantity;
    private Context mContext;
    private String pid, uid, did, sqlID, packID;
    String mrp, sp, tax;
    String image, unit, company, name;
    private int maxQuantity = Integer.MAX_VALUE, minQuantity = Integer.MAX_VALUE;
    private Button mButtonPlus, mButtonMinus, mButtonAdd;
    private TextView mQty;
    private LayoutInflater layoutInflater;
    private LinearLayout mLinearLayout;
    private MySQLiteHelper db;
    private ProgressBar mProgress;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically);
        void onLimitReached();
    }

    private OnQuantityChangeListener onQuantityChangeListener;
    private OnClickListener mTextViewClickListener;

    public AddToCart(Context context) {
        super(context);
        mContext = context;
        init(null, 0);
        sqlID = "0";
    }

    public AddToCart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0);
        sqlID = "0";
    }

    public AddToCart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs, defStyle);
        sqlID = "0";
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.QuantityView, defStyle, 0);
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.add_to_cart, null);
        db = new MySQLiteHelper(getContext());
        mButtonPlus = view.findViewById(R.id.mQtyPlus);
        mButtonMinus = view.findViewById(R.id.mQtyMinus);
        mButtonAdd = view.findViewById(R.id.mBtnAdd);
        mQty = view.findViewById(R.id.mQty);
        mProgress = view.findViewById(R.id.mProgress);
        mLinearLayout = view.findViewById(R.id.mLayoutMain);

        quantity = a.getInt(R.styleable.QuantityView_qv_quantity, 0);
        maxQuantity = a.getInt(R.styleable.QuantityView_qv_maxQuantity, Integer.MAX_VALUE);
        minQuantity = a.getInt(R.styleable.QuantityView_qv_minQuantity, 0);
        setQuantity(quantity);

        addView(view);

        mButtonAdd.setOnClickListener(this);
        mButtonPlus.setOnClickListener(this);
        mButtonMinus.setOnClickListener(this);
    }

    public void setQuantityClickListener(OnClickListener ocl) {
        mTextViewClickListener = ocl;
    }

    @Override
    public void onClick(View v) {
        showProgress();
        if (v == mButtonPlus) {
            addQty();
        } else if (v == mButtonMinus) {
            subtractQty();
        } else if (v == mButtonAdd) {
            if(chkCart()) {
                hideAddToCartButton();
            }
        }
    }

    private void showProgress(){
        showWorkingDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                removeWorkingDialog();
            }
        }, 1000);
    }

    private void showWorkingDialog() {
        mProgress.setVisibility(View.VISIBLE);
    }

    private void removeWorkingDialog() {
        mProgress.setVisibility(View.GONE);
    }

    private void showAddToCartButton(){
        mLinearLayout.setVisibility(View.GONE);
        mButtonAdd.setVisibility(View.VISIBLE);
    }

    private void hideAddToCartButton(){
        addQty();
        mLinearLayout.setVisibility(View.VISIBLE);
        mButtonAdd.setVisibility(View.GONE);
    }

    private void subtractQty(){
        if (quantity - 1 < minQuantity) {
            if (onQuantityChangeListener != null) onQuantityChangeListener.onLimitReached();
        } else {
            int oldQty = quantity;
            quantity -= 1;
            mQty.setText(String.valueOf(quantity));
            if (onQuantityChangeListener != null)
                onQuantityChangeListener.onQuantityChanged(oldQty, quantity, false);
            updateCart();
        }
        if(quantity == minQuantity) {
            showAddToCartButton();
        }
    }

    public void reset(){
        quantity = 0;
        setSqlID("0");
        mQty.setText(String.valueOf(quantity));
        mLinearLayout.setVisibility(View.GONE);
        mButtonAdd.setVisibility(View.VISIBLE);
    }
    public void showQty(Integer qty){
        quantity = qty;
        if(qty > 0) {
            mQty.setText(String.valueOf(quantity));
            mLinearLayout.setVisibility(View.VISIBLE);
            mButtonAdd.setVisibility(View.GONE);
        }
    }
    public void hideQty(Integer qty){
        quantity = qty;
        if(qty == 0) {
            mQty.setText(String.valueOf(quantity));
            mLinearLayout.setVisibility(View.GONE);
            mButtonAdd.setVisibility(View.VISIBLE);
        }
    }
    private void addQty(){
        if (quantity + 1 > maxQuantity) {
            if (onQuantityChangeListener != null) onQuantityChangeListener.onLimitReached();
        } else {
            int oldQty = quantity;
            quantity += 1;
            mQty.setText(String.valueOf(quantity));
            if (onQuantityChangeListener != null)
                onQuantityChangeListener.onQuantityChanged(oldQty, quantity, false);
            updateCart();
        }
    }
    public void hideKeyboard(View focus) {
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (focus != null) {
            inputManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public OnQuantityChangeListener getOnQuantityChangeListener() {
        return onQuantityChangeListener;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener onQuantityChangeListener) {
        this.onQuantityChangeListener = onQuantityChangeListener;
    }

    public int getQuantity() {
        return quantity;
    }

    private Boolean chkCart(){
        String pUnit = getUid();
        if(pUnit.equals("0")){
            Toast.makeText(getContext(), getContext().getString(R.string.err_unit), Toast.LENGTH_LONG).show();
            return false;
        }else{
            //updateCart();
            return true;
        }
    }

    public void removeCart(String sql_id){
        if(!sql_id.equals("")){
            db.removeCart(sql_id);
        }
    }

    private void updateCart(){
        String sid = getSqlID();
        String pid = getPid();
        String did = getDid();
        String pQty = String.valueOf(getQuantity());
        String pUnit = getUid();
        String pMrp = getMRP();
        String pSp = getSP();
        String company = getCompany();
        String unit = getUnit();
        String image = getImage();
        String name = getName();
        String tax = getTax();
        String packid = getPackID();
        int id = db.updateProduct(sid,did,name,pid,packid,company,pUnit,unit,pQty,pMrp,pSp,tax,image);
        setSqlID(String.valueOf(id));
        if(mContext.getClass().getSimpleName().equals("MainActivity")) {
            ((MainActivity) mContext).invalidateOptionsMenu();
        }else if(mContext.getClass().getSimpleName().equals("CategoryActivity")) {
            ((CategoryActivity) mContext).invalidateOptionsMenu();
        }else if(mContext.getClass().getSimpleName().equals("CartActivity")) {
            ((CartActivity) mContext).updateTotal();
        }else if(mContext.getClass().getSimpleName().equals("SearchProductActivity")) {
            ((SearchProductActivity) mContext).invalidateOptionsMenu();
        }
    }

    public void setQuantity(int newQuantity) {
        boolean limitReached = false;

        if (newQuantity > maxQuantity) {
            newQuantity = maxQuantity;
            limitReached = true;
        }
        if (newQuantity < minQuantity) {
            newQuantity = minQuantity;
            limitReached = true;
        }
        if (!limitReached) {
//            if (onQuantityChangeListener != null) {
//                onQuantityChangeListener.onQuantityChanged(quantity, newQuantity, true);
//            }
            this.quantity = newQuantity;
            mQty.setText(String.valueOf(this.quantity));
        } else {
            if (onQuantityChangeListener != null) onQuantityChangeListener.onLimitReached();
        }
    }

    private int pxFromDp(final float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void setPid(String pid){
        this.pid = pid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getPid(){
        return this.pid;
    }

    public String getUid(){
        return this.uid;
    }

    public void setDid(String did){
        this.did = did;
    }

    public void setMRP(String mrp){
        this.mrp = mrp;
    }

    public String getDid(){
        return this.did;
    }

    public String getMRP(){
        return this.mrp;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image){
        this.image = image;
    }

    public String getUnit(){
        return this.unit;
    }

    public void setUnit(String unit){
        this.unit = unit;
    }

    public String getCompany(){
        return this.company;
    }

    public void setCompany(String company){
        this.company = company;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getSqlID(){
        return this.sqlID;
    }

    public void setSqlID(String sqlid){
        this.sqlID = sqlid;
    }

    public String getPackID(){
        return this.packID;
    }

    public void setPackID(String packid){
        this.packID = packid;
    }

    public String getTax(){
        return this.tax;
    }

    public void setTax(String tax){
        this.tax = tax;
    }

    public String getSP(){
        return this.sp;
    }

    public void setSP(String sp){
        this.sp = sp;
    }

    public static boolean isValidNumber(String string) {
        try {
            return Integer.parseInt(string) <= Integer.MAX_VALUE;
        } catch (Exception e) {
            return false;
        }
    }
}
