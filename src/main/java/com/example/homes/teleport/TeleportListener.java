package com.example.homes.teleport;

import com.example.homes.HomesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeleportListener implements Listener {

    private final TeleportManager teleportManager;
    private final HomesPlugin plugin;

    public TeleportListener(TeleportManager teleportManager, HomesPlugin plugin) {
        this.teleportManager = teleportManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (teleportManager.hasPending(player.getUniqueId())) {
            teleportManager.cancel(player.getUniqueId());
            player.sendMessage(plugin.msg("messages.home_teleport_cancelled_damage"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        teleportManager.cancel(e.getPlayer().getUniqueId());
    }
}
