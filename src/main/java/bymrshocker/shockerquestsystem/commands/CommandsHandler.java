package bymrshocker.shockerquestsystem.commands;


import bymrshocker.shockerquestsystem.commands.args.*;
import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandsHandler implements TabExecutor {

    private final ArrayList<BaseCommandArg> argCommands = new ArrayList<>();
    private final ShockerQuestSystem plugin;

    public CommandsHandler(ShockerQuestSystem plugin){
        //список говна
        argCommands.add(new sqs_test());
        argCommands.add(new sqs_clear());
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {


        if (args.length > 0) {
            for (BaseCommandArg argCommand : argCommands) {
                if (args[0].equalsIgnoreCase(argCommand.getName())) {
                    if (commandSender instanceof Player)
                        if (commandSender.hasPermission(argCommand.getPermission())) argCommand.execute(plugin, (Player) commandSender, args);
                    return true;
                }
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length <= 1) {
            List<String> list = argCommands.stream().map(BaseCommandArg::getName).filter(name -> name.startsWith(strings[0])).toList();
            return list;
        }
        if (true) { //strings.length == 2
            for (BaseCommandArg argCommand : argCommands) {
                if (strings[0].equalsIgnoreCase(argCommand.getName())) {
                    if (commandSender instanceof Player)
                        if (commandSender.hasPermission(argCommand.getPermission())) {
                            //return argCommand.getTabList(strings[strings.length - 2]);
                            return argCommand.getTabList(strings[strings.length - 2], plugin).stream().filter(synt -> synt.startsWith(strings[strings.length -1])).toList();
                        }
                    return Collections.emptyList();
                }
            }
        }

        return new ArrayList<>();
    }
}
