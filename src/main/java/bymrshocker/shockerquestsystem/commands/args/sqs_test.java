package bymrshocker.shockerquestsystem.commands.args;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.commands.BaseCommandArg;
import bymrshocker.shockerquestsystem.data.quest.QuestTarget;
import bymrshocker.shockerquestsystem.data.ui.QuestsTabUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class sqs_test implements BaseCommandArg {

    @Override
    public List<String> getTabList(String key, ShockerQuestSystem plugin) {
        List<String> toReturn = new  ArrayList<>();
        toReturn.add("openMenu");
        return toReturn;
    }

    @Override
    public String getName() { return "test"; }

    @Override
    public String getDescription() { return "example"; }

    @Override
    public String getSyntax() { return "/sav example"; }

    @Override
    public String getPermission() {
        return "sqs.admin";
    }

    @Override
    public void execute(ShockerQuestSystem plugin, Player player, String[] args) {
        if (player.isValid()) {
            //execute code
            if (args.length < 3) {
                plugin.sendMessage(player, "Usage /sqs test [command] [id]");
                return;
            }
            switch (args[1]) {
                default: {
                    plugin.sendMessage(player, "Usage /sqs test [command]");
                    return;
                }
                case "openMenu": {
                    QuestsTabUI tab = new QuestsTabUI(plugin, args[2], player);
                    player.openInventory(tab.getInventory());
                    return;
                }
            }
        }
    }


}
