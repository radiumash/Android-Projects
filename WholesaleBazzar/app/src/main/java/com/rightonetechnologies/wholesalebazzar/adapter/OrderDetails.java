package com.rightonetechnologies.wholesalebazzar.adapter;

public class OrderDetails {
    private String mOrderID;
    private String mOrderNo;
    private String mOrderDate;
    private String mNetAmount;
    private String mOrderStatus;
    private String mPaymode;

    public OrderDetails(String order_id, String order_number, String order_date, String order_amount, String order_status, String paymode){
        this.mOrderID = order_id;
        this.mOrderNo = order_number;
        this.mOrderDate = order_date;
        this.mNetAmount = order_amount;
        this.mOrderStatus = order_status;
        this.mPaymode = paymode;
    }
    public String getOrderID(){
        return this.mOrderID;
    }
    public String getOrderNo(){
        return this.mOrderNo;
    }
    public String getOrderDate(){
        return this.mOrderDate;
    }
    public String getOrderAmount(){
        return this.mNetAmount;
    }
    public String getOrderStatus(){
        return this.mOrderStatus;
    }
    public String getPaymode(){
        return this.mPaymode;
    }
}
