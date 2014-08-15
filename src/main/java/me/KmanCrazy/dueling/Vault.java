package me.KmanCrazy.dueling;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Vault {
    private Inventory inventory;
    private String nextUpgrade;
    private final Plugin plugin;
    private String name;

    public Vault(Plugin plugin) {
        this.plugin = plugin;
    }
    public Vault(Plugin plugin,String nextUpgrades, Inventory inv) {
        this(plugin);
        this.nextUpgrade = nextUpgrades;
        this.inventory = inv;
        this.name = inv.getName();
    }
    public Inventory getVault(){
        return inventory;
    }
    public void enterVault(Player p){
        p.openInventory(inventory);
    }
    public void load(ConfigurationSection configuration) {
        List<ItemStack> c = (List<ItemStack>)configuration.get("contents");
        ItemStack[] contents = c.toArray(new ItemStack[0]); // the 'new ItemStack[0]' is just a type definition for toArray() to avoid casting.
        nextUpgrade = configuration.getParent().getString("upgrade");
        name = configuration.getString("name");
        Inventory inv = Bukkit.createInventory(null,27,name);
        inv.setContents(contents);
        inventory = inv;
        for (ItemStack is : inventory.getContents()){
            if (is == null){
                is.setType(Material.AIR);
            }
        }
    }

    public void save(ConfigurationSection configuration) {
        configuration.getParent().set("upgrade", nextUpgrade);

        ItemStack[] contents = inventory.getContents();
        configuration.set("contents",contents);
        configuration.set("name",name);
    }
    public void setUpgrade(String upgrade){
        nextUpgrade = upgrade;
    }
    public String getUpgrade(){
        return this.nextUpgrade;
    }


}
