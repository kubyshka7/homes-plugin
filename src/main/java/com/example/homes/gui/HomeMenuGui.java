package com.example.homes.gui;

import com.example.homes.HomesPlugin;
import com.example.homes.storage.HomeRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class HomeMenuGui {

    public static void open(Player player, HomesPlugin plugin, HomeRepository repository) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> homes = repository.findHomes(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                int size = Math.max(9, ((homes.size() / 9) + 1) * 9);
                Inventory inv = Bukkit.createInventory(null, size, "§6Твои дома");

                for (String homeName : homes) {
                    ItemStack item = new ItemStack(Material.GRASS_BLOCK);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§a" + homeName);
                    meta.setLore(List.of("§7Нажми чтобы открыть действия"));
                    item.setItemMeta(meta);
                    inv.addItem(item);
                }

                ItemStack addHome = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta addMeta = addHome.getItemMeta();
                addMeta.setDisplayName("§aДобавить новый дом");
                addMeta.setLore(List.of("§7Нажми чтобы установить дом на текущей позиции"));
                addHome.setItemMeta(addMeta);
                inv.setItem(inv.getSize() - 1, addHome);

                player.openInventory(inv);
            });
        });
    }
}