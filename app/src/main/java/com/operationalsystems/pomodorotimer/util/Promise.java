package com.operationalsystems.pomodorotimer.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Promise-like structure, helps with asynchronous
 * operations that return a result, like Firebase
 * queries.
 */
public class Promise {

    /**
     * Callback that will receive the result
     * if the method is successful.
     */
    public interface PromiseReceiver {
        public Object receive(Object t);
    }

    /**
     * Callback that will receive an error object
     * if the result is not successful.
     */
    public interface PromiseCatcher {
        public void catchError(Object reason);
    }

    /**
     * Helper to direct results and errors down the
     * promise chain.
     */
    private interface ResultConsumer {
        public void accept(Object value);
        public void passOnError(Object reason);
    }

    /**
     * Encapsulating the error handling.
     */
    private interface ErrorHandler {
        public void catchError(Object reason);
    }

    /**
     * Hidden class to adapt multiple results
     * feeding to a single composite promise.
     * E.g. Promise.all
     */
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

    /**
     * Lazy implementation of a composite promise that aggregates results of
     * multiple input input promises. The resulting promise is rejected if any
     * of the list are rejected, and resolved with an array of results when all
     * the promises in the list are resolved.
     * @param list Input promises.
     * @return Composite promise.
     */
    public static Promise all(Promise... list) {
        // cheap reference count, assuming each promise is either accepted or rejected zero or one times
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
     * This would be cleaner with Java 8 Function&lt;T,U&gt;
     * @param receiver Functional interface to receive the promised value.
     * @return Promise for the result of receiver.
     */
    public synchronized Promise then(final PromiseReceiver receiver) {
        final Promise promise = new Promise();
        if (this.isResolved) {
            Object recvrResult = receiver.receive(resolvedValue);
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
