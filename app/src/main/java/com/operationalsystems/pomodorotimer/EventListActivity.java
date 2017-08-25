package com.operationalsystems.pomodorotimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseContract;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.Team;
import com.operationalsystems.pomodorotimer.data.User;
import com.operationalsystems.pomodorotimer.util.Promise;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;

/**
 * TODO: add support for loading indicator
 */
public class EventListActivity extends AppCompatActivity {

    public static final String PLACEHOLDER_OWNER = "anonymous";
    public static final String PLACEHOLDER_TEAM = "test";
    public static final String EXTRA_EVENT_ID = "SelectedEventId";
    public static final String STORE_TEAM_DOMAIN = "TeamDomain";

    class TeamDisplay {

        int stringResourceId;
        String display;
        Team team;

        TeamDisplay(@NonNull Team t) {
            team = t;
            stringResourceId = -1;
            display = team.getDomainName();
        }

        TeamDisplay(int resourceId) {
            team = null;
            stringResourceId = resourceId;
            display = EventListActivity.this.getString(stringResourceId);
        }

        @Override
        public String toString() {
            return display;
        }

        @Override
        public boolean equals(Object o) {
            boolean isEqual = false;
            if (o instanceof TeamDisplay) {
                TeamDisplay other = (TeamDisplay)o;
                if (team == null) {
                    isEqual = other.team == null && stringResourceId == other.stringResourceId;
                } else {
                    isEqual = team.equals(other.team);
                }
            }

            return isEqual;
        }
    }

    private class AuthListener implements FirebaseAuth.AuthStateListener {

        @Override
        public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                onLogin(currentUser);
            } else {
                onLogout();
            }
        }
    }

    private static final String LOG_TAG = "EventListActivity";
    private static final int RESULT_AUTH_ID = 20532;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser theUser;

    private PomodoroFirebaseHelper database;

    @BindView(R.id.team_spinner) Spinner teamSpinner;
    @BindView(R.id.recycler_events) RecyclerView recycler;
    private EventListAdapter adapter;

    private ArrayAdapter<TeamDisplay> teamListAdapter;

    private String teamDomain = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        auth = FirebaseAuth.getInstance();
        authListener = new AuthListener();

        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 1);
        recycler.setLayoutManager(lm);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewEvent();
            }
        });

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onResume() {
        auth.addAuthStateListener(authListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.cleanup();
            adapter = null;
        }
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.teamDomain != null && !this.teamDomain.isEmpty()) {
            outState.putString(STORE_TEAM_DOMAIN, this.teamDomain);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.teamDomain = savedInstanceState.getString(STORE_TEAM_DOMAIN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_teams) {
            startActivity(new Intent(this, TeamJoinActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            this.auth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onLogin(final FirebaseUser user) {
        this.theUser = user;
        database = new PomodoroFirebaseHelper();
        User userInst = new User();
        userInst.setUid(theUser.getUid());
        userInst.setDisplayName(theUser.getDisplayName());
        database.createUser(userInst);
        // other things like kick off the data load
        setTeamSelection(teamDomain);
        viewTeamData(teamDomain);
    }

    private void onLogout() {
        this.theUser = null;
        if (this.adapter != null) {
            this.adapter.cleanup();
        }
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .setIsSmartLockEnabled(false)
                        .build(),
                RESULT_AUTH_ID);
    }

    private void eventSelected(Event event) {
        Log.d(LOG_TAG, "EVENT SELECTED");
        if (event.isActive()) {
            Intent timerActivityIntent = new Intent(this, EventTimerActivity.class);
            timerActivityIntent.putExtra(EXTRA_EVENT_ID, event.getKey());
            timerActivityIntent.putExtra(STORE_TEAM_DOMAIN, this.teamDomain);
            startActivity(timerActivityIntent);
        } else {
            Intent summaryViewIntent = new Intent(this, EventSummaryActivity.class);
            summaryViewIntent.putExtra(EXTRA_EVENT_ID, event.getKey());
            summaryViewIntent.putExtra(STORE_TEAM_DOMAIN, this.teamDomain);
            startActivity(summaryViewIntent);
        }
    }

    private void showNewEvent() {
        final Date now = new Date();
        final String defaultEventName = DateFormat.getDateInstance(DateFormat.SHORT).format(now);
        final String name = adapter.getUniqueEventName(defaultEventName);
        Bundle args = new Bundle();
        args.putString(CreateEventDlgFragment.BUNDLE_KEY_EVENTNAME, name);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EventListActivity.this);
        String activityMinutes = preferences.getString(getString(R.string.pref_activity_time_key), "");
        String breakMinutes = preferences.getString(getString(R.string.pref_break_time_key), "");

        try {
            args.putInt(CreateEventDlgFragment.BUNDLE_KEY_ACTIVITY_LENGTH, Integer.parseInt(activityMinutes));
            args.putInt(CreateEventDlgFragment.BUNDLE_KEY_BREAK_LENGTH, Integer.parseInt(breakMinutes));
        } catch (NumberFormatException nfe) {
            Log.d(LOG_TAG, "problem parsing preferences");
        }
        CreateEventDlgFragment fragment = new CreateEventDlgFragment();
        fragment.setArguments(args);
        fragment.setListener(new CreateEventDlgFragment.CreateEventListener() {
            @Override
            public void doCreateEvent(CreateEventDlgFragment.CreateEventParams params) {
                EventListActivity.this.createEvent(params);
            }
        });
        fragment.show(getFragmentManager(), "CreateEventDlgFragment");
    }

    void createEvent(CreateEventDlgFragment.CreateEventParams params) {
        Log.d(LOG_TAG, "Create event " + params.eventName);

        final Date createDate = new Date();
        final String user = this.theUser == null ? PLACEHOLDER_OWNER : this.theUser.getUid();
        Event newEvent = new Event(params.eventName, user, createDate, true,
                params.activityMinutes, params.breakMinutes, teamDomain);
        String key = database.createEvent(newEvent);
        Log.d(LOG_TAG, "   created event at " + key);
    }

    @OnItemSelected(R.id.team_spinner)
    private void spinnerChanged() {
        TeamDisplay selected = (TeamDisplay)teamSpinner.getSelectedItem();
        if (selected.team != null) {
            viewTeamData(selected.team.getDomainName());
        } else if (selected.stringResourceId == R.string.option_private_events) {
            viewTeamData(null);
        } else if (selected.stringResourceId == R.string.option_my_team) {
            startActivity(new Intent(this, TeamJoinActivity.class));
        }
    }

    private void setTeamSelection(String teamDomain) {
        int selectionIndex = -1;
        boolean isTeam = teamDomain != null && teamDomain.length() > 0;

        for (int i=0 ; i < teamListAdapter.getCount() ; ++i) {
            TeamDisplay td = teamListAdapter.getItem(i);
            if (isTeam && td.team != null && teamDomain.equals(td.team.getDomainName())) {
                selectionIndex = i;
                break;
            } if (!isTeam && td.stringResourceId == R.string.option_private_events) {
                selectionIndex = i;
                break;
            }
        }

        if (selectionIndex != -1)
            teamSpinner.setSelection(selectionIndex);
    }

    private void viewTeamData(String teamDomain) {
        if (adapter != null) {
            adapter.cleanup();
        }

        this.teamDomain = teamDomain;

        DatabaseReference eventReference = database.getEventsReference(teamDomain, theUser.getUid());
        adapter = new EventListAdapter(eventReference, new EventListAdapter.EventSelectionListener() {
            @Override
            public void eventSelected(Event event) {
                EventListActivity.this.eventSelected(event);
            }
        });
        recycler.setAdapter(adapter);
    }

    private void populateTeams() {
        teamListAdapter = new ArrayAdapter<TeamDisplay>(this, R.layout.support_simple_spinner_dropdown_item);
        teamListAdapter.add(new TeamDisplay(R.string.option_private_events));
        teamListAdapter.add(new TeamDisplay(R.string.option_my_team));
        teamSpinner.setAdapter(teamListAdapter);
        database.subscribeUserTeams(theUser.getUid(), new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String teamKey = dataSnapshot.getKey();
                database.queryTeam(teamKey).then(new Promise.PromiseReceiver() {
                    @Override
                    public Object receive(Object t) {
                        Team team = (Team)t;
                        teamListAdapter.add(new TeamDisplay(team));
                        return t;
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // no-op
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String teamKey = dataSnapshot.getKey();
                for (int i = 0 ; i < teamListAdapter.getCount() ; ++i) {
                    TeamDisplay td = teamListAdapter.getItem(i);
                    if (td.team.getDomainName().equals(teamKey)) {
                        teamListAdapter.remove(td);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // no-op
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // no-op
            }
        });
    }
}
