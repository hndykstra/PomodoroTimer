package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.util.Date;

/**
 * Domain object for team membership records.
 */

public class TeamMember {
    public enum Role {
        Owner, Admin, Member, Disabled
    }

    private int id;
    private String domainName;
    private String memberUid;
    private Role role;
    private Date addedOn;
    private String addedBy;

    public TeamMember(String domainName, String member, Role role, String addedBy) {
        this.domainName = domainName;
        this.memberUid = member;
        this.role = role;
        this.addedBy = addedBy;
        this.addedOn = new Date();
    }

    public TeamMember(Cursor row) {
        try {
            this.id = row.getInt(PomodoroEventContract.TeamMember.ID_INDEX);
            this.domainName = row.getString(PomodoroEventContract.TeamMember.DOMAIN_FK_INDEX);
            this.memberUid = row.getString(PomodoroEventContract.TeamMember.MEMBER_UID_INDEX);
            String roleText = row.getString(PomodoroEventContract.TeamMember.ROLE_INDEX);
            if (roleText != null) {
                try {
                    this.role = Role.valueOf(roleText);
                } catch (IllegalArgumentException e) {
                    this.role = Role.Disabled;
                }
            }
            String addedOnText = row.getString(PomodoroEventContract.TeamMember.ADDED_DT_INDEX);
            this.addedOn = DataUtil.dateFromDb(addedOnText);
            this.addedBy = addedBy;
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

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

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

    public Date getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Date addedOn) {
        this.addedOn = addedOn;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public ContentValues asContent() {
        ContentValues values = new ContentValues();
        //values.put(PomodoroEventContract.TeamMember.ID_COL, id);
        values.put(PomodoroEventContract.TeamMember.DOMAIN_FK_COL, domainName);
        values.put(PomodoroEventContract.TeamMember.MEMBER_UID_COL, memberUid);
        values.put(PomodoroEventContract.TeamMember.ROLE_COL, role == null ? null : role.toString());
        values.put(PomodoroEventContract.TeamMember.ADDED_BY_COL, addedBy);
        values.put(PomodoroEventContract.TeamMember.ADDED_DT_COL, DataUtil.dbFromDate(addedOn));
        return values;

    }
}
