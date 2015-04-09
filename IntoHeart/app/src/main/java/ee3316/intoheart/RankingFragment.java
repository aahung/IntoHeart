package ee3316.intoheart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ee3316.intoheart.Data.UserStore;
import ee3316.intoheart.HTTP.Connector;
import ee3316.intoheart.HTTP.JCallback;
import ee3316.intoheart.HTTP.Outcome;
import ee3316.intoheart.UIComponent.SimpleAlertController;

/**
 * Created by aahung on 3/7/15.
 */
public class RankingFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";


    Connector connector;
    UserStore userStore;


    public static RankingFragment newInstance(int sectionNumber) {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RankingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ranking, container, false);
        setHasOptionsMenu(true);
        connector = new Connector();
        ButterKnife.inject(this, rootView);
        userStore = new UserStore(getActivity());
        getRank();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_ranking, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_add:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @InjectView(R.id.ranking_list_view)
    ListView listView;

    public void getRank() {
        connector.rank(userStore.email, userStore.password, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                if (outcome.success) {
                    RankingListAdapter rankingListAdapter = new RankingListAdapter();
                    JsonArray array = (JsonArray) outcome.object;
                    for (JsonElement ele : array) {
                        JsonObject obj = ele.getAsJsonObject();
                        String name = obj.get("name").getAsString();
                        String email = obj.get("email").getAsString();
                        Integer score = obj.get("score").getAsInt();
                        rankingListAdapter.addData(new String[]{name, email, score.toString()});
                    }
                    rankingListAdapter.sort();
                    listView.setAdapter(rankingListAdapter);
                } else {
                    SimpleAlertController.showSimpleMessage("Sorry",
                            outcome.getString(), getActivity());
                }
            }
        });
    }

    @OnClick(R.id.add_new_friends)
    public void addFriend(View view) {
        final Intent intent = new Intent(getActivity(), AddFriendActivity.class);
        startActivity(intent);
    }



    public class RankingListAdapter extends BaseAdapter {
        private List<String[]> datas;
        private LayoutInflater mInflator;

        public RankingListAdapter() {
            super();
            datas = new ArrayList<>();
            mInflator = getActivity().getLayoutInflater();
        }

        public void addData (String[] data) {
            datas.add(data);
        }

        public String[] getData(int position) {
            return datas.get(position);
        }

        public void clear() {
            datas.clear();
        }

        public void sort() {
            Collections.sort(datas, new Comparator<String[]>() {
                @Override
                public int compare(String[] lhs, String[] rhs) {
                    return Integer.valueOf(lhs[2]) - Integer.valueOf(lhs[3]);
                }
            });
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_ranking, null);
                viewHolder = new ViewHolder();
                viewHolder.ranking = (TextView) view.findViewById(R.id.rankingText);
                viewHolder.name = (TextView) view.findViewById(R.id.nameText);
                viewHolder.email = (TextView) view.findViewById(R.id.emailText);
                viewHolder.score = (TextView) view.findViewById(R.id.scoreText);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            String[] data = datas.get(i);
            viewHolder.ranking.setText(String.valueOf(i + 1));
            viewHolder.name.setText(data[0]);
            viewHolder.email.setText(data[1]);
            viewHolder.score.setText(data[2]);

            return view;
        }

        public class ViewHolder {
            TextView ranking;
            TextView name;
            TextView email;
            TextView score;
        }
    }
}
