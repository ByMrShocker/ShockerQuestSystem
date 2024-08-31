package bymrshocker.shockerquestsystem;

import bymrshocker.shockerquestsystem.commands.CommandsHandler;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.SavedTickData;
import bymrshocker.shockerquestsystem.data.structures.QuestList;
import bymrshocker.shockerquestsystem.database.DatabaseManager;
import bymrshocker.shockerquestsystem.events.EventListener;
import bymrshocker.shockerquestsystem.playerfuncs.ConfigLoader;
import bymrshocker.shockerquestsystem.playerfuncs.ConfigVariables;
import bymrshocker.shockerquestsystem.playerfuncs.UFunctionLibrary;
import bymrshocker.shockerquestsystem.schedulers.Scheduler_Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class ShockerQuestSystem extends JavaPlugin {

    UFunctionLibrary functionLibrary;
    ConfigLoader configLoader;
    ConfigVariables configVariables;
    HashMap<String, QuestData> activeQuests;
    HashMap<String, QuestList> giverQuests;
    Scheduler_Main schedulerMain;
    SavedTickData savedTickData;
    DatabaseManager databaseManager;
    CommandsHandler commandsHandler;
    NamespacedKey questItemKey = new NamespacedKey(this, "questCommand");

    //TODO добавить возможность проверки значений в NBT. Например чтобы искало weaponID по всему NBT, не учитывая другие параметры
    //TODO и чтобы можно было ввести несколько значений для одного параметра.

    //TODO добавить Target для воровства чужих квестовых предметов

    //TODO добавить возможность активировать стартовый квест только после выполения квеста у другого торговца

    //TODO у Target_KillEntity добавить возможность проверять снаряжение энтити, его имя и префикс, если есть.


    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        functionLibrary = new UFunctionLibrary(this);
        configLoader = new ConfigLoader(this);
        configVariables = new ConfigVariables(getConfig());
        savedTickData = new SavedTickData();
        databaseManager = new DatabaseManager(this);
        commandsHandler = new CommandsHandler(this);
        activeQuests = new HashMap<>();
        giverQuests = new HashMap<>();
        configLoader.loadQuestsData();
        loadPlayersOnEnable();

        Objects.requireNonNull(getCommand("sqs")).setExecutor(commandsHandler);
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        schedulerMain = new Scheduler_Main(this);
    }

    private void loadPlayersOnEnable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            databaseManager.loadQuestsForPlayer(player.getName());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public ConfigVariables getConfigVariables() {
        return configVariables;
    }

    public void sendMessage(Player player, String message) {
        player.sendMessage(LegacyComponentSerializer.legacy('&').deserialize("&6[SAV] &f" + message).decoration(TextDecoration.ITALIC, false));
    }

    public Component getLegacyText(String text) {
        return LegacyComponentSerializer.legacy('&').deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public HashMap<String, QuestData> getActiveQuests() {
        return activeQuests;
    }

    public HashMap<String, QuestList> getGiverQuests() {
        return giverQuests;
    }

    public UFunctionLibrary getFunctionLibrary() {
        return functionLibrary;
    }

    public SavedTickData getSavedTickData() {
        return savedTickData;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public NamespacedKey getQuestItemKey() {
        return questItemKey;
    }
}
