package io.github.lumine1909.svsm;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        io.github.lumine1909.svsm.server.Player.createFromBukkit(player);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getCause() == PlayerKickEvent.Cause.TIMEOUT) {
            event.setCancelled(true);
        }
    }
}
