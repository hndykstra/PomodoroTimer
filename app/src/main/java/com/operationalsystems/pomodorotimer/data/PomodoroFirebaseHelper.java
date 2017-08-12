package com.operationalsystems.pomodorotimer.data;

import android.provider.ContactsContract;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Objects;

/**
 * Helper methods to reduce client code load.
 */
public class PomodoroFirebaseHelper {

    private FirebaseDatabase database;

    public PomodoroFirebaseHelper() {
        database = FirebaseDatabase.getInstance();
    }

    public PomodoroFirebaseHelper(final FirebaseDatabase database) {
        this.database = database;
    }

    /**
     * Queries the database for a user ID. The receiver will get a single callback with the snapshot
     * value when available.
     * @param userId Uid of the user to fetch
     * @param valueReceiver Single value listener receives a callback
     */
    public void queryUser(String userId, ValueEventListener valueReceiver) {
        PomodoroFirebaseContract.getUserReference(database, userId).addListenerForSingleValueEvent(valueReceiver);

    }

    public void queryTeam(String teamDomain, ValueEventListener valueReceiver) {
        PomodoroFirebaseContract.getTeamReference(database, teamDomain).addListenerForSingleValueEvent(valueReceiver);
    }

    public void queryUserTeams(String userId, ValueEventListener valueReceiver) {
        PomodoroFirebaseContract.getUserTeamsReference(database, userId).addListenerForSingleValueEvent(valueReceiver);
    }

    public void queryEvent(final String eventKey, final String teamDomain, final String uid, final ValueEventListener valueReceiver) {
        if (teamDomain == null || teamDomain.isEmpty()) {
            PomodoroFirebaseContract.getUserPrivateEventReference(database, uid)
                    .child(eventKey)
                    .addListenerForSingleValueEvent(valueReceiver);
        } else {
            PomodoroFirebaseContract.getTeamEventsReference(database, teamDomain)
                    .child(eventKey)
                    .addListenerForSingleValueEvent(valueReceiver);
        }
    }

    public DatabaseReference getEventsReference(final String teamDomain, final String uid) {
        if (teamDomain == null || teamDomain.isEmpty()) {
            return PomodoroFirebaseContract.getUserPrivateEventReference(this.database, uid);
        } else {
            return PomodoroFirebaseContract.getTeamEventsReference(this.database, teamDomain);
        }
    }

    public String createEvent(Event ev) {
        String key;
        if (ev.getTeamDomain() != null && !ev.getTeamDomain().isEmpty()) {
            // event created unter teams if domain is set
            DatabaseReference teamEventsReference  = PomodoroFirebaseContract.getTeamEventsReference(this.database, ev.getTeamDomain());
            DatabaseReference newEventRef = teamEventsReference.push();
            key = newEventRef.getKey();
            ev.setKey(key);
            newEventRef.setValue(ev);
            addMemberToEvent(key, ev.getTeamDomain(), ev.getOwner(), ev.getStartDt());
        } else {
            // event created under the owner uid if no domain
            DatabaseReference userEventsReference = PomodoroFirebaseContract.getUserPrivateEventReference(this.database, ev.getOwner());
            DatabaseReference newEventRef = userEventsReference.push();
            key = newEventRef.getKey();
            ev.setKey(key);
            newEventRef.setValue(ev);
        }

        return key;
    }

    public Task<Void> putEvent(Event ev) {
        if (ev.getTeamDomain() == null || ev.getTeamDomain().isEmpty()) {
            DatabaseReference eventRef = PomodoroFirebaseContract.getUserPrivateEventReference(this.database, ev.getOwner())
                    .child(ev.getKey());
            return eventRef.setValue(ev);
        } else {
            DatabaseReference eventRef = PomodoroFirebaseContract.getTeamEventsReference(this.database, ev.getTeamDomain())
                    .child(ev.getKey());
            return eventRef.setValue(ev);
        }
    }

    public Task<Void> deleteEvent(Event ev) {
        if (ev.getTeamDomain() == null || ev.getTeamDomain().isEmpty()) {
            DatabaseReference eventRef = PomodoroFirebaseContract.getUserPrivateEventReference(this.database, ev.getOwner())
                    .child(ev.getKey());
            return eventRef.removeValue();
        } else {
            DatabaseReference eventRef = PomodoroFirebaseContract.getTeamEventsReference(this.database, ev.getTeamDomain())
                    .child(ev.getKey());
            return eventRef.removeValue();
        }
    }

    public void addMemberToEvent(String eventKey, String team, String uid, Date joinDt) {
        // add event key to USER_MEMBER_OF_EVENTS
        DatabaseReference userMemberKey = PomodoroFirebaseContract.getUserEventsJoinedReference(this.database, uid, team);
        userMemberKey.setValue(joinDt);

        // add uid: joinDt to TEAM_EVENT_MEMBERS
        DatabaseReference eventMemberKey = PomodoroFirebaseContract.getTeamEventMembersReference(this.database, team, eventKey)
                .child(uid);
        eventMemberKey.setValue(joinDt);
    }
}
