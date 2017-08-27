package com.operationalsystems.pomodorotimer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeyDataSyncService extends Service {
    private static KeyDataSyncAdapter adapter;
    private static Object lock = new Object();

    public KeyDataSyncService() {
    }

    @Override
    public void onCreate() {
        synchronized (lock) {
            adapter = new KeyDataSyncAdapter(this, true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return adapter.getSyncAdapterBinder();
    }
}
