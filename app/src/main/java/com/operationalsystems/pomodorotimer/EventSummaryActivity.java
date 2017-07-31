package com.operationalsystems.pomodorotimer;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.EventMember;
import com.operationalsystems.pomodorotimer.data.Pomodoro;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;

import java.util.ArrayList;
import java.util.List;

public class EventSummaryActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_EVENT_ID = "SelectedEventId";

    private static final String LOG_TAG = "EventSummaryActivity";
    private static final int EVENT_MEMBER_LOADER_ID = 9047;
    private static final int POMODORO_LIST_LOADER_ID = 9048;
    private static final int EVENT_LOADER_ID = 9049;
    private int eventId;
    private Event event;
    private PomodoroListAdapter pomodoroAdapter;
    private EventMemberListAdapter memberAdapter;
    private Toolbar toolbar;
    private TextView totalTime;
    private TextView totalActivity;
    private TextView averageActivity;
    private TextView averageBreak;
    private RecyclerView pomodoroRecycler;
    private RecyclerView memberRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_summary);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        totalTime = (TextView) findViewById(R.id.label_time_total);
        totalActivity = (TextView) findViewById(R.id.label_activity_total);
        averageActivity = (TextView) findViewById(R.id.label_activity_average);
        averageBreak = (TextView) findViewById(R.id.label_break_average);

        pomodoroRecycler = (RecyclerView) findViewById(R.id.recycler_pomodoro_list);
        RecyclerView.LayoutManager lm1 = new GridLayoutManager(this, 1);
        pomodoroRecycler.setLayoutManager(lm1);
        pomodoroAdapter = new PomodoroListAdapter();
        pomodoroRecycler.setAdapter(pomodoroAdapter);

        memberRecycler = (RecyclerView) findViewById(R.id.recycler_members);
        RecyclerView.LayoutManager lm2 = new GridLayoutManager(this, 1);
        memberRecycler.setLayoutManager(lm2);
        memberAdapter = new EventMemberListAdapter();
        memberRecycler.setAdapter(memberAdapter);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else {
            this.eventId = getIntent().getIntExtra(EventListActivity.EXTRA_EVENT_ID, -1);
            initializeEventState();
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
        this.eventId = inState.getInt(EventListActivity.EXTRA_EVENT_ID);
        initializeEventState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(EventListActivity.EXTRA_EVENT_ID, this.eventId);
    }

    private void initializeEventState() {
        getSupportLoaderManager().initLoader(EVENT_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(EVENT_MEMBER_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(POMODORO_LIST_LOADER_ID, null, this);
    }

    private void updateEventData(final Event event) {
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
    }

    private void updatePomodoroData(final List<Pomodoro> pomodoroList) {
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
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                final Uri deleteUri = PomodoroEventContract.Event.uriForEventId(eventId);
                int deleted = getContentResolver().delete(deleteUri, null, null);
                return deleted == 1;
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                if (result) {
                    NavUtils.navigateUpFromSameTask(EventSummaryActivity.this);
                } else {
                    Snackbar.make(findViewById(R.id.main_layout),
                            "Failed to delete event", Snackbar.LENGTH_LONG)
                        .show();
                }
            }
        }.execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == EVENT_MEMBER_LOADER_ID) {
            final Uri queryUri = PomodoroEventContract.EventMember.uriForMembersByEvent(this.eventId);
            final CursorLoader loader = new CursorLoader(this, queryUri, PomodoroEventContract.EventMember.EVENTMEMBER_COLS, null, null,
                    PomodoroEventContract.EventMember.MEMBER_UID_COL);
            return loader;
        } else if (id == POMODORO_LIST_LOADER_ID) {
            final Uri queryUri = PomodoroEventContract.Timer.uriForEventTimers(this.eventId);
            final CursorLoader loader = new CursorLoader(this, queryUri, PomodoroEventContract.Timer.TIMER_COLS, null, null,
                    PomodoroEventContract.Timer.POMODORO_SEQ_COL);
            return loader;
        } else if (id == EVENT_LOADER_ID) {
            final Uri queryUri = PomodoroEventContract.Event.uriForEventId(this.eventId);
            final CursorLoader loader = new CursorLoader(this, queryUri, PomodoroEventContract.Event.EVENT_COLS, null, null, null);
            return loader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == EVENT_MEMBER_LOADER_ID) {
            final List<EventMember> memberList = new ArrayList<>();
            while (data.moveToNext()) {
                EventMember m = new EventMember(data);
                memberList.add(m);
            }
            memberAdapter.setEventMembers(memberList);
        } else if (loaderId == POMODORO_LIST_LOADER_ID) {
            final List<Pomodoro> pomodoroList = new ArrayList<>();
            while (data.moveToNext()) {
                Pomodoro p = new Pomodoro(data);
                pomodoroList.add(p);
            }
            pomodoroAdapter.setPomodoroList(pomodoroList);
            updatePomodoroData(pomodoroList);
        } else if (loaderId == EVENT_LOADER_ID) {
            if (data.moveToNext()) {
                updateEventData(new Event(data));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
