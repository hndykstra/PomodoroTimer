package com.operationalsystems.pomodorotimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseContract;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * TODO: add support for loading indicator
 */
public class EventListActivity extends AppCompatActivity {

    public static final String PLACEHOLDER_OWNER = "anonymous";
    public static final String PLACEHOLDER_TEAM = "test";
    public static final String EXTRA_EVENT_ID = "SelectedEventId";
    public static final String STORE_TEAM_DOMAIN = "TeamDomain";

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

    private RecyclerView recycler;
    private EventListAdapter adapter;

    private String teamDomain = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        auth = FirebaseAuth.getInstance();
        authListener = new AuthListener();

        recycler = (RecyclerView) findViewById(R.id.recycler_events);
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
            startActivity(new Intent(EventListActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onLogin(final FirebaseUser user) {
        this.theUser = user;
        database = new PomodoroFirebaseHelper();
        // other things like kick off the
        DatabaseReference eventReference = database.getEventsReference(teamDomain, theUser.getUid());
        adapter = new EventListAdapter(eventReference, new EventListAdapter.EventSelectionListener() {
            @Override
            public void eventSelected(Event event) {
                EventListActivity.this.eventSelected(event);
            }
        });
        recycler.setAdapter(adapter);
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
        loadingIndicator(false);
        fragment.show(getFragmentManager(), "CreateEventDlgFragment");
    }

    public void createEvent(CreateEventDlgFragment.CreateEventParams params) {
        Log.d(LOG_TAG, "Create event " + params.eventName);

        final Date createDate = new Date();
        final String user = this.theUser == null ? PLACEHOLDER_OWNER : this.theUser.getUid();
        Event newEvent = new Event(params.eventName, user, createDate, true,
                params.activityMinutes, params.breakMinutes, teamDomain);
        String key = database.createEvent(newEvent);
        Log.d(LOG_TAG, "   created event at " + key);
    }
}
