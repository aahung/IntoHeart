package ee3316.intoheart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ee3316.intoheart.Data.HeartRateStoreController;
import ee3316.intoheart.Data.InstantHeartRateStore;
import ee3316.intoheart.Data.UserStore;
import ee3316.intoheart.HTTP.JCallback;

/**
 * Created by aahung on 3/7/15.
 */
public class DashboardFragment extends Fragment {

    LineGraphSeries<DataPoint> series;

    boolean exercising = false;

    private static final String ARG_SECTION_NUMBER = "section_number";


    public static DashboardFragment newInstance(int sectionNumber) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public DashboardFragment() {

    }
    @InjectView(R.id.sports_man)
    ImageView sportsMan;
    @InjectView(R.id.heart)
    ImageView heart;

    private void setVisibility() {
        if (exercising) {
            heart.setVisibility(View.GONE);
            sportsMan.setVisibility(View.VISIBLE);
        } else {
            heart.setVisibility(View.VISIBLE);
            sportsMan.setVisibility(View.GONE);
        }
    }
    private InstantHeartRateStore getInstantHeartRateStore() {
        return ((IHApplication) getActivity().getApplication()).instantHeartRateStore;
    }

    private HeartRateStoreController getHeartRateStoreController() {
        return ((MainActivity) getActivity()).heartRateStoreController;
    }


    public void update(String hr) {
        TextView instantHRTextView = (TextView) getActivity().findViewById(R.id.instantHRTextView);
        instantHRTextView.setText(hr + " bpm");
        if (currentChart == HeartRateStoreController.CHART.INSTANT)
            series.resetData(getInstantHeartRateStore().hrs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        series = new LineGraphSeries<DataPoint>(getInstantHeartRateStore().hrs);
        ButterKnife.inject(this, rootView);
        graph.setOnTouchListener(new OnSwipeTouchListener(getActivity()));
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.dashboard, menu);
        if (exercising) {
            menu.findItem(R.id.menu_exercise).setVisible(false);
            menu.findItem(R.id.menu_normal).setVisible(true);
        } else {
            menu.findItem(R.id.menu_normal).setVisible(false);
            menu.findItem(R.id.menu_exercise).setVisible(true);
        }
        setVisibility();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.menu_exercise:
                // open the voice
                exercising = true;
                exerciseMonitor = new ExerciseMonitor();
                getActivity().invalidateOptionsMenu();
                reconstructChart();
                break;
            case R.id.menu_normal:
                exercising = false;
                exerciseMonitor.end();
                getActivity().invalidateOptionsMenu();
                reconstructChart();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private int currentChart = HeartRateStoreController.CHART.INSTANT;
    private String currentTitle = "";
    private DataPoint[] currentDataSet; // only for non-instant usage
    int currentOffset, preOffset;


    @InjectView(R.id.graph) GraphView graph;
    private void reconstructChart() {
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.setTitle(currentTitle);
        graph.getViewport().setXAxisBoundsManual(true);
        if (currentChart == HeartRateStoreController.CHART.INSTANT) {
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(getInstantHeartRateStore().n - 1);
        } else if (currentDataSet.length > 0) {
            graph.getViewport().setMinX(currentDataSet[0].getX());
            graph.getViewport().setMaxX(currentDataSet[currentDataSet.length - 1].getX());
        }
        graph.getViewport().setYAxisBoundsManual(true);
        if (exercising) {
            graph.getViewport().setMinY(50);
            graph.getViewport().setMaxY(210);
        } else {
            graph.getViewport().setMinY(getInstantHeartRateStore().MIN_HR);
            graph.getViewport().setMaxY(getInstantHeartRateStore().MAX_HR);
        }
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        if (currentChart == HeartRateStoreController.CHART.INSTANT) {
            staticLabelsFormatter.setHorizontalLabels(new String[]{"", ""});
        } else if (currentDataSet.length > 0) {
            if (currentChart == HeartRateStoreController.CHART.DAY) {
                staticLabelsFormatter.setHorizontalLabels(new String[]{
                        String.format("%d hours ago", (int) ((System.currentTimeMillis() / 1000L - currentDataSet[0].getX()) / 60 / 60)),
                        String.format("%d hours ago", (int) ((System.currentTimeMillis() / 1000L - currentDataSet[currentDataSet.length - 1].getX()) / 60 / 60))});

            } else {
                staticLabelsFormatter.setHorizontalLabels(new String[]{
                        String.format("%d days ago", (int) ((System.currentTimeMillis() / 1000L - currentDataSet[0].getX()) / HeartRateStoreController.SEC_PER_DAY)),
                        String.format("%d days ago", (int) ((System.currentTimeMillis() / 1000L - currentDataSet[currentDataSet.length - 1].getX()) / 6 / HeartRateStoreController.SEC_PER_DAY))});
            }
        }
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }

    @Override
    public void onResume() {
        super.onResume();
        reconstructChart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (exerciseMonitor != null) exerciseMonitor.end();
        super.onDestroy();
    }

    private void loadBatchData() {
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, "Loading data");
        Thread mThread = new Thread() {
            @Override
            public void run() {
                currentDataSet = getHeartRateStoreController().getDayDataSet(currentChart, currentOffset);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (currentDataSet.length > 0) {
                            reconstructChart();
                            series.resetData(currentDataSet);
                        } else {
                            currentOffset = preOffset;
                            Toast.makeText(getActivity(), "No data seems found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        mThread.start();
    }

    @OnClick(R.id.instant_hr_button)
    public void showInstantView(View view) {
        currentChart = HeartRateStoreController.CHART.INSTANT;
        currentTitle = "";
        reconstructChart();
    }

    @OnClick(R.id.day_hr_button)
    public void showDayView(View view) {
        currentChart = HeartRateStoreController.CHART.DAY;
        currentOffset = 0;
        loadBatchData();
    }

    @OnClick(R.id.week_hr_button)
    public void showWeekView(View view) {
        currentChart = HeartRateStoreController.CHART.WEEK;
        currentOffset = 0;
        loadBatchData();
    }

    @OnClick(R.id.month_hr_button)
    public void showMonthView(View view) {
        currentChart = HeartRateStoreController.CHART.MONTH;
        currentOffset = 0;
        loadBatchData();
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;


            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeLeft() {
            preOffset = currentOffset;
            currentOffset++;
            loadBatchData();
        }

        public void onSwipeRight() {
            preOffset = currentOffset;
            currentOffset--;
            loadBatchData();
        }
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

    class ExerciseMonitor {
        private final int EXERCISE_MAX_HR = 110;


        private TextToSpeech tts = null;
        private boolean speaking = false;

        private boolean ttsIsInit = false;


        public ExerciseMonitor() {
            initTextToSpeech();
        }

        private void initTextToSpeech() {
            //Initialize speech
            Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(intent, TTS_DATA_CHECK);
        }

        public void start() {
            tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        ttsIsInit = true;
                        if (tts.isLanguageAvailable(Locale.UK) >= 0) {
                            tts.setPitch(1.0f);
                            tts.setSpeechRate(1.1f);
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
                    if (getActivity() == null) return; // in case fragment not attached
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
            speak("Please stop,your heart rate is too high");
        }

        public void speak(String text) {
            if (tts != null && ttsIsInit) {
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
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
            heartRateUpdateListener = null;
        }

        public JCallback<Integer> heartRateUpdateListener;
    }


}
