package com.rightone.ashish.rightexecutive;

public class AccountDetail {
    public String mBusinessName;
    public String mServiceName;
    public String mLocCityState;
    public String mSubscription;
    public String mIsPremium;
    public String mProviderId;
    public String mBusinessPic;
    public String mAmount;


    public AccountDetail (String businessName, String serviceName, String locCityState, String subscription, String is_premium, String providerId, String businessPic, String amount){
        this.mBusinessName = businessName;
        this.mServiceName = serviceName;
        this.mLocCityState = locCityState;
        this.mSubscription = subscription;
        this.mIsPremium = is_premium;
        this.mProviderId = providerId;
        this.mBusinessPic = businessPic;
        this.mAmount = amount;
    }
}
