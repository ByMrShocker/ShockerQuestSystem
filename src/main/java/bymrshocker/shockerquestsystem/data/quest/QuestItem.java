package bymrshocker.shockerquestsystem.data.quest;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import jdk.jfr.Description;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class QuestItem {
    Material material;
    Component name;
    List<Component> lore;
    boolean inRaid = false;
    int customModelData;
    int amount = 1;
    Location spawnAtLocation;

    public QuestItem(ConfigurationSection section, ShockerQuestSystem plugin) {
        material = Material.valueOf(section.getString("material"));
        if (section.contains("name")) {
            name = plugin.getFunctionLibrary().getLegacyText(section.getString("name"));
        } else {
            name = Component.text("");
        }
        if (section.contains("lore")) {
            lore = plugin.getFunctionLibrary().makeLore(section.getStringList("lore"));
        } else {
            lore = new ArrayList<>();
        }
        if (section.contains("spawnAtLocation")) {
            spawnAtLocation = section.getLocation("spawnAtLocation");
        }
        if (section.contains("inRaid")) {
            inRaid = section.getBoolean("inRaid");
        }

        if (section.contains("customModelData")) {
            customModelData = section.getInt("customModelData");
        } else {
            customModelData = -1;
        }

        if (section.contains("amount")) {
            amount = section.getInt("amount");
        }


    }

    public QuestItem(ItemStack item, ShockerQuestSystem plugin) {
        material = item.getType();
        name = item.displayName();
        if (item.lore() != null) {
            lore = item.lore();
        } else {
            lore = new ArrayList<>();
        }
        inRaid = itemStackInRaid(item, plugin);
        if (item.getItemMeta().hasCustomModelData()) {
            customModelData = item.getItemMeta().getCustomModelData();
        } else {
            customModelData = -1;
        }
        amount = item.getAmount();
    }

    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (hasDisplayName(this)) {meta.displayName(name);}
        if (lore != null) {
            if (!lore.isEmpty()) {meta.lore(lore);}
        }
        if (customModelData != -1) {meta.setCustomModelData(customModelData);}
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }
    @Description("в вводных предмет в информации о квесте, а не предмет у игрока!")
    public boolean equalsItem(QuestItem questItem, ShockerQuestSystem plugin) {;
        if (questItem.inRaid) {
            if (questItem.inRaid != inRaid) return false;
        }
        if (questItem.material != material) return false;
        if (questItem.amount != amount) return false;
        if (questItem.customModelData != -1) {
            if (questItem.customModelData != customModelData) return false;
        }
        String serializedName = plugin.getFunctionLibrary().serializeText(questItem.name);
        if (hasDisplayName(questItem)) {
            if (!serializedName.equals(plugin.getFunctionLibrary().serializeText(name))) return false;
        }
        if (questItem.lore != null) {
            if (!questItem.lore.isEmpty()) {
                if (lore == null) return false;
                if (questItem.lore.size() != lore.size()) return false;
                for (int i = 0; i < lore.size(); i++) {
                    if (!plugin.getFunctionLibrary().serializeText(questItem.lore.get(i)).equals(plugin.getFunctionLibrary().serializeText(lore.get(i)))) return false;
                }
            }
        }

        return true;
    }

    private boolean hasDisplayName(QuestItem questItem) {
        return (!PlainTextComponentSerializer.plainText().serialize(questItem.name).isEmpty());
    }



    public boolean itemStackInRaid(ItemStack itemStack, ShockerQuestSystem plugin) {
        List<Component> lore = itemStack.lore();
        if (lore == null) return false;
        if (lore.isEmpty()) return false;
        String text = PlainTextComponentSerializer.plainText().serialize(lore.getLast());
        return text.contains(plugin.getConfigVariables().foundInRaidText);
    }

    public Location getSpawnAtLocation() {
        return spawnAtLocation;
    }
}
