package com.operationalsystems.pomodorotimer.util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Single value event listener to resolve a promise when data is returned
 */

public class PromiseValueEventListener implements ValueEventListener {
    public interface EntityHelper {
        public Object preprocessEntity(Object o);
    }
    private Promise promise;
    private Class<?> valueClass;
    private EntityHelper helper;

    public PromiseValueEventListener(Promise p, Class<?> valueClass) {
        this.promise = p;
        this.valueClass = valueClass;
        this.helper = null;
    }

    public PromiseValueEventListener(Promise p, Class<?> valueClass, EntityHelper helper) {
        this.promise = p;
        this.valueClass = valueClass;
        this.helper = helper;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Object valueFound = null;
        if (dataSnapshot.exists()) {
            valueFound = dataSnapshot.getValue(valueClass);
        }

        if (valueFound != null && helper != null) {
            valueFound = helper.preprocessEntity(valueFound);
        }
        promise.resolve(valueFound);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        promise.reject(databaseError);
    }
}
