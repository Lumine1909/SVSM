package io.github.lumine1909.svsm;

import io.github.lumine1909.svsm.server.VirtualServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SVSMPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        WatchdogHandler.disableWatchdog();
        VirtualServer.startServer();
    }

    @Override
    public void onDisable() {
        VirtualServer.shutDown();
        WatchdogHandler.enableWatchdog();
    }
}
