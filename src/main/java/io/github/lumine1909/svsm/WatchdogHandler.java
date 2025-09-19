package io.github.lumine1909.svsm;

import org.spigotmc.WatchdogThread;

import java.lang.reflect.Field;

public class WatchdogHandler {

    private static final Field field$WatchdogThread$timeoutTime;
    private static final long watchdogTimeout;
    private static final WatchdogThread watchdogThread;

    static {
        try {
            field$WatchdogThread$timeoutTime = WatchdogThread.class.getDeclaredField("timeoutTime");
            field$WatchdogThread$timeoutTime.setAccessible(true);
            Field field$WatchdogThread$instance = WatchdogThread.class.getDeclaredField("instance");
            field$WatchdogThread$instance.setAccessible(true);
            watchdogThread = (WatchdogThread) field$WatchdogThread$instance.get(null);
            watchdogTimeout = field$WatchdogThread$timeoutTime.getLong(field$WatchdogThread$instance.get(watchdogThread));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void enableWatchdog() {
        try {
            field$WatchdogThread$timeoutTime.setLong(watchdogThread, watchdogTimeout);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void disableWatchdog() {
        try {
            field$WatchdogThread$timeoutTime.setLong(watchdogThread, -1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}