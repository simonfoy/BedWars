package me.simonfoy.bedwars.instance;

public enum Team {

    RED("Red"),
    BLUE("Blue"),
    GREEN("Green"),
    YELLOW("Yellow"),
    AQUA("Aqua"),
    PINK("Pink"),
    WHITE("White"),
    GRAY("Gray");




    private String name;
    Team(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
