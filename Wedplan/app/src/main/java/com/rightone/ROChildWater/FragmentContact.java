package com.rightone.ROChildWater;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class FragmentContact extends Fragment {
    String pid = null;
    AutoCompleteTextView mName,mEmail,mMobile,mMessage;
    String tvName,tvEmail,tvMobile,tvMessage,android_id;

    Button mSend;
    ProgressBar mDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_contact, container, false);
        pid = getArguments().getString("ID");

        mName = fragmentView.findViewById(R.id.name);
        mEmail = fragmentView.findViewById(R.id.email);
        mMobile = fragmentView.findViewById(R.id.mobile);
        mMessage = fragmentView.findViewById(R.id.message);

        mSend = fragmentView.findViewById(R.id.btnSend);
        mDialog = fragmentView.findViewById(R.id.progress_loader);

        android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setVisibility(View.VISIBLE);
                if (isOnline(getActivity())){
                    tvName = mName.getText().toString();
                    tvEmail = mEmail.getText().toString();
                    tvMobile = mMobile.getText().toString();
                    tvMessage = mMessage.getText().toString();

                    if (tvName.trim().length() == 0)
                        Toast.makeText(getActivity(), getString(R.string.check_name), Toast.LENGTH_SHORT).show();

                    else if (tvEmail.trim().length() == 0)
                        Toast.makeText(getActivity(), getString(R.string.check_email), Toast.LENGTH_SHORT).show();

                    else if (tvMobile.trim().length() == 0)
                        Toast.makeText(getActivity(), getString(R.string.check_mobile), Toast.LENGTH_SHORT).show();

                    else if (tvMessage.trim().length() == 0)
                        Toast.makeText(getActivity(), getString(R.string.check_message), Toast.LENGTH_SHORT).show();

                    else if(!isOnline(getActivity()))
                        Toast.makeText(getActivity(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();

                    else
                        new SaveContact().execute(tvName, tvEmail, tvMobile, tvMessage, android_id, pid);

                }else{
                    mDialog.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });


        return fragmentView;
    }

    private boolean isOnline(Context mContext)
    {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }


    /**
     * Represents an asynchronous Resend OTP task
     */
    public class SaveContact extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            NetworkADO networkADO;
            String jsonResponse;
            String mName = params[0];
            String mEmail = params[1];
            String mMobile = params[2];
            String mMessage = params[3];
            String mDeviceId = params[4];
            String mPid = params[5];

            try {
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("task", "user");
                postDataParams.put("action", "save_contact");
                postDataParams.put("key", getString(R.string.api_key));
                postDataParams.put("name", mName);
                postDataParams.put("email", mEmail);
                postDataParams.put("mobile", mMobile);
                postDataParams.put("message", mMessage);
                postDataParams.put("device_id", mDeviceId);
                postDataParams.put("provider_id", mPid);

                networkADO = new NetworkADO();
                jsonResponse = networkADO.getHTTPResponse(postDataParams, getString(R.string.server_url));
                try {
                    JSONObject rootJSON = new JSONObject(jsonResponse);
                    Boolean success = rootJSON.getBoolean("success");

                    if (success.equals(true)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }  catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mDialog.setVisibility(View.INVISIBLE);
            if(result){
                mName.setText("");
                mEmail.setText("");
                mMobile.setText("");
                mMessage.setText("");
                Toast.makeText(getActivity(), getString(R.string.message_saved), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), getString(R.string.message_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
