package com.operationalsystems.pomodorotimer.data;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.operationalsystems.pomodorotimer.util.Promise;
import com.operationalsystems.pomodorotimer.util.PromiseValueEventListener;
import com.operationalsystems.pomodorotimer.util.TaskPromise;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Helper methods to reduce client code load.
 */
public class PomodoroFirebaseHelper {

    private static final String LOG_TAG = "PomodoroFirebaseHelper";

    static class TeamHelper implements PromiseValueEventListener.EntityHelper {
        private String key;
        public TeamHelper(String key) {
            this.key = key;
        }

        @Override
        public Object preprocessEntity(Object o) {
            Team t = (Team)o;
            t.setDomainName(key);
            return t;
        }
    }

    static class EventHelper implements PromiseValueEventListener.EntityHelper {
        private String key;
        public EventHelper(String key) {
            this.key = key;
        }

        @Override
        public Object preprocessEntity(Object o) {
            Event e = (Event)o;
            e.setKey(key);
            return e;
        }
    }

    static class UserHelper implements PromiseValueEventListener.EntityHelper {
        private String uid;
        public UserHelper(String uid) {
            this.uid = uid;
        }

        @Override
        public Object preprocessEntity(Object o) {
            User u = (User)o;
            u.setUid(uid);
            return u;
        }
    }

    private FirebaseDatabase database;

    public PomodoroFirebaseHelper() {
        database = FirebaseDatabase.getInstance();
        //database.setPersistenceEnabled(true);
    }

    public PomodoroFirebaseHelper(final FirebaseDatabase database) {
        this.database = database;
    }

    /**
     * Queries the database for a user ID. The receiver will get a single callback with the snapshot
     * value when available.
     * @param userId Uid of the user to fetch
     * @return Promise for a user, resolved on the ValueEventListener thread.
     */
    public Promise queryUser(String userId) {
        final Promise p = new Promise();
        ValueEventListener valueReceiver = new PromiseValueEventListener(p, User.class, new UserHelper(userId));
        PomodoroFirebaseContract.getUserReference(database, userId).addListenerForSingleValueEvent(valueReceiver);
        return p;
    }

    public Promise updateUserRecentTeam(String userId, String teamDomain) {
        DatabaseReference ref = PomodoroFirebaseContract.getUserReference(database, userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("recentTeam", teamDomain);

        Task<Void> task = ref.updateChildren(updates);
        return TaskPromise.of(task);
    }

    public Promise updateUserRecentEvent(String userId, Event event) {
        DatabaseReference ref = PomodoroFirebaseContract.getUserReference(database, userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("recentTeam", event.getTeamDomain());
        updates.put("recentEvent", event.getKey());

        Task<Void> task = ref.updateChildren(updates);
        return TaskPromise.of(task);
    }

    /**
     * Queries for a specific team by the key value.
     * @param teamDomain Domain name for the team to find.
     * @return Promise for a Team object, resolved on thee ValueEventListener thread.
     */
    public Promise queryTeam(String teamDomain) {
        final Promise p = new Promise();
        ValueEventListener valueReceiver = new PromiseValueEventListener(p, Team.class, new TeamHelper(teamDomain));
        PomodoroFirebaseContract.getTeamReference(database, teamDomain).addListenerForSingleValueEvent(valueReceiver);
        return p;
    }

    public void subscribeUserTeams(String userId, ChildEventListener subscriber) {
        PomodoroFirebaseContract.getUserTeamsReference(database, userId).addChildEventListener(subscriber);
    }

    public void unsubscribeUserTeams(String userId, ChildEventListener subscriber) {
        PomodoroFirebaseContract.getUserTeamsReference(database, userId).removeEventListener(subscriber);
    }

    public Promise queryEvent(final String eventKey, final String teamDomain, final String uid) {
        Promise p = new Promise();
        ValueEventListener valueReceiver = new PromiseValueEventListener(p, Event.class, new EventHelper(eventKey));
        if (teamDomain == null || teamDomain.isEmpty()) {
            PomodoroFirebaseContract.getUserPrivateEventsReference(database, uid)
                    .child(eventKey)
                    .addListenerForSingleValueEvent(valueReceiver);
        } else {
            PomodoroFirebaseContract.getTeamEventsReference(database, teamDomain)
                    .child(eventKey)
                    .addListenerForSingleValueEvent(valueReceiver);
        }

        return p;
    }

    public void subscribePomodoros(final Event event, final ChildEventListener receiver) {
        DatabaseReference reference;
        if (event.getTeamDomain() == null || event.getTeamDomain().length() == 0) {
            reference = PomodoroFirebaseContract.getUserPrivateEventsReference(database, event.getOwner())
                    .child(event.getKey()).child("pomodoros");
        } else {
            reference = PomodoroFirebaseContract.getTeamEventsReference(database, event.getTeamDomain())
                    .child(event.getKey()).child("pomodoros");
        }

        Log.d(LOG_TAG, "SUBSCRIBE " + reference.toString());
        reference.addChildEventListener(receiver);
    }

    public void unsubscribePomodoros(final Event event, final ChildEventListener receiver) {
        DatabaseReference reference;
        if (event.getTeamDomain() != null && event.getTeamDomain().length() > 0) {
            reference = PomodoroFirebaseContract.getUserPrivateEventsReference(database, event.getOwner())
                    .child(event.getKey()).child("pomodoros");
        } else {
            reference = PomodoroFirebaseContract.getTeamEventsReference(database, event.getTeamDomain())
                    .child(event.getKey()).child("pomodoros");
        }

        Log.d(LOG_TAG, "UNSUBSCRIBE " + reference.toString());
        reference.removeEventListener(receiver);
    }

    public DatabaseReference getEventsReference(final String teamDomain, final String uid) {
        if (teamDomain == null || teamDomain.isEmpty()) {
            return PomodoroFirebaseContract.getUserPrivateEventsReference(this.database, uid);
        } else {
            return PomodoroFirebaseContract.getTeamEventsReference(this.database, teamDomain);
        }
    }

    public void createUser(final User aUser) {
        DatabaseReference userNode = PomodoroFirebaseContract.getUsersReference(database)
                .child(aUser.getUid());
        userNode.updateChildren(Collections.singletonMap("displayName", (Object)aUser.getDisplayName()));
    }

    public Promise createTeam(Team team) {
        String key = team.getDomainName();
        final DatabaseReference ref = database.getReference();
        final String teamPath = "teams/" + key;
        final String userTeamPath = "users/" + team.getOwnerUid() + "/teams/" + key;
        Map<String,Object> updates = new HashMap<>();
        updates.put(teamPath, team);
        updates.put(userTeamPath, TeamMember.Role.Owner.toString());
        Task<Void> updateTask = ref.updateChildren(updates);
        return TaskPromise.of(updateTask);
    }

    public Promise joinEvent(final Event ev, final String uid, final Date joinTime) {
        final Promise p = new Promise();
        // add user to event members, add event to user joined events
        final DatabaseReference ref = database.getReference();
        boolean isTeamEvent = ev.getTeamDomain() != null && ev.getTeamDomain().length() > 0;
        if (!isTeamEvent)
            throw new IllegalArgumentException("Cannot join a private event");
        DatabaseReference eventRef = PomodoroFirebaseContract.getTeamEventsReference(database, ev.getTeamDomain()).child(ev.getKey());
        final String userJoinedEventsPath = String.format(PomodoroFirebaseContract.USER_EVENT_MEMBERS, uid)
                + "/" + ev.getTeamDomain();
        final String eventMemberPath = String.format(PomodoroFirebaseContract.TEAM_EVENT_MEMBERS, ev.getTeamDomain(), ev.getKey())
                + "/" + uid;
        final String dateStr = DataUtil.dbFromDate(joinTime);

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String,Object> updates = new HashMap<>();
                    updates.put(userJoinedEventsPath, dateStr);
                    updates.put(eventMemberPath, dateStr);
                    TaskPromise.of(ref.updateChildren(updates))
                        .then(new Promise.PromiseReceiver() {
                            @Override
                            public Object receive(Object t) {
                                p.resolve(true);
                                return null;
                            }
                        });
                } else {
                    p.resolve(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                p.reject(databaseError);
            }
        });

        return p;
    }

    public void updateTeamRole(final String teamDomain, final String uid, final TeamMember.Role role, final String updatedBy) {
        final DatabaseReference teamMemberRef = PomodoroFirebaseContract.getTeamMembersReference(database, teamDomain)
                .child(uid);
        teamMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    TeamMember mem = dataSnapshot.getValue(TeamMember.class);
                    mem.setRole(role);
                    mem.setAddedBy(updatedBy);
                    mem.setAddedOn(new Date());
                    dataSnapshot.getRef().setValue(mem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public Promise quitTeam(final String teamDomain, final String uid) {
        final Promise promise = new Promise();

        // two things: remove the user from the team, and remove the team from the user
        final DatabaseReference ref = database.getReference();
        final String teamMemberPath = "teams/" + teamDomain + "/members/" + uid;
        final String userTeamPath = "users/" + uid + "/teams/" + teamDomain;
        final Map<String, Object> updates = new HashMap<>();
        final DatabaseReference teamRef = PomodoroFirebaseContract.getTeamReference(database, teamDomain);
        teamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    updates.put(teamMemberPath, null);
                    updates.put(userTeamPath, null);
                    Task<Void> task = ref.updateChildren(updates);
                    task.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                promise.reject(false);
                            else
                                promise.resolve(true);
                        }
                    });
                } else {
                    promise.reject("Team not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                promise.reject(false);
            }
        });

        return promise;
    }

    /**
     * Adds a specified user to a specified team in specified role.
     * @param teamDomain Name of the team to add user to.
     * @param uid Identifies the user to add.
     * @param role Role of the user.
     * @param updateBy User requesting the update.
     * @return Promise to be fulfilled when actions are completed.
     */
    public Promise joinTeam(final String teamDomain, final String uid, final TeamMember.Role role, final String updateBy) {
        final Promise promise = new Promise();
        // two things: set the user on the team with role, and set the team in the user
        final DatabaseReference ref = database.getReference();
        final String teamMemberPath = "teams/" + teamDomain + "/members/" + uid;
        final String userTeamPath = "users/" + uid + "/teams/" + teamDomain;
        final Map<String, Object> updates = new HashMap<>();
        final DatabaseReference teamRef = PomodoroFirebaseContract.getTeamReference(database, teamDomain);
        teamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    TeamMember member = new TeamMember(uid, role, updateBy);
                    updates.put(teamMemberPath, member);
                    updates.put(userTeamPath, role);
                    Task<Void> task = ref.updateChildren(updates);
                    task.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                promise.reject(false);
                            else
                                promise.resolve(true);
                        }
                    });
                } else {
                    promise.reject("Team not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                promise.reject(false);
            }
        });

        return promise;
    }

    public void queryTeamMembers(String teamDomain, ValueEventListener receiver) {
        DatabaseReference ref = PomodoroFirebaseContract.getTeamMembersReference(database, teamDomain);
        ref.addListenerForSingleValueEvent(receiver);
    }

    public String createEvent(Event ev) {
        String key;
        if (ev.getTeamDomain() != null && !ev.getTeamDomain().isEmpty()) {
            // event created unter teams if domain is set
            DatabaseReference teamEventsReference  = PomodoroFirebaseContract.getTeamEventsReference(this.database, ev.getTeamDomain());
            DatabaseReference newEventRef = teamEventsReference.push();
            key = newEventRef.getKey();
            // todo: unify this in one atomic operation
            ev.setKey(key);
            newEventRef.setValue(ev);
            addMemberToEvent(key, ev.getTeamDomain(), ev.getOwner(), ev.getStartDt());
        } else {
            // event created under the owner uid if no domain
            DatabaseReference userEventsReference = PomodoroFirebaseContract.getUserPrivateEventsReference(this.database, ev.getOwner());
            DatabaseReference newEventRef = userEventsReference.push();
            key = newEventRef.getKey();
            ev.setKey(key);
            newEventRef.setValue(ev);
        }

        return key;
    }

    public Promise putEvent(Event ev) {
        if (ev.getTeamDomain() == null || ev.getTeamDomain().isEmpty()) {
            DatabaseReference eventRef = PomodoroFirebaseContract.getUserPrivateEventsReference(this.database, ev.getOwner())
                    .child(ev.getKey());
            return TaskPromise.of(eventRef.setValue(ev));
        } else {
            DatabaseReference eventRef = PomodoroFirebaseContract.getTeamEventsReference(this.database, ev.getTeamDomain())
                    .child(ev.getKey());
            return TaskPromise.of(eventRef.setValue(ev));
        }
    }

    public Promise updatePomodoro(Event inEvent, Pomodoro pomo) {
        if (inEvent.getTeamDomain() == null || inEvent.getTeamDomain().isEmpty()) {
            DatabaseReference pomoRef = PomodoroFirebaseContract.getUserPrivateEventsReference(this.database, inEvent.getOwner())
                    .child(inEvent.getKey()).child("pomodoros").child(pomo.getKey());
            return TaskPromise.of(pomoRef.setValue(pomo));
        } else {
            DatabaseReference pomoRef = PomodoroFirebaseContract.getTeamEventsReference(this.database, inEvent.getTeamDomain())
                    .child(inEvent.getKey()).child("pomodoros").child(pomo.getKey());
            return TaskPromise.of(pomoRef.setValue(pomo));
        }
    }

    public Task<Void> deleteEvent(Event ev) {
        if (ev.getTeamDomain() == null || ev.getTeamDomain().isEmpty()) {
            DatabaseReference eventRef = PomodoroFirebaseContract.getUserPrivateEventsReference(this.database, ev.getOwner())
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
