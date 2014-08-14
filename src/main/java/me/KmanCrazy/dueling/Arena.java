package me.KmanCrazy.dueling;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.*;
import org.bukkit.util.Vector;

import java.util.*;

public class Arena {
    private static Random random = new Random();

    private ArenaState state = ArenaState.LOBBY;
    private Set<String> players = new HashSet<String>();
    private List<Location> spawns = new ArrayList<Location>();
    private final Plugin plugin;

    private Location spectating;
    //private Location spawn;
    private Location treasure;
    private Location lobby;

    private Vector randomizeSpawn;

    private int maxPlayers;
    private int minPlayers;

    private String arenaType;

    public Arena(Plugin plugin) {
        this.plugin = plugin;
    }

    public Arena(Plugin plugin, Location location, int min, int max,String type) {
        this(plugin);
        spectating = location.clone();
        treasure = location.clone();
        lobby = location.clone();
        spawns.add(location.clone());

        maxPlayers = max;
        minPlayers = min;

        arenaType = type;
    }

    public static String fromVector(Vector vector) {
        if (vector == null) return "";
        return vector.getX() + "," + vector.getY() + "," + vector.getZ();
    }

    public static Vector toVector(Object o) {
        if (o instanceof Vector) {
            return (Vector)o;
        }
        if (o instanceof String) {
            try {
                String[] pieces = StringUtils.split((String)o, ',');
                double x = Double.parseDouble(pieces[0]);
                double y = Double.parseDouble(pieces[1]);
                double z = Double.parseDouble(pieces[2]);
                return new Vector(x, y, z);
            } catch(Exception ex) {
                return null;
            }
        }
        return null;
    }

    public String fromLocation(Location location) {
        if (location == null) return "";
        if (location.getWorld() == null) return "";
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName()
                + "," + location.getYaw() + "," + location.getPitch();
    }

    public Location toLocation(Object o) {
        if (o instanceof Location) {
            return (Location)o;
        }
        if (o instanceof String) {
            try {
                float pitch = 0;
                float yaw = 0;
                String[] pieces = StringUtils.split((String)o, ',');
                double x = Double.parseDouble(pieces[0]);
                double y = Double.parseDouble(pieces[1]);
                double z = Double.parseDouble(pieces[2]);
                World world = null;
                if (pieces.length > 3) {
                    world = Bukkit.getWorld(pieces[3]);
                } else {
                    world = Bukkit.getWorlds().get(0);
                }
                if (pieces.length > 5) {
                    yaw = Float.parseFloat(pieces[4]);
                    pitch = Float.parseFloat(pieces[5]);
                }
                return new Location(world, x, y, z, yaw, pitch);
            } catch(Exception ex) {
                return null;
            }
        }
        return null;
    }

    public void load(ConfigurationSection configuration) {
        minPlayers = configuration.getInt("minplayers");
        maxPlayers = configuration.getInt("maxplayers");

        arenaType = configuration.getString("type");

        spectating = new Location(
                plugin.getServer().getWorld(configuration.getString("spec.world")),
                configuration.getInt("spec.x"),
                configuration.getInt("spec.y"),
                configuration.getInt("spec.z")
        );
        spectating.setPitch(configuration.getInt("spec.pitch"));
        spectating.setYaw(configuration.getInt("spec.yaw"));

        lobby = new Location(
                plugin.getServer().getWorld(configuration.getString("lobby.world")),
                configuration.getInt("lobby.x"),
                configuration.getInt("lobby.y"),
                configuration.getInt("lobby.z")
        );
        lobby.setPitch(configuration.getInt("lobby.pitch"));
        lobby.setYaw(configuration.getInt("lobby.yaw"));

        if (configuration.contains("spawn.world")) {
            Location legacySpawn = new Location(
                    plugin.getServer().getWorld(configuration.getString("spawn.world")),
                    configuration.getInt("spawn.x"),
                    configuration.getInt("spawn.y"),
                    configuration.getInt("spawn.z")
            );
            spawns.add(legacySpawn);
        }

        for (String s : configuration.getStringList("spawns")){
            spawns.add(toLocation(s));
        }

        treasure = new Location(
                plugin.getServer().getWorld(configuration.getString("treasureroom.world")),
                configuration.getInt("treasureroom.x"),
                configuration.getInt("treasureroom.y"),
                configuration.getInt("treasureroom.z")

        );
        treasure.setPitch(configuration.getInt("treasureroom.pitch"));
        treasure.setYaw(configuration.getInt("treasureroom.yaw"));
        if (configuration.contains("randomize.spawn")) {
            randomizeSpawn = toVector(configuration.getString("randomize.spawn"));
        }
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("minplayers", minPlayers);
        configuration.set("maxplayers", maxPlayers);

        configuration.set("type",arenaType);

        configuration.set("spec.world", spectating.getWorld().getName());
        configuration.set("spec.x", spectating.getBlockX());
        configuration.set("spec.y", spectating.getBlockY());
        configuration.set("spec.z", spectating.getBlockZ());
        configuration.set("spec.pitch", spectating.getPitch());
        configuration.set("spec.yaw", spectating.getYaw());

        configuration.set("lobby.world", lobby.getWorld().getName());
        configuration.set("lobby.x", lobby.getBlockX());
        configuration.set("lobby.y", lobby.getBlockY());
        configuration.set("lobby.z", lobby.getBlockZ());
        configuration.set("lobby.pitch", lobby.getPitch());
        configuration.set("lobby.yaw", lobby.getYaw());

        List<String> spawnList = new ArrayList<String>();
        for (Location spawn : spawns) {
            spawnList.add(fromLocation(spawn));
        }
        configuration.set("spawns", spawnList);

        configuration.set("treasureroom.world", treasure.getWorld().getName());
        configuration.set("treasureroom.x", treasure.getBlockX());
        configuration.set("treasureroom.y", treasure.getBlockY());
        configuration.set("treasureroom.z", treasure.getBlockZ());
        configuration.set("treasureroom.pitch", treasure.getPitch());
        configuration.set("treasureroom.yaw", treasure.getYaw());

        if (randomizeSpawn != null) {
            configuration.set("randomize.spawn", fromVector(randomizeSpawn));
        }
    }

    public void start() {
        state = ArenaState.ACTIVE;
        Server server = plugin.getServer();
        int num = 0;
        for (String playerName : players) {
            Player player = server.getPlayer(playerName);
            player.sendMessage("Begin!");

            Location spawn = spawns.get(num);
            if (randomizeSpawn != null) {
                spawn = spawn.clone();
                spawn.add
                (
                    (2 * random.nextDouble() - 1) * randomizeSpawn.getX(),
                    (2 * random.nextDouble() - 1) * randomizeSpawn.getY(),
                    (2 * random.nextDouble() - 1) * randomizeSpawn.getZ()
                );
            }

            // Wrap index around to player
            num = (num + 1) % spawns.size();
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
        int playerCount = players.size();
        if (playerCount < minPlayers) {
            String message = ChatColor.AQUA + String.valueOf(playerCount) + ChatColor.GOLD + "/" + ChatColor.AQUA + String.valueOf(maxPlayers) + " players.";
            messagePlayers(message);
        }
    }

    public void messagePlayers(String message) {
        Server server = plugin.getServer();
        Collection<String> names = new ArrayList<String>(players);
        for (String playerName : names) {
            Player player = server.getPlayer(playerName);
            if (player == null) {
                players.remove(playerName);
            } else {
                player.sendMessage(message);
            }
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
        player.teleport(lobby);
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

    public void addSpawn(Location location) {
        spawns.add(location.clone());
    }

    public void removeSpawn(Location location) {
        Location l = location;
        int range = 3;
        int minX = l.getBlockX() - range / 2;
        int minY = l.getBlockY() - range / 2;
        int minZ = l.getBlockZ() - range / 2;

        for (int x = minX; x < minX + range; x++) {
            for (int y = minY; y < minY + range; y++) {
                for (int z = minZ; z < minZ + range; z++) {
                    Location loc = location.getWorld().getBlockAt(x, y, z).getLocation();
                    if (spawns.contains(loc)) {
                        spawns.remove(loc);
                    }
                }
            }
        }
    }

    public void setMinPlayers(int players) {
        minPlayers = players;
    }

    public void setMaxPlayers(int players) {
        maxPlayers = players;
    }

    public void setType(ArenaType types){
        switch (types) {
            case FFA:
                setMinPlayers(2);
                setMaxPlayers(20);
                arenaType = types.name();
                break;
            case FOURVFOUR:
                setMinPlayers(8);
                setMaxPlayers(8);
                arenaType = types.name();
                break;
            case ONEVONE:
                setMinPlayers(2);
                setMinPlayers(2);
                arenaType = types.name();
                break;
            case TWOVTWO:
                setMinPlayers(4);
                setMaxPlayers(4);
                arenaType = types.name();
                break;
            case THREEVTHREE:
                setMinPlayers(6);
                setMaxPlayers(6);
                arenaType = types.name();
                break;
            case SPLEEF:
                setMinPlayers(5);
                setMaxPlayers(15);
                arenaType = types.name();
                break;
        }
    }

    public String getType(){
        return arenaType;
    }

    public void setRandomizeSpawn(Vector vector) {
        randomizeSpawn = vector;
    }
}
