package bymrshocker.shockerquestsystem.data.quest.targets;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.quest.QuestItem;
import bymrshocker.shockerquestsystem.data.quest.QuestTarget;
import bymrshocker.shockerquestsystem.data.structures.DropItemData;
import bymrshocker.shockerquestsystem.data.structures.PlantItemTimer;
import bymrshocker.shockerquestsystem.data.structures.QuestCheckResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Target_DropItems implements QuestTarget, Listener {

    DropItemData dropItemData;
    QuestData questData;
    HashMap<String, DropItemData> playerProgress;
    HashMap<Player, PlantItemTimer> plantItemTimerData;
    double radius;
    int plantTime = 0;
    ShockerQuestSystem plugin;

    public Target_DropItems(QuestData data) {
        questData = data;
    }


    @Override
    public boolean ticking() {
        return true;
    }

    @Override
    public void loadData(ConfigurationSection section, ShockerQuestSystem plugin) {
        this.plugin = plugin;
        dropItemData = new DropItemData();
        playerProgress = new HashMap<>();
        radius = section.getDouble("radius");
        for (String key : section.getConfigurationSection("locations").getKeys(false)) {
            List<QuestItem> questItems = new ArrayList<>();
            Location location = null;
            for (String keyItem : section.getConfigurationSection("locations").getConfigurationSection(key).getKeys(false)) {
                if (keyItem.equals("location")) {
                    location = section.getConfigurationSection("locations").getConfigurationSection(key).getLocation("location");
                } else {
                    questItems.add(new QuestItem(section.getConfigurationSection("locations").getConfigurationSection(key).getConfigurationSection(keyItem), plugin));
                }
            }

            dropItemData.getLocations().put(location, questItems);
        }

        if (section.contains("plantTime")) {
            plantTime = section.getInt("plantTime");
            plantItemTimerData = new HashMap<>();
        }


    }

    @Override
    public boolean clearData(Player player) {
        if (playerProgress.containsKey(player.getName())) {
            playerProgress.remove(player.getName());
            return true;
        }
        return false;
    }

    @Override
    public QuestCheckResult canPlayerCompleteQuest(Player player, QuestData data, ShockerQuestSystem plugin) {
        QuestCheckResult result = new QuestCheckResult();
        if (playerProgress.containsKey(player.getName())) {
            result.result = playerProgress.get(player.getName()).isSimilarTo(dropItemData, plugin);
        }

        return result;
    }


    @Override
    public int[] getProgress(Player player) {
        int current = 0;
        if (playerProgress.containsKey(player.getName())) {
            current = playerProgress.get(player.getName()).getLocations().size();
        }

        return new int[]{current, dropItemData.getLocations().size()};
    }

    @Override
    public void tick(ArrayList<String> players, ShockerQuestSystem plugin, QuestData data) {
        if (plantItemTimerData == null) return;
        List<Player> keysToRemove = new ArrayList<>();
        for (Player player : plantItemTimerData.keySet()) {

            PlantItemTimer plantData = plantItemTimerData.get(player);

            player.sendActionBar(plugin.getLegacyText(plugin.getConfigVariables().plantItemMessageText.replace("%plantTime%", String.valueOf(plantData.getTime()))));

            if (!player.isOnline() || player.isDead() ||
                    !player.getLocation().toVector().equals(plantData.getLocation().toVector())) {
                cancelPlant(plantData, player);
                keysToRemove.add(player);
                continue;
            }

            if (plantData.getTime() <= 0) {
                player.sendActionBar(plugin.getLegacyText(plugin.getConfigVariables().plantItemCompleteText));
                keysToRemove.add(player);
                continue;
            }

            plantData.setTime(plantData.getTime() - 1);

        }

        for (Player player : keysToRemove) {
            plantItemTimerData.remove(player);
        }

    }

    private void cancelPlant(PlantItemTimer plantData, Player player) {
        Item itemEntity = (Item) plantData.getLocation().getWorld().spawnEntity(plantData.getLocation(), EntityType.ITEM);
        itemEntity.setItemStack(plantData.getItemStack());
        itemEntity.setPickupDelay(2);
        player.sendActionBar(plugin.getLegacyText(plugin.getConfigVariables().plantItemCancelText));
    }



    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerDropItemEvent(PlayerDropItemEvent event) {
        if (questData.getPlayersInProgress().contains(event.getPlayer().getName())) {
            Item itemEntity = event.getItemDrop();
            ItemStack item = itemEntity.getItemStack();
            QuestItem droppedQuestItem = new QuestItem(item, plugin);

            for (Location location : dropItemData.getLocations().keySet()) {
                if (location.getWorld() != itemEntity.getWorld()) continue;
                if (location.distance(itemEntity.getLocation()) > radius) continue;

                for (QuestItem questItem : dropItemData.getLocations().get(location)) {
                    if (!droppedQuestItem.equalsItem(questItem, plugin)) continue;



                    if (!playerProgress.containsKey(event.getPlayer().getName())) {
                        playerProgress.put(event.getPlayer().getName(), new DropItemData());
                    }

                    DropItemData playerDropData = playerProgress.get(event.getPlayer().getName());

                    if (!playerDropData.getLocations().containsKey(location)) {
                        playerDropData.getLocations().put(location, new ArrayList<>());
                    }

                    for (QuestItem check : playerDropData.getLocations().get(location)) {
                        if (droppedQuestItem.equalsItem(check, plugin)) return;
                    }

                    if (plantTime > 0) {
                        plantItemTimerData.put(event.getPlayer(), new PlantItemTimer(plantTime, event.getPlayer().getLocation(), item));
                        itemEntity.remove();
                        return;
                    }

                    playerDropData.getLocations().get(location).add(droppedQuestItem);
                    event.getPlayer().sendActionBar(plugin.getLegacyText(plugin.getConfigVariables().subtaskCompleteText));

                    itemEntity.remove();
                    return;
                }
            }


        }
    }

}
