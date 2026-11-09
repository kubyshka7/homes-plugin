package com.example.homes.command;

import com.example.homes.HomesPlugin;
import com.example.homes.model.Home;
import com.example.homes.storage.HomeRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class SetHomeCommand implements CommandExecutor {

    private final HomeRepository repository;
    private final HomesPlugin plugin;

    public SetHomeCommand(HomeRepository repository, HomesPlugin plugin) {
        this.repository = repository;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("homes.sethome")) {
            player.sendMessage(plugin.msg("messages.no_permission"));
            return true;
        }

        String name = args.length > 0 ? args[0] : "default";

        if (!name.matches("[a-zA-Z0-9_-]{1,16}")) {
            player.sendMessage(plugin.msg("messages.invalid_name"));
            return true;
        }

        final String finalName = name.toLowerCase();
        final Location loc = player.getLocation();
        final int limit = plugin.homeLimit(player);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Optional<Home> existing = repository.findHome(player.getUniqueId(), finalName);
            boolean isNew = existing.isEmpty();

            if (isNew) {
                int current = repository.countHomes(player.getUniqueId());

                if (current >= limit) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> player.sendMessage(plugin.msg("messages.home_limit")
                                    .replace("{limit}", String.valueOf(limit))));
                    return;
                }
            }

            Home home = new Home(
                    player.getUniqueId(),
                    finalName,
                    loc.getWorld().getName(),
                    loc.getX(),
                    loc.getY(),
                    loc.getZ(),
                    loc.getYaw(),
                    loc.getPitch()
            );

            repository.saveHome(home);

            Bukkit.getScheduler().runTask(plugin,
                    () -> player.sendMessage(plugin.msg("messages.home_set").replace("{name}", finalName)));
        });

        return true;
    }

}