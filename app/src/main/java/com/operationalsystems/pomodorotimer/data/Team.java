package com.operationalsystems.pomodorotimer.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain object for a defined team domain.
 */
public class Team {
    private String domainName;
    private Date createdDt;
    private String ownerUid;
    private boolean active;
    private Map<String,TeamMember> members;

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
        this.members = new HashMap<>();
        TeamMember ownerMember = new TeamMember(owner, TeamMember.Role.Owner, owner);
        this.members.put(owner, ownerMember);
    }

    @Exclude
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String name) {
        this.domainName = name;
    }

    @Exclude
    public Date getCreatedDt() {
        return createdDt;
    }

    @Exclude
    public void setCreatedDt(Date createdDt) {
        this.createdDt = createdDt;
    }

    public String getCreatedTime() {
        return DataUtil.dbFromDate(createdDt);
    }

    public void setCreatedTime(String time) {
        try {
            this.createdDt = DataUtil.dateFromDb(time);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid createdDt", e);
        }
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

    public Map<String, TeamMember> getMembers() {
        return members;
    }

    public void setMembers(Map<String, TeamMember> members) {
        this.members = members;
    }

    public TeamMember findTeamMember(final String uid) {
        return this.members.get(uid);
    }

    @Override
    public int hashCode() {
        return this.getDomainName().hashCode();
    }

    @Override
    public String toString() {
        return "Team: " + this.getDomainName();
    }
    @Override
    public boolean equals(Object o) {
        return (o instanceof Team && getDomainName() != null && getDomainName().equals(((Team) o).getDomainName()));
    }
}
