package com.operationalsystems.pomodorotimer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.Pomodoro;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.User;
import com.operationalsystems.pomodorotimer.util.Promise;

/**
 * Implementation of App Widget functionality.
 */
public class PomodoroWidget extends AppWidgetProvider {

    static class FirebaseLookup implements FirebaseAuth.AuthStateListener {

        private Context context;
        private AppWidgetManager widgetManager;
        private int[] widgetIds;


        FirebaseLookup(Context context, AppWidgetManager widgetManager, int[] widgetIds) {
            this.context = context;
            this.widgetManager = widgetManager;
            this.widgetIds = widgetIds;
        }

        public void doLookup() {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.addAuthStateListener(this);
        }

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() != null) {
                queryCurrent(firebaseAuth.getCurrentUser());
            } else {
                updateNotAvailable();
            }
            firebaseAuth.removeAuthStateListener(this);
        }

        private void queryCurrent(FirebaseUser user) {
            final String uid = user.getUid();
            final PomodoroFirebaseHelper database = new PomodoroFirebaseHelper();

            database.queryUser(uid).then(new Promise.PromiseReceiver() {
                @Override
                public Object receive(Object t) {
                    User u = (User)t;
                    if (u.getRecentEvent() == null || u.getRecentEvent().length() == 0) {
                        return null;
                    } else {
                        return database.queryEvent(u.getRecentEvent(), u.getRecentTeam(), u.getUid());
                    }
                }
            }).then(new Promise.PromiseReceiver() {
                @Override
                public Object receive(Object t) {
                    Event event = (Event)t;
                    updateEvent(event);
                    return t;
                }
            });
        }

        private void updateNotAvailable() {
            String notAvailable = context.getString(R.string.widget_not_available);
            for (int i=0 ; i < widgetIds.length ; ++i) {
                updateAppWidget(context, widgetManager, widgetIds[i], notAvailable, "", null);
            }
        }

        private void updateEvent(Event e) {
            String mainText = e == null ? "" : e.getName();
            String status = "";
            if (e != null && e.isActive()) {
                Pomodoro current = e.getCurrentPomodoro();
                if (current == null) {
                    status = context.getString(R.string.widget_status_waiting);
                } else if (current.getEndDt() != null) {
                    status = context.getString(R.string.widget_status_intermission);
                } else if (current.getBreakDt() != null) {
                    status = context.getString(R.string.widget_status_break);
                } else {
                    status = context.getString(R.string.widget_status_activity);
                }
            } else {
                mainText = context.getString((R.string.widget_none_active));
            }
            for (int i=0 ; i < widgetIds.length ; ++i) {
                updateAppWidget(context, widgetManager, widgetIds[i], mainText, status, e == null ? null : e.getKey());
            }
        }
    }

    static Intent broadcastUodate(Context fromContext) {
        Intent intent = new Intent(fromContext, PomodoroWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(fromContext).getAppWidgetIds(new ComponentName(fromContext, PomodoroWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        return intent;
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String mainText, String statusText, String eventKey) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pomodoro_widget);
        views.setTextViewText(R.id.widget_main_text, mainText);
        views.setTextViewText(R.id.widget_status_text, statusText);

        Intent activityIntent = (eventKey == null || eventKey.length() ==0)
                ? new Intent(context, EventListActivity.class)
                : new Intent(context, EventTimerActivity.class);

        activityIntent.putExtra(EventListActivity.EXTRA_EVENT_ID, eventKey);

        PendingIntent intent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout, intent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        FirebaseLookup lookup = new FirebaseLookup(context, appWidgetManager, appWidgetIds);
        lookup.doLookup();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

