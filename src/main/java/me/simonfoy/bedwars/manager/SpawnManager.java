package me.simonfoy.bedwars.manager;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.instance.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class SpawnManager {

    private HashMap<Team, Location> teamSpawns;
    private BedWars bedWars;

    public SpawnManager(BedWars bedWars) {
        this.bedWars = bedWars;
        this.teamSpawns = new HashMap<>();
        loadTeamSpawns();
    }

    private void loadTeamSpawns() {
        FileConfiguration config = bedWars.getConfig();
        ConfigurationSection spawnsSection = config.getConfigurationSection("spawns");
        if (spawnsSection != null) {
            for (String teamName : spawnsSection.getKeys(false)) {
                String locationString = spawnsSection.getString(teamName);
                Location loc = stringToLocation(locationString);
                teamSpawns.put(Team.valueOf(teamName.toUpperCase()), loc);
            }
        }
    }

    private Location stringToLocation(String locString) {
        String[] parts = locString.split(",");
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Location(world, x, y, z);
    }

    public Location getSpawn(Team team) {
        return teamSpawns.get(team);
    }

}