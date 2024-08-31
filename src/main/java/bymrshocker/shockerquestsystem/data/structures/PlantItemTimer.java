package bymrshocker.shockerquestsystem.data.structures;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class PlantItemTimer {
    int time;
    Location location;
    ItemStack itemStack;

    public PlantItemTimer(int time, Location location, ItemStack itemStack) {
        this.time = time;
        this.location = location;
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getTime() {
        return time;
    }

    public Location getLocation() {
        return location;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
