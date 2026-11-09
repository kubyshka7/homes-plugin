package com.example.homes.command;

import com.example.homes.HomesPlugin;
import com.example.homes.storage.HomeRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DelHomeCommand implements CommandExecutor {

    private final HomeRepository repository;
    private final HomesPlugin plugin;

    public DelHomeCommand(HomeRepository repository, HomesPlugin plugin) {
        this.repository = repository;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("homes.delhome")) {
            player.sendMessage(plugin.msg("messages.no_permission"));
            return true;
        }

        String name = (args.length > 0 ? args[0] : "default").toLowerCase();

        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            boolean deleted = repository.deleteHome(uuid, name);
            String key = deleted ? "messages.home_deleted" : "messages.home_not_found";

            Bukkit.getScheduler().runTask(plugin,
                    () -> player.sendMessage(plugin.msg(key).replace("{name}", name)));
        });

        return true;
    }
}