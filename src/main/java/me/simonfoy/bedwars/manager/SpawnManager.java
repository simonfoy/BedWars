package me.simonfoy.bedwars.manager;

import me.simonfoy.bedwars.BedWars;
import me.simonfoy.bedwars.instance.BedLocation;
import me.simonfoy.bedwars.instance.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class SpawnManager {

    private HashMap<Team, Location> teamSpawns;
    private HashMap<Team, BedLocation> bedSpawns;
    private int yRespawn;
    private BedWars bedWars;

    public SpawnManager(BedWars bedWars) {
        this.bedWars = bedWars;
        this.teamSpawns = new HashMap<>();
        this.bedSpawns = new HashMap<>();
        this.yRespawn = 0;
        loadSpawns();
    }

    private void loadSpawns() {
        FileConfiguration config = bedWars.getConfig();
        loadSpecificSpawns(config, "playerSpawns", teamSpawns);
        loadSpecificSpawns(config, "bedSpawns", bedSpawns);

        this.yRespawn = config.getInt("y-respawn", 0);
    }

    private void loadSpecificSpawns(FileConfiguration config, String sectionName, HashMap<Team, ?> spawnMap) {
        ConfigurationSection spawnsSection = config.getConfigurationSection(sectionName);
        if (spawnsSection != null) {
            for (String teamName : spawnsSection.getKeys(false)) {
                String locationString = spawnsSection.getString(teamName);
                if (sectionName.equals("bedSpawns")) {
                    BedLocation loc = stringToBedLocation(locationString);
                    bedSpawns.put(Team.valueOf(teamName.toUpperCase()), loc);
                } else {
                    Location loc = stringToLocation(locationString);
                    teamSpawns.put(Team.valueOf(teamName.toUpperCase()), loc);
                }
            }
        }
    }

    private Location stringToLocation(String locString) {
        String[] parts = locString.split(",");
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);

        float yaw = 0.0f;
        float pitch = 0.0f;

        if (parts.length >= 6) {
            yaw = Float.parseFloat(parts[4]);
            pitch = Float.parseFloat(parts[5]);
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    private BedLocation stringToBedLocation(String locString) {
        String[] parts = locString.split(",");
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        BlockFace facing = BlockFace.valueOf(parts[4].toUpperCase());

        return new BedLocation(world, x, y, z, facing);
    }

    public Location getPlayerSpawn(Team team) {
        return teamSpawns.get(team);
    }

    public BedLocation getBedSpawn(Team team) {
        return bedSpawns.get(team);
    }

    public int getyRespawn() { return yRespawn; }

}