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
    private String email;
    private String recentEvent;
    private String recentTeam;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRecentEvent() {
        return recentEvent;
    }

    public void setRecentEvent(String eventKey) {
        recentEvent = eventKey;
    }

    public String getRecentTeam() {
        return recentTeam;
    }

    public void setRecentTeam(String teamDomain) {
        recentTeam = teamDomain;
    }
}
