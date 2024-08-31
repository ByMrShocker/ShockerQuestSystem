package bymrshocker.shockerquestsystem.data.structures;

import java.util.ArrayList;
import java.util.List;

public class QuestCheckResult {

    public boolean result;
    public List<Integer> inventorySlots;



    public QuestCheckResult() {
        result = false;
        inventorySlots = new ArrayList<>();
    }
}
