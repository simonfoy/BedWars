package me.simonfoy.bedwars.instance;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.GameState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BedWarsGame extends GameListener {

    public BedWarsGame(BedWars bedWars, Game game) {
        super(bedWars, game);
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
            teamsWithPlayers.add(team);

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
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        event.setCancelled(true);
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
