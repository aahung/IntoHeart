package ee3316.intoheart;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import ee3316.intoheart.Data.UserStore;
import ee3316.intoheart.HTTP.JCallback;
import ee3316.intoheart.HTTP.Outcome;

/**
 * Created by aahung on 3/7/15.
 */
public class LifestyleFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";


    public static LifestyleFragment newInstance(int sectionNumber) {
        LifestyleFragment fragment = new LifestyleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    Map<Integer, RateNotes> rateNotesMap;

    public LifestyleFragment() {
        rateNotesMap = new HashMap<>();
        rateNotesMap.put(0, new RateNotes() {
            @Override
            public String get(float rate) {
                if (rate < 5)
                    return String.format("%d cigarette per day", (int)(2 * rate));
                else
                    return "far more then 10";
            }
        });
        rateNotesMap.put(1, new RateNotes() {
            @Override
            public String get(float rate) {
                if (rate < 5)
                    return String.format("%d drink per day", (int)(2 * rate));
                else
                    return "far more then 10";
            }
        });
        rateNotesMap.put(2, new RateNotes() {
            @Override
            public String get(float rate) {
                if (rate < 5)
                    return String.format("%.1f hour per day", rate);
                else
                    return "more then 5 hours";
            }
        });
        rateNotesMap.put(3, new RateNotes() {
            @Override
            public String get(float rate) {
                return String.format("%d%% disorder", (int)(rate * 100 / 5));
            }
        });
        rateNotesMap.put(4, new RateNotes() {
            @Override
            public String get(float rate) {
                if (rate < 5)
                    return String.format("%.1f hour per day", rate);
                else
                    return "more then 5 hours";
            }
        });
    }


    @InjectView(R.id.alcohol_block)
    LinearLayout alcohol_block;
    @InjectView(R.id.smoking_block)
    LinearLayout smoking_block;
    @InjectView(R.id.stay_up_late_block)
    LinearLayout stay_up_late_block;
    @InjectView(R.id.overwork_block)
    LinearLayout overwork_block;
    @InjectView(R.id.eating_disorder_block)
    LinearLayout eating_disorder_block;

    @InjectViews({
            R.id.ratingBarSmoking,
            R.id.ratingBarAlcohol,
            R.id.ratingBarOverwork,
            R.id.ratingBarEatig,
            R.id.ratingBarStayup}) RatingBar[] ratingBars;

    @InjectViews({R.id.rateNote2, R.id.rateNote1, R.id.rateNote3, R.id.rateNote4, R.id.rateNote5})
    TextView[] rateNotes;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lifestyle, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this, rootView);
        fetchLifestyle = new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                userStore.fetch();
                for (int i = 0; i < 5; ++i)
                    ratingBars[i].setRating(userStore.lifestyles[i]);
                updateRateNotes();
            }
        };
        updateLifestyle = new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                updateRateNotes();
                userStore.fetch();
                for (int i = 0; i < 5; ++i) {
                    float value = ratingBars[i].getRating();
                    userStore.lifestyles[i] = value;
                }
                userStore.save();
            }
        };
        userStore = new UserStore(getActivity());
        fetchLifestyle.call(null);
        if (userStore.getLogin())
            userStore.fetchFromOnline(fetchLifestyle);
        for (int i = 0; i < 5; ++i)
            ratingBars[i].setOnRatingBarChangeListener(updateLifestyle);
        return rootView;
    }

    UserStore userStore;

    public JCallback<Outcome> fetchLifestyle;
    public RatingBar.OnRatingBarChangeListener updateLifestyle;
    public void updateRateNotes() {
        for (int i = 0; i < 5; ++i) {
            float value = ratingBars[i].getRating();
            String note = rateNotesMap.get(i).get(value);
            rateNotes[i].setText(note);
        }
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

    public abstract class RateNotes {
        public abstract String get(float rate);
    }
}
