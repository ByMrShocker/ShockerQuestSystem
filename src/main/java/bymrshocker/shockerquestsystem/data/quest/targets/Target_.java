package bymrshocker.shockerquestsystem.data.quest.targets;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.quest.QuestTarget;
import bymrshocker.shockerquestsystem.data.structures.QuestCheckResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Target_ implements QuestTarget {

    @Override
    public boolean ticking() {
        return false;
    }

    @Override
    public void loadData(ConfigurationSection section, ShockerQuestSystem plugin) {

    }

    @Override
    public boolean clearData(Player player) {
        return true;
    }

    @Override
    public QuestCheckResult canPlayerCompleteQuest(Player player, QuestData data, ShockerQuestSystem plugin) {
        QuestCheckResult result = new QuestCheckResult();
        return result;
    }

    @Override
    public int[] getProgress(Player player) {
        return new int[0];
    }

    @Override
    public void tick(ArrayList<String> players, ShockerQuestSystem plugin, QuestData data) {

    }

}
