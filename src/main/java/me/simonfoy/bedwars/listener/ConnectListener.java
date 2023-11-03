package me.simonfoy.bedwars.listener;

import me.simonfoy.bedwars.BedWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectListener implements Listener {

    private BedWars bedWars;

    public ConnectListener(BedWars bedWars) {
        this.bedWars = bedWars;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        handlePlayerJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        handlePlayerQuit(event.getPlayer());
    }

    public void handlePlayerJoin(Player player) {
        bedWars.getGame().addPlayer(player);
    }

    public void handlePlayerQuit(Player player) {
        bedWars.getGame().removePlayer(player);
    }

}
