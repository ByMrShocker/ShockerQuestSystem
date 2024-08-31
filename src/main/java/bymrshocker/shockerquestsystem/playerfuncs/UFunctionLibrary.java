package bymrshocker.shockerquestsystem.playerfuncs;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UFunctionLibrary {
    ShockerQuestSystem plugin;

    public UFunctionLibrary(ShockerQuestSystem plugin) {
        this.plugin = plugin;
    }

    public List<Component> makeLore(List<String> strings) {
        List<Component> list = new ArrayList<>();
        for (String string : strings) {
            list.add(getLegacyText(string));
        }
        return list;
    }

    public Component getLegacyText(String text) {
        return LegacyComponentSerializer.legacy('&').deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public String serializeText(Component text) {
        return GsonComponentSerializer.gson().serialize(text);
    }



    public Component truncateText(Component component, int maxLength) {
        // Сериализуем компонент в строку без форматирования
        String plainText = PlainTextComponentSerializer.plainText().serialize(component);

        // Если текст короче или равен допустимой длине, возвращаем исходный компонент
        if (plainText.length() <= maxLength) {
            return component;
        }

        // Обрезаем текст до нужной длины
        String truncatedText = plainText.substring(0, maxLength - 3) + "...";

        // Создаем и возвращаем новый компонент на основе обрезанного текста
        return Component.text(truncatedText).color(TextColor.color(180, 180, 180));
    }


    public boolean isPlayersInRadius(Location location, double radius) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(location.getWorld())) {
                if (player.getLocation().distance(location) <= radius) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Player> getPlayersInRadius(Location location, double radius) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(location.getWorld())) {
                if (player.getLocation().distance(location) <= radius) {
                    players.add(player);
                }
            }
        }
        return players;
    }



}
