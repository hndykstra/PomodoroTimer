package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.util.Date;

/**
 * Pojo for event membership records.
 */

public class EventMember {
    private int id;
    private int eventId;
    private String memberUid;
    private Date joinDt;

    public EventMember(Cursor row) {
        try {
            id = row.getInt(PomodoroEventContract.EventMember.ID_INDEX);
            eventId = row.getInt(PomodoroEventContract.EventMember.EVENT_ID_INDEX);
            memberUid = row.getString(PomodoroEventContract.EventMember.MEMBER_INDEX);
            String joinDtText = row.getString(PomodoroEventContract.EventMember.JOIN_DT_INDEX);
            joinDt = DataUtil.dateFromDb(joinDtText);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
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

    public ContentValues asContent() {
        ContentValues values = new ContentValues();
        //values.put(PomodoroEventContract.EventMember.ID_COL, id);
        values.put(PomodoroEventContract.EventMember.EVENT_FK_COL, eventId);
        values.put(PomodoroEventContract.EventMember.MEMBER_UID_COL, memberUid);
        values.put(PomodoroEventContract.EventMember.JOIN_DT_COL, DataUtil.dbFromDate(joinDt));
        return values;
    }
}
