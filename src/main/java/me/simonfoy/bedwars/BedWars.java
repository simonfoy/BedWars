package me.simonfoy.bedwars;

import me.simonfoy.bedwars.commands.GameCommand;
import me.simonfoy.bedwars.instance.Game;
import me.simonfoy.bedwars.listener.ConnectListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class BedWars extends JavaPlugin {

    private Game game;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        game = new Game(this);

        getCommand("game").setExecutor(new GameCommand(this, game));

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
