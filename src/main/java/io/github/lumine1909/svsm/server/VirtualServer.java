package io.github.lumine1909.svsm.server;

import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.LockSupport;

public class VirtualServer {

    public static final long NSPT = 1_000_000_000L;

    public static VirtualServer SERVER;

    private final List<Player> players = new CopyOnWriteArrayList<>();
    private final Map<String, Player> playerByName = new ConcurrentHashMap<>();

    private Thread serverThread;
    private long currTickNano;
    private long nextTickNano;

    public static VirtualServer startServer() {
        SERVER = new VirtualServer();
        Thread serverThread = new Thread(SERVER::runServer);
        serverThread.setName("SVSM Thread");
        serverThread.setDaemon(true);
        SERVER.serverThread = serverThread;
        serverThread.start();
        return SERVER;
    }

    public static void shutDown() {
        if (SERVER == null) {
            return;
        }
        SERVER.players.forEach(player -> player.info().channel().pipeline().replace("timeout", "timeout", new ReadTimeoutHandler(30)));
        SERVER.serverThread.interrupt();
        SERVER = null;
    }

    public void runServer() {
        while (true) {
            currTickNano = System.nanoTime();
            nextTickNano = currTickNano + NSPT;
            players.forEach(Player::keepAlive);
            long curr = System.nanoTime();
            LockSupport.parkNanos(nextTickNano - curr);
        }
    }

    public void playerDisconnect(Player player) {
        players.remove(player);
        playerByName.put(player.info().name(), player);
    }

    public void playerConnect(Player player) {
        players.add(player);
        playerByName.put(player.info().name(), player);
    }
}
