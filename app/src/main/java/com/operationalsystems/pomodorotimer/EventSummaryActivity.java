package com.operationalsystems.pomodorotimer;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.Pomodoro;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.util.Promise;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventSummaryActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "SelectedEventId";

    private static final String LOG_TAG = "EventSummaryActivity";


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

    private String eventKey;
    private String teamDomain;
    private Event event;
    private PomodoroListAdapter pomodoroAdapter;
    private EventMemberListAdapter memberAdapter;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.label_time_total) TextView totalTime;
    @BindView(R.id.label_activity_total) TextView totalActivity;
    @BindView(R.id.label_activity_average) TextView averageActivity;
    @BindView(R.id.label_break_average) TextView averageBreak;
    @BindView(R.id.recycler_pomodoro_list) RecyclerView pomodoroRecycler;
    @BindView(R.id.recycler_members) RecyclerView memberRecycler;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser theUser;

    private PomodoroFirebaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Summary activity onCreate");
        setContentView(R.layout.activity_event_summary);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        authListener = new AuthListener();

        RecyclerView.LayoutManager lm1 = new GridLayoutManager(this, 1);
        pomodoroRecycler.setLayoutManager(lm1);
        pomodoroAdapter = new PomodoroListAdapter(null);
        pomodoroRecycler.setAdapter(pomodoroAdapter);

        RecyclerView.LayoutManager lm2 = new GridLayoutManager(this, 1);
        memberRecycler.setLayoutManager(lm2);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else {
            this.eventKey = getIntent().getStringExtra(EventListActivity.EXTRA_EVENT_ID);
            this.teamDomain = getIntent().getStringExtra(EventListActivity.STORE_TEAM_DOMAIN);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        this.eventKey = inState.getString(EventListActivity.EXTRA_EVENT_ID);
        this.teamDomain = inState.getString(EventListActivity.STORE_TEAM_DOMAIN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EventListActivity.EXTRA_EVENT_ID, this.eventKey);
        outState.putString(EventListActivity.STORE_TEAM_DOMAIN, this.teamDomain);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Summary activity onResume");
        auth.addAuthStateListener(authListener);
    }


    private void onLogin(FirebaseUser user) {
        this.theUser = user;
        database = new PomodoroFirebaseHelper();
        memberAdapter = new EventMemberListAdapter(null, database);
        memberRecycler.setAdapter(memberAdapter);
        initializeEventState();
    }

    private void onLogout() {
        // can't stay here if we are logged out
        NavUtils.navigateUpFromSameTask(this);
    }

    private void initializeEventState() {
        if (this.eventKey != null && !this.eventKey.isEmpty()) {
            database.queryEvent(this.eventKey, teamDomain, theUser.getUid())
                .then(new Promise.PromiseReceiver() {
                    @Override
                    public Object receive(Object t) {
                        Event e = (Event)t;
                        bindToEvent(e);
                        return t;
                    }
                });
        }
    }

    private void bindToEvent(final Event event) {
        this.event = event;
        this.toolbar.setTitle(event.getName());
        if (event.getEndDt() != null) {
            long msecDiff = event.getEndDt().getTime() - event.getStartDt().getTime();
            long secDiff = msecDiff / 1000L;
            long minDiff = secDiff / 60L;
            secDiff = secDiff % 60L;
            long hrDiff = minDiff / 60L;
            minDiff = minDiff % 60L;
            final String totalTimeFmt = String.format("%d h %02d m", hrDiff, minDiff);
            this.totalTime.setText(getString(R.string.label_time_total, totalTimeFmt));
        } else {
            this.totalTime.setText(getString(R.string.label_time_total, "*"));
        }
        updatePomodoroData(event.getPomodoros().values());
        this.pomodoroAdapter.setEvent(event);
        this.memberAdapter.setEvent(event);
    }

    private void updatePomodoroData(final Collection<Pomodoro> pomodoroList) {
        // compute totals for activity time and break time
        long totalActivityMsec = 0L;
        long totalBreakMsec = 0L;
        int activityCount = 0;
        int breakCount = 0;
        for (Pomodoro p : pomodoroList) {
            if (p.getBreakDt() != null) {
                activityCount++;
                totalActivityMsec += p.getBreakDt().getTime() - p.getStartDt().getTime();
                if (p.getEndDt() != null) {
                    if (p.getEndDt() != p.getBreakDt()) {
                        breakCount++;
                    }
                    totalBreakMsec += p.getEndDt().getTime() - p.getBreakDt().getTime();
                }
            }
        }
        if (totalActivityMsec > 0L) {
            long secActivity = totalActivityMsec / 1000L;
            long minActivity = secActivity / 60L;
            long hrActivity = minActivity / 60L;
            minActivity = minActivity % 60L;

            final String totalActFmt = String.format("%d h %02d m", hrActivity, minActivity);
            this.totalActivity.setText(getString(R.string.label_activity_total, totalActFmt));

            int avgMinActivity = Math.round((float)secActivity / (float)activityCount) / 60;
            int avgHrActivity = avgMinActivity / 60;
            avgMinActivity = avgMinActivity % 60;
            final String avgActFmt = String.format("%d h %02d m", avgHrActivity, avgMinActivity);
            this.averageActivity.setText(getString(R.string.label_activity_average, avgActFmt));
        } else {
            this.totalActivity.setText(getString(R.string.label_activity_total, "*"));
            this.averageActivity.setText(getString(R.string.label_activity_average, "*"));
        }

        if (totalBreakMsec > 0L && breakCount > 0) {
            long secBreak = totalBreakMsec / 1000L;
            long minBreak = secBreak / 60L;
            long hrBreak = minBreak / 60L;
            minBreak = minBreak % 60L;

            int avgMinBreak = Math.round((float)secBreak / (float)breakCount) / 60;
            int avgHrBreak = avgMinBreak / 60;
            avgMinBreak = avgMinBreak % 60;
            final String avgBreakFmt = String.format("%d h %02d m", avgHrBreak, avgMinBreak);
            this.averageBreak.setText(getString(R.string.label_break_average, avgBreakFmt));
        } else {
            this.averageBreak.setText(getString(R.string.label_break_average, "*"));
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_event_title)
                .setMessage(R.string.confirm_delete_event_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.confirm_delete_event_positive, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteAndReturn();
                    }})
                .setNegativeButton(R.string.confirm_delete_event_negative, null).show();
    }

    private void deleteAndReturn() {
        database.deleteEvent(this.event).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    NavUtils.navigateUpFromSameTask(EventSummaryActivity.this);
                } else {
                    Snackbar.make(findViewById(R.id.main_layout),
                            "Failed to delete event", Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
}
