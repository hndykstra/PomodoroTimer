package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Domain object for a defined team domain.
 */
public class Team {
    private transient String domainName;
    private Date createdDt;
    private String ownerUid;
    private boolean active;
    private Map<String,Date> members;

    /**
     * Default constructor for dynamic creation
     */
    public Team() {
    }

    public Team(String name, String owner) {
        this.domainName = name;
        this.ownerUid = owner;
        this.active = true;
        this.createdDt = new Date();
    }

    public String getDomainName() {
        return domainName;
    }

    public Date getCreatedDt() {
        return createdDt;
    }

    public void setCreatedDt(Date createdDt) {
        this.createdDt = createdDt;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, Date> getMembers() {
        return members;
    }
}
