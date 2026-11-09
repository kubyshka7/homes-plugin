package com.example.homes.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class HomeActionGui {

    public static void open(Player player, String homeName) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Дом: §a" + homeName);

        ItemStack teleport = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tMeta = teleport.getItemMeta();
        tMeta.setDisplayName("§aТелепортироваться");
        tMeta.setLore(List.of("§7Нажми чтобы телепортироваться"));
        teleport.setItemMeta(tMeta);
        inv.setItem(11, teleport);

        ItemStack setHere = new ItemStack(Material.COMPASS);
        ItemMeta sMeta = setHere.getItemMeta();
        sMeta.setDisplayName("§eУстановить здесь");
        sMeta.setLore(List.of("§7Обновить точку дома на текущую позицию"));
        setHere.setItemMeta(sMeta);
        inv.setItem(12, setHere);

        ItemStack rename = new ItemStack(Material.NAME_TAG);
        ItemMeta rMeta = rename.getItemMeta();
        rMeta.setDisplayName("§bПереименовать");
        rMeta.setLore(List.of("§7Напиши новое имя в чат"));
        rename.setItemMeta(rMeta);
        inv.setItem(13, rename);

        ItemStack delete = new ItemStack(Material.BARRIER);
        ItemMeta dMeta = delete.getItemMeta();
        dMeta.setDisplayName("§cУдалить");
        dMeta.setLore(List.of("§7Удалить этот дом навсегда"));
        delete.setItemMeta(dMeta);
        inv.setItem(15, delete);

        player.openInventory(inv);
    }
}
