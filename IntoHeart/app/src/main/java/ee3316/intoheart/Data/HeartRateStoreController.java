package ee3316.intoheart.Data;

import android.app.Activity;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aahung on 4/10/15.
 */
public class HeartRateStoreController {

    private Activity activity;

    public HeartRateContract heartRateContract;

    public HeartRateStoreController(Activity activity) {
        this.activity = activity;
        heartRateContract = new HeartRateContract(activity);
    }

    private List<Integer> stagingHRs = new ArrayList<>();
    private long lastUnixTime = 0;

    private long MSEC_PER_10_MINS = 600000;

    // save the hr and add timestamp
    public void addHR(int hr) {
        long unixTime = System.currentTimeMillis();
        stagingHRs.add(hr);
        if (stagingHRs.size() == 1) {
            lastUnixTime = unixTime;
        } else {
            // 10 mins -> day view
            if (lastUnixTime / MSEC_PER_10_MINS != unixTime / MSEC_PER_10_MINS) {
                HeartRateStoreController.AnalysisResult analysisResult
                        = getAnalysisResult();
                heartRateContract.insertHR("day",
                        unixTime - unixTime % MSEC_PER_10_MINS,
                        analysisResult.average, analysisResult.max, analysisResult.min, analysisResult.std_dev);
            }
        }
    }

    public DataPoint[] getDayDataSet() {
        return new DataPoint[]{};
    }

    public AnalysisResult getAnalysisResult() {
        long sum = 0;
        long sum2 = 0;
        int min = 1000;
        int max = -1;
        for (int hr : stagingHRs) {
            sum += hr;
            sum2 += hr * hr;
            if (min > hr) min = hr;
            if (max < hr) max = hr;
        }
        double ave = (sum * 1.0 / stagingHRs.size());
        double ave2 = (sum2 * 1.0 / stagingHRs.size());
        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.average = ave;
        analysisResult.std_dev = Math.sqrt(ave2 - ave * ave);
        analysisResult.max = max;
        analysisResult.min = min;
        return analysisResult;
    }

    public class AnalysisResult {
        public double average;
        public double std_dev;
        public int max;
        public int min;
    }
}
