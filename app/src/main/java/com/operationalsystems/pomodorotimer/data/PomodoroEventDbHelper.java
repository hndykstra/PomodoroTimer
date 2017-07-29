package com.operationalsystems.pomodorotimer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Hans on 6/27/2017.
 */

public class PomodoroEventDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = "PomodoroEventDb";
    private static final String DB_NAME = "pomodoroEvent.db";
    private static final int DATABASE_VERSION = 2;

    public PomodoroEventDbHelper(Context context) { super(context, DB_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String dropEventSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.Event.TABLE + ";";
        final String dropEventMemberSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.EventMember.TABLE + ";";
        final String dropTimerSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.Timer.TABLE + ";";
        final String dropTeamSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.TeamDomain.TABLE + ";";
        final String dropTeamMemberSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.TeamMember.TABLE + ";";
        db.execSQL(dropTeamMemberSQL);
        db.execSQL(dropTeamSQL);
        db.execSQL(dropEventMemberSQL);
        db.execSQL(dropTimerSQL);
        db.execSQL(dropEventSQL);

        final String createTeamSQL = "CREATE TABLE " + PomodoroEventContract.TeamDomain.TABLE + " (" +
                PomodoroEventContract.TeamDomain.DOMAIN_COL + " TEXT PRIMARY KEY, " +
                PomodoroEventContract.TeamDomain.OWNER_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.TeamDomain.CREATED_DT_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.TeamDomain.ACTIVE_COL + " INTEGER NOT NULL DEFAULT 1);";

        final String createTeamMemberSQL = "CREATE TABLE " + PomodoroEventContract.TeamMember.TABLE + " (" +
                PomodoroEventContract.TeamMember.ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PomodoroEventContract.TeamMember.DOMAIN_FK_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.TeamMember.MEMBER_UID_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.TeamMember.ROLE_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.TeamMember.ADDED_DT_COL + " TEXT, " +
                PomodoroEventContract.TeamMember.ADDED_BY_COL + " TEXT, " +
                "FOREIGN KEY (" + PomodoroEventContract.TeamMember.DOMAIN_FK_COL + ") REFERENCES " +
                PomodoroEventContract.TeamDomain.TABLE + " (" + PomodoroEventContract.TeamDomain.DOMAIN_COL + ") ON DELETE CASCADE, " +
                "UNIQUE (" + PomodoroEventContract.TeamMember.DOMAIN_FK_COL + ", " + PomodoroEventContract.TeamMember.MEMBER_UID_COL + ") ON CONFLICT ABORT);";

        final String createEventSQL = "CREATE TABLE " + PomodoroEventContract.Event.TABLE + " (" +
                PomodoroEventContract.Event.ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PomodoroEventContract.Event.EVENT_NAME_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.Event.OWNER_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.Event.ACTIVE_COL + " INTEGER NOT NULL DEFAULT 1, " +
                PomodoroEventContract.Event.EVENT_TIMER_MINUTES_COL + " INTEGER NOT NULL DEFAULT 25, " +
                PomodoroEventContract.Event.EVENT_BREAK_MINUTES_COL + " INTEGER NOT NULL DEFAULT 5, " +
                PomodoroEventContract.Event.TEAM_DOMAIN_COL + " TEXT, " +
                PomodoroEventContract.Event.START_DT_COL + " TEXT, " +
                PomodoroEventContract.Event.END_DT_COL + " TEXT, " +
                "FOREIGN KEY (" + PomodoroEventContract.Event.TEAM_DOMAIN_COL + ") REFERENCES " +
                PomodoroEventContract.TeamDomain.TABLE + " (" + PomodoroEventContract.TeamDomain.DOMAIN_COL + ") ON DELETE CASCADE, " +
                "UNIQUE (" + PomodoroEventContract.Event.EVENT_NAME_COL + ", " + PomodoroEventContract.Event.TEAM_DOMAIN_COL + ") ON CONFLICT ABORT);";

        final String createTimerSQL = "CREATE TABLE " + PomodoroEventContract.Timer.TABLE + " (" +
                PomodoroEventContract.Timer.ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PomodoroEventContract.Timer.EVENT_FK_COL + " INTEGER NOT NULL, " +
                PomodoroEventContract.Timer.POMODORO_NAME_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.Timer.POMODORO_SEQ_COL + " INTEGER NOT NULL, " +
                PomodoroEventContract.Timer.ACTIVE_COL + " INTEGER NOT NULL DEFAULT 0, " +
                PomodoroEventContract.Timer.TIMER_MINUTES_COL + " INTEGER NOT NULL, " +
                PomodoroEventContract.Timer.BREAK_MINUTES_COL + " INTEGER NOT NULL, " +
                PomodoroEventContract.Timer.START_DT_COL + " TEXT, " +
                PomodoroEventContract.Timer.BREAK_DT_COL + " TEXT, " +
                PomodoroEventContract.Timer.END_DT_COL + " TEXT, " +
                " FOREIGN KEY (" + PomodoroEventContract.Timer.EVENT_FK_COL + ") REFERENCES " +
                PomodoroEventContract.Event.TABLE + "(" + PomodoroEventContract.Event.ID_COL + ") ON DELETE CASCADE, " +
                "UNIQUE (" + PomodoroEventContract.Timer.EVENT_FK_COL + ", " + PomodoroEventContract.Timer.POMODORO_SEQ_COL + ") ON CONFLICT ABORT);";

        final String createMemberSQL = "CREATE TABLE " + PomodoroEventContract.EventMember.TABLE + "(" +
                PomodoroEventContract.EventMember.ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PomodoroEventContract.EventMember.EVENT_FK_COL + " INTEGER NOT NULL, " +
                PomodoroEventContract.EventMember.MEMBER_UID_COL + " TEXT NOT NULL, " +
                PomodoroEventContract.EventMember.JOIN_DT_COL + " TEXT, " +
                " FOREIGN KEY (" + PomodoroEventContract.EventMember.EVENT_FK_COL + ") REFERENCES " +
                PomodoroEventContract.Event.TABLE +"(" + PomodoroEventContract.Event.ID_COL + ") ON DELETE CASCADE, "+
                "UNIQUE (" + PomodoroEventContract.EventMember.EVENT_FK_COL + ", " + PomodoroEventContract.EventMember.MEMBER_UID_COL + ") ON CONFLICT ABORT);";

        try {
            db.execSQL(createTeamSQL);
            db.execSQL(createTeamMemberSQL);
            db.execSQL(createEventSQL);
            db.execSQL(createTimerSQL);
            db.execSQL(createMemberSQL);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to create tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no upgrade supported right now
        final String dropEventSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.Event.TABLE + ";";
        final String dropEventMemberSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.EventMember.TABLE + ";";
        final String dropTimerSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.Timer.TABLE + ";";
        final String dropTeamSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.TeamDomain.TABLE + ";";
        final String dropTeamMemberSQL = "DROP TABLE IF EXISTS " + PomodoroEventContract.TeamMember.TABLE + ";";
        db.execSQL(dropTeamMemberSQL);
        db.execSQL(dropTeamSQL);
        db.execSQL(dropEventMemberSQL);
        db.execSQL(dropTimerSQL);
        db.execSQL(dropEventSQL);
        onCreate(db);
    }
}
