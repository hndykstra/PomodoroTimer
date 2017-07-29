package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Hans on 7/2/2017.
 */

public class Pomodoro {
    private int id;
    private int eventId;
    private String name;
    private int sequence;
    private int timerMinutes;
    private int breakMinutes;
    private Date startDt;
    private Date breakDt;
    private Date endDt;
    private boolean active;

    public Pomodoro(int eventId, String name, int sequence, int timerMinutes, int breakMinutes, Date startDt) {
        this.id = -1;
        this.eventId = eventId;
        this.name = name;
        this.sequence = sequence;
        this.timerMinutes = timerMinutes;
        this.breakMinutes = breakMinutes;
        this.active = true;
        this.startDt = startDt;
        this.endDt = null;
        this.breakDt = null;
    }

    public Pomodoro(Cursor row) {
        try {
            id = row.getInt(PomodoroEventContract.Timer.ID_INDEX);
            eventId = row.getInt(PomodoroEventContract.Timer.EVENT_ID_INDEX);
            name = row.getString(PomodoroEventContract.Timer.NAME_INDEX);
            sequence = row.getInt(PomodoroEventContract.Timer.SEQ_INDEX);
            timerMinutes = row.getInt(PomodoroEventContract.Timer.TIMER_MIN_INDEX);
            breakMinutes = row.getInt(PomodoroEventContract.Timer.BREAK_MIN_INDEX);
            String startDtText = row.getString(PomodoroEventContract.Timer.START_DT_INDEX);
            startDt = DataUtil.dateFromDb(startDtText);
            String breakDtText = row.getString(PomodoroEventContract.Timer.BREAK_DT_INDEX);
            breakDt = DataUtil.dateFromDb(breakDtText);
            String endDtText = row.getString(PomodoroEventContract.Timer.END_DT_INDEX);
            endDt = DataUtil.dateFromDb(endDtText);
            active = (row.getInt(PomodoroEventContract.Timer.ACTIVE_INDEX) != 0);
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

    public Date getStartDt() {
        return startDt;
    }

    public void setStartDt(Date startDt) {
        this.startDt = startDt;
    }

    public Date getBreakDt() {
        return breakDt;
    }

    public void setBreakDt(Date breakDt) {
        this.breakDt = breakDt;
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

    public ContentValues asContent() {
        ContentValues values = new ContentValues();
        //values.put(PomodoroEventContract.Timer.ID_COL, id);
        values.put(PomodoroEventContract.Timer.EVENT_FK_COL, eventId);
        values.put(PomodoroEventContract.Timer.POMODORO_NAME_COL, name);
        values.put(PomodoroEventContract.Timer.POMODORO_SEQ_COL, sequence);
        values.put(PomodoroEventContract.Timer.TIMER_MINUTES_COL, timerMinutes);
        values.put(PomodoroEventContract.Timer.BREAK_MINUTES_COL, breakMinutes);
        values.put(PomodoroEventContract.Timer.ACTIVE_COL, active ? 1 : 0);
        values.put(PomodoroEventContract.Timer.START_DT_COL, DataUtil.dbFromDate(startDt));
        values.put(PomodoroEventContract.Timer.BREAK_DT_COL, DataUtil.dbFromDate(breakDt));
        values.put(PomodoroEventContract.Timer.END_DT_COL, DataUtil.dbFromDate(endDt));
        return values;
    }
}
