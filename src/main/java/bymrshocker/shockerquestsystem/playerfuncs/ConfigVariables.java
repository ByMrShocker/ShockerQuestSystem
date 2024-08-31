package bymrshocker.shockerquestsystem.playerfuncs;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigVariables {
    public String foundInRaidText;
    public int tickTimer;
    public String newQuestText;
    public String inProgressText;
    public String acceptQuestUIText;
    public String backUIText;
    public String completeQuestUIText;
    public String infoUIText;
    public String infoMessageText;
    public String subtaskCompleteText;
    public String failMessageText;
    public String plantItemMessageText;
    public String plantItemCancelText;
    public String plantItemCompleteText;
    public int ItemLoreMaxLengthUI;
    public double checkItemSpawnRadius;
    public int checkItemSpawnTimerTicks;
    public int respawnItemDelay;

    public ConfigVariables(FileConfiguration config) {
        tickTimer = config.getInt("tickTimer");
        checkItemSpawnRadius = config.getDouble("checkItemSpawnRadius");
        checkItemSpawnTimerTicks = config.getInt("checkItemSpawnTimerTicks");
        respawnItemDelay = config.getInt("respawnItemDelay");

        foundInRaidText = config.getConfigurationSection("display").getString("foundInRaidText");
        newQuestText = config.getConfigurationSection("display").getString("newQuestText");
        inProgressText = config.getConfigurationSection("display").getString("inProgressText");
        acceptQuestUIText = config.getConfigurationSection("display").getString("acceptQuestUIText");
        completeQuestUIText = config.getConfigurationSection("display").getString("completeQuestUIText");
        backUIText = config.getConfigurationSection("display").getString("backUIText");
        infoUIText = config.getConfigurationSection("display").getString("infoUIText");
        infoMessageText = config.getConfigurationSection("display").getString("infoMessageText");
        ItemLoreMaxLengthUI = config.getConfigurationSection("display").getInt("ItemLoreMaxLengthUI");
        subtaskCompleteText = config.getConfigurationSection("display").getString("subtaskCompleteText");
        plantItemMessageText = config.getConfigurationSection("display").getString("plantItemMessageText");
        plantItemCancelText = config.getConfigurationSection("display").getString("plantItemCancelText");
        plantItemCompleteText = config.getConfigurationSection("display").getString("plantItemCompleteText");
        failMessageText = config.getConfigurationSection("display").getString("failMessageText");
    }

}
