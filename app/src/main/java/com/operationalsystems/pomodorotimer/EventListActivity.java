package com.operationalsystems.pomodorotimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.Pomodoro;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * TODO: add support for loading indicator
 */
public class EventListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String PLACEHOLDER_OWNER = "opsysinc";
    public static final String PLACEHOLDER_TEAM = "test.operationalsystems";
    public static final String EXTRA_EVENT_ID = "SelectedEventId";

    private class AuthListener implements FirebaseAuth.AuthStateListener {

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                theUser = currentUser;
            } else {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(
                                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                .build(),
                        RESULT_AUTH_ID);            }
        }
    }

    private static final String LOG_TAG = "EventListActivity";
    private static final int EVENT_LOADER_ID = 9034;
    private static final int RESULT_AUTH_ID = 20532;
    private static final String EVENT_LIST_SORT = PomodoroEventContract.Event.ACTIVE_COL + " DESC, "
            + PomodoroEventContract.Event.START_DT_COL + " DESC";

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser theUser;

    private RecyclerView recycler;
    private EventListAdapter adapter;

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

        adapter = new EventListAdapter(new EventListAdapter.EventSelectionListener() {
            @Override
            public void eventSelected(Event event) {
                EventListActivity.this.eventSelected(event);
            }
        });
        recycler.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewEvent();
            }
        });

        if (savedInstanceState != null) {
            getSupportLoaderManager().restartLoader(EVENT_LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().initLoader(EVENT_LOADER_ID, null, this);
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
        auth.removeAuthStateListener(authListener);
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

    private void eventSelected(Event event) {
        Log.d(LOG_TAG, "EVENT SELECTED");
        if (event.isActive()) {
            Intent timerActivityIntent = new Intent(this, EventTimerActivity.class);
            timerActivityIntent.putExtra(EXTRA_EVENT_ID, event.getId());
            startActivity(timerActivityIntent);
        } else {
            Intent summaryViewIntent = new Intent(this, EventSummaryActivity.class);
            summaryViewIntent.putExtra(EXTRA_EVENT_ID, event.getId());
            startActivity(summaryViewIntent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == EVENT_LOADER_ID) {
            final Uri queryUri = PomodoroEventContract.BASE_CONTENT_URI.buildUpon()
                    .appendPath(PomodoroEventContract.PATH_EVENT)
                    .build();
            CursorLoader loader = new CursorLoader(this, queryUri, PomodoroEventContract.Event.EVENT_COLS, null, null,
                    EVENT_LIST_SORT);

            return loader;
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == EVENT_LOADER_ID) {
            List<Event> eventsLoaded = new ArrayList<>();
            while (data.moveToNext()) {
                eventsLoaded.add(new Event(data));
            }
            this.adapter.setEvents(eventsLoaded);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // noop
    }

    private void loadingIndicator(boolean show) {
        // TODO
    }

    private void showNewEvent() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                loadingIndicator(true);
            }

            @Override
            protected String doInBackground(Void... params) {
                Date now = new Date();
                final String defaultEventName = DateFormat.getDateInstance(DateFormat.SHORT).format(now);
                final Uri queryEvents = PomodoroEventContract.BASE_CONTENT_URI.buildUpon()
                        .appendPath(PomodoroEventContract.PATH_EVENT)
                        .build();
                final String[] projection = PomodoroEventContract.Event.EVENT_COLS;
                final String selection = PomodoroEventContract.Event.TEAM_DOMAIN_COL + "=?";
                final String[] selectionArgs = new String[]{PLACEHOLDER_TEAM};
                Cursor cursor = getContentResolver().query(queryEvents, projection, selection, selectionArgs, null);
                final ArrayList<String> names = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String name = cursor.getString(PomodoroEventContract.Event.NAME_INDEX);
                    names.add(name);
                }
                int count = 0;
                String eventName = defaultEventName;
                while (names.contains(eventName)) {
                    ++count;
                    eventName = String.format("%s - %02d", defaultEventName, count);
                }
                return eventName;
            }

            @Override
            public void onPostExecute(String name) {
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
        };
        task.execute();
    }

    public void createEvent(CreateEventDlgFragment.CreateEventParams params) {
        Log.d(LOG_TAG, "Create event " + params.eventName);
        final Uri eventUri = PomodoroEventContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(PomodoroEventContract.PATH_EVENT)
                .build();
        Date createDate = new Date();
        Event newEvent = new Event(params.eventName, PLACEHOLDER_OWNER, createDate, true,
                params.activityMinutes, params.breakMinutes, PLACEHOLDER_TEAM);
        Uri insertedUri = getContentResolver().insert(eventUri, newEvent.asContent());
        getSupportLoaderManager().restartLoader(EVENT_LOADER_ID, null, this);
        this.adapter.notifyDataSetChanged();
    }
}
