package com.operationalsystems.pomodorotimer.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Content provider for SQLiteDabase
 */

public class PomodoroEventProvider extends ContentProvider {

    private static final int EVENT = 100;
    private static final int EVENT_ACTIVE = 101;
    private static final int EVENT_BY_ID = 102;

    private static final int TIMER = 200;
    private static final int EVENT_TIMER_ACTIVE = 201;
    private static final int TIMER_FOR_EVENT = 202;
    private static final int TIMER_BY_ID = 203;

    private static final int EVENT_MEMBER = 300;
    private static final int EVENT_MEMBER_FOR_EVENT = 301;
    private static final int EVENT_MEMBER_BY_ID = 302;

    private static final int TEAM = 400;
    private static final int TEAM_BY_ID = 401;

    private static final int TEAM_MEMBER = 500;
    private static final int TEAM_MEMBER_FOR_TEAM = 501;
    private static final int TEAM_MEMBER_BY_ID = 502;

    private static final UriMatcher matcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PomodoroEventContract.CONTENT_AUTHORITY;
        return matcher;
    }

    private PomodoroEventDbHelper dbHelper = null;

    @Override
    public boolean onCreate() {
        dbHelper = new PomodoroEventDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor queryResult = null;
        return queryResult;
    }

    private Cursor queryMembersForEvent(SQLiteDatabase db, String eventId, String[] projection, String selector, String[] selectionArgs, String sort) {
        Cursor queryResult = null;
        return queryResult;
    }

    private Cursor queryMembersForTeam(SQLiteDatabase db, String teamId, String[] projection, String selector, String[] selectionArgs, String sort) {
        Cursor queryResult = null;
        return queryResult;
    }

    private Cursor queryTimersForEvent(SQLiteDatabase db, String eventId, String[] projection, String selector, String[] selectionArgs) {
        Cursor queryResult = null;
        return queryResult;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull final Uri uri, @Nullable final ContentValues values) {
        Uri inserted = null;
        return inserted;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int result = 0;
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int result = 0;
        return result;
    }
}
