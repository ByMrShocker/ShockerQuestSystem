package bymrshocker.shockerquestsystem.playerfuncs;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.structures.QuestList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ConfigLoader {
    ShockerQuestSystem plugin;

    public ConfigLoader(ShockerQuestSystem plugin) {
        this.plugin = plugin;
    }

    public YamlConfiguration loadConfig(String name, String type) {
        File configFinder = new File(plugin.getDataFolder(), type);
        if (!configFinder.exists()) {
            configFinder.mkdir();
        }
        if (configFinder.listFiles() == null) {
            System.out.println("Config load failed, listFiles is null!");
        }


        if (configFinder.exists()) {
            for (File cur : Objects.requireNonNull(configFinder.listFiles())) {
                if (cur.getName().equals(name)) {
                    YamlConfiguration config = new YamlConfiguration();
                    try {
                        config.load(cur);
                        return config;
                    } catch (IOException | InvalidConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }


    public List<String> getConfigNamesByType(String type) {
        List<String> names = new ArrayList<>();
        File configFinder = new File(plugin.getDataFolder(), type);
        if (!configFinder.exists()) {
            configFinder.mkdir();
        }
        if (configFinder.listFiles() == null) {
            System.out.println("Config load failed, listFiles is null!");
        }


        if (configFinder.exists()) {
            for (File cur : Objects.requireNonNull(configFinder.listFiles())) {
                names.add(cur.getName());
            }
        }
        return names;
    }


    public void loadQuestsData() {
        for (String cfgName : getConfigNamesByType("quests")) {
            YamlConfiguration configuration = loadConfig(cfgName, "quests");
            String giver = configuration.getString("giverName");
            String displayName = configuration.getString("giverDisplayName");

            QuestList questList = new QuestList();
            questList.setStartQuest(giver + "-" + configuration.getString("startQuest"));
            questList.setQuestGiverDisplayName(plugin.getLegacyText(displayName));

            for (String key : configuration.getConfigurationSection("quests").getKeys(false)) {
                ConfigurationSection section = configuration.getConfigurationSection("quests").getConfigurationSection(key);
                QuestData questData = new QuestData(section, plugin, configuration.getString("giverName"));
                questList.getQuests().add(questData.getQuestID());
                plugin.getActiveQuests().put(questData.getQuestID(), questData);
            }
            plugin.getGiverQuests().put(giver, questList);
        }
    }



}
