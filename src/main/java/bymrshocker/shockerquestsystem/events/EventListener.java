package bymrshocker.shockerquestsystem.events;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.InventoryHolderExpanded;
import bymrshocker.shockerquestsystem.data.structures.KillEntityData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class EventListener implements Listener {
    ShockerQuestSystem plugin;

    public EventListener(ShockerQuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void EntityDeathEvent(EntityDeathEvent event) {
        if (event.getDamageSource().getCausingEntity() instanceof Player) {
            Player player = (Player) event.getDamageSource().getCausingEntity();
            if (!plugin.getSavedTickData().getPlayerKilledEntities().containsKey(player.getName())) {
                plugin.getSavedTickData().getPlayerKilledEntities().put(player.getName(), new ArrayList<>());
            }

            plugin.getSavedTickData().getPlayerKilledEntities().get(player.getName()).add(new KillEntityData(event.getEntity(), player.getInventory().getItemInMainHand()));

        }
    }


    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        try {
            if (event.getClickedInventory().getHolder() instanceof InventoryHolderExpanded expanded) {
                expanded.getQuestsTabUI().onClickedInventory(event);
            }
        } catch (NullPointerException e) {

        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        try {
            if (event.getInventory().getHolder() instanceof InventoryHolderExpanded expanded) {
                expanded.getQuestsTabUI().onPreDestructInventory();
            }
        } catch (NullPointerException e) {

        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        String deadName = event.getPlayer().getName();
        plugin.getSavedTickData().getPlayersDead().add(deadName);
    }

    @EventHandler(priority =  EventPriority.MONITOR)
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        plugin.getDatabaseManager().loadQuestsForPlayer(event.getPlayer().getName());
    }

    @EventHandler(priority =  EventPriority.MONITOR)
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getDatabaseManager().unloadQuestsForPlayer(event.getPlayer().getName());
    }

}
