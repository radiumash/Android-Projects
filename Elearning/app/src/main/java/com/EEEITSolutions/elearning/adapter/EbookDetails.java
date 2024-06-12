package com.EEEITSolutions.elearning.adapter;

public class EbookDetails {
    private String mEbookID;
    private String mEbookName;
    private String mEbookDesc;
    private String mEbookFile;
    private String mChapterID;
    private String mChapterName;
    private String mTopicID;
    private String mTopicName;
    private String mRating;

    public EbookDetails (String chapterid, String chaptername, String topicid, String topicname, String ebookname, String ebookdesc, String ebookfile, String ebookid, String rating) {
        this.mChapterID = chapterid;
        this.mChapterName = chaptername;
        this.mTopicID = topicid;
        this.mTopicName = topicname;
        this.mEbookID = ebookid;
        this.mEbookName = ebookname;
        this.mEbookDesc = ebookdesc;
        this.mEbookFile = ebookfile;
        this.mRating = rating;
    }

    public String getChapterID(){
        return mChapterID;
    }
    public String getChapterName(){
        return mChapterName;
    }
    public String getEbookName(){
        return mEbookName;
    }
    public String getTopicID(){
        return mTopicID;
    }
    public String getTopicName(){
        return mTopicName;
    }
    public String getEbookDesc(){
        return mEbookDesc;
    }
    public String getEbookFile(){
        return mEbookFile;
    }
    public String getmEbookID(){
        return mEbookID;
    }
    public String getRating(){
        return mRating;
    }
}
