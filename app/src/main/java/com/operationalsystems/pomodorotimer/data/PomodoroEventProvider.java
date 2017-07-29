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
        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT_ACTIVE, EVENT_ACTIVE);
        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT + "/#", EVENT_BY_ID);
        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT, EVENT);

        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT_TIMER_ACTIVE, EVENT_TIMER_ACTIVE);
        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT_TIMER, TIMER_FOR_EVENT);
        matcher.addURI(authority, PomodoroEventContract.PATH_TIMER + "/#", TIMER_BY_ID);
        matcher.addURI(authority, PomodoroEventContract.PATH_TIMER, TIMER);

        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT_MEMBER_EVENT, EVENT_MEMBER_FOR_EVENT);
        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT_MEMBER + "/#", EVENT_MEMBER_BY_ID);
        matcher.addURI(authority, PomodoroEventContract.PATH_EVENT_MEMBER_EVENT, EVENT_MEMBER_FOR_EVENT);

        matcher.addURI(authority, PomodoroEventContract.PATH_TEAM_MEMBER_TEAM, TEAM_MEMBER_FOR_TEAM);
        matcher.addURI(authority, PomodoroEventContract.PATH_TEAM_MEMBER + "/#", TEAM_MEMBER_BY_ID);
        matcher.addURI(authority, PomodoroEventContract.PATH_TEAM_MEMBER, TEAM_MEMBER);

        matcher.addURI(authority, PomodoroEventContract.PATH_TEAM_DOMAIN + "/#", TEAM_BY_ID);
        matcher.addURI(authority, PomodoroEventContract.PATH_TEAM_DOMAIN, TEAM);

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
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int code = matcher.match(uri);
        switch (code) {
            case EVENT:
                queryResult = db.query(PomodoroEventContract.Event.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EVENT_BY_ID:
                String[] paths = new String[] { uri.getLastPathSegment() };
                String selector = PomodoroEventContract.Event.ID_COL + "=?";
                queryResult = db.query(PomodoroEventContract.Event.TABLE, projection, selector, paths, null, null, sortOrder);
                break;
            case EVENT_ACTIVE:
                String activeSelector = PomodoroEventContract.Event.ACTIVE_COL + "=1";
                String activeSelection = activeSelector;
                if (selection != null) {
                    activeSelection = "(" + selection + ") AND "  + activeSelector;
                }
                queryResult = db.query(PomodoroEventContract.Event.TABLE, projection, activeSelection, selectionArgs, null, null, sortOrder);
                break;
            case TIMER:
                queryResult = db.query(PomodoroEventContract.Timer.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TIMER_BY_ID:
                String[] timerPaths = new String[] { uri.getLastPathSegment()};
                String timerSelector = PomodoroEventContract.Timer.ID_COL + "=?";
                queryResult = db.query(PomodoroEventContract.Timer.TABLE, projection, timerSelector, timerPaths, null, null, sortOrder);
                break;
            case TIMER_FOR_EVENT:
                List<String> eventTimerPaths = uri.getPathSegments();
                String eventIdArg = null;
                // should be event, id, timer so we need second to last
                if (eventTimerPaths.size() >= 2) {
                    eventIdArg = eventTimerPaths.get(eventTimerPaths.size()-2);
                } else {
                    throw new IllegalStateException("URI matching failed for " + eventTimerPaths);
                }
                queryResult = queryTimersForEvent(db, eventIdArg, projection, selection, selectionArgs);
                break;
            case EVENT_TIMER_ACTIVE:
                List<String> activeTimerPaths = uri.getPathSegments();
                String activeIdArg = null;
                // should be event, id, timer, active so we need third to last
                if (activeTimerPaths.size() >= 3) {
                    eventIdArg = activeTimerPaths.get(activeTimerPaths.size()-3);
                } else {
                    throw new IllegalStateException("URI matching failed for " + activeTimerPaths);
                }
                final String activeTimerSelector = PomodoroEventContract.Timer.ACTIVE_COL + "=1";
                queryResult = queryTimersForEvent(db, eventIdArg, projection, activeTimerSelector, null);
                break;
            case EVENT_MEMBER:
                queryResult = db.query(PomodoroEventContract.EventMember.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EVENT_MEMBER_BY_ID:
                final String[] eventMemberIdArg = new String[] { uri.getLastPathSegment() };
                final String eventMemberIdSelector = PomodoroEventContract.EventMember.ID_COL + "=?";
                queryResult = db.query(PomodoroEventContract.EventMember.TABLE, projection, eventMemberIdSelector, eventMemberIdArg, null, null, sortOrder);
                break;
            case EVENT_MEMBER_FOR_EVENT:
                final List<String> eventMemberPaths = uri.getPathSegments();
                String eventMemEventIdArg = null;
                // should be event, id, eventMember so we need second to last
                if (eventMemberPaths.size() >= 2) {
                    eventMemEventIdArg = eventMemberPaths.get(eventMemberPaths.size()-2);
                } else {
                    throw new IllegalStateException("URI matching failed for " + eventMemberPaths);
                }
                queryResult = queryMembersForEvent(db, eventMemEventIdArg, projection, selection, selectionArgs, sortOrder);
                break;
            case TEAM:
                queryResult = db.query(PomodoroEventContract.TeamDomain.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TEAM_BY_ID:
                final String[] teamIdArg = new String[] { uri.getLastPathSegment() };
                final String teamIdSelector = PomodoroEventContract.TeamDomain.DOMAIN_COL + "=?";
                queryResult = db.query(PomodoroEventContract.TeamDomain.TABLE, projection, teamIdSelector, teamIdArg, null, null, sortOrder);
                break;
            case TEAM_MEMBER_FOR_TEAM:
                final List<String> teamMemberPaths = uri.getPathSegments();
                String teamMemTeamIdArg = null;
                // should be event, id, eventMember so we need second to last
                if (teamMemberPaths.size() >= 2) {
                    teamMemTeamIdArg = teamMemberPaths.get(teamMemberPaths.size()-2);
                } else {
                    throw new IllegalStateException("URI matching failed for " + teamMemberPaths);
                }
                queryResult = queryMembersForTeam(db, teamMemTeamIdArg, projection, selection, selectionArgs, sortOrder);
                break;
            case TEAM_MEMBER_BY_ID:
                final String[] teamMemberIdArg = new String[] { uri.getLastPathSegment() };
                final String teamMemberIdSelector = PomodoroEventContract.TeamMember.ID_COL + "=?";
                queryResult = db.query(PomodoroEventContract.TeamMember.TABLE, projection, teamMemberIdSelector, teamMemberIdArg, null, null, sortOrder);
                break;
            case TEAM_MEMBER:
                queryResult = db.query(PomodoroEventContract.TeamMember.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI type " + uri.toString());
        }
        return queryResult;
    }

    private Cursor queryMembersForEvent(SQLiteDatabase db, String eventId, String[] projection, String selector, String[] selectionArgs, String sort) {
        Cursor queryResult = null;
        String memberForEventSelector = PomodoroEventContract.EventMember.EVENT_FK_COL + "=?";
        String[] eventIdArg = new String[] { eventId };
        if (selector != null) {
            memberForEventSelector = "(" + selector + ") AND " + memberForEventSelector;
            if (selectionArgs != null) {
                String[] tmp = new String[selectionArgs.length + eventIdArg.length];
                System.arraycopy(selectionArgs, 0, tmp, 0, selectionArgs.length);
                System.arraycopy(eventIdArg, 0, tmp, selectionArgs.length, eventIdArg.length);
                eventIdArg = tmp;
            }
        }

        queryResult = db.query(PomodoroEventContract.EventMember.TABLE, projection, memberForEventSelector, eventIdArg, null, null, sort);
        return queryResult;
    }

    private Cursor queryMembersForTeam(SQLiteDatabase db, String teamId, String[] projection, String selector, String[] selectionArgs, String sort) {
        Cursor queryResult = null;
        String memberForTeamSelector = PomodoroEventContract.TeamMember.DOMAIN_FK_COL + "=?";
        String[] teamIdArg = new String[] { teamId };
        if (selector != null) {
            memberForTeamSelector = "(" + selector + ") AND " + memberForTeamSelector;
            if (selectionArgs != null) {
                String[] tmp = new String[selectionArgs.length + teamIdArg.length];
                System.arraycopy(selectionArgs, 0, tmp, 0, selectionArgs.length);
                System.arraycopy(teamIdArg, 0, tmp, selectionArgs.length, teamIdArg.length);
                teamIdArg = tmp;
            }
        }

        queryResult = db.query(PomodoroEventContract.EventMember.TABLE, projection, memberForTeamSelector, teamIdArg, null, null, sort);
        return queryResult;
    }

    private Cursor queryTimersForEvent(SQLiteDatabase db, String eventId, String[] projection, String selector, String[] selectionArgs) {
        Cursor queryResult = null;
        String timerForEventSelector = PomodoroEventContract.Timer.EVENT_FK_COL + "=?";
        String[] eventIdArg = new String[] { eventId };
        if (selector != null) {
            timerForEventSelector = "(" + selector + ") AND " + timerForEventSelector;
            if (selectionArgs != null) {
                String[] tmp = new String[selectionArgs.length + eventIdArg.length];
                System.arraycopy(selectionArgs, 0, tmp, 0, selectionArgs.length);
                System.arraycopy(eventIdArg, 0, tmp, selectionArgs.length, eventIdArg.length);
                eventIdArg = tmp;
            }
        }
        final String sort = PomodoroEventContract.Timer.POMODORO_SEQ_COL;
        queryResult = db.query(PomodoroEventContract.Timer.TABLE, projection, timerForEventSelector, eventIdArg, null, null, sort);
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
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int code = matcher.match(uri);
        long id = 0L;
        switch(code) {
            case EVENT:
                id = db.insert(PomodoroEventContract.Event.TABLE, null, values);
                inserted = PomodoroEventContract.Event.uriForEventId((int)id);
                break;
            case EVENT_MEMBER:
                id = db.insert(PomodoroEventContract.EventMember.TABLE, null, values);
                inserted = PomodoroEventContract.EventMember.uriForInstance((int) id);
                break;
            case TIMER:
                id = db.insert(PomodoroEventContract.Timer.TABLE, null, values);
                inserted = PomodoroEventContract.Timer.uriForInstance((int)id);
                break;
            case TEAM:
                String domainName = values.getAsString(PomodoroEventContract.TeamDomain.DOMAIN_COL);
                id = db.insert(PomodoroEventContract.TeamDomain.TABLE, null, values);
                inserted = PomodoroEventContract.TeamDomain.uriForInstance(domainName);
                break;
            case TEAM_MEMBER:
                id = db.insert(PomodoroEventContract.TeamMember.TABLE, null, values);
                inserted = PomodoroEventContract.TeamMember.uriForInstance((int)id);
            default:
                throw new UnsupportedOperationException("Unknown URI type " + uri.toString());
        }
        return inserted;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int result = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int code = matcher.match(uri);
        switch (code) {
            case EVENT:
                result = db.delete(PomodoroEventContract.Event.TABLE, selection, selectionArgs);
                break;
            case EVENT_BY_ID:
                result = db.delete(PomodoroEventContract.Event.TABLE, PomodoroEventContract.Event.ID_COL + "=?",
                        new String[] {uri.getLastPathSegment()});
                break;
            case TIMER:
                result = db.delete(PomodoroEventContract.Timer.TABLE, selection, selectionArgs);
                break;
            case TIMER_BY_ID:
                result = db.delete(PomodoroEventContract.Timer.TABLE, PomodoroEventContract.Timer.ID_COL + "=?",
                        new String[] {uri.getLastPathSegment()});
                break;
            case EVENT_MEMBER:
                result = db.delete(PomodoroEventContract.EventMember.TABLE, selection, selectionArgs);
                break;
            case EVENT_MEMBER_BY_ID:
                result = db.delete(PomodoroEventContract.EventMember.TABLE, PomodoroEventContract.EventMember.ID_COL + "=?",
                        new String[] {uri.getLastPathSegment()});
                break;
            case EVENT_MEMBER_FOR_EVENT:
                if (uri.getPathSegments().size() >= 2) {
                    result = db.delete(PomodoroEventContract.EventMember.TABLE, PomodoroEventContract.EventMember.EVENT_FK_COL + "=?",
                            new String[] { uri.getPathSegments().get(uri.getPathSegments().size()-2)});
                } else {
                    result = 0;
                }
                break;
            case TEAM:
                result = db.delete(PomodoroEventContract.TeamDomain.TABLE, selection, selectionArgs);
                break;
            case TEAM_BY_ID:
                result = db.delete(PomodoroEventContract.TeamDomain.TABLE, PomodoroEventContract.TeamDomain.DOMAIN_COL + "=?",
                        new String[] {uri.getLastPathSegment()});
                break;
            case TEAM_MEMBER:
                result = db.delete(PomodoroEventContract.TeamMember.TABLE, selection, selectionArgs);
                break;
            case TEAM_MEMBER_BY_ID:
                result = db.delete(PomodoroEventContract.TeamMember.TABLE, PomodoroEventContract.TeamMember.ID_COL + "=?",
                        new String[] {uri.getLastPathSegment()});
                break;
            case TEAM_MEMBER_FOR_TEAM:
                if (uri.getPathSegments().size() >= 2) {
                    result = db.delete(PomodoroEventContract.TeamMember.TABLE, PomodoroEventContract.TeamMember.DOMAIN_FK_COL + "=?",
                            new String[] { uri.getPathSegments().get(uri.getPathSegments().size()-2)});
                } else {
                    result = 0;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI type " + uri.toString());
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int result = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int code = matcher.match(uri);
        switch (code) {
            case EVENT:
                result = db.update(PomodoroEventContract.Event.TABLE, values, selection, selectionArgs);
                break;
            case EVENT_BY_ID:
                result = db.update(PomodoroEventContract.Event.TABLE, values, PomodoroEventContract.Event.ID_COL + "=?",
                        new String[] { uri.getLastPathSegment() });
                break;
            case TIMER:
                result = db.update(PomodoroEventContract.Timer.TABLE, values, selection, selectionArgs);
                break;
            case TIMER_BY_ID:
                result = db.update(PomodoroEventContract.Timer.TABLE, values, PomodoroEventContract.Timer.ID_COL + "=?",
                        new String[] { uri.getLastPathSegment() });
                break;
            case EVENT_MEMBER:
                result = db.update(PomodoroEventContract.EventMember.TABLE, values, selection, selectionArgs);
                break;
            case EVENT_MEMBER_BY_ID:
                result = db.update(PomodoroEventContract.EventMember.TABLE, values, PomodoroEventContract.EventMember.ID_COL + "=?",
                        new String[] { uri.getLastPathSegment() });
                break;
            case TEAM:
                result = db.update(PomodoroEventContract.TeamDomain.TABLE, values, selection, selectionArgs);
                break;
            case TEAM_BY_ID:
                result = db.update(PomodoroEventContract.TeamDomain.TABLE, values, PomodoroEventContract.TeamDomain.DOMAIN_COL + "=?",
                        new String[] { uri.getLastPathSegment() });
                break;
            case TEAM_MEMBER:
                result = db.update(PomodoroEventContract.TeamMember.TABLE, values, selection, selectionArgs);
                break;
            case TEAM_MEMBER_BY_ID:
                result = db.update(PomodoroEventContract.TeamMember.TABLE, values, PomodoroEventContract.TeamMember.ID_COL + "=?",
                        new String[] { uri.getLastPathSegment() });
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI type " + uri.toString());
        }

        return result;
    }
}
