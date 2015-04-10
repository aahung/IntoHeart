package ee3316.intoheart;

/**
 * Created by Vivian on 9/4/15.
 */
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FastDiseaseMainActivity extends Activity {
    private Button buttonFast;
    private ImageButton button_return;
    private FragmentManager manager;

    private FragmentTransaction transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hra_fast);

        manager = getFragmentManager();

        buttonFast = (Button) findViewById(R.id.fast_disease_button);

        buttonFast.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                transaction = manager.beginTransaction();
                FastDiseaseListFragment articleListFragment = new FastDiseaseListFragment();
                transaction.add(R.id.disease_list_fast, articleListFragment, "disease_list_fast");
                transaction.commit();
            }
        });
        button_return = (ImageButton) findViewById(R.id.fast_return_button);

        button_return.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

}


