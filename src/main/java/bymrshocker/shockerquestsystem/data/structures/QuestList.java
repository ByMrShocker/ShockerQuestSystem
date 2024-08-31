package bymrshocker.shockerquestsystem.data.structures;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class QuestList {
    String startQuest;
    List<String> quests;
    Component questGiverDisplayName;

    public void setQuestGiverDisplayName(Component questGiverDisplayName) {
        this.questGiverDisplayName = questGiverDisplayName;
    }

    public Component getQuestGiverDisplayName() {
        return questGiverDisplayName;
    }

    public QuestList() {
        quests = new ArrayList<>();
    }

    public void setStartQuest(String startQuest) {
        this.startQuest = startQuest;
    }

    public List<String> getQuests() {
        return quests;
    }

    public String getStartQuest() {
        return startQuest;
    }
}
