package com.example.homes.gui;

import com.example.homes.HomesPlugin;
import com.example.homes.model.Home;
import com.example.homes.storage.HomeRepository;
import com.example.homes.teleport.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiListener implements Listener {

    private final HomesPlugin plugin;
    private final HomeRepository repository;
    private final Map<UUID, String> awaitingRename = new ConcurrentHashMap<>(); // чат приходит не из основного потока
    private final TeleportManager teleportManager;

    public GuiListener(HomesPlugin plugin, HomeRepository repository, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.repository = repository;
        this.teleportManager = teleportManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();
        if (!title.equals("§6Твои дома") && !title.startsWith("§6Дом: §a")) return;
        e.setCancelled(true); // иначе предметы меню можно забрать в инвентарь

        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;
        String itemName = clicked.getItemMeta().getDisplayName();

        if (title.equals("§6Твои дома")) {
            if (itemName.startsWith("§a") && !itemName.equals("§aДобавить новый дом")) {
                String homeName = itemName.substring(2);
                player.closeInventory();
                HomeActionGui.open(player, homeName);
            } else if (itemName.equals("§aДобавить новый дом")) {
                player.closeInventory();
                awaitingRename.put(player.getUniqueId(), "__new__");
                player.sendMessage(plugin.msg("messages.home_new_prompt"));
                player.sendMessage(plugin.msg("messages.home_new_prompt_default"));
            }
            return;
        }

        if (title.startsWith("§6Дом: §a")) {
            String homeName = title.substring("§6Дом: §a".length());

            switch (itemName) {
                case "§aТелепортироваться" -> {
                    player.closeInventory();

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        Optional<Home> homeOpt = repository.findHome(player.getUniqueId(), homeName);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (homeOpt.isEmpty()) {
                                player.sendMessage(plugin.msg("messages.home_not_found").replace("{name}", homeName));
                                return;
                            }
                            Home home = homeOpt.get();
                            World world = Bukkit.getWorld(home.world());
                            if (world == null) {
                                player.sendMessage(plugin.msg("messages.world_not_found"));
                                return;
                            }
                            teleportManager.startTeleport(player, new Location(world, home.x(), home.y(), home.z(), home.yaw(), home.pitch()), homeName);
                        });
                    });
                }
                case "§eУстановить здесь" -> {
                    player.closeInventory();
                    Location loc = player.getLocation();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        Optional<Home> existing = repository.findHome(player.getUniqueId(), homeName);
                        if (existing.isEmpty()) return;
                        repository.saveHome(new Home(
                                player.getUniqueId(), homeName,
                                loc.getWorld().getName(),
                                loc.getX(), loc.getY(), loc.getZ(),
                                loc.getYaw(), loc.getPitch()
                        ));
                        Bukkit.getScheduler().runTask(plugin,
                                () -> player.sendMessage(plugin.msg("messages.home_updated").replace("{name}", homeName)));
                    });
                }
                case "§bПереименовать" -> {
                    player.closeInventory();
                    awaitingRename.put(player.getUniqueId(), homeName);
                    player.sendMessage(plugin.msg("messages.home_new_prompt"));
                }
                case "§cУдалить" -> {
                    player.closeInventory();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        boolean deleted = repository.deleteHome(player.getUniqueId(), homeName);
                        String key = deleted ? "messages.home_deleted" : "messages.home_not_found";
                        Bukkit.getScheduler().runTask(plugin,
                                () -> player.sendMessage(plugin.msg(key).replace("{name}", homeName)));
                    });
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingRename.containsKey(uuid)) return;

        e.setCancelled(true);
        String oldName = awaitingRename.remove(uuid);
        String newName = PlainTextComponentSerializer.plainText().serialize(e.message()).trim().toLowerCase();

        if (!newName.matches("[a-zA-Z0-9_-]{1,16}")) {
            player.sendMessage(plugin.msg("messages.invalid_name"));
            return;
        }

        if (oldName.equals("__new__")) {
            String finalName = newName.trim().isEmpty() || newName.equals("-") ? "default" : newName;
            // позицию и права игрока читаем в основном потоке, а в базу идём асинхронно
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location loc = player.getLocation();
                int limit = plugin.homeLimit(player);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    int current = repository.countHomes(uuid);
                    if (current >= limit) {
                        Bukkit.getScheduler().runTask(plugin,
                                () -> player.sendMessage(plugin.msg("messages.home_limit").replace("{limit}", String.valueOf(limit))));
                        return;
                    }
                    repository.saveHome(new Home(
                            uuid, finalName,
                            loc.getWorld().getName(),
                            loc.getX(), loc.getY(), loc.getZ(),
                            loc.getYaw(), loc.getPitch()
                    ));
                    Bukkit.getScheduler().runTask(plugin,
                            () -> player.sendMessage(plugin.msg("messages.home_set").replace("{name}", finalName)));
                });
            });
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<Home> homeOpt = repository.findHome(uuid, oldName);
            if (homeOpt.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(plugin.msg("messages.home_not_found").replace("{name}", oldName)));
                return;
            }
            Home old = homeOpt.get();
            repository.saveHome(new Home(uuid, newName, old.world(), old.x(), old.y(), old.z(), old.yaw(), old.pitch()));
            repository.deleteHome(uuid, oldName);
            Bukkit.getScheduler().runTask(plugin,
                    () -> player.sendMessage(plugin.msg("messages.home_renamed").replace("{old}", oldName).replace("{new}", newName)));
        });
    }
}
