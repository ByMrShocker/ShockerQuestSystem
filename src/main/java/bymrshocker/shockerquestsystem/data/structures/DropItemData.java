package bymrshocker.shockerquestsystem.data.structures;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.quest.QuestItem;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;

public class DropItemData {
    HashMap<Location, List<QuestItem>> locations;

    public DropItemData() {
        locations = new HashMap<>();
    }

    public HashMap<Location, List<QuestItem>> getLocations() {
        return locations;
    }

    public void setLocations(HashMap<Location, List<QuestItem>> locations) {
        this.locations = locations;
    }

    public boolean isSimilarTo(DropItemData otherData, ShockerQuestSystem plugin) {
        int matches = 0;
        int total = 0;
        for (Location location : locations.keySet()) {
            if (!otherData.locations.containsKey(location)) return false;
            total = total + locations.get(location).size();

            for (QuestItem item : locations.get(location)) {
                for (QuestItem playerDropped : otherData.locations.get(location)) {
                    if (item.equalsItem(playerDropped, plugin)) {
                        matches++;
                        break;
                    }
                }

            }

        }
        return (matches == total);
    }
}
