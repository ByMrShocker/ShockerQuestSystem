package bymrshocker.shockerquestsystem.data.structures;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class KillEntityData {
    Entity entity;
    ItemStack usedItem;

    public KillEntityData(Entity entity, ItemStack withItem) {
        this.entity = entity;
        this.usedItem = withItem;
    }

    public Entity getEntity() {
        return entity;
    }

    public ItemStack getUsedItem() {
        return usedItem;
    }
}
