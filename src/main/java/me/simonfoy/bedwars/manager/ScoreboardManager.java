package me.simonfoy.bedwars.manager;

import me.simonfoy.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreboardManager {

    private BedWars bedWars;

    public ScoreboardManager(BedWars bedWars) {
        this.bedWars = bedWars;
    }

    public void setupGamePreparingScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Preparing_BedWars", "dummy",
                ChatColor.AQUA + "" + ChatColor.BOLD + "BED WARS");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = formatter.format(today);
        Score date = objective.getScore(ChatColor.GRAY + formattedDate);
        date.setScore(10);

        Score space1 = objective.getScore(" ");
        space1.setScore(9);

        Team playerCount = scoreboard.registerNewTeam("playerCount");
        playerCount.addEntry(ChatColor.GOLD.toString());
        playerCount.setPrefix(ChatColor.WHITE + "Players: ");
        playerCount.setSuffix(ChatColor.GREEN + "" + bedWars.getGame().getPlayers().size() + "/" + bedWars.getGame().getRequiredPlayers());
        objective.getScore(ChatColor.GOLD.toString()).setScore(8);

        Score space2 = objective.getScore("  ");
        space2.setScore(7);

        Score waiting = objective.getScore(ChatColor.WHITE + "Waiting...");
        waiting.setScore(6);

        Score space3 = objective.getScore("   ");
        space3.setScore(5);

        Score mode = objective.getScore(ChatColor.WHITE + "Mode: " + ChatColor.GREEN + "Bed Wars");
        mode.setScore(4);

        Score version = objective.getScore(ChatColor.WHITE + "Version: " + ChatColor.GRAY + "v1.0");
        version.setScore(3);

        Score space4 = objective.getScore("    ");
        space4.setScore(2);

        Score website = objective.getScore(ChatColor.AQUA + "www.simonfoy.com");
        website.setScore(1);

        player.setScoreboard(scoreboard);
    }

    public void setupGamePreStartScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Pre_Start_BedWars", "dummy",
                ChatColor.AQUA + "" + ChatColor.BOLD + "Bed Wars");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = formatter.format(today);
        Score date = objective.getScore(ChatColor.GRAY + formattedDate);
        date.setScore(10);

        Score space1 = objective.getScore(" ");
        space1.setScore(9);

        Team playerCount = scoreboard.registerNewTeam("playerCount");
        playerCount.addEntry(ChatColor.GOLD.toString());
        playerCount.setPrefix(ChatColor.WHITE + "Players: ");
        playerCount.setSuffix(ChatColor.GREEN + "" + bedWars.getGame().getPlayers().size() + "/" + bedWars.getGame().getRequiredPlayers());
        objective.getScore(ChatColor.GOLD.toString()).setScore(8);

        Score space2 = objective.getScore("  ");
        space2.setScore(7);

        Team countdown = scoreboard.registerNewTeam("countdown");
        countdown.addEntry(ChatColor.BLACK.toString());
        countdown.setPrefix(ChatColor.WHITE + "Starting in: ");
        countdown.setSuffix(ChatColor.GREEN + "15");
        objective.getScore(ChatColor.BLACK.toString()).setScore(6);

        Score space3 = objective.getScore("   ");
        space3.setScore(5);

        Score mode = objective.getScore(ChatColor.WHITE + "Mode: " + ChatColor.GREEN + "Bed Wars");
        mode.setScore(4);

        Score version = objective.getScore(ChatColor.WHITE + "Version: " + ChatColor.GRAY + "v1.0");
        version.setScore(3);

        Score space4 = objective.getScore("    ");
        space4.setScore(2);

        Score website = objective.getScore(ChatColor.AQUA + "www.simonfoy.com");
        website.setScore(1);

        player.setScoreboard(scoreboard);
    }

    public void setupGameStartScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Start_BedWars", "dummy",
                ChatColor.AQUA + "" + ChatColor.BOLD + "Bed Wars");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = formatter.format(today);
        Score date = objective.getScore(ChatColor.GRAY + formattedDate);
        date.setScore(6);

        Team timer = scoreboard.registerNewTeam("timer");
        timer.addEntry(ChatColor.LIGHT_PURPLE.toString());
        timer.setPrefix(ChatColor.WHITE + "Time Left: ");
        timer.setSuffix(ChatColor.GREEN + "2:00");
        objective.getScore(ChatColor.LIGHT_PURPLE.toString()).setScore(5);

        Score space1 = objective.getScore(" ");
        space1.setScore(4);

        Score alive = objective.getScore(ChatColor.WHITE + "Players: " + ChatColor.AQUA + bedWars.getGame().getPlayers().size());
        alive.setScore(3);

        Score space3 = objective.getScore("   ");
        space3.setScore(2);

        Score website = objective.getScore(ChatColor.AQUA + "www.simonfoy.com");
        website.setScore(1);

        player.setScoreboard(scoreboard);
    }

    public void updateCountdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective("Pre_Start_BedWars");

            if (objective == null) {
                continue; //
            }

            int countdownSeconds = bedWars.getGame().getCountdown().getCountdownSeconds();

            int minutes = countdownSeconds / 60;
            int seconds = countdownSeconds % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);

            Team countdownTeam = objective.getScoreboard().getTeam("countdown");
            if (countdownTeam != null) {
                countdownTeam.setSuffix(ChatColor.GREEN + timeFormatted);
            }
        }
    }

    public void updateGameTimer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective("Start_BedWars");

            if (objective == null) {
                continue; //
            }

            int timerSeconds = bedWars.getGame().getGameTimer().getTimerSeconds();

            int minutes = timerSeconds / 60;
            int seconds = timerSeconds % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);

            Team timerTeam = objective.getScoreboard().getTeam("timer");
            if (timerTeam != null) {
                timerTeam.setPrefix(ChatColor.WHITE + "Time Elapsed: ");
                timerTeam.setSuffix(ChatColor.GREEN + timeFormatted);
            }
        }
    }

    public void clearScoreboard(Player player) {
        Scoreboard clearScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(clearScoreboard);
    }
}