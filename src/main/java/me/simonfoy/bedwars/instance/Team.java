package me.simonfoy.bedwars.instance;

import org.bukkit.ChatColor;

public enum Team {

    RED("Red", ChatColor.RED),
    BLUE("Blue", ChatColor.BLUE),
    GREEN("Green", ChatColor.GREEN),
    YELLOW("Yellow", ChatColor.YELLOW),
    CYAN("Cyan", ChatColor.AQUA),
    PINK("Pink", ChatColor.LIGHT_PURPLE),
    WHITE("White", ChatColor.WHITE),
    GRAY("Gray", ChatColor.GRAY);

    private final String name;
    private final ChatColor chatColor;

    Team(String name, ChatColor chatColor) {
        this.name = name;
        this.chatColor = chatColor;
    }

    public String getName() {
        return name;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }
}
