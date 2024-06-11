package com.rightone.ashish.rightexecutive;

public class Account {
    public String mMonthName;
    public String mYear;
    public int mTotalForm;
    public String mNameOfImage;
    public String mCreateDate;

    public Account (String nameOfMonth, int totalForm, String dateImage, String createDate, String year){
        this.mMonthName = nameOfMonth;
        this.mYear = year;
        this.mTotalForm = totalForm;
        this.mNameOfImage = dateImage;
        this.mCreateDate = createDate;
    }

    public String getCreateDate(){return mCreateDate;}
}
