package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.util.Date;

/**
 * Domain object for team membership records.
 */

public class TeamMember {
    public enum Role {
        None, Owner, Admin, Member, Applied, Disabled
    }

    private String memberUid;
    private Role role;
    private Date addedOn;
    private String addedBy;

    /**
     * No args constructure for deserialization.
     */
    public TeamMember() {
    }

    public TeamMember(String member, Role role, String addedBy) {
        this.memberUid = member;
        this.role = role;
        this.addedBy = addedBy;
        this.addedOn = new Date();
    }

    @Exclude
    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(String memberUid) {
        this.memberUid = memberUid;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Exclude
    public Date getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Date addedOn) {
        this.addedOn = addedOn;
    }

    public String getAddedOnTime() {
        return DataUtil.dbFromDate(addedOn);
    }

    public void setAddedOnTime(String time) {
        try {
            this.addedOn = DataUtil.dateFromDb(time);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid addedOn", e);
        }
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
}
