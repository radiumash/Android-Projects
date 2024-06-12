package com.EEEITSolutions.elearning.adapter;

public class VideoDetails {
    private String mChapterID;
    private String mChapterName;
    private String mTopicID;
    private String mTopicName;
    private String mVideoFile;
    private String mVideoThumb;
    private String mVideoID;
    private String mVideoName;
    private String mRating;

    public VideoDetails (String chapterid, String chaptername, String topicid, String topicname, String videofile, String videothumb, String videoid, String videoname, String rating) {
        this.mChapterID = chapterid;
        this.mChapterName = chaptername;
        this.mTopicID = topicid;
        this.mTopicName = topicname;
        this.mVideoFile = videofile;
        this.mVideoThumb = videothumb;
        this.mVideoID = videoid;
        this.mVideoName = videoname;
        this.mRating = rating;
    }
    public String getChapterID(){
        return mChapterID;
    }
    public String getChapterName(){
        return mChapterName;
    }
    public String getTopicID(){
        return mTopicID;
    }
    public String getTopicName(){return mTopicName;}
    public String getVideoFile(){
        return mVideoFile;
    }
    public String getVideoThumb(){
        return mVideoThumb;
    }
    public String getVideoID(){
        return mVideoID;
    }
    public String getVideoName(){
        return mVideoName;
    }
    public String getRating(){
        return mRating;
    }
}
