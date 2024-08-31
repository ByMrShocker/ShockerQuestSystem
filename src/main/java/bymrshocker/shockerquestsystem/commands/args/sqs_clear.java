package bymrshocker.shockerquestsystem.commands.args;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.commands.BaseCommandArg;
import bymrshocker.shockerquestsystem.data.ui.QuestsTabUI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class sqs_clear implements BaseCommandArg {

    @Override
    public List<String> getTabList(String key, ShockerQuestSystem plugin) {
        List<String> toReturn = new  ArrayList<>();
        toReturn.add("PlayerName or leave it empty to clear yourself");
        return toReturn;
    }

    @Override
    public String getName() { return "clear"; }

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
            String name = player.getName();
            if (args.length == 2) {
                name = args[1];
            }
            int id = plugin.getDatabaseManager().findIdByColumnValue("player", name);
            if (id == -1) {
                plugin.sendMessage(player, "Player not found in database!");
                return;
            }
            plugin.getDatabaseManager().removeIdFromDB(id);
            plugin.sendMessage(player, "Removed " + name + " from database with id: " + id);
        }
    }


}
