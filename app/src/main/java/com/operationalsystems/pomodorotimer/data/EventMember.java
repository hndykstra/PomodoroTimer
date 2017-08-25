package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.util.Date;

/**
 * Pojo for event membership records.
 */

public class EventMember {
    private transient String key;
    private String memberUid;
    private Date joinDt;

    public EventMember(String uid, Date joinDt) {
        this.memberUid = uid;
        this.joinDt = joinDt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(String memberUid) {
        this.memberUid = memberUid;
    }

    public Date getJoinDt() {
        return joinDt;
    }

    public void setJoinDt(Date joinDt) {
        this.joinDt = joinDt;
    }
}
