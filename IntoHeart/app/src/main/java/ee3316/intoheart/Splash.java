package ee3316.intoheart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;

/**
 * Created by Lily on 2015/4/8.
 */
public class Splash extends Activity{

    private final int SPLASH_DISPLAY_LENGHT = 3000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.openpage);
        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {
                Intent mainIntent = new Intent(Splash.this,MainActivity.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }

        }, SPLASH_DISPLAY_LENGHT);
    }
}
