package io.github.lumine1909.svsm;

import io.github.lumine1909.reflexion.Field;
import org.spigotmc.WatchdogThread;

public class WatchdogHandler {

    private static final Field<Long> field$WatchdogThread$timeoutTime = Field.of(WatchdogThread.class, "timeoutTime");
    private static final WatchdogThread watchdogThread = (WatchdogThread) Field.of(WatchdogThread.class, "instance").get(null);
    private static final long watchdogTimeout = field$WatchdogThread$timeoutTime.getLong(watchdogThread);

    public static void enableWatchdog() {
        field$WatchdogThread$timeoutTime.setLong(watchdogThread, watchdogTimeout);
    }

    public static void disableWatchdog() {
        field$WatchdogThread$timeoutTime.setLong(watchdogThread, -1L);
    }
}