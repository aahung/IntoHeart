package ee3316.intoheart;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ee3316.intoheart.BLE.BluetoothLeService;
import ee3316.intoheart.BLE.SensorConnectionManager;
import ee3316.intoheart.Data.HeartRateContract;
import ee3316.intoheart.Data.HeartRateStoreController;
import ee3316.intoheart.Data.InstantHeartRateStore;
import ee3316.intoheart.Data.UserStore;
import ee3316.intoheart.HTTP.JCallback;
import ee3316.intoheart.UIComponent.SimpleAlertController;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String PREFS_NAME_RECENT_ADDRESS = "PrefsRecentAddr";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    public HeartRateStoreController heartRateStoreController = new HeartRateStoreController(this);
    private CharSequence mTitle;

    public List<String> sectionTitles;

    JCallback<Integer> onConnectionStateChanged;


    public SensorConnectionManager sensorConnectionManager;

    public MainActivity() {
        sectionTitles = new ArrayList<>();
        sectionTitles.add("Dashboard");
        sectionTitles.add("Analysis");
        sectionTitles.add("Sensors");
        sectionTitles.add("Ranking");
        sectionTitles.add("My Info");
        sectionTitles.add("Lifestyle");


        sensorConnectionManager = new SensorConnectionManager(this);


    }

    @InjectView(R.id.skullImage)
    ImageView skullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        skullImageView.setVisibility(View.GONE);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, sensorConnectionManager.mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(sensorConnectionManager.mGattUpdateReceiver,
                sensorConnectionManager.makeGattUpdateIntentFilter());
        sensorConnectionManager.mBluetoothLeService = new BluetoothLeService();

        // read device address
        SharedPreferences settings = getSharedPreferences(PREFS_NAME_RECENT_ADDRESS, 0);
        sensorConnectionManager.mDeviceAddress = settings.getString(PREFS_NAME_RECENT_ADDRESS, null);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_NAME_RECENT_ADDRESS, null);
        editor.commit();
        if (sensorConnectionManager.mBluetoothLeService != null) {
            final boolean result
                = sensorConnectionManager.mBluetoothLeService.
                connect(sensorConnectionManager.mDeviceAddress);
        }

        ((IHApplication) getApplication()).mainActivity = this;

        emergencyMonitor = new EmergencyMonitor();

        onConnectionStateChanged = new JCallback<Integer>() {
            @Override
            public void call(final Integer integer) {
                Thread mThread = new Thread() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (integer == BluetoothLeService.STATE_DISCONNECTED) {
                                    SimpleAlertController.showSimpleMessage("Oops", "Sensor seems to be disconnected.", MainActivity.this);
                                }
                            }
                        });
                    }
                };
                mThread.start();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME_RECENT_ADDRESS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_NAME_RECENT_ADDRESS, sensorConnectionManager.mDeviceAddress);
        editor.commit();
        unbindService(sensorConnectionManager.mServiceConnection);
        sensorConnectionManager.mBluetoothLeService = null;
        onConnectionStateChanged = null;
        unregisterReceiver(sensorConnectionManager.mGattUpdateReceiver);

        // remove the exercise monitor
        if (exerciseMonitor != null) {
            exerciseMonitor.end();
            exerciseMonitor = null;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        Fragment fragment;
        switch (position) {
            default:
            case 0:
                fragment = DashboardFragment.newInstance(position + 1);
                break;
            case 1:
                fragment = AnalysisFragment.newInstance(position + 1);
                break;

            case 2:
                fragment = SensorsFragment.newInstance(position + 1);
                break;

            case 3:
                fragment = RankingFragment.newInstance(position + 1);
                break;
            case 4:
                fragment = UserinfoFragment.newInstance(position + 1);
                break;
            case 5:
                fragment = LifestyleFragment.newInstance(position + 1);
                break;

        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        int index = number - 1;
        if (index >= sectionTitles.size()) {
            index = 0;
        }
        mTitle = sectionTitles.get(index);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            if (emergencyMonitor.shouldCall) {
                ((MenuItem) menu.findItem(R.id.action_emergency_off)).setVisible(true);
                ((MenuItem) menu.findItem(R.id.action_emergency_on)).setVisible(false);
            } else {
                ((MenuItem) menu.findItem(R.id.action_emergency_off)).setVisible(false);
                ((MenuItem) menu.findItem(R.id.action_emergency_on)).setVisible(true);
            }
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_emergency_on) {
            emergencyMonitor.shouldCall = true;
            invalidateOptionsMenu();
        }

        if (id == R.id.action_emergency_off) {
            emergencyMonitor.shouldCall = false;
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    EmergencyMonitor emergencyMonitor;
    public boolean exercising = false;

    class EmergencyMonitor {
        private int EMERGENT_MIN_HR = 70;
        private int EMERGENT_MAX_HR = 90;
        private int EMERGENT_MAX_HR_EXERCISING = 200;

        private boolean shouldCall = false;

        public JCallback<Integer> heartRateUpdateListener;

        public EmergencyMonitor() {
            heartRateUpdateListener = new JCallback<Integer>() {
                @Override
                public void call(Integer integer) {
                    boolean tooHigh = true;
                    for (int i = getInstantHeartRateStore().n - 1;
                         i >= getInstantHeartRateStore().n - 10; --i) {
                        int max = (exercising)? EMERGENT_MAX_HR_EXERCISING : EMERGENT_MAX_HR;
                        if (getInstantHeartRateStore().hrs[i].getY() >= EMERGENT_MIN_HR
                                && getInstantHeartRateStore().hrs[i].getY() <= max) {
                            tooHigh = false;
                            break;
                        }
                    }
                    if (tooHigh) {
                        if (shouldCall) {
                            callEmergency((new UserStore(MainActivity.this)).emergencyTel);
                            emergencyMonitor.shouldCall = false;
                        }
                    }
                }
            };

            getInstantHeartRateStore().addUpdateListener(heartRateUpdateListener);
        }

        public void callEmergency(String number){
            Intent intent = new Intent();
            intent.setAction("android.intent.action.CALL");
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }

    private InstantHeartRateStore getInstantHeartRateStore() {
        return ((IHApplication) getApplication()).instantHeartRateStore;
    }

    ExerciseMonitor exerciseMonitor;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (exercising) {
            if (requestCode == TTS_DATA_CHECK) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    exerciseMonitor.start();
                } else {
                    Intent installAllVoice = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installAllVoice);
                }
            }
        }
    }

    private static int TTS_DATA_CHECK = 1;

    public void createExerciseMonitor() {
        exerciseMonitor = new ExerciseMonitor();
    }

    public void destroyExerciseMonitor() {
        if (exerciseMonitor != null) {
            exerciseMonitor.end();
            exerciseMonitor = null;
        }
    }

    class ExerciseMonitor {
        private final int EXERCISE_MAX_HR = 110;


        private TextToSpeech tts = null;
        private boolean speaking = false;

        private boolean ttsIsInit = false;

        private List<Integer> hrs;


        public ExerciseMonitor() {
            initTextToSpeech();
        }

        private void initTextToSpeech() {
            //Initialize speech
            Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(intent, TTS_DATA_CHECK);
        }

        public void start() {
            hrs = new ArrayList<>();
            tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        ttsIsInit = true;
                        if (tts.isLanguageAvailable(Locale.UK) >= 0) {
                            tts.setPitch(1.0f);
                            tts.setSpeechRate(1.0f);
//                            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
//                                @Override
//                                public void onStart(String utteranceId) {
//                                    speaking = true;
//                                }
//
//                                @Override
//                                public void onDone(String utteranceId) {
//                                    speaking = false;
//                                }
//
//                                @Override
//                                public void onError(String utteranceId) {
//                                    speaking = false;
//                                }
//                            });
                            exerciseMonitor.welcome();
                        }
                    }
                }
            });

            heartRateUpdateListener = new JCallback<Integer>() {
                @Override
                public void call(Integer integer) {
                    hrs.add(integer);
                    if (speaking) return;
                    boolean tooHigh = true;
                    for (int i = getInstantHeartRateStore().n - 1;
                         i >= getInstantHeartRateStore().n - 10; --i) {
                        if (getInstantHeartRateStore().hrs[i].getY() < EXERCISE_MAX_HR) {
                            tooHigh = false;
                            break;
                        }
                    }
                    if (tooHigh && !speaking) {
                        alert();
                    }
                }
            };

            getInstantHeartRateStore().addUpdateListener(heartRateUpdateListener);
        }

        public void welcome() {
            String text = String.format("Hey! You are now in exercise mode, "
                    + "I will tell you when your heart rate is too high, "
                    + "current threshold is %s beats per minute.", EXERCISE_MAX_HR);
            speak(text);
        }

        public void alert() {
            skullImageView.setVisibility(View.VISIBLE);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            skullImageView.setVisibility(View.GONE);
                        }
                    });
                }
            }, 3 * 1000);
            speak("Please stop,your heart rate is too high");
        }

        public void conclude(int ave, int score, int fake) {
            speak(String.format("Exercise mode ends, your average heart rate is %d, and the score you got is %d out of 100",
                    ave, score));
        }

        public void speak(String text) {
            if (tts != null && ttsIsInit) {
                // for demo, force the speaker on
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(true);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                speaking = true;
                Timer timer = new Timer();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        speaking = false;
                    }
                }, 10 * 1000);
            }
        }

        public void end() {
            heartRateUpdateListener = null;
            if (hrs == null || hrs.size() == 0) return;
            int ave = 0;
            for (int i = 0; i < hrs.size(); ++i) {
                ave += hrs.get(i);
            }
            ave /= hrs.size();
            UserStore userStore = new UserStore(MainActivity.this);
            userStore.fetch();
            userStore.markingManager.evaluateExercise(userStore.age, ave);
            userStore.save();
            Toast.makeText(MainActivity.this, String.format("Saving exercise score: %d", userStore.markingManager.mark[0]), Toast.LENGTH_SHORT).show();

            // for demo
            Random rand = new Random();
            int randomNum = rand.nextInt((100 - 50) + 1) + 50;
            conclude(ave, userStore.markingManager.mark[0], randomNum);
            Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (tts != null) {
                        tts.stop();
                        tts.shutdown();
                    }
                }
            }, 10 * 1000);

        }

        public JCallback<Integer> heartRateUpdateListener;
    }

    // confirm before close the app
    @Override
    public void onBackPressed() {
        SimpleAlertController.showDestructiveMessageWithHandler("Exit", "Are you sure to exit?", "Exit", this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.super.onBackPressed();
            }
        });
    }
}
