package bymrshocker.shockerquestsystem.data;

import bymrshocker.shockerquestsystem.data.ui.QuestsTabUI;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class InventoryHolderExpanded implements InventoryHolder {

    QuestsTabUI questsTabUI;

    public InventoryHolderExpanded(QuestsTabUI questsTabUI) {
        this.questsTabUI = questsTabUI;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public void setQuestsTabUI(QuestsTabUI questsTabUI) {
        this.questsTabUI = questsTabUI;
    }

    public QuestsTabUI getQuestsTabUI() {
        return questsTabUI;
    }
}
