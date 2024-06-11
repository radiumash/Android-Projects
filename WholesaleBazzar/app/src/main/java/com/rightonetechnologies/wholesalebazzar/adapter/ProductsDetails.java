package com.rightonetechnologies.wholesalebazzar.adapter;

import com.rightonetechnologies.wholesalebazzar.common.AddToCart;

public class ProductsDetails {
    private String mID;
    private String mCompanyName;
    private String mProductID;
    private String mProductName;
    private String mUnitID;
    private String mUnitName;
    private String mQty;
    private String mProductMRP;
    private String mProductSP;
    private String mTax;
    private String mProductImage;

    public ProductsDetails(String id, String company, String pid, String name, String uid, String uname, String qty, String mrp, String sp, String tax, String image_url){
        this.mID = id;
        this.mCompanyName = company;
        this.mProductID = pid;
        this.mProductName = name;
        this.mUnitID = uid;
        this.mUnitName = uname;
        this.mQty = qty;
        this.mProductMRP = mrp;
        this.mProductSP = sp;
        this.mTax = tax;
        this.mProductImage = image_url;
    }

    public String getID(){
        return this.mID;
    }
    public String getCompanyName(){
        return this.mCompanyName;
    }
    public String getProductID(){
        return this.mProductID;
    }
    public String getProductName(){
        return this.mProductName;
    }
    public String getUnitID(){
        return this.mUnitID;
    }
    public String getUnitName(){
        return this.mUnitName;
    }
    public String getPackQty(){
        return this.mQty;
    }
    public String getProductMRP(){
        return this.mProductMRP;
    }
    public String getProductSP(){
        return this.mProductSP;
    }
    public String getTax(){
        return this.mTax;
    }
    public String getProductImage(){
        return this.mProductImage;
    }
}
