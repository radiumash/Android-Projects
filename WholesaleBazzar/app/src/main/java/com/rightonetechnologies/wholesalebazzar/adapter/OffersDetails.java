package com.rightonetechnologies.wholesalebazzar.adapter;

public class OffersDetails {
    private String mID;
    private String mCompanyName;
    private String mProductID;
    private String mProductName;
    private String mUnitName;
    private String mQty;
    private String mProductMRP;
    private String mProductSP;
    private String mTax;
    private String mProductImage;

    public OffersDetails(String id, String company, String pid, String name, String uname, String qty, String mrp, String sp, String tax, String image_url){
        this.mID = id;
        this.mCompanyName = company;
        this.mProductID = pid;
        this.mProductName = name;
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
    public String getUnitName(){
        return this.mUnitName;
    }
    public String getQty(){
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
