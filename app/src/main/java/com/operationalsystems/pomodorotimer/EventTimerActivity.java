package com.operationalsystems.pomodorotimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.Pomodoro;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;

import java.util.Date;

public class EventTimerActivity extends AppCompatActivity {
    private static final String LOG_TAG = "EventTimerActivity";

    private static class TimerData {
        String formattedText;
        boolean overtime;
    }

    private enum ActivityState {
        UNINITIALIZED,
        BREAK,
        ACTIVITY,
        INTERMISSION,
        WAITING
    }

    private class AlarmReceiver implements Runnable {

        @Override
        public void run() {
            if (alarmer == this && soundAlarm) {
                Log.d(LOG_TAG, "Notification firing");
                playNotification();
                cancelAlarm();
            }
        }
    }

    private class UIUpdater implements Runnable {
        Handler updateHandler;
        boolean active;

        @Override
        public void run() {
            // check if this timer is still the updater
            if (updater == this) {
                updateTimerUI();
            }
            if (active) {
                updateHandler.postDelayed(this, 1000);
            }
        }
    }

    // activity state information
    private int eventId;
    private Event currentEvent;
    private Pomodoro currentPomodoro;
    private ActivityState state;
    private boolean soundAlarm;

    // UI elements
    Toolbar toolbar;
    TextView pomodoroName;
    TextView timerCount;
    TextView currentStatus;
    Button toggleState;
    Button intermission;
    Button endEvent;

    // timer management
    Handler timerHandler;
    UIUpdater updater;
    Handler alarmHandler;
    AlarmReceiver alarmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_timer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pomodoroName = (TextView) findViewById(R.id.text_pomodoro_name);
        timerCount = (TextView) findViewById(R.id.text_timer_count);
        currentStatus = (TextView) findViewById(R.id.text_current_status);
        toggleState = (Button) findViewById(R.id.button_toggle_state);
        intermission = (Button) findViewById(R.id.button_intermission);
        endEvent = (Button) findViewById(R.id.button_end_event);

        toggleState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePomodoro();
            }
        });

        intermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIntermission();
            }
        });

        endEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeEvent();
            }
        });

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else {
            this.eventId = getIntent().getIntExtra(EventListActivity.EXTRA_EVENT_ID, -1);
            initializeEventState();
        }
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.soundAlarm = preferences.getBoolean(getString(R.string.pref_sound_alarm_key), true);
    }

    private void updateState(ActivityState newState) {
        if (this.state != newState) {
            this.state = newState;
            this.toolbar.setTitle(currentEvent.getName());
            if (currentPomodoro == null) {
                pomodoroName.setText(R.string.waiting_for_pomodoro);
            } else {
                pomodoroName.setText(currentPomodoro.getName());
            }
            if (state == ActivityState.WAITING) {
                this.intermission.setEnabled(false);
                this.toggleState.setEnabled(true);
                this.toggleState.setText(R.string.next_pomodoro_btn);
                this.endEvent.setEnabled(true);
                this.currentStatus.setText(R.string.activity_status_waiting);
                this.currentStatus.setTextColor(ContextCompat.getColor(this, R.color.waitingColor));
                this.timerCount.setText(R.string.blank_timer);
                this.timerCount.setTextColor(ContextCompat.getColor(this, R.color.waitingTimerColor));
            } else if (state == ActivityState.ACTIVITY) {
                this.intermission.setEnabled(true);
                this.toggleState.setEnabled(true);
                this.toggleState.setText(R.string.start_break_btn);
                this.endEvent.setEnabled(true);
                this.currentStatus.setText(R.string.activity_status_activity);
                this.timerCount.setTextColor(ContextCompat.getColor(this, R.color.activeTimerColor));
            } else if (state == ActivityState.INTERMISSION) {
                this.intermission.setEnabled(false);
                this.toggleState.setEnabled(true);
                this.toggleState.setText(R.string.next_pomodoro_btn);
                this.endEvent.setEnabled(true);
                this.currentStatus.setText(R.string.activity_status_intermission);
                this.currentStatus.setTextColor(ContextCompat.getColor(this, R.color.waitingColor));
                this.timerCount.setTextColor(ContextCompat.getColor(this, R.color.waitingTimerColor));
            } else if (state == ActivityState.BREAK) {
                this.intermission.setEnabled(true);
                this.toggleState.setEnabled(true);
                this.toggleState.setText(R.string.next_pomodoro_btn);
                this.endEvent.setEnabled(true);
                this.currentStatus.setText(R.string.activity_status_break);
                this.timerCount.setTextColor(ContextCompat.getColor(this, R.color.breakTimerColor));
            }

            updateTimerUI();
        }
    }
    private void updateTimerUI() {
        if (state == ActivityState.WAITING) {
        } else if (state == ActivityState.ACTIVITY) {
            TimerData timerInfo = computeTimer(currentPomodoro.getTimerMinutes(), currentPomodoro.getStartDt(), new Date());
            this.timerCount.setText(timerInfo.formattedText);
            if (timerInfo.overtime) {
                this.currentStatus.setTextColor(ContextCompat.getColor(this, R.color.activityColorOvertime));
            } else {
                this.currentStatus.setTextColor(ContextCompat.getColor(this, R.color.activityColor));
            }
        } else if (state == ActivityState.INTERMISSION) {
            // for now in intermission just leave the timer text at whatever it was when intermission started
            TimerData timerInfo = computeTimer(Integer.MAX_VALUE, currentPomodoro.getEndDt(), new Date());
            this.timerCount.setText(timerInfo.formattedText);
        } else if (state == ActivityState.BREAK) {
            TimerData timerInfo = computeTimer(currentPomodoro.getBreakMinutes(), currentPomodoro.getBreakDt(), new Date());
            this.timerCount.setText(timerInfo.formattedText);
            if (timerInfo.overtime) {
                this.currentStatus.setTextColor(ContextCompat.getColor(this, R.color.breakColorOvertime));
            } else {
                this.currentStatus.setTextColor(ContextCompat.getColor(this, R.color.breakColor));
            }
        }
    }

    private void closeEvent() {
        Date closeTime = new Date();
        if (currentPomodoro != null) {
            if (currentPomodoro.getEndDt() == null) {
                currentPomodoro.setEndDt(closeTime);
            }
            if (currentPomodoro.getBreakDt() == null) {
                currentPomodoro.setBreakDt(closeTime);
            }
            currentPomodoro.setActive(false);
            updatePomodoro();
        }
        currentEvent.setActive(false);
        currentEvent.setEndDt(closeTime);
        Intent summaryIntent = new Intent(this, EventSummaryActivity.class);
        summaryIntent.putExtra(EventSummaryActivity.EXTRA_EVENT_ID, currentEvent.getId());
        updateEvent(summaryIntent);
    }

    /*
     * Formats the timer tex
     * @return True if the timer is
     */
    private TimerData computeTimer(int timeLimitMinutes, Date start, Date end) {
        TimerData result = new TimerData();
        result.overtime = false;
        long timeDiff = end.getTime() - start.getTime();
        if (timeDiff <= 0) {
            result.formattedText = "0:00";
        } else {
            int minutes = (int) (timeDiff / (60 * 1000));
            int seconds = (int) (timeDiff % (60 * 1000)) / 1000;
            result.overtime = (minutes >= timeLimitMinutes);
            int hours = minutes / 60;
            minutes = minutes % 60;
            if (hours > 0) {
                result.formattedText = String.format("%d:%02d:%02d", hours, minutes, seconds);
            } else {
                result.formattedText = String.format("%d:%02d", minutes, seconds);
            }
        }
        return result;
    }

    private void playNotification() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (alert != null) {
            Ringtone ring = RingtoneManager.getRingtone(this, alert);
            if (ring != null && !ring.isPlaying()) {
                ring.play();
            }
        }
    }
    private void startTimer() {
        // only call this on the UI thread.
        if (timerHandler == null) {
            timerHandler = new Handler();
        }
        updater = new UIUpdater();
        updater.updateHandler = timerHandler;
        updater.active = true;
        timerHandler.postDelayed(updater, 1000);
        setAlarm();
    }

    private void setAlarm() {
        cancelAlarm();
        if (currentPomodoro != null) {
            long alarmDuration = 0L;
            long currentTimeMillis = System.currentTimeMillis();
            switch (state) {
                case BREAK:
                    alarmDuration = (currentPomodoro.getBreakMinutes() * 60000L) - (currentTimeMillis - currentPomodoro.getBreakDt().getTime());
                    break;
                case ACTIVITY:
                    alarmDuration = (currentPomodoro.getTimerMinutes() * 60000L) - (currentTimeMillis - currentPomodoro.getStartDt().getTime());
                    break;
                default:
            }

            if (alarmDuration > 0L) {
                alarmer = new AlarmReceiver();
                alarmHandler = new Handler();
                Log.d(LOG_TAG, "Setting alarm for " + alarmDuration + "msec");
                alarmHandler.postDelayed(alarmer, alarmDuration);
            }
        }
    }

    private void cancelAlarm() {
        if (alarmHandler != null) {
            final AlarmReceiver rcvr = alarmer;
            alarmer = null;
            alarmHandler.removeCallbacks(rcvr);
        }
    }

    private void stopTimer() {
        // only call this on the UI thread
        if (updater != null) {
            // will cause the updater to no longer resubmit itself.
            updater.active = false;
            timerHandler.removeCallbacks(updater);
            updater = null;
        }
    }

    private void togglePomodoro() {
        Date toggleDate = new Date();
        if (state == ActivityState.ACTIVITY) {
            stopTimer();
            // start a break;
            this.currentPomodoro.setBreakDt(toggleDate);
            updateState(ActivityState.BREAK);
            updatePomodoro();
            startTimer();
        } else if (state == ActivityState.BREAK
                || state == ActivityState.WAITING
                || state == ActivityState.INTERMISSION) {
            showNextPomodoro();
        }
    }

    private void showNextPomodoro() {
        Bundle args = new Bundle();
        int nextSequence = 1;
        if (currentPomodoro != null) {
            nextSequence = currentPomodoro.getSequence() + 1;
        }
        String name = getString(R.string.default_pomodoro_name, nextSequence);
        args.putString(CreatePomodoroDlgFragment.BUNDLE_KEY_EVENTNAME, this.currentEvent.getName());
        args.putString(CreatePomodoroDlgFragment.BUNDLE_KEY_POMODORONAME, name);
        args.putInt(CreatePomodoroDlgFragment.BUNDLE_KEY_ACTIVITY_LENGTH, this.currentEvent.getActivityMinutes());
        args.putInt(CreatePomodoroDlgFragment.BUNDLE_KEY_BREAK_LENGTH, this.currentEvent.getBreakMinutes());
        CreatePomodoroDlgFragment fragment = new CreatePomodoroDlgFragment();
        fragment.setArguments(args);
        fragment.setListener(new CreatePomodoroDlgFragment.CreatePomodoroListener() {
            @Override
            public void doCreatePomodoro(CreatePomodoroDlgFragment.CreatePomodoroParams params) {
                EventTimerActivity.this.startNewPomodoro(params.pomodoroName, params.activityMinutes, params.breakMinutes);
            }
        });
        fragment.show(getFragmentManager(), "CreateEventDlgFragment");
    }

    // invoke when new pomo dialog is accepted
    private void startNewPomodoro(String name, int activityMinutes, int breakMinutes) {
        stopTimer();
        Date startDate = new Date();
        int sequence = 1;
        if (currentPomodoro != null) {
            currentPomodoro.setActive(false);
            if (currentPomodoro.getEndDt() == null) {
                currentPomodoro.setEndDt(startDate);
            }
            if (currentPomodoro.getBreakDt() == null) {
                currentPomodoro.setBreakDt(startDate);
            }
            sequence = currentPomodoro.getSequence() + 1;
            updatePomodoro();
        }
        currentPomodoro = new Pomodoro(currentEvent.getId(), name, sequence, activityMinutes, breakMinutes, new Date());
        insertPomodoro();
        updateState(ActivityState.ACTIVITY);
        startTimer();
    }

    private void startIntermission() {
        Date startTime = new Date();
        // state must be ACTIVITY or BREAK
        // currentPomodoro must be non-null
        if (state == ActivityState.ACTIVITY) {
            currentPomodoro.setBreakDt(startTime);
            currentPomodoro.setEndDt(startTime);
            currentPomodoro.setActive(false);
        } else {
            currentPomodoro.setEndDt(startTime);
            currentPomodoro.setActive(false);
        }
        stopTimer();
        updateState(ActivityState.INTERMISSION);
        updatePomodoro();
        startTimer();
    }

    private void initializeEventState() {
        this.state = ActivityState.UNINITIALIZED;
        if (this.eventId >= 0) {
            AsyncTask<Void, Void, ActivityState> loadEventTask = new AsyncTask<Void, Void, ActivityState>() {
                @Override
                protected ActivityState doInBackground(Void... params) {
                    Uri queryOneEvent = PomodoroEventContract.Event.uriForEventId(eventId);
                    ActivityState returnState = ActivityState.WAITING;
                    try (Cursor cursor = getContentResolver().query(queryOneEvent, PomodoroEventContract.Event.EVENT_COLS, null, null, null)) {
                        if (cursor != null && cursor.moveToNext()) {
                            currentEvent = new Event(cursor);
                            Uri queryEventTimers = PomodoroEventContract.Timer.uriForEventTimers(eventId);
                            try (Cursor timerCursor = getContentResolver().query(queryEventTimers, PomodoroEventContract.Timer.TIMER_COLS, null, null,
                                    PomodoroEventContract.Timer.POMODORO_SEQ_COL)) {
                                if (timerCursor != null && timerCursor.moveToLast()) {
                                    currentPomodoro = new Pomodoro(timerCursor);
                                    if (currentPomodoro.getStartDt() == null) {
                                        // strange state where a pomodoro is created without a start time
                                        returnState = ActivityState.INTERMISSION;
                                    } else if (currentPomodoro.getBreakDt() == null) {
                                        returnState = ActivityState.ACTIVITY;
                                    } else if (currentPomodoro.getEndDt() == null) {
                                        returnState = ActivityState.BREAK;
                                    } else {
                                        // current pomodoro ended but new one not started
                                        returnState = ActivityState.INTERMISSION;
                                    }
                                } else {
                                    currentPomodoro = null;
                                    returnState = ActivityState.WAITING;
                                }
                            }
                        }
                    }
                    return returnState;
                }

                @Override
                protected void onPostExecute(ActivityState value) {
                    updateState(value);
                    // if the state is activity or break, start the timer as the pomodoro is already running
                    if (state == ActivityState.ACTIVITY || state == ActivityState.BREAK || state == ActivityState.INTERMISSION) {
                        startTimer();
                    }
                }
            };

            loadEventTask.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void updatePomodoro() {
        AsyncTask<Pomodoro, Void, String> updateTask = new AsyncTask<Pomodoro, Void, String>() {
            @Override
            protected String doInBackground(Pomodoro... params) {
                Pomodoro pomo = params[0];
                ContentValues values = pomo.asContent();
                Uri updatePomodoro = PomodoroEventContract.Timer.uriForInstance(pomo.getId());
                getContentResolver().update(updatePomodoro, values, null, null);
                return null;
            }
        };

        updateTask.execute(currentPomodoro);
    }

    private void updateEvent(final Intent after) {
        AsyncTask<Event, Void, Boolean> updateTask = new AsyncTask<Event, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Event... params) {
                Event ev = params[0];
                ContentValues values = ev.asContent();
                Log.d(LOG_TAG, values.toString());
                Uri updateEvent = PomodoroEventContract.Event.uriForEventId(ev.getId());
                int updated = getContentResolver().update(updateEvent, values, null, null);
                return Boolean.valueOf(updated == 1);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    if (after != null) {
                        startActivity(after);
                    }
                } else {
                    Log.d(LOG_TAG, "Event update failed");
                }
            }
        };

        final Event current = this.currentEvent;
        Log.d(LOG_TAG, "Launcing event update");
        updateTask.execute(current);
    }

    private void insertPomodoro() {
        AsyncTask<Pomodoro, Void, String> updateTask = new AsyncTask<Pomodoro, Void, String>() {
            @Override
            protected String doInBackground(Pomodoro... params) {
                Pomodoro pomo = params[0];
                ContentValues values = pomo.asContent();
                Uri insertPomodoro = PomodoroEventContract.Timer.TIMER_URI;
                Uri inserted = getContentResolver().insert(insertPomodoro, pomo.asContent());
                pomo.setId(PomodoroEventContract.Timer.idFromInstanceUri(inserted));
                return null;
            }
        };

        updateTask.execute(currentPomodoro);
    }
}
