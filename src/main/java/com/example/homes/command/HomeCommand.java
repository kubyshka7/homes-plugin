package com.example.homes.command;

import com.example.homes.HomesPlugin;
import com.example.homes.model.Home;
import com.example.homes.storage.HomeRepository;
import com.example.homes.teleport.TeleportManager;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class HomeCommand implements CommandExecutor {

    private final HomeRepository repository;
    private final HomesPlugin plugin;

    private final TeleportManager teleportManager;

    public HomeCommand(HomeRepository repository, HomesPlugin plugin, TeleportManager teleportManager) {
        this.repository = repository;
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("homes.home")) {
            player.sendMessage(plugin.msg("messages.no_permission"));
            return true;
        }

        String name = (args.length > 0 ? args[0] : "default").toLowerCase();
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Optional<Home> homeOpt = repository.findHome(uuid, name);

            if (homeOpt.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(plugin.msg("messages.home_not_found").replace("{name}", name)));
                return;
            }

            Home home = homeOpt.get();

            Bukkit.getScheduler().runTask(plugin, () -> {

                World world = Bukkit.getWorld(home.world());
                if (world == null) {
                    player.sendMessage(plugin.msg("messages.world_not_found"));
                    return;
                }

                Location location = new Location(
                        world,
                        home.x(),
                        home.y(),
                        home.z(),
                        home.yaw(),
                        home.pitch()
                );

                teleportManager.startTeleport(player, location, home.homeName());
            });
        });

        return true;
    }
}