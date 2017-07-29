package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.util.Date;

/**
 * POJO for event data.
 */

public class Event {

    private int id;
    private String name;
    private String owner;
    private Date startDt;
    private Date endDt;
    private boolean active;
    private String teamDomain;
    private int activityMinutes;
    private int breakMinutes;

    /**
     * Constructor for a newly created event.
     * @param name Event name
     * @param owner Event owner
     * @param startDt Creation date
     * @param active Create as active
     * @param domain Team domain name
     */
    public Event(String name, String owner, Date startDt, boolean active, int activityMinutes, int breakMinutes, String domain) {
        id = -1;
        this.name = name;
        this.owner = owner;
        this.startDt = startDt;
        this.endDt = null;
        this.active = active;
        this.teamDomain = domain;
        this.activityMinutes = activityMinutes;
        this.breakMinutes = breakMinutes;
    }

    public Event(Cursor row) {
        try {
            id = row.getInt(PomodoroEventContract.Event.ID_INDEX);
            name = row.getString(PomodoroEventContract.Event.NAME_INDEX);
            owner = row.getString(PomodoroEventContract.Event.OWNER_INDEX);
            String startDtText = row.getString(PomodoroEventContract.Event.START_DT_INDEX);
            startDt = DataUtil.dateFromDb(startDtText);
            String endDtText = row.getString(PomodoroEventContract.Event.END_DT_INDEX);
            endDt = DataUtil.dateFromDb(endDtText);
            active = (row.getInt(PomodoroEventContract.Event.ACTIVE_INDEX) != 0);
            teamDomain = row.getString(PomodoroEventContract.Event.TEAM_DOMAINN_INDEX);
            activityMinutes = row.getInt(PomodoroEventContract.Event.EVENT_TIMER_MINUTES_INDEX);
            breakMinutes = row.getInt(PomodoroEventContract.Event.EVENT_BREAK_MINUTES_INDEX);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getStartDt() {
        return startDt;
    }

    public void setStartDt(Date startDt) {
        this.startDt = startDt;
    }

    public Date getEndDt() {
        return endDt;
    }

    public void setEndDt(Date endDt) {
        this.endDt = endDt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    public void setTeamDomain(String teamDomain) {
        this.teamDomain = teamDomain;
    }

    public int getActivityMinutes() {
        return activityMinutes;
    }

    public void setActivityMinutes(int activityMinutes) {
        this.activityMinutes = activityMinutes;
    }

    public int getBreakMinutes() {
        return breakMinutes;
    }

    public void setBreakMinutes(int breakMinutes) {
        this.breakMinutes = breakMinutes;
    }

    public ContentValues asContent() {
        ContentValues values = new ContentValues();
        //values.put(PomodoroEventContract.Event.ID_COL, id);
        values.put(PomodoroEventContract.Event.EVENT_NAME_COL, name);
        values.put(PomodoroEventContract.Event.ACTIVE_COL, active ? 1 : 0);
        values.put(PomodoroEventContract.Event.OWNER_COL, owner);
        values.put(PomodoroEventContract.Event.TEAM_DOMAIN_COL, teamDomain);
        values.put(PomodoroEventContract.Event.START_DT_COL, DataUtil.dbFromDate(startDt));
        values.put(PomodoroEventContract.Event.END_DT_COL, DataUtil.dbFromDate(endDt));
        values.put(PomodoroEventContract.Event.EVENT_TIMER_MINUTES_COL, activityMinutes);
        values.put(PomodoroEventContract.Event.EVENT_BREAK_MINUTES_COL, breakMinutes);
        return values;
    }
}
