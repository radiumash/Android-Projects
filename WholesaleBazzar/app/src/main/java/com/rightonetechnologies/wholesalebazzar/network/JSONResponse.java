package com.rightonetechnologies.wholesalebazzar.network;

import com.google.gson.annotations.SerializedName;

public class JSONResponse {
    @SerializedName("success")
    private Boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("results")
    private String results;


    public void JSONResponse(Boolean success, String message, String results) {
        this.success = success;
        this.message = message;
        this.results = results;
    }

    public Boolean getSuccess() {
        return this.success;
    }


    public String getMessage() {
        return this.message;
    }

    public String getResults() {
        return this.results;
    }

}
