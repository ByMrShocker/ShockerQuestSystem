package bymrshocker.shockerquestsystem.data.quest.targets;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.quest.QuestItem;
import bymrshocker.shockerquestsystem.data.quest.QuestTarget;
import bymrshocker.shockerquestsystem.data.structures.QuestCheckResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

public class Target_GetItems implements QuestTarget {


    List<QuestItem> requiredItems;
    boolean ticking = false;
    int tickDelay;
    int playerFoundDelay = 0;

    @Override
    public void loadData(ConfigurationSection section, ShockerQuestSystem plugin) {
        requiredItems = new ArrayList<>();
        for (String key : section.getConfigurationSection("requiredItems").getKeys(false)) {
            QuestItem item = new QuestItem(section.getConfigurationSection("requiredItems").getConfigurationSection(key), plugin);
            requiredItems.add(item);
            if (item.getSpawnAtLocation() != null) {
                ticking = true;
            }
        }
    }

    @Override
    public boolean clearData(Player player) {
        return true;
    }

    @Override
    public boolean ticking() {
        return ticking;
    }

    @Override
    public QuestCheckResult canPlayerCompleteQuest(Player player, QuestData data, ShockerQuestSystem plugin) {
        QuestCheckResult result = new QuestCheckResult();
        result.result = false;
        result.inventorySlots = null;
        return result;
    }

    @Override
    public int[] getProgress(Player player) {
        return new int[0];
    }

    @Override
    public void tick(ArrayList<String> players, ShockerQuestSystem plugin, QuestData data) {
        List<QuestItem> itemsToSpawn = new ArrayList<>();
        double radius = plugin.getConfigVariables().checkItemSpawnRadius;
        for (QuestItem item : requiredItems) {
            if (item.getSpawnAtLocation() != null) {
                List<Player> playersInRadius = plugin.getFunctionLibrary().getPlayersInRadius(item.getSpawnAtLocation(), radius);
                if (playersInRadius.isEmpty()) continue;

                boolean hasPlayersInProgress = false;
                for (Player player : playersInRadius) {
                    if (data.getPlayersInProgress().contains(player.getName())) {
                        hasPlayersInProgress = true;
                        break;
                    }
                }
                if (hasPlayersInProgress) {
                    itemsToSpawn.add(item);
                }

            }
        }

        if (tickDelay > 0 || playerFoundDelay > 0) {
            tickDelay--;
            playerFoundDelay--;
            return;
        } else {
            tickDelay = plugin.getConfigVariables().checkItemSpawnTimerTicks;
        }


        Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
            boolean itemSpawned = false;
            for (QuestItem item : itemsToSpawn) {
                Location location = item.getSpawnAtLocation();
                if (location.isWorldLoaded()) {
                    ItemFrame itemFrame = null;
                    Collection<ItemFrame> itemFrames = location.getNearbyEntitiesByType(ItemFrame.class, 2);
                    boolean ignore = false;
                    if (!itemFrames.isEmpty()) {
                        for (ItemFrame frame : itemFrames) {

                            if (frame.getFacing() != BlockFace.UP) continue;

                            if (frame.getItem().equals(item.getItemStack())) {
                                ignore = true;
                                break;
                            } else if (frame.getItem().isEmpty()) {
                                itemFrame = frame;
                            }
                        }
                    }
                    if (ignore) continue;

                    if (itemFrame == null) {
                        itemFrame = (ItemFrame) location.getWorld().spawnEntity(location, EntityType.ITEM_FRAME);
                    }

                    itemFrame.setInvisible(true);
                    itemFrame.setFacingDirection(BlockFace.UP);
                    itemFrame.setItem(item.getItemStack());
                    itemSpawned = true;
                }
            }
            if (itemSpawned) {
                playerFoundDelay = plugin.getConfigVariables().respawnItemDelay;
            }
        });




    }



    private int isPartOfRequiredItems(ItemStack item, ShockerQuestSystem plugin, Collection<Integer> ignoreIndex) {
        QuestItem questItem = new QuestItem(item, plugin);

        for (int i = 0; i < requiredItems.size(); i++) {
            if (ignoreIndex.contains(i)) continue;
            if (requiredItems.get(i).equalsItem(questItem, plugin)) {
                return i;
            }
        }
        return -1;
    }

    public List<QuestItem> getRequiredItems() {
        return requiredItems;
    }
}
