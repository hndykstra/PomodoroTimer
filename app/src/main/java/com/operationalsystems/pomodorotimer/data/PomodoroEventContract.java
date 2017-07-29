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

    public static final String PATH_FRAG_ACTIVE = "active";
    public static final String PATH_EVENT = "event";
    public static final String PATH_EVENT_ACTIVE = PATH_EVENT + "/" + PATH_FRAG_ACTIVE;

    public static final String PATH_TIMER = "timer";
    /** Use PATH_EVENT /{id}/ PATH_TIMER */
    public static final String PATH_EVENT_TIMER = PATH_EVENT + "/#/" + PATH_TIMER;
    public static final String PATH_EVENT_TIMER_ACTIVE = PATH_EVENT_TIMER + "/" + PATH_FRAG_ACTIVE;

    public static final String PATH_EVENT_MEMBER = "eventmember";
    /** Use PATH_EVENT /{id}/ PATH_EVENT_MEMBER */
    public static final String PATH_EVENT_MEMBER_EVENT = PATH_EVENT + "/#/" + PATH_EVENT_MEMBER;

    public static final String PATH_TEAM_DOMAIN = "domain";
    public static final String PATH_TEAM_MEMBER = "teammember";

    /** Use PATH_DIMAIN / {id} / PATH_TEAM_MEMBER */
    public static final String PATH_TEAM_MEMBER_TEAM = PATH_TEAM_DOMAIN + "/#" + PATH_TEAM_MEMBER;

    public static class Event {
        public static final String TABLE = "PomoEvent";
        public static final String ID_COL = "Id";
        public static final String EVENT_NAME_COL = "EventName";
        public static final String OWNER_COL = "OwnerUid";
        public static final String START_DT_COL = "StartDt";
        public static final String ACTIVE_COL = "Active";
        public static final String END_DT_COL = "EndDt";
        public static final String EVENT_TIMER_MINUTES_COL = "TimerMinutes";
        public static final String EVENT_BREAK_MINUTES_COL = "BreakMinutes";
        public static final String TEAM_DOMAIN_COL = "TeamDomain";

        public static final String[] EVENT_COLS = {
          ID_COL, EVENT_NAME_COL, OWNER_COL, START_DT_COL, ACTIVE_COL, END_DT_COL,
                EVENT_TIMER_MINUTES_COL, EVENT_BREAK_MINUTES_COL, TEAM_DOMAIN_COL
        };
        public static final int ID_INDEX = 0;
        public static final int NAME_INDEX = 1;
        public static final int OWNER_INDEX = 2;
        public static final int START_DT_INDEX = 3;
        public static final int ACTIVE_INDEX = 4;
        public static final int END_DT_INDEX = 5;
        public static final int EVENT_TIMER_MINUTES_INDEX = 6;
        public static final int EVENT_BREAK_MINUTES_INDEX = 7;
        public static final int TEAM_DOMAINN_INDEX = 8;

        public static Uri ACTIVE_EVENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT_ACTIVE).build();
        public static Uri uriForEventId(int eventId) {
            return BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_EVENT)
                    .appendPath(String.valueOf(eventId))
                    .build();
        }

        public static int idFromInstanceUri(Uri uri) {
            List<String> paths = uri.getPathSegments();
            if (paths.size() == 2) {
                try {
                    return Integer.valueOf(paths.get(1));
                } catch (NumberFormatException e) {
                }
            }
            return -1;
        }
    }

    public static class Timer {
        public static final String TABLE = "PomodoroTimer";
        public static final String ID_COL = "Id";
        public static final String EVENT_FK_COL = "EventId";
        public static final String POMODORO_NAME_COL = "PomodoroName";
        public static final String POMODORO_SEQ_COL = "PomodoroSequence";
        public static final String TIMER_MINUTES_COL = "TimerMinutes";
        public static final String BREAK_MINUTES_COL = "BreakMinutes";
        public static final String START_DT_COL = "StartDt";
        public static final String BREAK_DT_COL = "BreakDt";
        public static final String END_DT_COL = "EndDt";
        public static final String ACTIVE_COL = "Active";

        public static final String[] TIMER_COLS = {
                ID_COL, EVENT_FK_COL, POMODORO_NAME_COL, POMODORO_SEQ_COL,
                TIMER_MINUTES_COL, BREAK_MINUTES_COL, START_DT_COL, ACTIVE_COL, BREAK_DT_COL, END_DT_COL
        };

        public static final int ID_INDEX = 0;
        public static final int EVENT_ID_INDEX = 1;
        public static final int NAME_INDEX = 2;
        public static final int SEQ_INDEX = 3;
        public static final int TIMER_MIN_INDEX = 4;
        public static final int BREAK_MIN_INDEX = 5;
        public static final int START_DT_INDEX = 6;
        public static final int ACTIVE_INDEX = 7;
        public static final int BREAK_DT_INDEX = 8;
        public static final int END_DT_INDEX = 9;

        public static final Uri TIMER_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TIMER).build();
        public static Uri uriForEventTimers(int eventId) {
            return BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_EVENT)
                    .appendPath(String.valueOf(eventId))
                    .appendPath(PATH_TIMER)
                    .build();
        }

        public static Uri uriForInstance(int timerId) {
            return BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_TIMER)
                    .appendPath(String.valueOf(timerId))
                    .build();
        }

        public static int idFromInstanceUri(Uri uri) {
            List<String> paths = uri.getPathSegments();
            if (paths.size() == 2) {
                try {
                    return Integer.valueOf(paths.get(1));
                } catch (NumberFormatException e) {
                }
            }
            return -1;
        }
    }

    public static class EventMember {
        public static final String TABLE = "EventMember";
        public static final String ID_COL = "Id";
        public static final String EVENT_FK_COL = "EventId";
        public static final String MEMBER_UID_COL = "MemberUid";
        public static final String JOIN_DT_COL = "JoinDt";

        public static final String[] EVENTMEMBER_COLS = {
                ID_COL, EVENT_FK_COL, MEMBER_UID_COL, JOIN_DT_COL
        };

        public static final int ID_INDEX = 0;
        public static final int EVENT_ID_INDEX = 1;
        public static final int MEMBER_INDEX = 2;
        public static final int JOIN_DT_INDEX = 3;

        public static final Uri EVENT_MEMBER_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT_MEMBER).build();
        public static Uri uriForInstance(int id) {
            return BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_EVENT_MEMBER)
                    .appendPath(String.valueOf(id))
                    .build();
        }
        public static Uri uriForMembersByEvent(int eventId) {
            return BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_EVENT)
                    .appendPath(String.valueOf(eventId))
                    .appendPath(PATH_EVENT_MEMBER)
                    .build();
        }

        public static int idFromInstanceUri(Uri uri) {
            List<String> paths = uri.getPathSegments();
            if (paths.size() == 2) {
                try {
                    return Integer.valueOf(paths.get(1));
                } catch (NumberFormatException e) {
                }
            }
            return -1;
        }
    }

    public static class TeamDomain {
        public static final String TABLE = "TeamDomain";
        public static final String DOMAIN_COL = "DomainPK";
        public static final String OWNER_COL = "OwnerUid";
        public static final String ACTIVE_COL = "Active";
        public static final String CREATED_DT_COL = "CreatedDt";

        public static final String[] TEAM_COLS = {
          DOMAIN_COL, OWNER_COL, ACTIVE_COL, CREATED_DT_COL
        };

        public static final int DOMAIN_INDEX = 0;
        public static final int OWNER_INDEX = 1;
        public static final int ACTIVE_INDEX = 2;
        public static final int CREATED_DT_INDEX = 3;

        public static final Uri TEAM_DOMAIN_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAM_DOMAIN).build();

        public static final Uri uriForInstance(String teamDomainName) {
            return TEAM_DOMAIN_URI.buildUpon()
                    .appendPath(teamDomainName)
                    .build();
        }
    }


    public static class TeamMember {
        public static final String TABLE = "TeamMember";
        public static final String ID_COL = "Id";
        public static final String DOMAIN_FK_COL = "DomainFK";
        public static final String MEMBER_UID_COL = "MemberUid";
        public static final String ROLE_COL = "Role";
        public static final String ADDED_DT_COL = "AddedDt";
        public static final String ADDED_BY_COL = "AddedBy";

        public static final String[] TEAM_MEMBER_COLS = {
                ID_COL, DOMAIN_FK_COL, MEMBER_UID_COL, ROLE_COL, ADDED_DT_COL, ADDED_BY_COL
        };

        public static final int ID_INDEX = 0;
        public static final int DOMAIN_FK_INDEX = 1;
        public static final int MEMBER_UID_INDEX = 2;
        public static final int ROLE_INDEX = 3;
        public static final int ADDED_DT_INDEX = 4;
        public static final int ADDED_BY_INDEX = 5;

        public static final Uri TEAM_MEMBER_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAM_MEMBER).build();
        public static final Uri uriForInstance(int memberEntryId) {
            return TEAM_MEMBER_URI.buildUpon().appendPath(String.valueOf(memberEntryId)).build();
        }
    }
}
