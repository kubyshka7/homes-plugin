package com.example.homes.command;

import com.example.homes.HomesPlugin;
import com.example.homes.gui.HomeMenuGui;
import com.example.homes.storage.HomeRepository;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class HomesCommand implements CommandExecutor {

    private final HomeRepository repository;
    private final HomesPlugin plugin;

    public HomesCommand(HomeRepository repository, HomesPlugin plugin) {
        this.repository = repository;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("homes.list")) {
            player.sendMessage(plugin.msg("messages.no_permission"));
            return true;
        }
        HomeMenuGui.open(player, plugin, repository);
        return true;
    }
}