package io.github.lumine1909.svsm;

import io.github.lumine1909.svsm.server.VirtualServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SVSMPlugin extends JavaPlugin {

    public static SVSMPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        WatchdogHandler.disableWatchdog();
        VirtualServer.start();
        for (Player player : Bukkit.getOnlinePlayers()) {
            io.github.lumine1909.svsm.server.Player.createFromBukkit(player);
        }
    }

    @Override
    public void onDisable() {
        VirtualServer.stop();
        WatchdogHandler.enableWatchdog();
    }
}
