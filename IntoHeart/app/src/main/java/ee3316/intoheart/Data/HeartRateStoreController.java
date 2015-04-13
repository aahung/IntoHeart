package ee3316.intoheart.Data;

import android.app.Activity;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ee3316.intoheart.HTTP.JCallback;

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

    public static final long SEC_PER_10_MINS = 600;
    public static final long SEC_PER_DAY = 86400;
    public static final long SEC_PER_WEEK = 604800;
    public static final long SEC_PER_MONTH = 2592000;

    // save the hr and add timestamp
    public void addHR(int hr) {
        long unixTime = System.currentTimeMillis() / 1000L;
        stagingHRs.add(hr);
        if (stagingHRs.size() == 1) {
            lastUnixTime = unixTime;
        } else {
            // 10 mins -> day view
            if (lastUnixTime / SEC_PER_10_MINS != unixTime / SEC_PER_10_MINS) {
                HeartRateStoreController.AnalysisResult analysisResult
                        = getAnalysisResult();
                heartRateContract.insertHR("day",
                        unixTime - unixTime % SEC_PER_10_MINS,
                        analysisResult.average, analysisResult.max, analysisResult.min, analysisResult.std_dev);
                UserStore userStore = new UserStore(activity);
                userStore.markingManager.evaluateRest((int)analysisResult.average);
                userStore.save();
                stagingHRs.clear();
                addHR(hr);
            }
        }
    }

    public static class CHART {
        public static int INSTANT = 0;
        public static int DAY = 1;
        public static int WEEK = 2;
        public static int MONTH = 3;
    }

    public DataPoint[] getDayDataSet(int chart, int offset) {
        long start, end;
        if (chart == CHART.DAY) {
            end = System.currentTimeMillis() / 1000L;
            end = end - end % SEC_PER_10_MINS;
            start = end - SEC_PER_DAY;
            end += offset * SEC_PER_DAY;
            start += offset * SEC_PER_DAY;
        } else if (chart == CHART.WEEK) {
            end = System.currentTimeMillis() / 1000L;
            end = end - end % SEC_PER_DAY;
            start = end - SEC_PER_WEEK;
            end += offset * SEC_PER_WEEK;
            start += offset * SEC_PER_WEEK;
        } else {
            end = System.currentTimeMillis() / 1000L;
            end = end - end % SEC_PER_DAY;
            start = end - SEC_PER_MONTH;
            end += offset * SEC_PER_MONTH;
            start += offset * SEC_PER_MONTH;
        }
        List<Long[]> ds = heartRateContract.getHRs(start, end);
        DataPoint[] dps = new DataPoint[ds.size()];
        int count = 0;
        for (Long[] d : ds) {//new Date(d[0])
            dps[count++] = new DataPoint(d[0], d[1]);
        }
        return dps;
    }

    public AnalysisResult getDayAnalysis() {
        AnalysisResult analysisResult = new AnalysisResult();
        long start, end;
            end = System.currentTimeMillis() / 1000L;
            end = end - end % SEC_PER_10_MINS;
            start = end - SEC_PER_DAY;
        List<Long[]> ds = heartRateContract.getHRs(start, end);
        analysisResult.average = 0;
        analysisResult.min = 1000;
        analysisResult.max = -1;
        for (Long[] d : ds) {//new Date(d[0])
            analysisResult.average += d[1];
            analysisResult.min = Math.min(analysisResult.min, d[3].intValue());
            analysisResult.max = Math.max(analysisResult.max, d[2].intValue());
        }
        analysisResult.average /= ds.size();
        return analysisResult;
    }

    private static AnalysisResult analyse(List<Integer> hrs) {
        long sum = 0;
        long sum2 = 0;
        int min = 1000;
        int max = -1;
        for (int hr : hrs) {
            sum += hr;
            sum2 += hr * hr;
            if (min > hr) min = hr;
            if (max < hr) max = hr;
        }
        double ave = (sum * 1.0 / hrs.size());
        double ave2 = (sum2 * 1.0 / hrs.size());
        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.average = ave;
        analysisResult.std_dev = Math.sqrt(ave2 - ave * ave);
        analysisResult.max = max;
        analysisResult.min = min;
        return analysisResult;
    }

    public AnalysisResult getAnalysisResult() {
        return analyse(stagingHRs);
    }

    public static class AnalysisResult {
        public double average;
        public double std_dev;
        public int max;
        public int min;
    }

    public void generateTestGaussian(int average, double sd, JCallback<Integer> callback) {
        TestDataGenerator testDataGenerator = new TestDataGenerator();
        testDataGenerator.generatorNormalHeartRates(average, sd, callback);
    }

    public class TestDataGenerator {

        Random randomGenerator = null;

        public TestDataGenerator() {
            randomGenerator = new Random();
        }

        public void generatorNormalHeartRates(int average, double sd, JCallback<Integer> callback) {
            long unixTime = System.currentTimeMillis() / 1000L;
            long current10MinsUnixTime = unixTime - unixTime % SEC_PER_10_MINS;
            int J = 10 * 24 * 6;
            for (int j = 0; j < J; ++j) {
                List<Integer> hrs = new ArrayList<>();
                for (int i = 0; i < 60 * 10; ++i) {
                    int hr = average + (int)(sd * randomGenerator.nextGaussian());
                    hrs.add(hr);
                }
                AnalysisResult analysisResult = analyse(hrs);
                heartRateContract.insertHR("day",
                        current10MinsUnixTime,
                        analysisResult.average, analysisResult.max, analysisResult.min, analysisResult.std_dev);
                current10MinsUnixTime -= SEC_PER_10_MINS;
                callback.call(100 * j / J);
            }
        }
    }
}
