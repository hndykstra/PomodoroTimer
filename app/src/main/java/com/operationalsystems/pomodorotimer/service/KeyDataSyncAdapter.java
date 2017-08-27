package com.operationalsystems.pomodorotimer.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.User;
import com.operationalsystems.pomodorotimer.util.Promise;

/**
 * Synchronizes key data by querying firebase - if the system is online, this will force
 * read of key data and ensure the firebase disk persistence has cached it.
 */

public class KeyDataSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = "KeyDataSyncAdapter";

    public KeyDataSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public KeyDataSyncAdapter(Context context, boolean autoInitialize, boolean allowParallel) {
        super(context, autoInitialize, allowParallel);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    // logged in so perform the query
                    queryFirebaseKeyData(firebaseAuth.getCurrentUser());
                }
            }
        });
    }

    private void queryFirebaseKeyData(final FirebaseUser user) {
        // this just queries the data, if the device is online
        final PomodoroFirebaseHelper database = new PomodoroFirebaseHelper();
        database.queryUser(user.getUid()).then(new Promise.PromiseReceiver() {
            @Override
            public Object receive(Object t) {
                Log.d(LOG_TAG, "Sync query received user");
                User u = (User)t;
                String team = u.getRecentTeam();
                if (team != null && team.length() > 0) {
                    database.queryTeam(team);
                }
                if (u.getRecentEvent() != null && u.getRecentEvent().length() > 0) {
                    database.queryEvent(u.getRecentEvent(), team, user.getUid());
                }
                return t;
            }
        }).orElse(new Promise.PromiseCatcher() {
            @Override
            public void catchError(Object reason) {
                Log.d(LOG_TAG, "Sync query failed");
            }
        });
    }

}
