package bymrshocker.shockerquestsystem.data.quest.targets;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.quest.QuestTarget;
import bymrshocker.shockerquestsystem.data.structures.QuestCheckResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Target_MoveToLocation implements QuestTarget {

    private List<Location> locations;
    private HashMap<String, ArrayList<Integer>> playerReachedLocationIndexes;
    private double radius;


    @Override
    public boolean ticking() {
        return true;
    }

    @Override
    public void loadData(ConfigurationSection section, ShockerQuestSystem plugin) {
        locations = new ArrayList<>();
        playerReachedLocationIndexes = new HashMap<>();
        radius = section.getDouble("checkRadius");
        for (String key : section.getConfigurationSection("locations").getKeys(false)) {
            locations.add(section.getConfigurationSection("locations").getLocation(key));
        }

    }

    @Override
    public boolean clearData(Player player) {
        if (playerReachedLocationIndexes.containsKey(player.getName())) {
            playerReachedLocationIndexes.remove(player.getName());
            return true;
        }
        return false;
    }

    @Override
    public QuestCheckResult canPlayerCompleteQuest(Player player, QuestData data, ShockerQuestSystem plugin) {
        QuestCheckResult result = new QuestCheckResult();
        if (playerReachedLocationIndexes.containsKey(player.getName())) {
            result.result = (playerReachedLocationIndexes.get(player.getName()).size() == locations.size());
        }

        return result;
    }


    @Override
    public int[] getProgress(Player player) {
        int current = 0;
        if (playerReachedLocationIndexes.containsKey(player.getName())) {
            current = playerReachedLocationIndexes.get(player.getName()).size();
        }

        return new int[]{current, locations.size()};
    }

    @Override
    public void tick(ArrayList<String> players, ShockerQuestSystem plugin, QuestData data) {
        for (String playerName : players) {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) continue;

            for (int i = 0; i < locations.size(); i++) {

                Location location = locations.get(i);
                if (location == null) {
                    System.out.println("ERR. Location is null in Target_MoveToLocation.tick()");
                    continue;
                }

                if (playerReachedLocationIndexes.containsKey(playerName)) {
                    if (playerReachedLocationIndexes.get(playerName).contains(i)) continue;
                }
                if (player.getWorld() != location.getWorld()) continue;
                if (player.getLocation().distance(location) <= radius) {
                    if (!playerReachedLocationIndexes.containsKey(playerName)) {
                        playerReachedLocationIndexes.put(playerName, new ArrayList<>());
                    }

                    ArrayList<Integer> list = playerReachedLocationIndexes.get(playerName);
                    list.add(i);
                    playerReachedLocationIndexes.put(playerName, list);
                    player.sendActionBar(plugin.getLegacyText(plugin.getConfigVariables().subtaskCompleteText));

                }
            }

        }
    }

}
