package com.EEEITSolutions.elearning.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.EEEITSolutions.elearning.R;
import com.EEEITSolutions.elearning.adapter.Activity;
import com.EEEITSolutions.elearning.adapter.ActivityDetails;
import com.EEEITSolutions.elearning.common.SessionManager;
import com.EEEITSolutions.elearning.network.ConnectivityReceiver;
import com.EEEITSolutions.elearning.network.NetworkADO;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityActivity extends AppCompatActivity {

    private String questions[];
    private String choices[][];
    private String correctAnswers[];

    private String activity_id, activity_name;
    SessionManager session;
    ActivityDetails activityDetails = null;
    private ArrayList<Activity> activityArrayList;
    GetActivityDetails getP;
    private Activity activity;
    TextView mQuestionTitle, mQuestion, mQuestionNo, mTotalQuestions;
    RadioGroup mRadioGroup;
    RadioButton mOption1, mOption2, mOption3, mOption4;
    Button mSubmit;
    ProgressBar mProgressBar;
    Integer currentQuestion = 1;
    Integer questionIndex = 0;
    Integer rightQuestion = 0;
    Integer wrongQuestion = 0;
    Integer totalQuestions = 0;
    Integer totalMarks = 0;
    Integer marksObtained = 0;
    Integer markOfOneQuestion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity);

        ConnectivityReceiver cr = new ConnectivityReceiver(this);
        if(!cr.isConnected()){
            Toast.makeText(getApplicationContext(),getString(R.string.check_internet),Toast.LENGTH_LONG).show();
            return;
        }
        session = new SessionManager(getApplicationContext());
        if(!session.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        Bundle extra = getIntent().getExtras();
        try {
            activity_id = extra.getString("ID");
            activity_name = extra.getString("NAME");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if(!activity_name.isEmpty()){
            this.setTitle(activity_name + ": " + getString(R.string.activity));
        }
        mProgressBar = findViewById(R.id.progressBar);
        mQuestionTitle = findViewById(R.id.questionTitle);
        mQuestion = findViewById(R.id.question);
        mQuestionNo = findViewById(R.id.questionNo);
        mTotalQuestions = findViewById(R.id.totalQuestion);

        mRadioGroup = findViewById(R.id.answers);
        mRadioGroup.clearCheck();

        mOption1 = findViewById(R.id.option1);
        mOption2 = findViewById(R.id.option2);
        mOption3 = findViewById(R.id.option3);
        mOption4 = findViewById(R.id.option4);

        mSubmit = findViewById(R.id.btnDisplay);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mOption1.isChecked() && !mOption2.isChecked() && !mOption3.isChecked() && !mOption4.isChecked()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_option), Toast.LENGTH_LONG).show();
                }else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    if (currentQuestion != totalQuestions) {
                        checkQuesiton();
                        moveToNextQuestion();
                    } else {
                        checkQuesiton();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityActivity.this);
                        builder.setMessage(getString(R.string.youG) + " " + marksObtained + " " + getString(R.string.outof) + " " + totalMarks + " " + getString(R.string.inThisActivity) + "\n" +
                                getString(R.string.questionAttemped) + " " + totalQuestions + "\n" +
                                getString(R.string.rightQuestions) + " " + rightQuestion + "\n" +
                                getString(R.string.wrongQuestions) + " " + wrongQuestion)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(i);
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        getP = new GetActivityDetails(activity_id);
        getP.execute((Void) null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkQuesiton(){
        Integer ans = 0;
        if (!mOption1.isChecked() && !mOption2.isChecked() && !mOption3.isChecked() && !mOption4.isChecked()) {
            Toast.makeText(getApplicationContext(),getString(R.string.error_option),Toast.LENGTH_LONG).show();
        } else if (mOption1.isChecked()) {
            ans = Integer.parseInt(mOption1.getTag().toString());
        } else if (mOption2.isChecked()) {
            ans = Integer.parseInt(mOption2.getTag().toString());
        } else if (mOption3.isChecked()) {
            ans = Integer.parseInt(mOption3.getTag().toString());
        } else if (mOption4.isChecked()) {
            ans = Integer.parseInt(mOption4.getTag().toString());
        }
        if (ans == Integer.parseInt(activity.getAnswer())) {
            marksObtained += markOfOneQuestion;
            rightQuestion += 1;
        }else{
            wrongQuestion += 1;
        }
    }

    private void moveToNextQuestion(){
        currentQuestion+=1;
        questionIndex+=1;
        mRadioGroup.clearCheck();
        activity = activityArrayList.get(questionIndex);
        String qT = getString(R.string.question) + currentQuestion.toString();
        mQuestionTitle.setText(qT);
        mQuestion.setText(activity.getQuestion());
        mOption1.setText(activity.getOption1());
        mOption2.setText(activity.getOption2());
        mOption3.setText(activity.getOption3());
        mOption4.setText(activity.getOption4());
        mTotalQuestions.setText(activityDetails.getTotalQuestions());
        mQuestionNo.setText(currentQuestion.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * Represents an asynchronous Resend OTP task
     */
    public class GetActivityDetails extends AsyncTask<Void, Void, Boolean> {

        private static final int ACC_REQUEST = 1;

        private final String mActivityID;

        private String errorMessage = null;

        GetActivityDetails(String chapter_id) {
            mActivityID = chapter_id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String url="";
            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("key", getString(R.string.api_key));
                url = getString(R.string.server_url) + "activitylist";
                postDataParams.put("activityid", mActivityID);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, url);

                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");
                    errorMessage = rootJSON.getString("message");

                    if (success.equals(true)) {
                        JSONObject accounts = rootJSON.getJSONObject("results").getJSONObject("activity");
                        String activityID = accounts.getString("activityid");
                        String activityName = accounts.getString("activityname");
                        String activityType = accounts.getString("activitytype");
                        String totalMarks = accounts.getString("totalmarks");
                        String totalQuestions = accounts.getString("totalquestions");
                        String eachMarks = accounts.getString("eachmarks");
                        String timeRequired = accounts.getString("timerequired");
                        String videoRating = "5";

                        activityDetails = new ActivityDetails("","","","",activityID,activityName,activityType,totalMarks,totalQuestions,eachMarks,timeRequired,videoRating);

                        JSONArray questions = rootJSON.getJSONObject("results").getJSONArray("questionanswerlist");
                        activityArrayList = new ArrayList<>();
                        for (int i = 0; i < questions.length(); i++) {
                            String questionID = questions.getJSONObject(i).getString("questionanswerid");
                            String questionTitle = questions.getJSONObject(i).getString("question");
                            String questionType = questions.getJSONObject(i).getString("questiontype");
                            String option1 = questions.getJSONObject(i).getString("option1");
                            String option2 = questions.getJSONObject(i).getString("option2");
                            String option3 = questions.getJSONObject(i).getString("option3");
                            String option4 = questions.getJSONObject(i).getString("option4");
                            String answer = questions.getJSONObject(i).getString("answer");
                            activityArrayList.add(new Activity(questionID,questionTitle,questionType,option1,option2,option3,option4,answer));
                        }

                        return true;
                    }else{
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            getP = null;
            if (success) {
                totalMarks = Integer.parseInt(activityDetails.getTotalMarks());
                markOfOneQuestion = Integer.parseInt(activityDetails.getEachMarks());
                activity = activityArrayList.get(questionIndex);
                String qT = getString(R.string.question) + currentQuestion.toString();
                mQuestionTitle.setText(qT);
                mQuestion.setText(activity.getQuestion());
                mOption1.setText(activity.getOption1());
                mOption2.setText(activity.getOption2());
                mOption3.setText(activity.getOption3());
                mOption4.setText(activity.getOption4());
                totalQuestions = Integer.parseInt(activityDetails.getTotalQuestions());
                mTotalQuestions.setText(activityDetails.getTotalQuestions());
                mQuestionNo.setText(currentQuestion.toString());
            } else {
                if (errorMessage != "") {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            getP = null;
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
