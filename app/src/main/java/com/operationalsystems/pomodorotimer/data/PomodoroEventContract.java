package com.operationalsystems.pomodorotimer.data;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

/**
 * Contract for querying pomodoro events, timers, teams, and members
 */

public class PomodoroEventContract {
    public static final String CONTENT_AUTHORITY = "com.operationalsystems.pomodorotimer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

}
