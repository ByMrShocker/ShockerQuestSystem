package bymrshocker.shockerquestsystem.data.quest;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.structures.QuestCheckResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public interface QuestTarget {

    QuestCheckResult canPlayerCompleteQuest(Player player, QuestData data, ShockerQuestSystem plugin);

    void loadData(ConfigurationSection section, ShockerQuestSystem plugin);

    boolean clearData(Player player);

    void tick(ArrayList<String> players, ShockerQuestSystem plugin, QuestData data);

    int[] getProgress(Player player);

    boolean ticking();

}
