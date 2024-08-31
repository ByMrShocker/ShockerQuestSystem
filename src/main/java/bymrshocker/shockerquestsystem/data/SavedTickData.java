package bymrshocker.shockerquestsystem.data;

import bymrshocker.shockerquestsystem.data.structures.KillEntityData;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SavedTickData {

    HashMap<String, ArrayList<KillEntityData>> playerKilledEntities = new HashMap<>();
    List<String> playersDead = new ArrayList<>();

    public SavedTickData() {

    }

    public void clear() {
        playerKilledEntities.clear();
    }



    public HashMap<String, ArrayList<KillEntityData>> getPlayerKilledEntities() {
        return playerKilledEntities;
    }

    public List<String> getPlayersDead() {
        return playersDead;
    }
}
