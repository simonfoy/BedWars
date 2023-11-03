package me.simonfoy.bedwars.instance;

import me.simonfoy.bedwars.BedWars;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer extends BukkitRunnable {

    private BedWars bedWars;
    private Game game;

    private boolean isRunning = false;

    private int gameTimer;

    public GameTimer(BedWars bedWars, Game game) {
        this.bedWars = bedWars;
        this.game = game;
        this.gameTimer = 0;
    }

    public void start() {
        isRunning = true;
        runTaskTimer(bedWars, 0, 20);
    }

    public void stop() {
        if (isRunning) {
            cancel();
            isRunning = false;
        }
    }

    @Override
    public void run() {
        if (gameTimer == 2700) {
            isRunning = false;
            cancel();
            game.sendMessage(ChatColor.RED + "The game has ended!");
            game.end();
            return;
        }

        if (gameTimer == 2475) {
            game.sendMessage(ChatColor.RED + "The game will end in 5 minutes!");
        }

        if (gameTimer >= 0) {
            bedWars.getGame().getScoreBoardManager().updateGameTimer();
        }

        gameTimer++;
    }

    public int getTimerSeconds() {
        return gameTimer;
    }
    public boolean isRunning() {
        return isRunning;
    }
}
