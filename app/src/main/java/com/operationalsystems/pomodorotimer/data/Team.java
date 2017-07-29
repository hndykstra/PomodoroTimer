package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.util.Date;

/**
 * Domain object for a defined team domain.
 */
public class Team {
    private String domainName;
    private Date createdDt;
    private String ownerUid;
    private boolean active;

    public Team(String name, String owner) {
        this.domainName = name;
        this.ownerUid = owner;
        this.active = true;
        this.createdDt = new Date();
    }

    public Team(Cursor row) {
        try {
            this.domainName = row.getString(PomodoroEventContract.TeamDomain.DOMAIN_INDEX);
            this.ownerUid = row.getString(PomodoroEventContract.TeamDomain.OWNER_INDEX);
            String createdText = row.getString(PomodoroEventContract.TeamDomain.CREATED_DT_INDEX);
            this.createdDt = DataUtil.dateFromDb(createdText);
            active = row.getInt(PomodoroEventContract.TeamDomain.ACTIVE_INDEX) != 0;
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
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

    public ContentValues asContent() {
        ContentValues values = new ContentValues();
        values.put(PomodoroEventContract.TeamDomain.DOMAIN_COL, domainName);
        values.put(PomodoroEventContract.TeamDomain.ACTIVE_COL, active ? 1 : 0);
        values.put(PomodoroEventContract.TeamDomain.OWNER_COL, ownerUid);
        values.put(PomodoroEventContract.TeamDomain.CREATED_DT_COL, DataUtil.dbFromDate(createdDt));
        return values;
    }
}
