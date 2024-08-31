package bymrshocker.shockerquestsystem.commands;


import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public interface BaseCommandArg {

    HashMap<String, String[]> argListMap = new HashMap<>();

    List<String> getTabList(String key, ShockerQuestSystem plugin);

    String getName();

    String getDescription();

    String getSyntax();

    String getPermission();

    void execute (ShockerQuestSystem plugin, Player player, String[] args);


}
