package features;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

/**
 * Created by Owner on 5/4/2016.
 */
public class ThreadControl {
    private final Lock lock = new ReentrantLock();
    String tag="PayFuel: "+ThreadControl.class.getSimpleName();
    private Condition pauseCondition = lock.newCondition();
    private boolean paused = false, cancelled = false;

    /**
     * Sets the control status to paused. Any thread that calls
     * waitIfPaused() at this point will begin waiting.
     */
    public void pause() {
        lock.lock();

        Log.v(tag, "Pausing");
        paused = true;

        lock.unlock();
    }
    /**
     * Sets the control status to resumed. Any thread that called
     * waitIfPaused() will finish waiting at this point.
     */
    public void resume() {
        lock.lock();
        try {
            Log.v(tag, "Resuming");
            if (!paused) {
                return;
            }
            paused = false;
            pauseCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
    /**
     * Sets the control status to cancelled. Any thread that called
     * waitIfPaused() will finish waiting at this point.
     */
    public void cancel() {
        lock.lock();
        try {
            Log.v(tag, "Cancelling");
            if (cancelled) {
                return;
            }
            cancelled = true;
            pauseCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void waitIfPaused() throws InterruptedException {
        lock.lock();

        try {
            while (paused && !cancelled) {
                Log.v(tag, "Going to wait");
                pauseCondition.await();
                Log.v(tag, "Done waiting");
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}