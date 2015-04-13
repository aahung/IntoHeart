package ee3316.intoheart;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import ee3316.intoheart.Data.InstantHeartRateStore;
import ee3316.intoheart.Data.UserStore;

/**
 * Created by aahung on 4/10/15.
 */
public class IHApplication extends Application {
    public InstantHeartRateStore instantHeartRateStore;
    public UserStore userStore;

    public MainActivity mainActivity;

    public IHApplication() {
        super();
        instantHeartRateStore = new InstantHeartRateStore();
         
    }



    public void callEmergency(String number){

        Intent intent = new Intent();
        intent.setAction("android.intent.action.CALL");
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }
}
