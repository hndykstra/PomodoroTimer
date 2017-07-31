package com.operationalsystems.pomodorotimer.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Assistance to navigating the Firebase schema.
 */

public class PomodoroFirebaseContract {
    public static final String USER_ROOT = "users";
    public static final String USER_EVENTS = "users/%s/privateEvents";
    public static final String USER_MEMBER_OF_EVENTS = "users/%s/eventsJoined";
    public static final String USER_TEAMS = "users/%s/teams";

    public static final String TEAM_ROOT = "teams";
    public static final String TEAM_EVENTS = "teams/%s/events";
    public static final String TEAM_MEMBERS = "teams/%s/members";

    public static final String EVENT_ROOT = "events";
    public static final String EVENT_MEMBERS = "events/%s/members";
    public static final String EVENT_POMODOROS = "events/%s/pomodoros";

    public static DatabaseReference getUsersReference(final FirebaseDatabase database) {
        return database.getReference(USER_ROOT);
    }

    public static DatabaseReference getUserReference(final FirebaseDatabase database, final String uid) {
        return database.getReference(USER_ROOT + "/" + uid);
    }

    public static DatabaseReference getUserPrivateEventReference(final FirebaseDatabase database, final String uid) {
        return database.getReference(String.format(USER_EVENTS, uid));
    }

    public static DatabaseReference getUserEventsJoinedReference(final FirebaseDatabase database, final String uid) {
        return database.getReference(String.format(USER_MEMBER_OF_EVENTS, uid));
    }

    public static DatabaseReference getUserTeamsReference(final FirebaseDatabase database, final String uid) {
        return database.getReference(String.format(USER_TEAMS, uid));
    }

    public static DatabaseReference getTeamsReference(final FirebaseDatabase database) {
        return database.getReference(TEAM_ROOT);
    }

    public static DatabaseReference getTeamReference(final FirebaseDatabase database, final String teamDomain) {
        return database.getReference(TEAM_ROOT + "/" + teamDomain);
    }

    public static DatabaseReference getTeamEventsReference(final FirebaseDatabase database, final String teamDomain) {
        return database.getReference(String.format(TEAM_EVENTS, teamDomain));
    }

    public static DatabaseReference getTeamMembersReference(final FirebaseDatabase database, final String teamDomain) {
        return database.getReference(String.format(TEAM_MEMBERS, teamDomain));
    }

    public static DatabaseReference getEventsReference(final FirebaseDatabase database) {
        return database.getReference(EVENT_ROOT);
    }

    public static DatabaseReference getEventReference(final FirebaseDatabase database, final String eventId) {
        return database.getReference(EVENT_ROOT + "/" + eventId);
    }

    public static DatabaseReference getEventMembersReference(final FirebaseDatabase database, final String eventId) {
        return database.getReference(String.format(EVENT_MEMBERS, eventId));
    }

    public static DatabaseReference getEventPomodorosReference(final FirebaseDatabase database, final String eventId) {
        return database.getReference(String.format(EVENT_POMODOROS, eventId));
    }
}
