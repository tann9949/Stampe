package com.example.stampe;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chompakorn on 21/11/2560.
 */

public class User {

    private String username;
    private int stamp;
    private int benefit;

    public User(String id, int stamp, int benefit) {
        this.username = id; this.stamp = stamp; this.benefit=benefit;
    }

    public User() {

    }

    public String getid() {
        return username;
    }

    public int getStamp() {
        return stamp;
    }

    public int getBenefit() {
        return benefit;
    }

}
