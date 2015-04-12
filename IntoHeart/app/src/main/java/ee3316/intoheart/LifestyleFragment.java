package ee3316.intoheart;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ee3316.intoheart.Data.UserStore;

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

    public LifestyleFragment() {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lifestyle, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this,rootView);
        return rootView;
    }

    private void prompt(final int index) {
        final UserStore userStore = new UserStore(getActivity());
        String[] titles = new String[]{
                "Smoking",
                "Alcohol",
                "Overwork",
                "Eating disorder",
                "Stay up late"
        };

        final Map<Integer, RateNotes> rateNotesMap = new HashMap<>();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.popup, null);
        final RatingBar ratingbar = (RatingBar)dialogView.findViewById(R.id.ratingBar);
        final TextView rateNoteText = (TextView)dialogView.findViewById(R.id.rate_note_text);
        ratingbar.setRating(userStore.lifestyles[index]);
        RatingBar.OnRatingBarChangeListener onRatingBarChangeListener = new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String rateNote = rateNotesMap.get(index).get(rating);
                rateNoteText.setText(rateNote);
            }
        };
        ratingbar.setOnRatingBarChangeListener(onRatingBarChangeListener);
        onRatingBarChangeListener.onRatingChanged(null, userStore.lifestyles[index], false);

        builder.setView(dialogView)
                .setTitle(titles[index])
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    userStore.lifestyles[index] = ratingbar.getRating();
                    userStore.save();
                    dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.alcohol)
    public void alcoholPrompt(View view) {
        prompt(1);
    }
    @OnClick(R.id.smoking)
    public void smokingPrompt(View view) {
        prompt(0);
    }
    @OnClick(R.id.stay_up_late)
    public void stayuplatePrompt(View view) {
        prompt(4);
    }
    @OnClick(R.id.overwork)
    public void overworkPrompt(View view) {
        prompt(2);
    }
    @OnClick(R.id.eating_disorder)
    public void eatingPrompt(View view) {
        prompt(3);
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
