package ee3316.intoheart;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ee3316.intoheart.Data.MarkingManager;
import ee3316.intoheart.Data.UserStore;

/**
 * Created by Lily on 2015/4/7.
 */
public class MarkingActivity extends Activity{
    UserStore userStore;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marking);

    }
    @InjectView(R.id.heart_rate_score)
    TextView heart_rate_score;
    @InjectView(R.id.exercise_score)
    TextView exercise_score;
    @InjectView(R.id.life_style_score)
    TextView life_style_score;

    @Override
    public void onResume() {
        super.onResume();
        heart_rate_score.setText(String.valueOf(userStore.markingManager.getRestMark()));
        exercise_score.setText(String.valueOf(userStore.markingManager.getExerciseMark()));
        life_style_score.setText(String.valueOf(userStore.markingManager.getLifeStyleMark()));

    }

}
