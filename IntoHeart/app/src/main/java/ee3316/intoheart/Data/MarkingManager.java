package ee3316.intoheart.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aahung on 4/12/15.
 */
public class MarkingManager {
    public Map<Integer, int[]> targetHRs = new HashMap<>();

    public MarkingManager() {
        targetHRs.put(20, new int[]{100, 170, 200});
        targetHRs.put(30, new int[]{95, 162, 190});
        targetHRs.put(35, new int[]{93, 157, 185});
        targetHRs.put(40, new int[]{90, 153, 180});
        targetHRs.put(45, new int[]{88, 149, 175});
    }

    public int mark[] = new int[]{100, 100, 100};

    private int matchTableAge(int age) {
        int tableAge = (Integer) targetHRs.keySet().toArray()[0];
        for (int key: targetHRs.keySet()) {
            if (age >= key) {
                tableAge = key;
                break;
            }
        }
        return tableAge;
    }

    public void evaluateExercise(int age, int ave) {
        int tableAge = matchTableAge(age);
        if (ave >= targetHRs.get(tableAge)[0])
            if (ave >= targetHRs.get(tableAge)[2])
                mark[0] = targetHRs.get(tableAge)[2] / ave * targetHRs.get(tableAge)[1] * 100 / ave;
            else if (ave >= targetHRs.get(tableAge)[1])
                mark[0] = targetHRs.get(tableAge)[1] * 100 / ave;
        else
            mark[0] = ave * 100 / targetHRs.get(tableAge)[0];
    }

    public void evaluateRest(int ave) {
        if (ave < 60) mark[1] = ave * 100 / 60;
        else if (ave > 100) mark[1] = 100 * 100 / ave;
        else mark[1] = 100;
    }

    public void evaluateLifestyle(float[] lifestyle) {
        mark[2] = 4 * (25 -
                (int)(lifestyle[0] + lifestyle[1] + lifestyle[2] + lifestyle[3] + lifestyle[4])
        );
    }

    public int getFinalMark() {
        return (int)(mark[0] * 0.3 + mark[1] * 0.5 + mark[2] * 0.2);
    }
}
