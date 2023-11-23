package me.simonfoy.bedwars.instance;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.GameState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
            player.closeInventory();
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
            player.setHealth(20);
            player.setFoodLevel(20);
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
        }

        game.getTasks().add(Bukkit.getScheduler().runTaskTimer((bedWars), () -> {
            for (UUID uuid : game.getAlive()) {
                if (Bukkit.getPlayer(uuid).getLocation().getY() <= game.getSpawnManager().getyRespawn()) {
                    death(Bukkit.getPlayer(uuid));
                }
            }
        }, 4, 4));
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (game != null && game.getState().equals(GameState.IN_PROGRESS)) {
            death(event.getEntity());
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
        if (game.getTeams().get(player.getUniqueId()) == team) {
             return true;
        }
        game.sendMessage(player.getName() + " has broken " + team.getName() + "'s bed!");
        game.getBedsAlive().put(team, false);
        return false;
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
