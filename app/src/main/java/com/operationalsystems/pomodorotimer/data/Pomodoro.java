package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Data object for a pomodoro
 */

public class Pomodoro {
    private transient String key;
    private transient String eventKey;
    private String name;
    private int sequence;
    private int timerMinutes;
    private int breakMinutes;
    private transient Date startDt;
    private String startTime;
    private transient Date breakDt;
    private String breakTime;
    private transient Date endDt;
    private String endTime;
    private boolean active;

    public Pomodoro() {
    }

    public Pomodoro(String eventKey, String name, int sequence, int timerMinutes, int breakMinutes, Date startDt) {
        this.key = String.format(Locale.US, "%03d", sequence);
        this.eventKey = eventKey;
        this.name = name;
        this.sequence = sequence;
        this.timerMinutes = timerMinutes;
        this.breakMinutes = breakMinutes;
        this.active = true;
        setStartDt(startDt);
        setEndDt(endDt);
        setBreakDt(breakDt);
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getTimerMinutes() {
        return timerMinutes;
    }

    public void setTimerMinutes(int timerMinutes) {
        this.timerMinutes = timerMinutes;
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

    public String getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(String breakTime) {
        this.breakTime = breakTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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
    public Date getBreakDt() {
        if (breakDt == null && breakTime != null && !breakTime.isEmpty()) {
            try {
                breakDt = DataUtil.dateFromDb(breakTime);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid date/time " + breakTime, e);
            }
        }
        return breakDt;
    }

    public void setBreakDt(Date breakDt) {
        this.breakDt = breakDt;
        if (breakDt == null) {
            this.breakTime = null;
        } else {
            this.breakTime = DataUtil.dbFromDate(breakDt);
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
}
