package ee3316.intoheart;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by aahung on 3/7/15.
 */
public class DashboardFragment extends Fragment {

    LineGraphSeries<DataPoint> series;

    GraphView graph;
    final static int n = 50;
    final static int MAX_HR = 130;
    final static int MIN_HR = 10;
    DataPoint[] hrs = new DataPoint[n];

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

    public void setHR(String hr) {
        TextView instantHRTextView = (TextView) getActivity().findViewById(R.id.instantHRTextView);
        instantHRTextView.setText(hr + " bpm");
        long unixTime = System.currentTimeMillis();
        for (int i = 0; i < n - 1; ++i) {
            hrs[i] = new DataPoint(i, hrs[i + 1].getY());
        }
        hrs[n - 1] = new DataPoint(n - 1, Integer.valueOf(hr));
        series.resetData(hrs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
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
        for (int i = 0; i < n; ++i) {
            hrs[i] = new DataPoint(i, (MAX_HR + MIN_HR) / 2);
        }
        series = new LineGraphSeries<DataPoint>(hrs);
    }

    @Override
    public void onResume() {
        super.onResume();
        graph = (GraphView) getActivity().findViewById(R.id.graph);
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.setTitle("");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(n - 1);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(MIN_HR);
        graph.getViewport().setMaxY(MAX_HR);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{"", ""});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
