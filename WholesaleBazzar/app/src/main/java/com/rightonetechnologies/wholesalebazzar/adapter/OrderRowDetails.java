package com.rightonetechnologies.wholesalebazzar.adapter;

public class OrderRowDetails {
    private String mProductName;
    private String mPrice;
    private String mQty;
    private String mSubTotal;
    private String mTax;
    private String mOrderTotal;
    private String mUrl;

    public OrderRowDetails(String name, String price, String qty, String sub_total, String tax, String net_total, String url){
        this.mProductName = name;
        this.mPrice = price;
        this.mQty = qty;
        this.mSubTotal = sub_total;
        this.mTax = tax;
        this.mOrderTotal = net_total;
        this.mUrl = url;
    }
    public String getProductName(){
        return this.mProductName;
    }
    public String getPrice(){
        return this.mPrice;
    }
    public String getQty(){
        return this.mQty;
    }
    public String getSubTotal(){
        return this.mSubTotal;
    }
    public String getTax(){
        return this.mTax;
    }
    public String getOrderTotal(){
        return this.mOrderTotal;
    }
    public String getUrl(){
        return this.mUrl;
    }
}
