package bymrshocker.shockerquestsystem.data;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.quest.QuestItem;
import bymrshocker.shockerquestsystem.data.quest.QuestTarget;
import bymrshocker.shockerquestsystem.data.quest.targets.*;
import bymrshocker.shockerquestsystem.data.structures.QuestCheckResult;
import bymrshocker.shockerquestsystem.events.EventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class QuestData {
    Component name;
    List<Component> lore;
    Component completeText;
    List<String> unlockQuests;
    List<QuestItem> rewardItems = new ArrayList<>();
    List<QuestItem> startItems;
    double rewardReputation = 0;
    double failReputation = 0;
    QuestTarget questTarget;
    String questID;
    ArrayList<String> playersInProgress = new ArrayList<>();
    List<String> failQuestConditions;
    String questGiver;


    public QuestData(ConfigurationSection section, ShockerQuestSystem plugin, String giverName) {
        questGiver = giverName;
        name = plugin.getLegacyText(section.getString("name"));
        lore = plugin.getFunctionLibrary().makeLore(section.getStringList("lore"));
        completeText = plugin.getLegacyText(section.getString("completeText"));
        unlockQuests = new ArrayList<>();
        for (String string : section.getStringList("unlockQuests")) {
            if (!string.contains("-")) {
                unlockQuests.add(questGiver + "-" + string);
            }
        }
        questTarget = createQuestTarget(section.getString("questTargetType"), plugin);
        questTarget.loadData(section.getConfigurationSection("target"), plugin);
        rewardReputation = section.getDouble("rewardReputation");
        if (section.contains("failReputation")) {
            failReputation = section.getDouble("failReputation");
        } else {
            failReputation = rewardReputation * -1;
        }
         for (String key : section.getConfigurationSection("rewardItems").getKeys(false)) {
            QuestItem item = new QuestItem(section.getConfigurationSection("rewardItems").getConfigurationSection(key), plugin);
            rewardItems.add(item);
        }

        if (section.contains("startItems")) {
            startItems = new ArrayList<>();
            for (String key : section.getConfigurationSection("startItems").getKeys(false)) {
                QuestItem item = new QuestItem(section.getConfigurationSection("startItems").getConfigurationSection(key), plugin);
                startItems.add(item);
            }
        }

        if (section.contains("failConditions")) {
            failQuestConditions = section.getStringList("failConditions");
        }


        questID = (questGiver + "-" + section.getName());
        System.out.println("Loaded quest " + questID);
    }


    private QuestTarget createQuestTarget(String targetType, ShockerQuestSystem plugin) {
        switch (targetType) {
            default: {
                System.out.println("WARNING! Quest " + questID + " has invalid questTargetType! Plugin will cause Null exceptions!");
                return null;
            }
            case "getItems": {
                return new Target_GetItems();
            }
            case "killEntity": {
                return new Target_KillEntity();
            }
            case "moveToLocation": {
                return new Target_MoveToLocation();
            }
            case "dropItems": {
                Target_DropItems target = new Target_DropItems(this);
                Bukkit.getPluginManager().registerEvents(target, plugin);
                return target;
            }
        }
    }


    public void checkFailedConditions(ShockerQuestSystem plugin) {
        for (String player : playersInProgress) {
            if (checkFailCondition(player, plugin)) {
                failQuestForPlayer(player, plugin);
            }
        }
    }

    private boolean checkFailCondition(String player, ShockerQuestSystem plugin) {
        for (String condition : failQuestConditions) {
            switch (condition) {
                default: {break;}
                case "playerDeath": {
                    if (plugin.getSavedTickData().getPlayersDead().contains(player)) {
                        return true;
                    }
                    break;
                }
                case "playerKillEntity": {
                    if (plugin.getSavedTickData().getPlayerKilledEntities().containsKey(player)) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }




    public void failQuestForPlayer(String player, ShockerQuestSystem plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            Player playerReference = Bukkit.getPlayer(player);
            if (playerReference != null) {
                plugin.sendMessage(playerReference, plugin.getConfigVariables().failMessageText.replace("%questName%",
                        PlainTextComponentSerializer.plainText().serialize(name)));
            }
            List<String> playerActiveQuests = plugin.getDatabaseManager().getActiveQuestsFromPlayer(player);
            playerActiveQuests.remove(questID);
            plugin.getDatabaseManager().setActiveQuestsForPlayer(player, playerActiveQuests);
            plugin.getDatabaseManager().addReputationForPlayer(player, getQuestGiver(), getFailReputation());
            questTarget.clearData(playerReference);
            playersInProgress.remove(player);
            System.out.println("Updated database for player " + player + ". Executed by: FailQuest");
        });
    }



    public Component getName() {
        return name;
    }

    public List<Component> getLore() {
        return lore;
    }

    public List<QuestItem> getRewardItems() {
        return rewardItems;
    }

    public List<String> getUnlockQuests() {
        return unlockQuests;
    }

    public QuestTarget getQuestTarget() {
        return questTarget;
    }

    public String getQuestID() {
        return questID;
    }

    public ArrayList<String> getPlayersInProgress() {
        return playersInProgress;
    }

    public Component getCompleteText() {
        return completeText;
    }

    public double getRewardReputation() {
        return rewardReputation;
    }

    public String getQuestGiver() {
        return questGiver;
    }

    public List<QuestItem> getStartItems() {
        return startItems;
    }

    public List<String> getFailQuestConditions() {
        return failQuestConditions;
    }

    public double getFailReputation() {
        return failReputation;
    }
}
