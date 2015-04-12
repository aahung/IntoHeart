package ee3316.intoheart;

/**
 * Created by Vivian on 9/4/15.
 */
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

public class DiseaseListActivity extends ListActivity {


    private int diseaseKind;

    String[] diseaseNames;
    String[] diseaseURLs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        diseaseKind = b.getInt("kind");

        loadDiseases();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String title = diseaseNames[position];
        String url = diseaseURLs[position];
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), DiseaseDetailActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void loadDiseases() {
        if (diseaseKind == DISEASE_KIND.SLOW) {
            diseaseNames = getResources().getStringArray(R.array.slowdiseasename_array);
            diseaseURLs = getResources().getStringArray(R.array.slowdiseaseurls_array);
        } else if (diseaseKind == DISEASE_KIND.FAST) {
            diseaseNames = getResources().getStringArray(R.array.fastdiseasename_array);
            diseaseURLs = getResources().getStringArray(R.array.fastdiseaseurls_array);
        }
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, diseaseNames));
    }

    public static class DISEASE_KIND {
        public final static int SLOW = 0;
        public final static int FAST = 1;
    }

}


