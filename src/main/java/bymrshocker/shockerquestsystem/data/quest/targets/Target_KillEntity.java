package bymrshocker.shockerquestsystem.data.quest.targets;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.quest.QuestItem;
import bymrshocker.shockerquestsystem.data.quest.QuestTarget;
import bymrshocker.shockerquestsystem.data.structures.KillEntityData;
import bymrshocker.shockerquestsystem.data.structures.QuestCheckResult;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class Target_KillEntity implements QuestTarget {

    EntityType type;
    Class<?> targetEntityClass = null;
    HashMap<String, Integer> playerKilled;
    int required;
    Location killInLocation;
    QuestItem useItem;
    HashMap<Integer, QuestItem> haveItemsInSlots;
    double radius;
    String entityCustomName;
    ShockerQuestSystem plugin;


    @Override
    public boolean ticking() {
        return true;
    }

    @Override
    public void loadData(ConfigurationSection section, ShockerQuestSystem plugin) {
        this.plugin = plugin;
        playerKilled = new HashMap<>();

        if (section.contains("entityType")) {
            type = EntityType.valueOf(section.getString("entityType"));
        } else if (section.contains("entityClass")) {
            try {
                targetEntityClass = Class.forName(section.getString("entityClass"));
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("WARNING! Invalid kill target Class name in " + section.getParent().getName() + " of giver " + section.getRoot().getString("giverName"));
            }
        } else {
            plugin.getLogger().warning("WARNING! Entity not specified in " + section.getParent().getName() + " of giver " + section.getRoot().getString("giverName"));
            plugin.getLogger().warning("Please use entityType or entityClass");
        }
        required = section.getInt("amount");
        if (section.contains("location")) {
            killInLocation = section.getLocation("location");
            radius = section.getDouble("radius");
        }

        if (section.contains("useItem")) {
            useItem = new QuestItem(section.getConfigurationSection("useItem"), plugin);
        }

        if (section.contains("checkPlayerSlots")) {
            haveItemsInSlots = new HashMap<>();
            for (String key : section.getConfigurationSection("slots").getKeys(false)) {
                haveItemsInSlots.put(Integer.parseInt(key), new QuestItem(section.getConfigurationSection("slots").getConfigurationSection(key), plugin));
            }
        }

        if (section.contains("entityName")) {
            entityCustomName = section.getString("entityName");
        }

    }

    @Override
    public boolean clearData(Player player) {
        return true;
    }

    @Override
    public QuestCheckResult canPlayerCompleteQuest(Player player, QuestData data, ShockerQuestSystem plugin) {
        QuestCheckResult result = new QuestCheckResult();
        if (playerKilled.containsKey(player.getName())) {
            result.result = (playerKilled.get(player.getName()) >= required);
        }
        return result;
    }

    @Override
    public int[] getProgress(Player player) {
        int current = 0;
        if (playerKilled.containsKey(player.getName())) {
            current = playerKilled.get(player.getName());
        }
        return new int[]{current, required};
    }

    @Override
    public void tick(ArrayList<String> players, ShockerQuestSystem plugin, QuestData data) {
        for (String playerName : players) {
            if (!plugin.getSavedTickData().getPlayerKilledEntities().containsKey(playerName)) continue;
            ArrayList<KillEntityData> entities = plugin.getSavedTickData().getPlayerKilledEntities().get(playerName);
            if (!playerKilled.containsKey(playerName)) {
                playerKilled.put(playerName, 0);
            }
            int current = playerKilled.get(playerName);
            for (KillEntityData killData : entities) {
                Entity entity = killData.getEntity();
                if (targetEntityClass != null) {
                    if (!targetEntityClass.isInstance(entity)) continue;
                } else {
                    if (entity.getType() != type) continue;
                }

                if (entityCustomName != null) {
                    if (entity.customName() == null) continue;
                    if (!PlainTextComponentSerializer.plainText().serialize(entity.customName()).equals(entityCustomName)) continue;
                }

                if (useItem != null) {
                    QuestItem questItem = new QuestItem(killData.getUsedItem(), plugin);
                    if (!questItem.equalsItem(useItem, plugin)) continue;
                }
                boolean result = true;
                if (haveItemsInSlots != null) {
                    for (int slot : haveItemsInSlots.keySet()) {
                        Player player = Bukkit.getPlayer(playerName);
                        if (player == null) {
                            result = false;
                            break;
                        }
                        if (player.getInventory().getItem(slot) == null) {
                            result = false;
                            break;
                        }

                        QuestItem questItem = new QuestItem(Objects.requireNonNull(player.getInventory().getItem(slot)), plugin);
                        if (!questItem.equalsItem(haveItemsInSlots.get(slot), plugin)) {
                            result = false;
                            break;
                        }
                    }
                }

                if (!result) continue;

                if (killInLocation != null) {
                    if (entity.getWorld() != killInLocation.getWorld()) continue;
                    if (entity.getLocation().distance(killInLocation) > radius) continue;
                }

                current++;
            }
            if (current != playerKilled.get(playerName)) {
                Player playerToSend = Bukkit.getPlayer(playerName);

                if (playerToSend != null) {
                    playerToSend.sendActionBar(plugin.getLegacyText(plugin.getConfigVariables().subtaskCompleteText));
                }

                playerKilled.put(playerName, current);
            }
        }
    }

}
