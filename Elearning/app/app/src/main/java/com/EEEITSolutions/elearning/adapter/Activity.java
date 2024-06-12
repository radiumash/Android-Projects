package com.EEEITSolutions.elearning.adapter;

public class Activity {
    private String mQuestionID;
    private String mQuestionTitle;
    private String mQuestionType;
    private String mOption1;
    private String mOption2;
    private String mOption3;
    private String mOption4;
    private String mAnswer;

    public Activity (String questionanswerid, String question, String questiontype, String option1, String option2,
                            String option3, String option4, String answer) {
        this.mQuestionID = questionanswerid;
        this.mQuestionTitle = question;
        this.mQuestionType = questiontype;
        this.mOption1 = option1;
        this.mOption2 = option2;
        this.mOption3 = option3;
        this.mOption4 = option4;
        this.mAnswer = answer;
    }

    public String getQuestionID(){
        return mQuestionID;
    }
    public String getQuestion(){
        return mQuestionTitle;
    }
    public String getQuestionType(){
        return mQuestionType;
    }
    public String getOption1(){
        return mOption1;
    }
    public String getOption2(){
        return mOption2;
    }
    public String getOption3(){
        return mOption3;
    }

    public String getOption4(){
        return mOption4;
    }
    public String getAnswer(){
        return mAnswer;
    }
}
