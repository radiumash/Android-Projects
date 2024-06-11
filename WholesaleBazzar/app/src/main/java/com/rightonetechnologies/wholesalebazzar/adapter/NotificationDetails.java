package com.rightonetechnologies.wholesalebazzar.adapter;

public class NotificationDetails {
    private String mNid;
    private String mNTitle;
    private String mNCreated;
    private String mNType;
    private String mNUserName;
    private String mNMessage;
    private String mNSender;
    private String mNUrl;

    public NotificationDetails(String id, String title, String created, String type, String user_name, String message, String sender, String url){
        this.mNid = id;
        this.mNTitle = title;
        this.mNCreated = created;
        this.mNType = type;
        this.mNUserName = user_name;
        this.mNMessage = message;
        this.mNSender = sender;
        this.mNUrl = url;
    }
    public String getNotificationID(){
        return this.mNid;
    }
    public String getNotificationTitle(){
        return this.mNTitle;
    }
    public String getNotificationCreateDate(){
        return this.mNCreated;
    }
    public String getNotificationType(){
        return this.mNType;
    }
    public String getNotificationUserName(){
        return this.mNUserName;
    }
    public String getNotificationMessage(){
        return this.mNMessage;
    }
    public String getNotificationSender(){
        return this.mNSender;
    }
    public String getNotificationUrl(){
        return this.mNUrl;
    }
}
