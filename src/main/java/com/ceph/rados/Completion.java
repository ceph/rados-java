package com.ceph.rados;

import static com.ceph.rados.Library.rados;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import com.ceph.rados.exceptions.RadosException;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class Completion extends RadosBase implements Closeable {
    // Static members
    private static Map<Integer, Completion> completionMap = new HashMap<>();
    private static int nextCompletionId = 1;

    // Instance members
    private boolean safe;
    private boolean complete;
    private boolean notifyOnComplete;
    private boolean notifyOnSafe;

    // Callback support
    private static Callback completeCallback = new Callback() {
        @SuppressWarnings("unused")
        public void callback(Pointer completionPointer, Pointer callbackContext) throws RadosException {
            final int completionId = (int) Pointer.nativeValue(callbackContext);
            Completion completion;
            synchronized (completionMap) {
                completion = completionMap.get(completionId);
            }

            // If the completion has not been closed yet, call the handler(s).
            // Since Ceph Luminous "complete" and "safe" are the same event, so
            // the (deprecated) safe callback is driven from the complete callback.
            if (completion != null) {
                completion.complete = true;
                completion.safe = true;
                if (completion.notifyOnComplete) {
                    completion.onComplete();
                }
                if (completion.notifyOnSafe) {
                    completion.onSafe();
                }
            }
        }
    };

    // Instance members
    private Pointer pointer;
    private int id;

    /**
     * Constructs a completion to use with asynchronous operations.
     * <p>
     * The complete and safe callbacks correspond to operations being acked and
     * committed, respectively. The callbacks are called in order of receipt, so
     * the safe callback may be triggered before the complete callback, and vice
     * versa. This is affected by journalling on the OSDs.
     * <p>
     * NOTE: Read operations only get a complete callback.
     * 
     * @param notifyOnComplete
     *            If true, onComplete() is called when the operation is in
     *            memory on all replicas
     * @param notifyOnSafe
     *            If true, onSafe() is called when the operation is on stable
     *            storage on all replicas
     * @throws RadosException
     */
    public Completion(final boolean notifyOnComplete, final boolean notifyOnSafe) throws RadosException {
        super();
        final PointerByReference pointerByReference = new PointerByReference();
        this.notifyOnComplete = notifyOnComplete;
        this.notifyOnSafe = notifyOnSafe;

        // Generate an ID for this completion.
        synchronized (completionMap) {
            id = nextCompletionId++;
            if (id <= 0) {
                id = 1;
                nextCompletionId = 2;
            }

            // If callbacks will be registered, then also record this object in
            // the global completion map
            // so that it can be accessed from the callback handlers.
            if (notifyOnComplete || notifyOnSafe)
                completionMap.put(id, this);
        }

        // Create the completion object.
        if (notifyOnComplete || notifyOnSafe) {
            handleReturnCode(() -> rados.rados_aio_create_completion(Pointer.createConstant((long) id), completeCallback, null, pointerByReference), "Failed to create completion with callbacks");

        } else {
            handleReturnCode(() -> rados.rados_aio_create_completion(null, null, null, pointerByReference), "Failed to create completion");
        }
        pointer = pointerByReference.getValue();
    }

    /**
     * Block until the operation completes. This means it is in memory on all
     * replicas.
     * 
     * @throws RadosException
     */
    public void waitForComplete() throws RadosException {
        handleReturnCode(() -> rados.rados_aio_wait_for_complete(getPointer()), "Failed to wait for AIO completion");
    }

    /**
     * Block until the operation is safe. This means it is on stable storage on all
     * replicas.
     *
     * @deprecated Since Ceph Luminous "complete" and "safe" are equivalent and
     *             rados_aio_wait_for_safe() is deprecated. Use {@link #waitForComplete()}.
     * @throws RadosException
     */
    @Deprecated
    public void waitForSafe() throws RadosException {
        waitForComplete();
    }

    /**
     * Override this function to implement callback handling. If notifyOnSafe is
     * true, this function is called when the operation is in memory on all
     * replicas.
     */
    public void onComplete() {
    }

    /**
     * Override this function to implement callback handling. If
     * notifyOnComplete is true, this function is called when the operation is
     * on stable storage on all replicas.
     */
    public void onSafe() {
    }

    /**
     * Release a completion
     * <p>
     * Call this when you no longer need the completion. It may not be freed
     * immediately if the operation is not acked and committed.
     */
    @Override
    public void close() {
        if (pointer != null) {
            rados.rados_aio_release(pointer);
            pointer = null;
        }
        if (id > 0) {
            synchronized (completionMap) {
                completionMap.remove(id);
            }
            id = 0;
        }
    }

    /**
     * @return A unique ID identifying the completion.
     */
    public int getId() {
        return id;
    }

    /**
     * @return True if the safe callback is enabled has occurred, else false.
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * @return True if the complete callback is enabled has occurred, else
     *         false.
     */
    public boolean isComplete() {
        return complete;
    }

    public Pointer getPointer() {
        return pointer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        if (!(that instanceof Completion))
            return false;
        return (id == ((Completion) that).id);
    }
}
