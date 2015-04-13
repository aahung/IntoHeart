package ee3316.intoheart.Data;

import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

import ee3316.intoheart.HTTP.JCallback;
import ee3316.intoheart.R;

/**
 * Created by aahung on 4/10/15.
 */
public class InstantHeartRateStore {
    public final static int n = 60;
    public final static int MAX_HR = 140;
    public final static int MIN_HR = 40;
    public DataPoint[] hrs = new DataPoint[n];

    JCallback<Integer> updateListener = null;

    public InstantHeartRateStore() {
        for (int i = 0; i < n; ++i) {
            hrs[i] = new DataPoint(i, (MAX_HR + MIN_HR) / 2);
        }
    }

    public void setHR(String hr) {
        for (int i = 0; i < n - 1; ++i) {
            hrs[i] = new DataPoint(i, hrs[i + 1].getY());
        }
        hrs[n - 1] = new DataPoint(n - 1, Integer.valueOf(hr));
        if (updateListener != null) updateListener.call(Integer.valueOf(hr));
    }

    public void setUpdateListener(JCallback<Integer> updateListener) {
        updateListener = updateListener;
    }

    public void removeUpdateListener() {
        updateListener = null;
    }
}
