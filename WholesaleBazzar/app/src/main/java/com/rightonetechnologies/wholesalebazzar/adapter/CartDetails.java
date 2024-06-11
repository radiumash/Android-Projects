package com.rightonetechnologies.wholesalebazzar.adapter;

public class CartDetails {
    private String mSqlID;
    private String mDealID;
    private String mCompanyName;
    private String mProductID;
    private String mPackageID;
    private String mProductName;
    private String mUnit;
    private String mUnitID;
    private String mQty;
    private String mProductMRP;
    private String mProductSP;
    private String mTax;
    private String mProductImage;

    public CartDetails(String sqlid, String dealid, String company, String pid, String packid, String name, String unit_id, String unit, String mrp, String sp, String tax, String qty, String image_url){
        this.mSqlID = sqlid;
        this.mDealID = dealid;
        this.mCompanyName = company;
        this.mProductID = pid;
        this.mPackageID = packid;
        this.mProductName = name;
        this.mUnit = unit;
        this.mUnitID = unit_id;
        this.mProductMRP = mrp;
        this.mProductSP = sp;
        this.mTax = tax;
        this.mQty = qty;
        this.mProductImage = image_url;
    }
    public String getSqlID(){
        return this.mSqlID;
    }
    public String getDealID(){
        return this.mDealID;
    }
    public String getCompanyName(){
        return this.mCompanyName;
    }
    public String getProductID(){
        return this.mProductID;
    }
    public String getPackageID(){ return this.mPackageID; }
    public String getProductName(){
        return this.mProductName;
    }
    public String getProductUnit(){
        return this.mUnit;
    }
    public String getProductQty(){
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

    public void setUnitID(String unit){
        this.mUnitID = unit;
    }

    public String getUnitID(){
        return this.mUnitID;
    }

    public void setUnit(String unit){
        this.mUnit = unit;
    }

    public String getUnit(){
        return this.mUnit;
    }
    public void setQty(String qty){
        this.mQty = qty;
    }

    public String getQty(){
        return this.mQty;
    }
}
