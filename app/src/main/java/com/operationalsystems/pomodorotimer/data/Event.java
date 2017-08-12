package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.text.Collator;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * POJO for event data.
 */

public class Event {

    private transient String key;
    private String name;
    private String owner;
    private transient Date startDt;
    private String startTime;
    private transient Date endDt;
    private String endTime;
    private boolean active;
    private String teamDomain;
    private int activityMinutes;
    private int breakMinutes;
    private TreeMap<String,Pomodoro> pomodoros = new TreeMap<>(Collator.getInstance(Locale.US));
    private Map<String,String> members = new HashMap<>();

    /**
     * Empty constructor for dynamic creation.
     */
    public Event() {
        // set to invalid values
        activityMinutes = -1;
        breakMinutes = -1;
    }

    /**
     * Constructor for a newly created event.
     * @param name Event name
     * @param owner Event owner
     * @param startDt Creation date
     * @param active Create as active
     * @param domain Team domain name
     */
    public Event(String name, String owner, Date startDt, boolean active, int activityMinutes, int breakMinutes, String domain) {
        this.key = null;
        this.name = name;
        this.owner = owner;
        setStartDt(startDt);
        setEndDt(endDt);
        this.active = active;
        this.teamDomain = domain;
        this.activityMinutes = activityMinutes;
        this.breakMinutes = breakMinutes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        synchPomodoros();
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

    @Exclude
    public Date getStartDt() {
        if (startDt == null && startTime != null && !startTime.isEmpty()) {
            try {
                startDt = DataUtil.dateFromDb(startTime);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid date/time " + startTime, e);
            }
        }
        return startDt;
    }

    public void setStartDt(Date startDt) {
        this.startDt = startDt;
        if (startDt == null) {
            this.startTime = null;
        } else {
            this.startTime = DataUtil.dbFromDate(startDt);
        }
    }

    @Exclude
    public Date getEndDt() {
        if (endDt == null && endTime != null && !endTime.isEmpty()) {
            try {
                endDt = DataUtil.dateFromDb(endTime);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid date/time " + endTime, e);
            }
        }
        return endDt;
    }

    public void setEndDt(Date endDt) {
        this.endDt = endDt;
        if (endDt == null) {
            this.endTime = null;
        } else {
            this.endTime = DataUtil.dbFromDate(endDt);
        }
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void addPomodoro(Pomodoro p) {
        this.pomodoros.put(p.getKey(), p);
        p.setEventKey(this.getKey());
    }

    public SortedMap<String, Pomodoro> getPomodoros() {
        return this.pomodoros;
    }

    public void setPomodoros(Map<String, Pomodoro> map) {
        this.pomodoros.clear();
        this.pomodoros.putAll(map);
    }

    public void clearPomodoros() {
        this.pomodoros.clear();
    }

    @Exclude
    public Pomodoro getCurrentPomodoro() {
        if (this.pomodoros.isEmpty()) {
            return null;
        }
        return this.pomodoros.lastEntry().getValue();
    }

    public void synchPomodoros() {
        for (Map.Entry<String, Pomodoro> entry : pomodoros.entrySet()) {
            Pomodoro p = entry.getValue();
            p.setKey(entry.getKey());
            p.setEventKey(this.getKey());
        }
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public void setMembers(Map<String,String> members) {
        this.members = members;
    }

    public void addMember(final String user, final Date asOf) {
        if (members.get(user) == null) {
            members.put(user, DataUtil.dbFromDate(asOf));
        }
    }

    public void removeMember(final String user) {
        members.remove(user);
    }
}
