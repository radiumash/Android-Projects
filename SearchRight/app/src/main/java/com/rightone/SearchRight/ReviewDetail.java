package com.rightone.SearchRight;

public class ReviewDetail {
    public Character mRChar;
    public String mRid;
    public String mName;
    public String mRTitle;
    public String mReview;
    public String mRating;
    public String mProviderId;


    public ReviewDetail (String id, Character rChar, String name, String title, String review, String rating, String providerId){
        this.mRChar = rChar;
        this.mRid = id;
        this.mName = name;
        this.mRTitle = title;
        this.mReview = review;
        this.mRating = rating;
        this.mProviderId = providerId;
    }
}
