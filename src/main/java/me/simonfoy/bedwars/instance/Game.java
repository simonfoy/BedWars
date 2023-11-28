package me.simonfoy.bedwars.instance;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.GameState;
import me.simonfoy.bedwars.manager.ScoreboardManager;
import me.simonfoy.bedwars.manager.SpawnManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Game {

    private BedWars bedWars;

    private Set<Location> blocksPlaced;
    private Set<BlockState> blocksDestroyed;
    private Location hub;
    private Location spawn;
    private GameState state;
    private List<UUID> players;
    private int requiredPlayers;
    private Countdown countdown;
    private GameTimer gameTimer;
    private BedWarsGame bedWarsGame;
    private HashMap<UUID, Team> teams;
    private List<BukkitTask> tasks;
    private HashMap<Team, Boolean> bedsAlive;
    private List<UUID> alive;
    private SpawnManager spawnManager;
    private HashMap<Team, BedLocation> beds;
    private ScoreboardManager scoreBoardManager;

    public Game(BedWars bedWars) {
        this.bedWars = bedWars;
        this.blocksPlaced = new HashSet<>();
        this.blocksDestroyed = new HashSet<>();
        this.hub = new Location(Bukkit.getWorld("world"), 0, 55, 0);
        this.spawn = new Location(Bukkit.getWorld("world"), 0, 55, 0);
        this.state = GameState.PREPARING;
        this.players = new ArrayList<>();
        this.requiredPlayers = 8;
        this.countdown = new Countdown(bedWars, this);
        this.gameTimer = new GameTimer(bedWars, this);
        this.bedWarsGame = new BedWarsGame(bedWars, this);
        this.teams = new HashMap<>();
        this.tasks = new ArrayList<>();
        this.bedsAlive = new HashMap<>();
        this.alive = new ArrayList<>();
        this.spawnManager = new SpawnManager(bedWars);
        this.beds = new HashMap<>();
        this.scoreBoardManager = new ScoreboardManager(bedWars);
    }

    /* BASE GAME INSTANCE LOGIC */

    public void start() {
        if (state != GameState.PREPARING && state != GameState.PRE_START) {
            return;
        }
        Bukkit.getWorld("world").getEntities().clear();

        World world = Bukkit.getWorld("world");
        if (world != null) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                }
            }
        }

        bedWarsGame.start();
        setState(GameState.IN_PROGRESS);

        for (Player players : Bukkit.getOnlinePlayers()) {
            getScoreBoardManager().clearScoreboard(players);
            getScoreBoardManager().setupGameStartScoreboard(players);
        }
        sendMessage(ChatColor.YELLOW + "Game is now in IN_PROGRESS State");
    }

    public void end() {
        if (state != GameState.IN_PROGRESS) {
            return;
        }
        setState(GameState.ENDING);
        sendMessage(ChatColor.YELLOW + "Game is now in ENDING State");

        cleanUp();
    }

    public void cleanUp() {
        if (state != GameState.ENDING) {
            return;
        }
        setState(GameState.CLEANING_UP);
        sendMessage(ChatColor.YELLOW + "Game is now in CLEANING_UP State");

        reset(true);
    }

    public void reset(boolean removePlayers) {
        List<UUID> currentPlayers = new ArrayList<>(players);

        if (removePlayers) {
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.teleport(hub);
                }
            }
            players.clear();
            alive.clear();
        }

        bedWarsGame.stopIronGenerators();
        bedWarsGame.stopGoldGenerators();
        bedWarsGame.stopDiamondGenerators();
        bedWarsGame.stopEmeraldGenerators();

        bedWarsGame.resetBeds();
        getTeams().clear();
        Bukkit.getWorld("world").getEntities().clear();
        resetChangedBlocks();
        sendTitle("", "");
        bedWarsGame.unregister();
        setState(GameState.PREPARING);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (currentPlayers.contains(player.getUniqueId())) {
                players.add(player.getUniqueId());
                getScoreBoardManager().clearScoreboard(player);
                getScoreBoardManager().setupGamePreparingScoreboard(player);

                player.setGameMode(GameMode.ADVENTURE);
                player.setHealth(20);
                player.setFoodLevel(20);
                player.teleport(spawn);
                player.closeInventory();
                player.getInventory().clear();

                for (Player players : Bukkit.getOnlinePlayers()) {
                    getScoreBoardManager().updatePlayerCounter();
                }
            }
        }

        if (countdown.isRunning()) {
            countdown.stop();
        }

        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }

        cancelTasks();
        Bukkit.getWorld("world").getEntities().clear();
        countdown = new Countdown(bedWars, this);
        gameTimer = new GameTimer(bedWars, this);
        bedWarsGame = new BedWarsGame(bedWars, this);

        if (players.size() >= requiredPlayers) {
            countdown.start();
            setState(GameState.PRE_START);
        }
    }

    /* UTILITIES */

    public void restart() {
        int countdownTime = 10;
        new BukkitRunnable() {
            int remaining = countdownTime;

            @Override
            public void run() {
                if (remaining > 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "Server is restarting in " + remaining + " seconds...");
                    remaining--;
                } else {
                    cancel();
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
                }
            }
        }.runTaskTimer(bedWars, 0L, 20L);
    }

    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
    }

    public void sendTitle(String title, String subtitle) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendTitle(title, subtitle);
        }
    }

    public void cancelTasks() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
    }

    public void resetChangedBlocks() {
        blocksPlaced.forEach(loc -> loc.getBlock().setType(Material.AIR));

        blocksDestroyed.forEach(blockState -> {
            Block block = blockState.getWorld().getBlockAt(blockState.getLocation());
            block.setType(blockState.getType(), false);
            block.setBlockData(blockState.getBlockData(), false);
        });

        blocksPlaced.clear();
        blocksDestroyed.clear();
    }

    /* PLAYER LOGIC */

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.teleport(spawn);
        player.closeInventory();
        player.getInventory().clear();
        getScoreBoardManager().clearScoreboard(player);
        getScoreBoardManager().setupGamePreparingScoreboard(player);

        for (Player players : Bukkit.getOnlinePlayers()) {
            getScoreBoardManager().updatePlayerCounter();
        }

        if (state.equals(GameState.PREPARING) && players.size() == requiredPlayers) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                getScoreBoardManager().clearScoreboard(players);
                getScoreBoardManager().setupGamePreStartScoreboard(players);
            }
            countdown.start();
            setState(GameState.PRE_START);
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.teleport(hub);
        getScoreBoardManager().clearScoreboard(player);
        player.sendTitle("", "");
        if (state == GameState.PREPARING) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                getScoreBoardManager().updatePlayerCounter();
            }
        }


        if (state == GameState.PRE_START && players.size() < requiredPlayers) {
            sendMessage(ChatColor.RED + "There is not enough players. Countdown stopped.");
            reset(false);
            return;
        }

        if (state == GameState.IN_PROGRESS && players.size() < requiredPlayers) {
            sendMessage(ChatColor.RED + "The game has ended as too many players have left.");
            end();
        }
    }

    public Set<Location> getBlocksPlaced() { return blocksPlaced; }
    public Set<BlockState> getBlocksDestroyed() { return blocksDestroyed; }
    public Location getSpawn() { return spawn; }
    public GameState getState() { return state; }
    public List<UUID> getPlayers() { return players; }
    public int getRequiredPlayers() { return requiredPlayers; }
    public BedWarsGame getBedWarsGame() { return bedWarsGame; }
    public Countdown getCountdown() { return countdown; }
    public GameTimer getGameTimer() { return gameTimer; }
    public HashMap<UUID, Team> getTeams() { return teams; }
    public List<BukkitTask> getTasks() { return tasks; }
    public HashMap<Team, Boolean> getBedsAlive() { return bedsAlive; }
    public List<UUID> getAlive() { return alive; }
    public SpawnManager getSpawnManager() { return spawnManager; }
    public HashMap<Team, BedLocation> getBeds() { return beds; }
    public ScoreboardManager getScoreBoardManager() { return scoreBoardManager; }
    public void setState(GameState state) { this.state = state; }

}
