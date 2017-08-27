package com.operationalsystems.pomodorotimer.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Promise-like structure
 */

public class Promise {

    public interface PromiseReceiver {
        public Object receive(Object t);
    }

    public interface PromiseCatcher {
        public void catchError(Object reason);
    }

    private interface ResultConsumer {
        public void accept(Object value);
        public void passOnError(Object reason);
    }

    private interface ErrorHandler {
        public void catchError(Object reason);
    }

    private static class PromiseAdapter {
        Object[] results;
        int count;
        Promise output;

        PromiseAdapter(Promise p, int size) {
            results = new Object[size];
            count = 0;
            output = p;
        }

        void receiveOne(int index, Object value) {
            results[index] = value;
            ++count;
            if (count == results.length) {
                if (!output.isRejected)
                    output.resolve(results);
            }
        }

        void rejectOne(int index, Object reason) {
            results[index] = reason;
            if (!output.isRejected)
                output.reject(results);
        }
    }

    /**
     * Shorthand that changes a static value to a resolved promise for that value.
     * @param value Resolved value.
     * @return A resolved promise for value.
     */
    public static Promise resolved(Object value) {
        Promise p = new Promise();
        p.resolve(value);
        return p;
    }

    /**
     * Shorthand that changes a static error result to a rejected promise with that error.
     * @param error The error that the promise will propagate.
     * @return A rejected promise with specified error reason.
     */
    public static Promise rejected(Object error) {
        Promise p = new Promise();
        p.reject(error);
        return p;
    }

    // cheap reference count, assuming each promise is either accepted or rejected zero or one times
    public static Promise all(Promise... list) {
        Promise promiseAll = new Promise();
        final PromiseAdapter holder = new PromiseAdapter(promiseAll, list.length);

        for (int i=0 ; i < list.length ; ++i) {
            final int index = i;
            final Promise p = list[i];
            PromiseReceiver recvr = new PromiseReceiver() {
                @Override
                public Object receive(Object t) {
                    holder.receiveOne(index, t);
                    return null;
                }
            };
            PromiseCatcher c = new PromiseCatcher() {
                @Override
                public void catchError(Object reason) {
                    holder.rejectOne(index, reason);
                }
            };
            p.then(recvr).orElse(c);
        }

        if (list.length == 0) {
            promiseAll.resolve(new Object[0]);
        }

        return promiseAll;
    }

    private ResultConsumer resultConsumer;
    private boolean isResolved;
    private Object resolvedValue;
    private boolean isRejected;
    private Object rejectReason;
    private ErrorHandler errorHandler;

    /**
     * Attaches a receiver to handle the fulfilled promise.
     * This would be easier with Java 8 Function&lt;T,U&gt;
     * @param receiver Functional interface to receive the promised value.
     * @return Promise for the result of receiver.
     */
    public synchronized Promise then(final PromiseReceiver receiver) {
        final Promise promise = new Promise();
        if (this.isResolved) {
            promise.resolve(resolvedValue);
        } else if (this.isRejected) {
            promise.reject(rejectReason);
        } else {
            resultConsumer = new ResultConsumer() {
                @Override
                public void accept(Object value) {
                    Object recvrResult = receiver.receive(value);
                    if (recvrResult instanceof Promise) {
                        // if the receiver returns a promise, don't continue the
                        // chain until that promise finishes.
                        Promise intermediate = (Promise) recvrResult;
                        intermediate.then(new PromiseReceiver() {
                            public Object receive(Object newResult) {
                                promise.resolve(newResult);
                                return null;
                            }
                        });
                    } else {
                        promise.resolve(recvrResult);
                    }
                }

                public void passOnError(Object reason) {
                    promise.reject(reason);
                }
            };
        }
        return promise;
    }

    /**
     * Attaches a receiver to handle a rejected promise.
     * This would be easier with Java 8 Producer&lt;T&gt;.
     * @param catcher Error handler to catch problems with the promise.
     */
    public synchronized void orElse(final PromiseCatcher catcher) {
        if (this.isResolved) {
            // no-op
        } else if (this.isRejected) {
            catcher.catchError(rejectReason);
        } else {
            errorHandler = new ErrorHandler() {
                @Override
                public void catchError(Object reason) {
                    catcher.catchError(reason);
                }
            };
        }
    }

    /**
     * Indicates the promise should be fulfilled with the specified value.
     * @param value The fulfillment value for the promise.
     */
    public void resolve(Object value) {
        synchronized (this) {
            if (this.isResolved)
                throw new IllegalStateException("Promise already resolved");
            if (this.isRejected)
                throw new IllegalStateException("Promise already rejected");
            isResolved = true;
            resolvedValue = value;
        }

        ResultConsumer c = this.resultConsumer;
        this.resultConsumer = null;
        if (c != null) {
            c.accept(value);
        }
    }

    /**
     * Indicates the promise should be rejected with an error or description of why.
     * @param reason Reason for the error, if any.
     */
    public void reject(Object reason) {
        synchronized (this) {
            if (this.isResolved)
                throw new IllegalStateException("Promise already resolved");
            if (this.isRejected)
                throw new IllegalStateException("Promise already rejected");

            isRejected = true;
            rejectReason = reason;
        }
        if (errorHandler != null) {
            ErrorHandler e = this.errorHandler;
            this.errorHandler = null;
            this.resultConsumer = null;
            e.catchError(reason);
        } else if (resultConsumer != null) {
            ResultConsumer c = this.resultConsumer;
            this.resultConsumer = null;
            this.errorHandler = null;
            c.passOnError(reason);
        }
    }
}
