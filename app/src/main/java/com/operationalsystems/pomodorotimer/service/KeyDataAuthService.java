package com.operationalsystems.pomodorotimer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class KeyDataAuthService extends Service {

    KeyDataAuthenticator auth;

    public KeyDataAuthService() {
    }

    @Override
    public void onCreate() {
        auth = new KeyDataAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return auth.getIBinder();
    }
}
