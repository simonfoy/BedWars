package me.simonfoy.bedwars;

import me.simonfoy.bedwars.instance.Game;
import me.simonfoy.bedwars.listener.ConnectListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class BedWars extends JavaPlugin {

    private Game game;

    @Override
    public void onEnable() {
        game = new Game(this);

        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Game getGame() {
        return game;
    }
}
