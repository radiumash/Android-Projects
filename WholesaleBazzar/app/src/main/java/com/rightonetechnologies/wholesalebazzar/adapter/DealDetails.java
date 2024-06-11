package com.rightonetechnologies.wholesalebazzar.adapter;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rightonetechnologies.wholesalebazzar.common.QuantityView;

import java.util.HashMap;

public class DealDetails {
    private String mDealID;
    private String mCompanyName;
    private String mProductID;
    private String mDealName;
    private String mProductName;
    private ArrayAdapter<String> mProductUnit;
    private String mUnitID;
    private HashMap<Integer, String> mUnitMap;
    private HashMap<Integer, String> mMRPMap;
    private HashMap<Integer, String> mSPMap;
    private String mQty;
    private String mProductMRP;
    private String mProductSP;
    private String mTax;
    private String mProductImage;

    public DealDetails(String dealid, String dname, String company, String pid, String name, ArrayAdapter<String> unit, HashMap<Integer, String> unitMap, String mrp, String sp, String tax, String image_url, HashMap<Integer,String> spMap, HashMap<Integer,String> mrpMap){
        this.mDealID = dealid;
        this.mCompanyName = company;
        this.mProductID = pid;
        this.mDealName = dname;
        this.mProductName = name;
        this.mProductUnit = unit;
        this.mUnitMap = unitMap;
        this.mTax = tax;
        this.mProductMRP = mrp;
        this.mProductSP = sp;
        this.mProductImage = image_url;
        this.mSPMap = spMap;
        this.mMRPMap = mrpMap;
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
    public String getProductName(){
        return this.mProductName;
    }
    public String getDealName(){
        return this.mDealName;
    }
    public ArrayAdapter<String> getProductUnit(){
        return this.mProductUnit;
    }
    public HashMap<Integer, String> getUnitMap() {
        return this.mUnitMap;
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
    public HashMap<Integer, String> getMRPMap() {
        return this.mMRPMap;
    }
    public HashMap<Integer, String> getSPMap() {
        return this.mSPMap;
    }
    public void setUnit(String unit){
        this.mUnitID = unit;
    }

    public String getUnit(){
        return this.mUnitID;
    }

    public void setQty(String qty){
        this.mQty = qty;
    }

    public String getQty(){
        return this.mQty;
    }
}
