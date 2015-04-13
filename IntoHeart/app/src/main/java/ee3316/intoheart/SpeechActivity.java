package ee3316.intoheart;

/**
 * Created by Vivian on 13/4/15.
 */
import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;

public class SpeechActivity extends Activity {

    private static int TTS_DATA_CHECK = 1;

    private TextToSpeech tts = null;


    private boolean ttsIsInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_exercise_main);
        initTextToSpeech();
    }

    private void initTextToSpeech() {

        //Initialize speech
        Intent intent = new Intent(Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, TTS_DATA_CHECK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_DATA_CHECK) {
            if (resultCode == Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, new OnInitListener() {

                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            ttsIsInit = true;
                            if (tts.isLanguageAvailable(Locale.UK) >= 0) {
                                tts.setPitch(1.5f);
                                tts.setSpeechRate(1.1f);
                                speak();
                            }
                        }
                    }

                    private void speak() {
                        if (tts != null && ttsIsInit) {
                            tts.speak("Please stop,your heart rate is too high", TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                });
            } else {
                Intent installAllVoice = new Intent(Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installAllVoice);
            }

        }
    }


    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}