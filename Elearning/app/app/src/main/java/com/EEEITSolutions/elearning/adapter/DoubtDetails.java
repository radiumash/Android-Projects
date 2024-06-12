package com.EEEITSolutions.elearning.adapter;

public class DoubtDetails {

    private String mID;
    private String mSubjectID;
    private String mChapterID;
    private String mName;
    private String mUrl;
    private String mType;

    public DoubtDetails(String id, String name, String url, String type, String subject, String chapter){
        this.mID = id;
        this.mName = name;
        this.mUrl = url;
        this.mType = type;
        this.mSubjectID = subject;
        this.mChapterID = chapter;
    }

    public String getID(){
        return mID;
    }
    public String getName(){
        return mName;
    }
    public String getUrl(){
        return mUrl;
    }
    public String getType(){
        return mType;
    }
    public String getSubject(){ return mSubjectID;}
    public String getChapter() {return mChapterID;}
}
