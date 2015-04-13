package ee3316.intoheart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ee3316.intoheart.Data.HeartRateStoreController;
import ee3316.intoheart.Data.InstantHeartRateStore;

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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.menu_exercise:
                // open the voice
                exercising = true;
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.menu_normal:
                exercising = false;
                getActivity().invalidateOptionsMenu();
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
        graph.getViewport().setMinY(getInstantHeartRateStore().MIN_HR);
        graph.getViewport().setMaxY(getInstantHeartRateStore().MAX_HR);
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
}
