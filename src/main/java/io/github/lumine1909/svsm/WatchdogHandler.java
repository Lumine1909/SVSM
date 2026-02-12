package io.github.lumine1909.svsm;

import io.github.lumine1909.reflexion.Field;
import org.spigotmc.WatchdogThread;

public class WatchdogHandler {

    private static final Field<Long> field$WatchdogThread$timeoutTime;
    private static final long watchdogTimeout;
    private static final WatchdogThread watchdogThread;

    static {
        try {
            field$WatchdogThread$timeoutTime = Field.of(WatchdogThread.class, "timeoutTime");
            watchdogThread = (WatchdogThread) Field.of(WatchdogThread.class, "instance").get(null);
            watchdogTimeout = field$WatchdogThread$timeoutTime.getLong(watchdogThread);
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