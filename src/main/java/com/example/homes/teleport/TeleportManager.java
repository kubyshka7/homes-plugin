package com.example.homes.teleport;

import com.example.homes.HomesPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final HomesPlugin plugin;
    private final Map<UUID, BukkitTask> pending = new HashMap<>();
    private final Map<UUID, BukkitTask> particleTasks = new HashMap<>();

    public TeleportManager(HomesPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTeleport(Player player, Location destination, String homeName) {
        UUID uuid = player.getUniqueId();

        cancel(uuid);

        player.sendMessage(plugin.msg("messages.home_teleport_pending"));

        BukkitTask particleTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, () -> spawnParticles(player), 0L, 10L);
        particleTasks.put(uuid, particleTask);

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            BukkitTask pt = particleTasks.remove(uuid);
            if (pt != null) pt.cancel();

            player.getWorld().spawnParticle(
                    Particle.END_ROD,
                    player.getLocation().add(0, 1, 0),
                    100, 0.5, 0.5, 0.5, 0.5
            );

            player.teleport(destination);
            player.sendMessage(plugin.msg("messages.home_teleport").replace("{name}", homeName));
            pending.remove(uuid);
        }, 200L);

        pending.put(uuid, task);
    }

    public void cancel(UUID uuid) {
        BukkitTask task = pending.remove(uuid);
        if (task != null) task.cancel();

        BukkitTask particleTask = particleTasks.remove(uuid);
        if (particleTask != null) particleTask.cancel();
    }

    public boolean hasPending(UUID uuid) {
        return pending.containsKey(uuid);
    }

    private void spawnParticles(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
            double x = Math.cos(angle) * 0.8;
            double z = Math.sin(angle) * 0.8;
            player.getWorld().spawnParticle(
                    Particle.END_ROD,
                    loc.clone().add(x, 0, z),
                    3, 0, 0, 0, 0
            );
        }
    }
}
