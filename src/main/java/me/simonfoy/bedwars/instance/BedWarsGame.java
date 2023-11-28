package me.simonfoy.bedwars.instance;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.GameState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

public class BedWarsGame extends GameListener {

    private BedWars bedWars;

    private Set<Location> bedLocations = new HashSet<>();

    private Map<String, Integer> ironGeneratorTaskIds = new HashMap<>();
    private Map<String, Integer> goldGeneratorTaskIds = new HashMap<>();
    private Map<String, Integer> diamondGeneratorTaskIds = new HashMap<>();
    private Map<String, Integer> emeraldGeneratorTaskIds = new HashMap<>();

    public BedWarsGame(BedWars bedWars, Game game) {
        super(bedWars, game);

        this.bedWars = bedWars;
    }

    @Override
    public void onStart() {
        startBedWarsGame();
    }

    public void startBedWarsGame() {

        onBedWarsGameStart();
    }

    public void endBedWarsGame() {

        onBedWarsGameEnd();
    }

    public void onBedWarsGameStart() {
        HashSet<Team> teamsWithPlayers = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            onSpawn(player);
        }

        List<UUID> shuffledPlayers = new ArrayList<>(game.getPlayers());
        Collections.shuffle(shuffledPlayers);

        List<Team> shuffledTeams = new ArrayList<>(Arrays.asList(Team.values()));
        Collections.shuffle(shuffledTeams);

        for (int i = 0; i < shuffledPlayers.size(); i++) {
            UUID uuid = shuffledPlayers.get(i);
            Team team = shuffledTeams.get(i % shuffledTeams.size());
            game.getTeams().put(uuid, team);
            game.getBedsAlive().put(team, true);
            teamsWithPlayers.add(team);
            game.getAlive().add(uuid);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                onSpawn(player);
                player.teleport(game.getSpawnManager().getPlayerSpawn(team));
                player.sendMessage("You are on the " + team.name() + " team!");
            }
        }

        for (Team team : teamsWithPlayers) {
            BedLocation bedLocation = (BedLocation) game.getSpawnManager().getBedSpawn(team);

            Material bedMaterial = getBedMaterialByTeam(team);

            Block footBlock = bedLocation.getBlock();
            footBlock.setType(bedMaterial, false);

            Block headBlock = footBlock.getRelative(bedLocation.getFacing());
            headBlock.setType(bedMaterial, false);

            Bed footBedData = (Bed) footBlock.getBlockData();
            footBedData.setPart(Bed.Part.FOOT);
            footBedData.setFacing(bedLocation.getFacing());
            footBlock.setBlockData(footBedData, false);

            Bed headBedData = (Bed) headBlock.getBlockData();
            headBedData.setPart(Bed.Part.HEAD);
            headBedData.setFacing(bedLocation.getFacing());
            headBlock.setBlockData(headBedData, false);

            String teamIdentifier = team.name();
            footBlock.setMetadata("teamBed", new FixedMetadataValue(this.bedWars, teamIdentifier));
            headBlock.setMetadata("teamBed", new FixedMetadataValue(this.bedWars, teamIdentifier));

            bedLocations.add(footBlock.getLocation());
            bedLocations.add(headBlock.getLocation());

            if (teamsWithPlayers.contains(team)) {
                Location ironGeneratorLocation = game.getSpawnManager().getIronGeneratorSpawn(team);
                if (ironGeneratorLocation != null) {
                    startIronGenerator(teamIdentifier, ironGeneratorLocation);
                }

                Location goldGeneratorLocation = game.getSpawnManager().getGoldGeneratorSpawn(team);
                if (goldGeneratorLocation != null) {
                    startGoldGenerator(teamIdentifier, goldGeneratorLocation);
                }

                Location diamondGeneratorLocation = game.getSpawnManager().getDiamondGeneratorSpawn(team);
                if (diamondGeneratorLocation != null) {
                    startDiamondGenerator(teamIdentifier, diamondGeneratorLocation);
                }

                Location emeraldGeneratorLocation = game.getSpawnManager().getEmeraldGeneratorSpawn(team);
                if (emeraldGeneratorLocation != null) {
                    startEmeraldGenerator(teamIdentifier, emeraldGeneratorLocation);
                }
            }
        }
        Bukkit.getWorld("world").setTime(1000);
        game.getGameTimer().start();
    }

    public void onBedWarsGameEnd() {
        game.end();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!game.getState().equals(GameState.IN_PROGRESS)) {
            event.setCancelled(true);
            return;
        }

        if ((((Player) event.getEntity()).getPlayer().getHealth() - event.getFinalDamage()) <= 0) {
            event.setCancelled(true);

            death(((Player) event.getEntity()).getPlayer());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBedBreak(BlockBreakEvent event) {
        if (game != null && game.getState().equals(GameState.IN_PROGRESS)) {
            Block block = event.getBlock();
            if ((block.getType() == Material.RED_BED || block.getType() == Material.BLUE_BED || block.getType() == Material.GREEN_BED || block.getType() == Material.YELLOW_BED || block.getType() == Material.CYAN_BED || block.getType() == Material.PINK_BED || block.getType() == Material.WHITE_BED || block.getType() == Material.GRAY_BED && block.hasMetadata("teamBed"))) {
                event.setCancelled(destroyBed(Team.valueOf(block.getMetadata("teamBed").get(0).asString()), event.getPlayer()));

            }
        }
    }

    @EventHandler
    public void onBedPickUp(EntityPickupItemEvent event) {
        if (game != null && game.getState().equals(GameState.IN_PROGRESS)) {
            Item item = event.getItem();
            Material itemType = item.getItemStack().getType();

            if (

                    itemType == Material.RED_BED || itemType == Material.BLUE_BED ||
                    itemType == Material.GREEN_BED || itemType == Material.YELLOW_BED ||
                    itemType == Material.CYAN_BED || itemType == Material.PINK_BED ||
                    itemType == Material.WHITE_BED || itemType == Material.GRAY_BED

            ) {

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (game != null && game.getState().equals(GameState.IN_PROGRESS)) {
            death(event.getEntity());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        game.getBlocksPlaced().add(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();

        if (game.getBlocksPlaced().contains(loc)) {
            game.getBlocksPlaced().remove(loc);
        } else {
            event.setCancelled(true);
        }
    }

    private void startIronGenerator(String generatorIdentifier, Location location) {
        if (ironGeneratorTaskIds.containsKey(generatorIdentifier)) {
            int taskId = ironGeneratorTaskIds.get(generatorIdentifier);
            Bukkit.getScheduler().cancelTask(taskId);
        }

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this.bedWars,
                () -> spawnIronAtGenerator(location),
                0L,
                20L * 9
        );
        ironGeneratorTaskIds.put(generatorIdentifier, taskId);
    }

    public void stopIronGenerators() {
        for (Integer taskId : ironGeneratorTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        ironGeneratorTaskIds.clear();
    }

    private void spawnIronAtGenerator(Location location) {
        World world = location.getWorld();
        if (world != null) {
            ItemStack ironItem = new ItemStack(Material.IRON_INGOT);
            Item iron = world.dropItemNaturally(location, ironItem);
            iron.setPickupDelay(10);
        }
    }

    private void startGoldGenerator(String generatorIdentifier, Location location) {
        if (goldGeneratorTaskIds.containsKey(generatorIdentifier)) {
            int taskId = goldGeneratorTaskIds.get(generatorIdentifier);
            Bukkit.getScheduler().cancelTask(taskId);
        }

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this.bedWars,
                () -> spawnGoldAtGenerator(location),
                0L,
                20L * 9
        );
        goldGeneratorTaskIds.put(generatorIdentifier, taskId);
    }

    public void stopGoldGenerators() {
        for (Integer taskId : goldGeneratorTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        goldGeneratorTaskIds.clear();
    }

    private void spawnGoldAtGenerator(Location location) {
        World world = location.getWorld();
        if (world != null) {
            ItemStack goldItem = new ItemStack(Material.GOLD_INGOT);
            Item gold = world.dropItemNaturally(location, goldItem);
            gold.setPickupDelay(10);
        }
    }

    private void startDiamondGenerator(String generatorIdentifier, Location location) {
        if (diamondGeneratorTaskIds.containsKey(generatorIdentifier)) {
            int taskId = diamondGeneratorTaskIds.get(generatorIdentifier);
            Bukkit.getScheduler().cancelTask(taskId);
        }

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this.bedWars,
                () -> spawnDiamondAtGenerator(location),
                0L,
                20L * 9
        );
        diamondGeneratorTaskIds.put(generatorIdentifier, taskId);
    }

    public void stopDiamondGenerators() {
        for (Integer taskId : diamondGeneratorTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        diamondGeneratorTaskIds.clear();
    }

    private void spawnDiamondAtGenerator(Location location) {
        World world = location.getWorld();
        if (world != null) {
            ItemStack diamondItem = new ItemStack(Material.DIAMOND);
            Item diamond = world.dropItemNaturally(location, diamondItem);
            diamond.setPickupDelay(10);
        }
    }

    private void startEmeraldGenerator(String generatorIdentifier, Location location) {
        if (emeraldGeneratorTaskIds.containsKey(generatorIdentifier)) {
            int taskId = emeraldGeneratorTaskIds.get(generatorIdentifier);
            Bukkit.getScheduler().cancelTask(taskId);
        }

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this.bedWars,
                () -> spawnEmeraldAtGenerator(location),
                0L,
                20L * 9
        );
        emeraldGeneratorTaskIds.put(generatorIdentifier, taskId);
    }

    public void stopEmeraldGenerators() {
        for (Integer taskId : emeraldGeneratorTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        emeraldGeneratorTaskIds.clear();
    }

    private void spawnEmeraldAtGenerator(Location location) {
        World world = location.getWorld();
        if (world != null) {
            ItemStack emeraldItem = new ItemStack(Material.EMERALD);
            Item emerald = world.dropItemNaturally(location, emeraldItem);
            emerald.setPickupDelay(10);
        }
    }

    public void death(Player player) {
        Team team = game.getTeams().get(player.getUniqueId());
        if (game.getBedsAlive().get(team)) {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(game.getSpawnManager().getPlayerSpawn(team));
            game.sendMessage(player.getName() + " died and is respawning.");

            Bukkit.getScheduler().scheduleSyncDelayedTask(bedWars, new Runnable() {
                public void run() {
                    player.setGameMode(GameMode.SURVIVAL);
                    onSpawn(player);
                    player.teleport(game.getSpawnManager().getPlayerSpawn(team));
                }
            }, 100L);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(game.getSpawnManager().getPlayerSpawn(team));
            game.sendMessage(player.getName() + " has been eliminated and will no longer respawn!");
            game.getAlive().remove(player.getUniqueId());

            if (game.getAlive().size() == 1) {
                game.sendMessage(Bukkit.getPlayer(game.getAlive().get(0)).getName() + " has won!");
                endBedWarsGame();
            }
        }
    }

    public boolean destroyBed(Team team, Player player) {
        Team playerTeam = game.getTeams().get(player.getUniqueId());
        if (playerTeam == team) {
            return true;
        }

        Location bedLocation = game.getSpawnManager().getBedSpawn(team);

        if (bedLocation != null) {
            bedLocation.getBlock().setType(Material.AIR);
        }

        ChatColor teamColor = playerTeam.getChatColor();
        String coloredPlayerName = teamColor + player.getName();

        game.sendMessage(ChatColor.BOLD + "BED DESTRUCTION > " + team.getChatColor() + team.getName() + " Bed" + ChatColor.GRAY + " was destroyed by " + coloredPlayerName + ChatColor.GRAY + "!");
        game.getBedsAlive().put(team, false);
        game.getScoreBoardManager().updateScoreboardAfterBedDestruction(team);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
        }

        return false;
    }

    public void resetBeds() {
        bedLocations.forEach(loc -> loc.getBlock().setType(Material.AIR));
        bedLocations.clear();
    }

    public void onSpawn(Player player) {
        player.getInventory().clear();
        player.closeInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
        player.getInventory().addItem(new ItemStack(Material.WHITE_WOOL, 64));
        player.setHealth(20);
        player.setFoodLevel(20);
    }

    private Material getBedMaterialByTeam(Team team) {
        switch (team) {
            case RED:
                return Material.RED_BED;
            case BLUE:
                return Material.BLUE_BED;
            case GREEN:
                return Material.GREEN_BED;
            case YELLOW:
                return Material.YELLOW_BED;
            case CYAN:
                return Material.CYAN_BED;
            case PINK:
                return Material.PINK_BED;
            case WHITE:
                return Material.WHITE_BED;
            case GRAY:
                return Material.GRAY_BED;
            default:
                return Material.WHITE_BED;
        }
    }

}
