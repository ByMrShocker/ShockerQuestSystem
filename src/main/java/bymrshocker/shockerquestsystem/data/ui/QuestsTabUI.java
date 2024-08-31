package bymrshocker.shockerquestsystem.data.ui;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.InventoryHolderExpanded;
import bymrshocker.shockerquestsystem.data.QuestData;
import bymrshocker.shockerquestsystem.data.quest.QuestItem;
import bymrshocker.shockerquestsystem.data.quest.targets.Target_GetItems;
import bymrshocker.shockerquestsystem.data.structures.QuestList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestsTabUI {
    Inventory inventory;
    Player player;
    ShockerQuestSystem plugin;
    ItemStack blockItem;
    ItemStack backItem;
    String currentTab;
    String giverId;
    List<String> playerCompletedQuests = new ArrayList<>();
    List<String> playerActiveQuests = new ArrayList<>();
    QuestList questList;
    QuestData currentQuestData;
    String currentQuestID;
    List<Integer> handledSlots = new ArrayList<>();
    int size = 54;
    ItemStack currentQuestItem;

    List<Integer> questItemSlots = new ArrayList<>(List.of(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));


    public QuestsTabUI(ShockerQuestSystem plugin, String id, Player player) {
        if (!plugin.getGiverQuests().containsKey(id)) {
            plugin.sendMessage(player, "this id doesn't contains in giverQuests map!");
            return;
        }
        this.plugin = plugin;
        this.player = player;
        giverId = id;
        questList = plugin.getGiverQuests().get(id);
        inventory = Bukkit.createInventory(new InventoryHolderExpanded(this), size, questList.getQuestGiverDisplayName());
        createBlockItem();
        createBackItem();


        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            playerCompletedQuests.addAll(plugin.getDatabaseManager().getCompletedQuestsFromPlayer(player.getName()));
            playerActiveQuests.addAll(plugin.getDatabaseManager().getActiveQuestsFromPlayer(player.getName()));
            createTab("main", null);
        });


    }

    public void closeInventory() {
        player.closeInventory();
    }

    public void onClickedInventory(InventoryClickEvent event) {
        event.setCancelled(!canClickOnSlot(event.getSlot()));

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, bukkitTask -> {
            update();
        }, 1);

        if (event.getCurrentItem() == null) return;
        PersistentDataContainer pdc = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
        if (!pdc.has(plugin.getQuestItemKey())) return;
        currentQuestItem = event.getCurrentItem();
        clickCommand(pdc.get(plugin.getQuestItemKey(), PersistentDataType.STRING).split(">"));

    }

    private void clickCommand(String[] command) {
        switch (command[0]) {
            default: {return;}
            case "open": {
                createTab(command[1], command);
                break;
            }
            case "quest": {
                clickCommandQuest(command);
            }
        }
    }

    private void clickCommandQuest(String[] command) {
        switch (command[1]) {
            default: {
                System.out.println("WARNING! Detected invalid command input " + command[1] + " for QuestsTabUI.clickCommandQuest()! ");
                System.out.println("UI owner = " + player.getName() + "; currentTab = " + currentTab + "; current questID = " + currentQuestID);
                return;
            }
            case "accept": {
                playerAcceptQuest();
                closeInventory();
                break;
            }
            case "info": {
                showPlayerQuestInfo();
                closeInventory();
                break;
            }
            case "complete": {
                if (checkCanCompleteQuest()) {
                    playerCompleteQuest();
                }
                break;
            }
        }
    }



    private void playerAcceptQuest() {
        loadQuestData(currentQuestID);
        if (currentQuestID == null || currentQuestData == null) {

            System.out.println("WARNING! Player " + player.getName() + " trying accept null quest!");
            System.out.println("QuestID: " + currentQuestID);
            return;
        }

        if (currentQuestData.getStartItems() != null) {
            for (QuestItem item : currentQuestData.getStartItems()) {
                player.getInventory().addItem(item.getItemStack());
            }
        }


        playerActiveQuests.add(currentQuestID);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            plugin.getDatabaseManager().setActiveQuestsForPlayer(player.getName(), playerActiveQuests);
            currentQuestData.getPlayersInProgress().add(player.getName());
            showPlayerQuestInfo();
        });
    }


    private void playerCompleteQuest() {
        if (!currentQuestData.getRewardItems().isEmpty()) {
            for (QuestItem questItem : currentQuestData.getRewardItems()) {
                player.getInventory().addItem(questItem.getItemStack());
            }
        }

        playerCompletedQuests.add(currentQuestID);
        playerActiveQuests.remove(currentQuestID);

        currentQuestData.getPlayersInProgress().remove(player.getName());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            plugin.getDatabaseManager().setCompletedQuestsForPlayer(player.getName(), playerCompletedQuests);
            plugin.getDatabaseManager().setActiveQuestsForPlayer(player.getName(), playerActiveQuests);
            plugin.getDatabaseManager().addReputationForPlayer(player.getName(), currentQuestData.getQuestGiver(), currentQuestData.getRewardReputation());
            System.out.println("Updated database for player " + player.getName() + ". Executed by: CompleteQuest");
        });

        plugin.sendMessage(player, "");
        player.sendMessage(currentQuestData.getCompleteText());
        inventory.clear();
        closeInventory();
    }



    private boolean canClickOnSlot(int id) {
        switch (currentTab) {
            default: return false;
            case "quest": {
                if (currentQuestData == null) return false;
                if (currentQuestData.getQuestTarget() instanceof Target_GetItems) {
                    return questItemSlots.contains(id);
                }
            }
        }
        return false;
    }



    public void createTab(String tab, String[] command) {
        onPreDestructInventory();
        currentTab = tab;
        handledSlots.clear();
        inventory.clear();

        switch (tab) {
            default: {
                plugin.sendMessage(player, "&4ERROR! &4Invalid tab name in QuestsTabUI.createTab().tab");
                return;
            }
            case "main": {
                createBlockedSlots();
                createQuestsListTab();
                break;
            }
            case "quest": {
                createBlockedSlots();
                createQuestInfoTab(command[2]);
                break;
            }
        }


    }

    public void onPreDestructInventory() {
        if (currentTab == null) return;
        if (currentTab.equals("quest")) {
            for (int id : questItemSlots) {
                if (inventory.getItem(id) != null) {
                    player.getInventory().addItem(Objects.requireNonNull(inventory.getItem(id)));
                }
            }
        }
    }



    private void createQuestInfoTab(String questID) {
        loadQuestData(questID);
        inventory.setItem(0, backItem);
        currentQuestID = questID;
        if (playerActiveQuests.contains(questID)) {
            inventory.setItem(41, createCompleteQuestItem());

        } else {
            inventory.setItem(41, createAcceptQuestItem());
        }

        inventory.setItem(39, createInfoQuestItem());

        if (currentQuestItem != null) {
            inventory.setItem(13, currentQuestItem);
        }


    }


    private void showPlayerQuestInfo() {
        String infoMessageText = plugin.getConfigVariables().infoMessageText;
        infoMessageText = infoMessageText.replace("%questName%", "&6" + PlainTextComponentSerializer.plainText().serialize(currentQuestData.getName()) + "&r&f");
        plugin.sendMessage(player, infoMessageText);
        for (Component text : currentQuestData.getLore()) {
            player.sendMessage(text);
        }
    }



    private void loadQuestData(String questID) {
        if (!plugin.getActiveQuests().containsKey(questID)) {
            System.out.println("WARNING! Trying to load invalid questId = " + questID);
            System.out.println("WARNING! Giver " + giverId + " contains invalid questID! Please fix it!");
            return;
        }
        currentQuestID = questID;
        currentQuestData = plugin.getActiveQuests().get(questID);
    }

    private void createBlockedSlots() {
        for (int i = 0; i < size; i++) {
            if (questItemSlots.contains(i)) continue;
            inventory.setItem(i, blockItem);
        }
    }


    private void createQuestsListTab() {
        if (!playerCompletedQuests.contains(questList.getStartQuest())) {
            createDefaultQuests();
            return;
        }
        //TODO если есть закрытые квесты
        for (String completed : playerCompletedQuests) {
            for (String quest : questList.getQuests()) {
                if (playerCompletedQuests.contains(quest)) continue;
                if (plugin.getActiveQuests().get(completed).getUnlockQuests().contains(quest)) {
                    addQuestItem(quest);
                }
            }
        }



    }

    public void update() {
        if (currentTab.equals("quest")) {
            if (currentQuestID == null) return;
            if (!playerActiveQuests.contains(currentQuestID)) return;
            inventory.setItem(41, createCompleteQuestItem());
        }
    }


    private boolean checkCanCompleteQuest() {
        if (currentQuestData == null) {
            return false;
        }

        if (currentQuestData.getQuestTarget() instanceof Target_GetItems getItems) {
            List<QuestItem> questItems = getItems.getRequiredItems();
            int found = 0;
            List<Integer> ignoreSlots = new ArrayList<>();

            for (QuestItem questItem : questItems) {
                for (int i : questItemSlots) {
                    if (ignoreSlots.contains(i)) continue;
                    ItemStack itemInSlot = inventory.getItem(i);
                    if (itemInSlot == null) continue;
                    QuestItem item = new QuestItem(itemInSlot, plugin);
                    if (item.equalsItem(questItem, plugin)) {
                        found++;
                        ignoreSlots.add(i);
                        break;
                    }
                }
            }
            return (questItems.size() == found);

        }


        return currentQuestData.getQuestTarget().canPlayerCompleteQuest(player, currentQuestData, plugin).result;
    }



    private void createDefaultQuests() {
        addQuestItem(questList.getStartQuest());
    }

    private void addQuestItem(String questID) {
        QuestData questData = plugin.getActiveQuests().get(questID);
        for (int id : questItemSlots) {
            if (handledSlots.contains(id)) continue;
            inventory.setItem(id, createQuestButtonItem(questData, playerActiveQuests.contains(questID)));
            handledSlots.add(id);
            break;
        }

    }


    private ItemStack createQuestButtonItem(QuestData data, boolean active) {
        ItemStack item;
        ItemMeta meta = backItem.getItemMeta();
        if (active) {
            item = new ItemStack(Material.BOOK);
            int[] progress = data.getQuestTarget().getProgress(player);
            if (progress.length == 2) {
                meta.displayName(data.getName().append(plugin.getLegacyText(plugin.getConfigVariables().inProgressText)).append(Component.text(" [" + progress[0] + "/" + progress[1] + "]")));
            } else {
                meta.displayName(data.getName().append(plugin.getLegacyText(plugin.getConfigVariables().inProgressText)));
            }
        } else {
            item = new ItemStack(Material.ENCHANTED_BOOK);
            meta.displayName(data.getName().append(plugin.getLegacyText(plugin.getConfigVariables().newQuestText)));
        }

        meta.getPersistentDataContainer().set(plugin.getQuestItemKey(), PersistentDataType.STRING, "open>quest>" + data.getQuestID());

        meta.lore(getTruncatedLore(data.getLore()));
        item.setItemMeta(meta);
        return item;
    }

    private List<Component> getTruncatedLore(List<Component> lore) {
        if (lore.isEmpty()) return lore;
        Component text = lore.get(0);
        List<Component> newText = new ArrayList<>();
        newText.add(plugin.getFunctionLibrary().truncateText(text, 32));
        return newText;
    }


    private ItemStack createAcceptQuestItem() {
        ItemStack itemStack = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(plugin.getLegacyText(plugin.getConfigVariables().acceptQuestUIText));
        meta.getPersistentDataContainer().set(plugin.getQuestItemKey(), PersistentDataType.STRING, "quest>accept");
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack createCompleteQuestItem() {
        ItemStack itemStack;
        if (checkCanCompleteQuest()) {
            itemStack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        } else {
            itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(plugin.getLegacyText(plugin.getConfigVariables().completeQuestUIText));
        meta.getPersistentDataContainer().set(plugin.getQuestItemKey(), PersistentDataType.STRING, "quest>complete");
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack createInfoQuestItem() {
        ItemStack itemStack = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(plugin.getLegacyText(plugin.getConfigVariables().infoUIText));
        meta.getPersistentDataContainer().set(plugin.getQuestItemKey(), PersistentDataType.STRING, "quest>info");
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void createBackItem() {
        backItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = backItem.getItemMeta();
        meta.displayName(plugin.getLegacyText(plugin.getConfigVariables().backUIText));
        meta.getPersistentDataContainer().set(plugin.getQuestItemKey(), PersistentDataType.STRING, "open>main");
        backItem.setItemMeta(meta);
    }

    private void createBlockItem() {
        blockItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = blockItem.getItemMeta();
        meta.displayName(Component.text(""));
        blockItem.setItemMeta(meta);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
