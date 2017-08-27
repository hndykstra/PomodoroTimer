package com.operationalsystems.pomodorotimer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for SQLiteDatabase instance
 */

public class PomodoroEventDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = "PomodoroEventDb";
    private static final String DB_NAME = "pomodoroEvent.db";
    private static final int DATABASE_VERSION = 2;

    public PomodoroEventDbHelper(Context context) { super(context, DB_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
