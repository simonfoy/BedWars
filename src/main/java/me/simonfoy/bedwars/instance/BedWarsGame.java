package me.simonfoy.bedwars.instance;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

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
        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getPlayer(player.getUniqueId()).closeInventory();
            Bukkit.getPlayer(player.getUniqueId()).teleport(game.getSpawn());
            Bukkit.getPlayer(player.getUniqueId()).setGameMode(GameMode.SURVIVAL);
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
}
