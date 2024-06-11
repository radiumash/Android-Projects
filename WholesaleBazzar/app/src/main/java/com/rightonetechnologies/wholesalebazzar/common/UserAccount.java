package com.rightonetechnologies.wholesalebazzar.common;

import java.io.Serializable;

public class UserAccount implements Serializable {

    private String userID;
    private String userName;

    private boolean active;

    public UserAccount(String id, String name)  {
        this.userID= id;
        this.userName = name;
        this.active= true;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String id) {
        this.userID = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return this.userName +" ("+ this.userID+")";
    }

}
