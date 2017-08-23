package com.operationalsystems.pomodorotimer.util;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts android Task to a promise-like structure
 */

public class TaskPromise extends Promise {

    public static <T> Promise of(Task<T> task) {
        final Promise promise = new Promise();
        task.addOnCompleteListener(new OnCompleteListener<T>() {
            @Override
            public void onComplete(@NonNull Task<T> task) {
                if (task.isComplete()) {
                    if (task.isSuccessful()) {
                        promise.resolve(task.getResult());
                    }
                }
            }
        });

        return promise;
    }

}
