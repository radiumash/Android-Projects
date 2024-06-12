package com.EEEITSolutions.elearning.adapter;

public class ChapterDetails {
    public String mChapterID;
    public String mSubjectID;
    public String mChapterName;
    public String mChapterDesc;
    public String mSubjectName;
    public String mRating;

    public ChapterDetails (String chapterid, String subjectid, String chanptername, String chapterdesc, String subjectname, String rating) {
        this.mChapterID = chapterid;
        this.mSubjectID = subjectid;
        this.mChapterName = chanptername;
        this.mChapterDesc = chapterdesc;
        this.mSubjectName = subjectname;
        this.mRating = rating;
    }

    public String getChapterID(){
        return mChapterID;
    }
    public String getSubjectID(){
        return mSubjectID;
    }
    public String getChapterName(){
        return mChapterName;
    }
    public String getChapterDesc(){
        return mChapterDesc;
    }
    public String getSubjectName(){
        return mSubjectName;
    }
    public String getRating(){
        return mRating;
    }
}
