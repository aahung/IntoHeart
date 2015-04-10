package ee3316.intoheart;

import android.app.Application;

import ee3316.intoheart.Data.InstantHeartRateStore;

/**
 * Created by aahung on 4/10/15.
 */
public class IHApplication extends Application {
    public InstantHeartRateStore instantHeartRateStore;

    public IHApplication() {
        super();
        instantHeartRateStore = new InstantHeartRateStore();
    }
}
