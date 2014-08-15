package me.KmanCrazy.dueling;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Vaults implements Listener {
    @EventHandler
    public void onRightClickInv(InventoryClickEvent e) {
        ClickType clickType = e.getClick();
        if (e.getInventory().getName().contains("Vault")) {
            if (clickType.equals(ClickType.RIGHT)) {

            }
        }
    }


}
