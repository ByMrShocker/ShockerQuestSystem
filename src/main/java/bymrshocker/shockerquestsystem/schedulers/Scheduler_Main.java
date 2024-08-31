package bymrshocker.shockerquestsystem.schedulers;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.quest.targets.Target_GetItems;
import org.bukkit.Bukkit;

public class Scheduler_Main {

    ShockerQuestSystem plugin;

    public Scheduler_Main(ShockerQuestSystem plugin) {
        this.plugin = plugin;
        int timer = plugin.getConfigVariables().tickTimer;
        if (plugin.getActiveQuests() == null) {
            System.out.println("ActiveQuests is null! Disabling timer!");
            return;
        }
        if (plugin.getActiveQuests().isEmpty()) {
            System.out.println("ActiveQuests is empty! Disabling timer!");
            return;
        }

        System.out.println("Starting Scheduler_Main with rate: " + timer);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, bukkitTask -> {


            for (QuestData quest : plugin.getActiveQuests().values()) {
                if (quest.getQuestTarget().ticking()) {
                    quest.getQuestTarget().tick(quest.getPlayersInProgress(), plugin, quest);
                }

                if (quest.getFailQuestConditions() != null) {
                    quest.checkFailedConditions(plugin);
                }

            }

        plugin.getSavedTickData().clear();

        }, timer, timer);


    }



}
