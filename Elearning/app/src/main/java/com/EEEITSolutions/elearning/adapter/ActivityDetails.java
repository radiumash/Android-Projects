package com.EEEITSolutions.elearning.adapter;

public class ActivityDetails {
    private String mActivityID;
    private String mActivityName;
    private String mActivityType;
    private String mTotalMarks;
    private String mTotalQuestions;
    private String mEachMarks;
    private String mTimeRequired;
    private String mChapterID;
    private String mChapterName;
    private String mTopicID;
    private String mTopicName;
    private String mRating;

    public ActivityDetails (String chapterid, String chaptername, String topicid, String topicname, String activityid,
                            String activityname, String activitytype, String totalmarks, String totalquestions, String eachmarks, String timerequired, String rating) {
        this.mChapterID = chapterid;
        this.mChapterName = chaptername;
        this.mTopicID = topicid;
        this.mTopicName = topicname;
        this.mActivityID = activityid;
        this.mActivityName = activityname;
        this.mActivityType = activitytype;
        this.mTotalMarks = totalmarks;
        this.mTotalQuestions = totalquestions;
        this.mEachMarks = eachmarks;
        this.mTimeRequired = timerequired;
        this.mRating = rating;
    }

    public String getChapterID(){
        return mChapterID;
    }
    public String getChapterName(){
        return mChapterName;
    }
    public String getActivityID(){
        return mActivityID;
    }
    public String getActivityName(){
        return mActivityName;
    }
    public String getTopicID(){
        return mTopicID;
    }
    public String getTopicName(){
        return mTopicName;
    }

    public String getActivityType(){
        return mActivityType;
    }
    public String getTotalMarks(){
        return mTotalMarks;
    }
    public String getTotalQuestions(){
        return mTotalQuestions;
    }
    public String getEachMarks(){
        return mEachMarks;
    }
    public String getTimeRequired(){
        return mTimeRequired;
    }
    
    public String getRating(){
        return mRating;
    }
}
