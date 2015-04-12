package ee3316.intoheart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ee3316.intoheart.Data.HeartRateContract;

/**
 * Created by aahung on 3/9/15.
 */
public class AnalysisFragment extends Fragment {


    private static final String ARG_SECTION_NUMBER = "section_number";

    public static AnalysisFragment newInstance(int sectionNumber) {
        AnalysisFragment fragment = new AnalysisFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AnalysisFragment() {

    }
    @InjectView(R.id.disease_fast_block)
    LinearLayout disease_fast_block;
    @InjectView(R.id.disease_slow_block)
    LinearLayout disease_slow_block;
    @InjectView(R.id.healthy_block)
    LinearLayout healthy_block;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analysis, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this,rootView);
        Button btn_slow=(Button)rootView.findViewById(R.id.view_detail_slow_button);
        btn_slow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSlowDisease(DiseaseListActivity.DISEASE_KIND.SLOW);
            }
        });
        Button btn_fast=(Button)rootView.findViewById(R.id.view_detail_fast_button);
        btn_fast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSlowDisease(DiseaseListActivity.DISEASE_KIND.FAST);
            }
        });
        Button btn_slow2=(Button)rootView.findViewById(R.id.view_detail_slow_button2);
        btn_slow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSlowDisease(DiseaseListActivity.DISEASE_KIND.SLOW);
            }
        });
        Button btn_fast2=(Button)rootView.findViewById(R.id.view_detail_fast_button2);
        btn_fast2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSlowDisease(DiseaseListActivity.DISEASE_KIND.FAST);
            }
        });
        return rootView;
    }

    public void showSlowDisease(final int diseaseKind) {

        final String[] diseaseNames;
        final String[] diseaseURLs;
        String title;
        if (diseaseKind == DiseaseListActivity.DISEASE_KIND.SLOW) {
            diseaseNames = getResources().getStringArray(R.array.slowdiseasename_array);
            diseaseURLs = getResources().getStringArray(R.array.slowdiseaseurls_array);
            title = "Slow diseases";
        } else {
            diseaseNames = getResources().getStringArray(R.array.fastdiseasename_array);
            diseaseURLs = getResources().getStringArray(R.array.fastdiseaseurls_array);
            title = "Fast diseases";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_disease_list, null);
        final ListView listView = ((ListView)dialogView.findViewById(R.id.list_view));
        listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, diseaseNames));
        builder.setView(dialogView)
                .setTitle(title);
        final AlertDialog dialog = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = diseaseNames[position];
                String url = diseaseURLs[position];
                Intent intent = new Intent();
                intent.setClass(getActivity(), DiseaseDetailActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", url);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {

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
    public void onResume() {
        super.onResume();
//        analysisResult = (new HeartRateContract(getActivity())).getAnalysisResult();
//        ((TextView)getActivity().findViewById(R.id.textAve)).setText("Recently HR average: " + round(analysisResult.average, 2));
//        ((TextView)getActivity().findViewById(R.id.textStdDev)).setText("Standard Deviation: " + round(analysisResult.std_dev, 2));
//        ((TextView)getActivity().findViewById(R.id.textMax)).setText("Max: " + analysisResult.max);
//        ((TextView)getActivity().findViewById(R.id.textMin)).setText("Min: " + analysisResult.min);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
