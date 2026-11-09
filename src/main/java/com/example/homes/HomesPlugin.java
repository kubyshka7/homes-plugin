package com.example.homes;

import com.example.homes.command.DelHomeCommand;
import com.example.homes.command.HomeCommand;
import com.example.homes.command.HomesCommand;
import com.example.homes.command.SetHomeCommand;
import com.example.homes.gui.GuiListener;
import com.example.homes.storage.Database;
import com.example.homes.storage.HomeRepository;
import com.example.homes.storage.JdbcHomeRepository;
import com.example.homes.teleport.TeleportListener;
import com.example.homes.teleport.TeleportManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class HomesPlugin extends JavaPlugin {

    private Database database;
    private HomeRepository homeRepository;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();

        database = new Database(this);
        database.connect();

        homeRepository = new JdbcHomeRepository(database);

        TeleportManager teleportManager = new TeleportManager(this);
        TeleportListener teleportListener = new TeleportListener(teleportManager, this);
        getServer().getPluginManager().registerEvents(teleportListener, this);

        GuiListener guiListener = new GuiListener(this, homeRepository, teleportManager);
        getServer().getPluginManager().registerEvents(guiListener, this);

        getCommand("sethome").setExecutor(new SetHomeCommand(homeRepository, this));
        getCommand("home").setExecutor(new HomeCommand(homeRepository, this, teleportManager));
        getCommand("homes").setExecutor(new HomesCommand(homeRepository, this));
        getCommand("delhome").setExecutor(new DelHomeCommand(homeRepository, this));
    }

    @Override
    public void onDisable() {
        database.close();
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            saveResource("config.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String msg(String path) {
        String prefix = messages.getString("prefix", "");
        String message = messages.getString(path, path);
        return MessageUtil.color(prefix + message);
    }

    /**
     * Лимит домов игрока: наибольшее N из прав homes.limit.N, иначе limits.default из конфига.
     * Вызывать в основном потоке сервера.
     */
    public int homeLimit(Player player) {
        int defaultLimit = getConfig().getInt("limits.default", 3);
        int maxLimit = 0;

        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            String permission = perm.getPermission();
            if (permission.startsWith("homes.limit.")) {
                try {
                    int value = Integer.parseInt(permission.substring("homes.limit.".length()));
                    if (value > maxLimit) maxLimit = value;
                } catch (NumberFormatException ignored) {}
            }
        }

        return maxLimit > 0 ? maxLimit : defaultLimit;
    }
}
