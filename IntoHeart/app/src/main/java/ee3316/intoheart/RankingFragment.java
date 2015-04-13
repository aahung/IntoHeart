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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    @InjectView(R.id.final_score_view)
    TextView finalScoreView;
    @InjectView(R.id.heart_rate_score)
    TextView heart_rate_score;
    @InjectView(R.id.exercise_score)
    TextView exercise_score;
    @InjectView(R.id.life_style_score)
    TextView life_style_score;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ranking, container, false);
        setHasOptionsMenu(true);
        connector = new Connector();

        ButterKnife.inject(this,rootView);

        userStore = new UserStore(getActivity());
        finalScoreView.setText(String.valueOf(userStore.markingManager.getFinalMark()));
        heart_rate_score.setText(String.valueOf(userStore.markingManager.getRestMark()));
        exercise_score.setText(String.valueOf(userStore.markingManager.getExerciseMark()));
        life_style_score.setText(String.valueOf(userStore.markingManager.getLifeStyleMark()));
        if (userStore.getLogin()) {
            getRank();
            getRequest();
        } else {
            emptyText.setVisibility(View.GONE);
        }
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

    MenuItem inboxMenuItem;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_ranking, menu);
        inboxMenuItem = menu.findItem(R.id.action_inbox);
        if (requestListAdapter != null)
            inboxMenuItem.setTitle(String.format("Inbox (%s)", requestListAdapter.datas.size()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_inbox:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.alertdialog_requests, null);
                final ListView requestListView = ((ListView)dialogView.findViewById(R.id.list_view));
                requestListView.setAdapter(requestListAdapter);
                builder.setView(dialogView)
                        .setTitle("Inbox")
                        .setNeutralButton("Ok", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @InjectView(R.id.ranking_list_view)
    ListView listView;

    @InjectView(R.id.empty_text_view)
    TextView emptyText;

    RequestListAdapter requestListAdapter;

    public void getRequest() {
        connector.friendRequests(userStore.email, userStore.password, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                if (outcome.success) {
                    requestListAdapter = new RequestListAdapter();
                    JsonArray array = (JsonArray) outcome.object;
                    for (JsonElement ele : array) {
                        JsonObject obj = ele.getAsJsonObject();
                        String name = obj.get("name").getAsString();
                        String email = obj.get("email").getAsString();
                        requestListAdapter.addData(new String[]{name, email});
                        requestListAdapter.notifyDataSetChanged();
                    }
                    inboxMenuItem.setTitle(String.format("Inbox (%s)", requestListAdapter.datas.size()));
                } else {
                    SimpleAlertController.showSimpleMessage("Sorry",
                            outcome.getString(), getActivity());
                }
            }
        });
    }

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
                        Integer score = 0;
                        if (obj.get("score") != null)
                            obj.get("score").getAsInt();
                        rankingListAdapter.addData(new String[]{name, email, score.toString()});
                    }
                    rankingListAdapter.sort();
                    listView.setAdapter(rankingListAdapter);
                    rankingListAdapter.notifyDataSetChanged();
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
                    return Integer.valueOf(rhs[2]) - Integer.valueOf(lhs[2]);
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


    public class RequestListAdapter extends BaseAdapter {
        private List<String[]> datas;
        private LayoutInflater mInflator;

        public RequestListAdapter() {
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
            view = mInflator.inflate(R.layout.listitem_request, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.nameText);
            viewHolder.email = (TextView) view.findViewById(R.id.emailText);
            viewHolder.agree = (Button) view.findViewById(R.id.add_new_friends);


            String[] data = datas.get(i);
            viewHolder.name.setText(data[0]);
            viewHolder.email.setText(data[1]);
            viewHolder.agree.setTag(data);
            viewHolder.agree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] theData = (String[]) v.getTag();
                    connector.responseRequest(userStore.email, userStore.password, theData[1], new JCallback<Outcome>() {
                        @Override
                        public void call(Outcome outcome) {
                            if (outcome.success) {
                                getRank();
                                getRequest();
                            } else {
                                SimpleAlertController.showSimpleMessage("Sorry",
                                        outcome.getString(), getActivity());
                            }
                        }
                    });
                }
            });

            return view;
        }

        public class ViewHolder {
            TextView name;
            TextView email;
            Button agree;
        }
    }
}
