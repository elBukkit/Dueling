package me.KmanCrazy.dueling;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuelingPlugin extends JavaPlugin implements Listener {
    public Map<String,String> queue = new HashMap<String,String>();
    public Map<String,String> dead = new HashMap<String, String>();
    public int b = 20*10;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
        for (String s : getConfig().getValues(false).keySet()) {
            List<String> list = new ArrayList<String>();
            getConfig().getConfigurationSection(s).set("players", list);
            getConfig().getConfigurationSection(s).set("lobbystate", true);
        }
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this,new Runnable() {
            @Override
            public void run() {

            }
        },0,20*5);
    }
    public void is(String arena){
        for (String s : getConfig().getValues(false).keySet()) {
            if (s.equals(arena)) {
                List<String> list = new ArrayList<String>();
                getConfig().getConfigurationSection(s).set("players", list);
                getConfig().getConfigurationSection(s).set("lobbystate", true);
            }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        for (String s : dead.keySet()){
            if (e.getPlayer().getName().equals(s)){
                ConfigurationSection arena = getConfig().getConfigurationSection(dead.get(s));
                 Location specroom = new Location(Bukkit.getWorld(arena.getString("spec.world")), arena.getInt("spec.x"), arena.getInt("spec.y"), arena.getInt("spec.z"));
                e.getPlayer().teleport(specroom);
                e.getPlayer().sendMessage(ChatColor.AQUA + "You have lost :( Better luck next time!");
                dead.remove(e.getPlayer().getName());
            }
        }
    }

    @Override
    public void onDisable() {

    }
    @EventHandler
    public void onPlayerLEavE(PlayerQuitEvent e){
        for (String s : getConfig().getValues(false).keySet()){
            if (getConfig().getConfigurationSection(s).getStringList("players").contains(e.getPlayer().getName())){
                e.setQuitMessage(ChatColor.AQUA + e.getPlayer().getName() + " was a wimp and quit out of an arena!");
                List<String> list = getConfig().getConfigurationSection(s).getStringList("players");
                list.remove(e.getPlayer().getName());
                getConfig().getConfigurationSection(s).set("players", list);
                saveConfig();
                reloadConfig();
                e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
            }
        }
    }
    @EventHandler
    public void onSignChange(SignChangeEvent e){
        if (e.getLine(0).contains("Duel")){
            if (!e.getLine(1).isEmpty()){
                if (getConfig().getConfigurationSection(e.getLine(1)) != null){
                    e.setLine(0,ChatColor.GOLD.toString() + ChatColor.BOLD + "[" +ChatColor.BLUE +  "Duel" +ChatColor.GOLD +  "]");

                }else{
                    e.getBlock().breakNaturally();
                    e.getPlayer().sendMessage(ChatColor.RED + "Unknown arena!");
                }
            }else{
                e.getBlock().breakNaturally();
                e.getPlayer().sendMessage(ChatColor.RED + "You must specify an arena!");
            }
        }
    }
    @EventHandler
    public void onPlayerrightClickSign(PlayerInteractEvent e){
        Block clickedBlock = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (clickedBlock.getType() == Material.SIGN || clickedBlock.getType() == Material.SIGN_POST || clickedBlock.getType() == Material.WALL_SIGN)) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            if (sign.getLine(0).contains("Duel")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dueling admin join " + sign.getLine(1) + " " + e.getPlayer().getName());
            }
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Plugin plugin = this;
        if (command.getName().equalsIgnoreCase("dueling")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.BLUE + "-----------------");
                sender.sendMessage(ChatColor.GOLD + "/dueling info");
                sender.sendMessage(ChatColor.GOLD + "/dueling help");
                if (sender.hasPermission("dueling.admin")) {
                    sender.sendMessage(ChatColor.GOLD + "/dueling admin");
                }
                sender.sendMessage(ChatColor.BLUE + "-----------------");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("info")) {
                    sender.sendMessage(ChatColor.BLUE + "-----------------");
                    sender.sendMessage(ChatColor.GOLD + "Plugin Creator: KmanCrazy");
                    sender.sendMessage(ChatColor.GOLD + "Plugin Website: kplugins.weebly.com");
                    sender.sendMessage(ChatColor.GOLD + "Hope you enjoy!");
                    sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Report any bugs or glitches to KmanCrazy, Dr00bles, NathanWolf! -Thanks!");
                    sender.sendMessage(ChatColor.BLUE + "-----------------");
                }
                if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(ChatColor.BLUE + "-----------------");
                    sender.sendMessage(ChatColor.GOLD + "/dueling info");
                    sender.sendMessage(ChatColor.GOLD + "/dueling help");
                    sender.sendMessage(ChatColor.GOLD + "/dueling join");
                    if (sender.hasPermission("dueling.admin")) {
                        sender.sendMessage(ChatColor.GOLD + "/dueling admin");
                    }
                    sender.sendMessage(ChatColor.BLUE + "-----------------");
                }
                if (args[0].equalsIgnoreCase("admin")) {
                    if (sender.hasPermission("dueling.admin")) {
                        sender.sendMessage(ChatColor.RED + "Please specify something to do! Create, Remove, Start, End");
                    }
                }

            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("admin")) {
                    if (args[1].equalsIgnoreCase("create")) {
                        sender.sendMessage("You must specify an arena! (Optional: Type) (Normal Type: FFA)");
                    }
                    if (args[1].equalsIgnoreCase("join")) {
                        sender.sendMessage("You must specify a name and a player!");

                    }
                    if (args[1].equalsIgnoreCase("start")) {
                        sender.sendMessage("You must specify an arena!");
                    }
                    if (args[1].equalsIgnoreCase("remove")) {
                        sender.sendMessage("You must specify an arena!");
                    }
                }

            } else if (args.length == 3) {

                if (args[0].equalsIgnoreCase("admin")) {
                    if (args[1].equalsIgnoreCase("create")) {
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            if (getConfig().getConfigurationSection(args[2]) == null) {
                                ConfigurationSection arena = getConfig().createSection(args[2]);
                                arena.set("lobby.x", p.getLocation().getX());
                                arena.set("lobby.y", p.getLocation().getY());
                                arena.set("lobby.z", p.getLocation().getZ());
                                arena.set("lobby.world", p.getLocation().getWorld().getName());
                                arena.set("spawn.x", p.getLocation().getX());
                                arena.set("spawn.y", p.getLocation().getY());
                                arena.set("spawn.z", p.getLocation().getZ());
                                arena.set("spawn.world", p.getLocation().getWorld().getName());
                                arena.set("treasureroom.x", p.getLocation().getX());
                                arena.set("treasureroom.y", p.getLocation().getY());
                                arena.set("treasureroom.z", p.getLocation().getZ());
                                arena.set("treasureroom.world", p.getLocation().getWorld().getName());
                                arena.set("spec.x", p.getLocation().getX());
                                arena.set("spec.y", p.getLocation().getY());
                                arena.set("spec.z", p.getLocation().getZ());
                                arena.set("spec.world", p.getLocation().getWorld().getName());
                                arena.set("maxplayers", 20);
                                arena.set("minplayers", 2);
                                arena.set("lobbystate", true);
                                List<String> list = new ArrayList<String>();
                                arena.set("players", list);
                                saveConfig();
                                reloadConfig();
                                p.sendMessage(ChatColor.AQUA + "Arena Created now do /options setlobby, setspawn, setspec, settreasureroom!");
                            } else {
                                sender.sendMessage(ChatColor.AQUA + "Arena already exists!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Silly console! Creating arenas are for players!");
                        }
                    }
                    if (args[1].equalsIgnoreCase("join")) {
                        sender.sendMessage(ChatColor.AQUA + "You must specify an arena!");

                    }
                    if (args[1].equalsIgnoreCase("start")) {
                        if (getConfig().getConfigurationSection(args[2]) != null) {
                            final String ar = args[2];
                            ConfigurationSection arena = getConfig().getConfigurationSection(args[2]);
                            if (arena.getStringList("players").size() >= arena.getInt("minplayers")) {
                                Location lobby = new Location(Bukkit.getWorld(arena.getString("lobby.world")), arena.getInt("lobby.x"), arena.getInt("lobby.y"), arena.getInt("lobby.z"));
                                final Location spawn = new Location(Bukkit.getWorld(arena.getString("spawn.world")), arena.getInt("spawn.x"), arena.getInt("spawn.y"), arena.getInt("spawn.z"));
                                final Location treasureroom = new Location(Bukkit.getWorld(arena.getString("treasureroom.world")), arena.getInt("treasureroom.x"), arena.getInt("treasureroom.y"), arena.getInt("treasureroom.z"));
                                final Location specroom = new Location(Bukkit.getWorld(arena.getString("spec.world")), arena.getInt("spec.x"), arena.getInt("spec.y"), arena.getInt("spec.z"));
                                for (String s : arena.getStringList("players")) {
                                    Bukkit.getPlayer(s).sendMessage("The game is beginning in 10 seconds!");
                                }
                                Bukkit.getScheduler().runTaskLater(this,new Runnable() {
                                    @Override
                                    public void run() {
                                        ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                        if (arena.getBoolean("lobbystate") == true) {
                                            if (arena.getStringList("players").size() >= arena.getInt("minplayers") ) {
                                                for (String s : arena.getStringList("players")){
                                                    Bukkit.getPlayer(s).sendMessage(ChatColor.AQUA + "Game starting in 5 seconds!");
                                                }
                                                //start stuff;

                                                Bukkit.getScheduler().runTaskLater(DuelingPlugin.this,new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                                        for (String s : arena.getStringList("players")){
                                                            Bukkit.getPlayer(s).sendMessage(ChatColor.AQUA + "Game starting in 4 seconds!");
                                                        }
                                                        Bukkit.getScheduler().runTaskLater(DuelingPlugin.this,new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                                                for (String s : arena.getStringList("players")){
                                                                    Bukkit.getPlayer(s).sendMessage(ChatColor.AQUA + "Game starting in 3 seconds!");
                                                                }
                                                                Bukkit.getScheduler().runTaskLater(DuelingPlugin.this,new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                                                        for (String s : arena.getStringList("players")){
                                                                            Bukkit.getPlayer(s).sendMessage(ChatColor.AQUA + "Game starting in 2 seconds!");
                                                                        }
                                                                        Bukkit.getScheduler().runTaskLater(DuelingPlugin.this,new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                                                                for (String s : arena.getStringList("players")){
                                                                                    Bukkit.getPlayer(s).sendMessage(ChatColor.AQUA + "Game starting in 1 seconds!");
                                                                                }
                                                                                Bukkit.getScheduler().runTaskLater(DuelingPlugin.this,new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                                                                        for (String s : arena.getStringList("players")) {
                                                                                            Bukkit.getPlayer(s).sendMessage("Begin!");
                                                                                            Bukkit.getPlayer(s).teleport(spawn);
                                                                                        }
                                                                                        arena.set("lobbystate", false);
                                                                                        saveConfig();
                                                                                        reloadConfig();
                                                                                    }
                                                                                },20);
                                                                            }
                                                                        },20);
                                                                    }
                                                                },20);
                                                            }
                                                        },20);
                                                    }
                                                },20);


                                            }
                                        }
                                    }


                                }, 20*5);

                                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                                    @Override
                                    public void run() {
                                        final ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                        if (arena.getBoolean("lobbystate") == false) {
                                            if (arena.getStringList("players").size() == 1) {
                                                for (final String s : arena.getStringList("players")) {
                                                    Bukkit.getPlayer(s).sendMessage(ChatColor.AQUA + "You have won! Congratulations!");
                                                    Bukkit.broadcastMessage(ChatColor.GOLD + s + " has won a battle!");

                                                    List<String> list = new ArrayList<String>();
                                                    b = 20 * 10;
                                                    arena.set("players", list);
                                                    saveConfig();
                                                    reloadConfig();

                                                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Bukkit.getPlayer(s).sendMessage(ChatColor.AQUA + "Enjoy the treasure!");
                                                            Bukkit.getPlayer(s).teleport(treasureroom);

                                                            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                                                    arena.set("lobbystate", true);
                                                                    for (String queued : queue.keySet()) {
                                                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dueling admin join " + queue.get(queued) + " " + queued);
                                                                        queue.remove(queued);
                                                                    }
                                                                }
                                                            }, 20);
                                                        }
                                                    }, 100);
                                                }
                                            }
                                        }
                                    }
                                }, 0, 4);
                                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                                    @Override
                                    public void run() {
                                        ConfigurationSection arena = getConfig().getConfigurationSection(ar);
                                        if (arena.getBoolean("lobbystate") == false) {
                                            for (String s : arena.getStringList("players")) {
                                                if (Bukkit.getPlayer(s).isDead()) {

                                                    dead.put(s,ar);

                                                    List<String> list = arena.getStringList("players");
                                                    list.remove(s);

                                                    arena.set("players", list);
                                                    saveConfig();
                                                    reloadConfig();
                                                }
                                            }

                                        }
                                    }
                                }, 0, 5);
                            } else {
                                sender.sendMessage(ChatColor.AQUA + "Not enough players!");
                            }
                        }
                    }
                    if (args[1].equalsIgnoreCase("remove")) {
                        sender.sendMessage(ChatColor.AQUA + "You must specify an arena!");
                        getConfig().getConfigurationSection(args[2]).set(args[2], null);
                        saveConfig();
                        reloadConfig();
                    }
                    if (args[1].equalsIgnoreCase("stop")) {

                    }
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("admin")) {
                    if (sender.hasPermission("dueling.admin")) {
                        if (args[1].equalsIgnoreCase("join")) {
                            if (getConfig().getConfigurationSection(args[2]) != null) {
                                if (Bukkit.getPlayer(args[3]) != null) {


                                    final ConfigurationSection arena = getConfig().getConfigurationSection(args[2]);
                                    if (!arena.getStringList("players").contains(args[3])) {
                                        if (arena.getBoolean("lobbystate") == true) {
                                            if (!arena.getStringList("players").contains(args[3])) {
                                                if (arena.getStringList("players").size() <= arena.getInt("maxplayers")) {
                                                    final Location lobby = new Location(Bukkit.getWorld(arena.getString("lobby.world")), arena.getInt("lobby.x"), arena.getInt("lobby.y"), arena.getInt("lobby.z"));
                                                    final Location spawn = new Location(Bukkit.getWorld(arena.getString("spawn.world")), arena.getInt("spawn.x"), arena.getInt("spawn.y"), arena.getInt("spawn.z"));
                                                    final Location treasureroom = new Location(Bukkit.getWorld(arena.getString("treasureroom.world")), arena.getInt("treasureroom.x"), arena.getInt("treasureroom.y"), arena.getInt("treasureroom.z"));
                                                    final Location specroom = new Location(Bukkit.getWorld(arena.getString("spec.world")), arena.getInt("spec.x"), arena.getInt("spec.y"), arena.getInt("spec.z"));
                                                    Bukkit.getPlayer(args[3]).teleport(lobby);
                                                    List<String> list = arena.getStringList("players");
                                                    list.add(args[3]);
                                                    Bukkit.broadcastMessage(ChatColor.AQUA + args[3] + " has joined the queue for " + args[2]);
                                                    arena.set("players", list);
                                                    saveConfig();
                                                    reloadConfig();
                                                    Bukkit.getPlayer(args[3]).sendMessage(ChatColor.AQUA + "You have joined the game!");
                                                    Bukkit.getPlayer(args[3]).setHealth(20.0);
                                                    Bukkit.getPlayer(args[3]).setFoodLevel(20);
                                                    Bukkit.getPlayer(args[3]).setFireTicks(0);
                                                    for ( PotionEffect pt : Bukkit.getPlayer(args[3]).getActivePotionEffects()){
                                                        Bukkit.getPlayer(args[3]).removePotionEffect(pt.getType());
                                                    }
                                                    final String ar = args[2];
                                                    if (arena.getBoolean("lobbystate") == true) {
                                                        if (arena.getStringList("players").size() >= arena.getInt("minplayers")) {

                                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dueling admin start " + ar);


                                                        }
                                                    }
                                                }else{
                                                    Bukkit.getPlayer(args[3]).sendMessage(ChatColor.RED + "There are too many players! Wait until next round!");
                                                    // sender.sendMessage("Too many players!");
                                                    Bukkit.getPlayer(args[3]).sendMessage(ChatColor.AQUA + "You have been added into the queue");
                                                    queue.put(args[3],args[2]);
                                                }
                                            }
                                        } else {
                                            //sender.sendMessage(ChatColor.AQUA + "That game is already in game!");
                                            Bukkit.getPlayer(args[3]).sendMessage(ChatColor.AQUA + "That game is already in game!");
                                            Bukkit.getPlayer(args[3]).sendMessage(ChatColor.AQUA + "You have been added into the queue");
                                            queue.put(args[3],args[2]);
                                        }

                                    } else {
                                        //sender.sendMessage(ChatColor.AQUA + "Already in game!");
                                        Bukkit.getPlayer(args[3]).sendMessage(ChatColor.AQUA + "Already in game!");
                                    }

                                } else {
                                    sender.sendMessage(ChatColor.AQUA + "Unknown player!");
                                }
                            }
                        }else if (args[1].equalsIgnoreCase("create")){
                            if (sender instanceof Player) {
                                Player p = (Player) sender;
                                if (getConfig().getConfigurationSection(args[2]) == null) {
                                    ConfigurationSection arena = getConfig().createSection(args[2]);
                                    if (args[3].equalsIgnoreCase("FFA")){
                                        arena.set("lobby.x", p.getLocation().getX());
                                        arena.set("lobby.y", p.getLocation().getY());
                                        arena.set("lobby.z", p.getLocation().getZ());
                                        arena.set("lobby.world", p.getLocation().getWorld().getName());
                                        arena.set("spawn.x", p.getLocation().getX());
                                        arena.set("spawn.y", p.getLocation().getY());
                                        arena.set("spawn.z", p.getLocation().getZ());
                                        arena.set("spawn.world", p.getLocation().getWorld().getName());
                                        arena.set("treasureroom.x", p.getLocation().getX());
                                        arena.set("treasureroom.y", p.getLocation().getY());
                                        arena.set("treasureroom.z", p.getLocation().getZ());
                                        arena.set("treasureroom.world", p.getLocation().getWorld().getName());
                                        arena.set("spec.x", p.getLocation().getX());
                                        arena.set("spec.y", p.getLocation().getY());
                                        arena.set("spec.z", p.getLocation().getZ());
                                        arena.set("spec.world", p.getLocation().getWorld().getName());
                                        arena.set("maxplayers", 20);
                                        arena.set("minplayers", 2);
                                        arena.set("lobbystate", true);
                                        List<String> list = new ArrayList<String>();
                                        arena.set("players", list);
                                        saveConfig();
                                        reloadConfig();
                                        p.sendMessage(ChatColor.AQUA + "Arena Created now do /options setlobby, setspawn, setspec, settreasureroom!");
                                    }
                                   else if (args[3].equalsIgnoreCase("1v1")){
                                        arena.set("lobby.x", p.getLocation().getX());
                                        arena.set("lobby.y", p.getLocation().getY());
                                        arena.set("lobby.z", p.getLocation().getZ());
                                        arena.set("lobby.world", p.getLocation().getWorld().getName());
                                        arena.set("spawn.x", p.getLocation().getX());
                                        arena.set("spawn.y", p.getLocation().getY());
                                        arena.set("spawn.z", p.getLocation().getZ());
                                        arena.set("spawn.world", p.getLocation().getWorld().getName());
                                        arena.set("treasureroom.x", p.getLocation().getX());
                                        arena.set("treasureroom.y", p.getLocation().getY());
                                        arena.set("treasureroom.z", p.getLocation().getZ());
                                        arena.set("treasureroom.world", p.getLocation().getWorld().getName());
                                        arena.set("spec.x", p.getLocation().getX());
                                        arena.set("spec.y", p.getLocation().getY());
                                        arena.set("spec.z", p.getLocation().getZ());
                                        arena.set("spec.world", p.getLocation().getWorld().getName());
                                        arena.set("maxplayers", 2);
                                        arena.set("minplayers", 2);
                                        arena.set("lobbystate", true);
                                        List<String> list = new ArrayList<String>();
                                        arena.set("players", list);
                                        saveConfig();
                                        reloadConfig();
                                        p.sendMessage(ChatColor.AQUA + "Arena Created now do /options setlobby, setspawn, setspec, settreasureroom!");
                                    }
                                    else if (args[3].equalsIgnoreCase("2v2")){
                                        arena.set("lobby.x", p.getLocation().getX());
                                        arena.set("lobby.y", p.getLocation().getY());
                                        arena.set("lobby.z", p.getLocation().getZ());
                                        arena.set("lobby.world", p.getLocation().getWorld().getName());
                                        arena.set("spawn.x", p.getLocation().getX());
                                        arena.set("spawn.y", p.getLocation().getY());
                                        arena.set("spawn.z", p.getLocation().getZ());
                                        arena.set("spawn.world", p.getLocation().getWorld().getName());
                                        arena.set("treasureroom.x", p.getLocation().getX());
                                        arena.set("treasureroom.y", p.getLocation().getY());
                                        arena.set("treasureroom.z", p.getLocation().getZ());
                                        arena.set("treasureroom.world", p.getLocation().getWorld().getName());
                                        arena.set("spec.x", p.getLocation().getX());
                                        arena.set("spec.y", p.getLocation().getY());
                                        arena.set("spec.z", p.getLocation().getZ());
                                        arena.set("spec.world", p.getLocation().getWorld().getName());
                                        arena.set("maxplayers", 4);
                                        arena.set("minplayers", 4);
                                        arena.set("lobbystate", true);
                                        List<String> list = new ArrayList<String>();
                                        arena.set("players", list);
                                        saveConfig();
                                        reloadConfig();
                                        p.sendMessage(ChatColor.AQUA + "Arena Created now do /options setlobby, setspawn, setspec, settreasureroom!");
                                    }else if (args[3].equalsIgnoreCase("3v3")){
                                        arena.set("lobby.x", p.getLocation().getX());
                                        arena.set("lobby.y", p.getLocation().getY());
                                        arena.set("lobby.z", p.getLocation().getZ());
                                        arena.set("lobby.world", p.getLocation().getWorld().getName());
                                        arena.set("spawn.x", p.getLocation().getX());
                                        arena.set("spawn.y", p.getLocation().getY());
                                        arena.set("spawn.z", p.getLocation().getZ());
                                        arena.set("spawn.world", p.getLocation().getWorld().getName());
                                        arena.set("treasureroom.x", p.getLocation().getX());
                                        arena.set("treasureroom.y", p.getLocation().getY());
                                        arena.set("treasureroom.z", p.getLocation().getZ());
                                        arena.set("treasureroom.world", p.getLocation().getWorld().getName());
                                        arena.set("spec.x", p.getLocation().getX());
                                        arena.set("spec.y", p.getLocation().getY());
                                        arena.set("spec.z", p.getLocation().getZ());
                                        arena.set("spec.world", p.getLocation().getWorld().getName());
                                        arena.set("maxplayers", 6);
                                        arena.set("minplayers", 6);
                                        arena.set("lobbystate", true);
                                        List<String> list = new ArrayList<String>();
                                        arena.set("players", list);
                                        saveConfig();
                                        reloadConfig();
                                        p.sendMessage(ChatColor.AQUA + "Arena Created now do /options setlobby, setspawn, setspec, settreasureroom!");
                                    }else if (args[3].equalsIgnoreCase("Spleef")){
                                        arena.set("lobby.x", p.getLocation().getX());
                                        arena.set("lobby.y", p.getLocation().getY());
                                        arena.set("lobby.z", p.getLocation().getZ());
                                        arena.set("lobby.world", p.getLocation().getWorld().getName());
                                        arena.set("spawn.x", p.getLocation().getX());
                                        arena.set("spawn.y", p.getLocation().getY());
                                        arena.set("spawn.z", p.getLocation().getZ());
                                        arena.set("spawn.world", p.getLocation().getWorld().getName());
                                        arena.set("treasureroom.x", p.getLocation().getX());
                                        arena.set("treasureroom.y", p.getLocation().getY());
                                        arena.set("treasureroom.z", p.getLocation().getZ());
                                        arena.set("treasureroom.world", p.getLocation().getWorld().getName());
                                        arena.set("spec.x", p.getLocation().getX());
                                        arena.set("spec.y", p.getLocation().getY());
                                        arena.set("spec.z", p.getLocation().getZ());
                                        arena.set("spec.world", p.getLocation().getWorld().getName());
                                        arena.set("maxplayers", 15);
                                        arena.set("minplayers", 5);
                                        arena.set("lobbystate", true);
                                        List<String> list = new ArrayList<String>();
                                        arena.set("players", list);
                                        saveConfig();
                                        reloadConfig();
                                        p.sendMessage(ChatColor.AQUA + "Arena Created now do /options setlobby, setspawn, setspec, settreasureroom!");
                                    }else{
                                        p.sendMessage(ChatColor.RED + "Unknown arena type please select one of the following: Spleef, FFA, 1v1, 2v2, 3v3");
                                    }

                                } else {
                                    sender.sendMessage(ChatColor.AQUA + "Arena already exists!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Silly console! Creating arenas are for players!");
                            }
                        }
                    }
                }
            }
        }
        if (command.getName().equalsIgnoreCase("options")) {
            if (sender.hasPermission("dueling.options")) {
                if (sender instanceof Player) {

                    Player p = (Player) sender;
                    if (args.length == 0) {
                        sender.sendMessage(ChatColor.AQUA + "Please specify a setting and arena name!");
                    } else if (args.length == 1) {
                        sender.sendMessage(ChatColor.AQUA + "Please specify a arena name!");
                    } else if (args.length == 2) {
                        if (getConfig().getConfigurationSection(args[1]) != null) {
                            ConfigurationSection arena = getConfig().getConfigurationSection(args[1]);
                            if (args[0].equalsIgnoreCase("setlobby")) {
                                arena.set("lobby.x", p.getLocation().getX());
                                arena.set("lobby.y", p.getLocation().getY());
                                arena.set("lobby.z", p.getLocation().getZ());
                                arena.set("lobby.world", p.getLocation().getWorld().getName());
                                saveConfig();reloadConfig();
                                p.sendMessage(ChatColor.AQUA + "You have set the treasure room!");
                            } else if (args[0].equalsIgnoreCase("setspawn")) {
                                arena.set("spawn.x", p.getLocation().getX());
                                arena.set("spawn.y", p.getLocation().getY());
                                arena.set("spawn.z", p.getLocation().getZ());
                                arena.set("spawn.world", p.getLocation().getWorld().getName());
                                saveConfig();reloadConfig();
                                p.sendMessage(ChatColor.AQUA + "You have set the treasure room!");
                            } else if (args[0].equalsIgnoreCase("setspec")) {
                                arena.set("spec.x", p.getLocation().getX());
                                arena.set("spec.y", p.getLocation().getY());
                                arena.set("spec.z", p.getLocation().getZ());
                                arena.set("spec.world", p.getLocation().getWorld().getName());
                                saveConfig();reloadConfig();
                                p.sendMessage(ChatColor.AQUA + "You have set the treasure room!");
                            } else if (args[0].equalsIgnoreCase("settreasureroom")) {
                                arena.set("treasureroom.x", p.getLocation().getX());
                                arena.set("treasureroom.y", p.getLocation().getY());
                                arena.set("treasureroom.z", p.getLocation().getZ());
                                arena.set("treasureroom.world", p.getLocation().getWorld().getName());
                                saveConfig();reloadConfig();
                                p.sendMessage(ChatColor.AQUA + "You have set the treasure room!");

                            }else if (args[0].equalsIgnoreCase("setminplayers")){
                                p.sendMessage(ChatColor.DARK_RED + "You must specify a number of minimum players!");

                            }
                            else if (args[0].equalsIgnoreCase("setmaxplayers")){
                                p.sendMessage(ChatColor.DARK_RED + "You must specify a number of maximum players!");
                            } else if (args[0].equalsIgnoreCase("settype")){
                                p.sendMessage(ChatColor.DARK_RED + "You must specify an arena type!");
                            }else {
                                sender.sendMessage(ChatColor.AQUA + "Unknown option!");
                            }
                        }

                    }else if (args.length == 3){
                        if (getConfig().getConfigurationSection(args[1]) != null) {
                            ConfigurationSection arena = getConfig().getConfigurationSection(args[1]);
                            if (args[0].equalsIgnoreCase("setminplayers")) {
                                arena.set("minplayers",args[2]);
                            } else if (args[0].equalsIgnoreCase("setmaxplayers")) {
                                arena.set("maxplayers",args[2]);
                                saveConfig();
                                reloadConfig();
                            }
                             else  if (args[2].equalsIgnoreCase("FFA")){
                                arena.set("maxplayers",20);
                                arena.set("minplayers",2);
                                saveConfig();
                                reloadConfig();

                            }else if (args[2].equalsIgnoreCase("1v1")){
                                arena.set("maxplayers",2);
                                arena.set("minplayers",2);
                                saveConfig();
                                reloadConfig();
                                }else if (args[2].equalsIgnoreCase("2v2")){
                                arena.set("maxplayers",4);
                                arena.set("minplayers",4);
                                saveConfig();
                                reloadConfig();
                                }else if (args[2].equalsIgnoreCase("3v3")){
                                arena.set("maxplayers",6);
                                arena.set("minplayers",6);
                                saveConfig();
                                reloadConfig();
                            }else if (args[2].equalsIgnoreCase("Spleef")){
                                arena.set("maxplayers",15);
                                arena.set("minplayers",5);
                                saveConfig();
                                reloadConfig();
                            }

                        }else{
                            p.sendMessage(ChatColor.RED + "ERROR!");
                        }
                    }else {
                        sender.sendMessage(ChatColor.RED + "Error!");
                    }
                }
            }else{
                sender.sendMessage(ChatColor.AQUA + "You dont have permissions!");
            }

        }
        return false;
    }
}