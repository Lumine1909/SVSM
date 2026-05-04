package io.github.lumine1909.svsm.server;

import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.Map;
import java.util.concurrent.*;

public class VirtualServer {

    private static final Map<String, Player> playerByName = new ConcurrentHashMap<>();
    public static volatile boolean isRunning = false;
    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> serverTask;

    public static void start() {
        if (isRunning) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SVSM Thread");
            thread.setDaemon(true);
            return thread;
        });
        serverTask = executor.scheduleAtFixedRate(() -> playerByName.values().forEach(Player::keepAlive), 1000, 1000, TimeUnit.MILLISECONDS);
        isRunning = true;
    }

    public static void stop() {
        isRunning = false;
        playerByName.values().forEach(player -> player.info().channel().pipeline().replace("timeout", "timeout", new ReadTimeoutHandler(30)));
        if (serverTask != null) {
            serverTask.cancel(true);
            serverTask = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        playerByName.clear();
    }

    public static void playerDisconnect(Player player) {
        playerByName.remove(player.info().name());
    }

    public static void playerConnect(Player player) {
        playerByName.put(player.info().name(), player);
    }
}
