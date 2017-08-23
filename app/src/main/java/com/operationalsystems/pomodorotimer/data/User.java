package com.operationalsystems.pomodorotimer.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Store information about users for display in teams and event members.
 */

public class User {
    private String uid;
    private String displayName;

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
