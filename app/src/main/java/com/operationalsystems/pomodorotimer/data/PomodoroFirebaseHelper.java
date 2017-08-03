package com.operationalsystems.pomodorotimer.data;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    /**
     * Adds a listener that receives child events for teams. This will receive childAdded events for existing teams
     * and other events as data changes, until unsubscribed.
     * @param teamReceiver Callback will receive data notifications.
     */
    public void subscribePublicTeams(final ChildEventListener teamReceiver) {
        PomodoroFirebaseContract.getTeamsReference(database)
                .orderByChild("public")
                .equalTo(true)
                .addChildEventListener(teamReceiver);
    }

    public void unsubscribePublicTeams(final ChildEventListener teamReceiver) {
        // TODO: does this work, or do I need to keep a reference to the original Query?
        // it appears the listener is added / removed to some underlying DB resource
        // so removing it this way will work... maybe
        PomodoroFirebaseContract.getTeamsReference(database)
                .removeEventListener(teamReceiver);
    }

}
