package me.simonfoy.bedwars.instance;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.GameState;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public abstract class GameListener implements Listener {

    protected Game game;

    public GameListener(BedWars bedWars, Game game) {
        this.game = game;
        Bukkit.getPluginManager().registerEvents(this, bedWars);
    }

    public void start() {
        game.setState(GameState.IN_PROGRESS);
        game.sendMessage("Game has started!");

        onStart();
    }

    public abstract void onStart();

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

}
