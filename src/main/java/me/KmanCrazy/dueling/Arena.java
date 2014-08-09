package me.KmanCrazy.dueling;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class Arena {
    private ArenaState state = ArenaState.LOBBY;
    private Set<String> players = new HashSet<String>();
    private final Plugin plugin;

    private Location spectating;
    private Location spawn;
    private Location treasure;
    private Location lobby;

    private int maxPlayers;
    private int minPlayers;

    public Arena(Plugin plugin) {
        this.plugin = plugin;
    }

    public Arena(Plugin plugin, Location location, int min, int max) {
        this(plugin);
        spectating = location.clone();
        spawn = location.clone();
        treasure = location.clone();
        lobby = location.clone();

        maxPlayers = max;
        minPlayers = min;
    }

    public void load(ConfigurationSection configuration) {
        minPlayers = configuration.getInt("minplayers");
        maxPlayers = configuration.getInt("maxplayers");

        spectating = new Location(
            plugin.getServer().getWorld(configuration.getString("spec.world")),
            configuration.getInt("spec.x"),
            configuration.getInt("spec.y"),
            configuration.getInt("spec.z")
        );

        lobby = new Location(
                plugin.getServer().getWorld(configuration.getString("lobby.world")),
                configuration.getInt("lobby.x"),
                configuration.getInt("lobby.y"),
                configuration.getInt("lobby.z")
        );

        spawn = new Location(
                plugin.getServer().getWorld(configuration.getString("spawn.world")),
                configuration.getInt("spawn.x"),
                configuration.getInt("spawn.y"),
                configuration.getInt("spawn.z")
        );

        treasure = new Location(
                plugin.getServer().getWorld(configuration.getString("treasureroom.world")),
                configuration.getInt("treasureroom.x"),
                configuration.getInt("treasureroom.y"),
                configuration.getInt("treasureroom.z")
        );
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("minplayers", minPlayers);
        configuration.set("maxplayers", maxPlayers);

        configuration.set("spec.world", spectating.getWorld().getName());
        configuration.set("spec.x", spectating.getBlockX());
        configuration.set("spec.y", spectating.getBlockY());
        configuration.set("spec.z", spectating.getBlockZ());

        configuration.set("lobby.world", lobby.getWorld().getName());
        configuration.set("lobby.x", lobby.getBlockX());
        configuration.set("lobby.y", lobby.getBlockY());
        configuration.set("lobby.z", lobby.getBlockZ());

        configuration.set("spawn.world", spawn.getWorld().getName());
        configuration.set("spawn.x", spawn.getBlockX());
        configuration.set("spawn.y", spawn.getBlockY());
        configuration.set("spawn.z", spawn.getBlockZ());

        configuration.set("treasureroom.world", treasure.getWorld().getName());
        configuration.set("treasureroom.x", treasure.getBlockX());
        configuration.set("treasureroom.y", treasure.getBlockY());
        configuration.set("treasureroom.z", treasure.getBlockZ());
    }

    public void start() {
        state = ArenaState.ACTIVE;
        Server server = plugin.getServer();
        for (String playerName : players) {
            Player player = server.getPlayer(playerName);
            player.sendMessage("Begin!");
            player.teleport(spawn);
        }
    }

    public boolean has(Player player) {
        return players.contains(player.getName());
    }

    public void remove(Player player) {
        players.remove(player.getName());
    }

    public Player getWinner() {
        if (state == ArenaState.ACTIVE && players.size() == 1) {
            state = ArenaState.LOBBY;
            String winner = players.iterator().next();
            players.clear();
            return plugin.getServer().getPlayer(winner);
        }

        return null;
    }

    public Location getSpectatingRoom() {
        return spectating;
    }

    public Location getTreasureRoom() {
        return treasure;
    }

    public boolean checkActive() {
        if (state != ArenaState.ACTIVE) return false;
        if (players.size() == 0) {
            state = ArenaState.LOBBY;
            return false;
        }

        return true;
    }

    public boolean isReady() {
        return state == ArenaState.LOBBY && players.size() >= minPlayers;
    }

    public void lobbyMessage() {
        String message = "Waiting for " + (minPlayers - players.size()) + " more players!";
        messagePlayers(message);
    }

    public void messagePlayers(String message) {
        Server server = plugin.getServer();
        for (String player : players) {
            server.getPlayer(player).sendMessage(message);
        }
    }

    public void startCountdown(int time) {
        if (state != ArenaState.LOBBY) return;
        state = ArenaState.COUNTDOWN;
        countdown(time);
    }

    private void countdown(final int time) {
        if (state != ArenaState.COUNTDOWN) {
            return;
        }

        if (time <= 0) {
            start();
            return;
        }

        if (time == 10 || time <= 5) {
            messagePlayers("Match is starting in " + time + " seconds");
        }
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskLater(plugin, new Runnable() {
           @Override
            public void run() {
               countdown(time - 1);
           }
        }, 20);
    }

    public boolean stop() {
        if (state == ArenaState.LOBBY) return false;
        messagePlayers("This match has been cancelled!");
        state = ArenaState.LOBBY;
        players.clear();
        return true;
    }

    public boolean isStarted() {
        return state == ArenaState.ACTIVE;
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public void add(Player player) {
        players.add(player.getName());
    }

    public void setSpectatingRoom(Location location) {
        spectating = location.clone();
    }

    public void setLobby(Location location) {
        lobby = location.clone();
    }

    public void setTreasureRoom(Location location) {
        treasure = location.clone();
    }

    public void setSpawn(Location location) {
        spawn = location.clone();
    }

    public void setMinPlayers(int players) {
        minPlayers = players;
    }

    public void setMaxPlayers(int players) {
        maxPlayers = players;
    }

    public void setType(ArenaType type){
        switch (type) {
            case FFA:
                setMinPlayers(2);
                setMaxPlayers(20);
                break;
            case FOURVFOUR:
                setMinPlayers(8);
                setMaxPlayers(8);
                break;
            case ONEVONE:
                setMinPlayers(2);
                setMinPlayers(2);
                break;
            case TWOVTWO:
                setMinPlayers(4);
                setMaxPlayers(4);
                break;
            case THREEVTHREE:
                setMinPlayers(6);
                setMaxPlayers(6);
                break;
            case SPLEEF:
                setMinPlayers(5);
                setMaxPlayers(15);
                break;

        }
    }
}
