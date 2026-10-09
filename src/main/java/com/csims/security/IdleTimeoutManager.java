package com.csims.security;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Monitors user inactivity across the entire application using global AWT event listeners.
 * Auto-triggers logout callback after 15 minutes of inactivity.
 */
public final class IdleTimeoutManager {

    private static final int DEFAULT_TIMEOUT_MILLIS = 15 * 60 * 1000; // 15 minutes
    private static volatile IdleTimeoutManager instance;

    private final Timer idleTimer;
    private Runnable timeoutCallback;
    private final AWTEventListener activityListener;
    private boolean active = false;

    private IdleTimeoutManager() {
        this.idleTimer = new Timer(DEFAULT_TIMEOUT_MILLIS, e -> triggerTimeout());
        this.idleTimer.setRepeats(false);

        this.activityListener = event -> resetTimer();
    }

    public static IdleTimeoutManager getInstance() {
        if (instance == null) {
            synchronized (IdleTimeoutManager.class) {
                if (instance == null) {
                    instance = new IdleTimeoutManager();
                }
            }
        }
        return instance;
    }

    /**
     * Starts monitoring user activity for the logged-in session.
     */
    public synchronized void start(Runnable onTimeout) {
        this.timeoutCallback = onTimeout;
        if (!active) {
            long eventMask = AWTEvent.MOUSE_EVENT_MASK
                           | AWTEvent.MOUSE_MOTION_EVENT_MASK
                           | AWTEvent.KEY_EVENT_MASK
                           | AWTEvent.WINDOW_EVENT_MASK;
            Toolkit.getDefaultToolkit().addAWTEventListener(activityListener, eventMask);
            active = true;
        }
        idleTimer.restart();
    }

    /**
     * Resets the inactivity countdown timer whenever user activity is detected.
     */
    public synchronized void resetTimer() {
        if (active && idleTimer.isRunning()) {
            idleTimer.restart();
        }
    }

    /**
     * Stops the timeout monitor upon logout or application exit.
     */
    public synchronized void stop() {
        idleTimer.stop();
        if (active) {
            try {
                Toolkit.getDefaultToolkit().removeAWTEventListener(activityListener);
            } catch (Exception ignored) {}
            active = false;
        }
        this.timeoutCallback = null;
    }

    /**
     * Configurable timeout duration (in milliseconds) primarily for testing.
     */
    public synchronized void setTimeoutMillis(int millis) {
        idleTimer.setInitialDelay(millis);
        if (idleTimer.isRunning()) {
            idleTimer.restart();
        }
    }

    private void triggerTimeout() {
        stop();
        if (timeoutCallback != null) {
            SwingUtilities.invokeLater(timeoutCallback);
        }
    }
}
