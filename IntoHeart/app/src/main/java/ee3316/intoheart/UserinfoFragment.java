package ee3316.intoheart;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by aahung on 3/7/15.
 */
public class UserinfoFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    EditText name;

    public static UserinfoFragment newInstance(int sectionNumber) {
        UserinfoFragment fragment = new UserinfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserinfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_userinfo, container, false);
        setHasOptionsMenu(true);
        return rootView;
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
        name = (EditText) getActivity().findViewById(R.id.user_name);
        SharedPreferences settings = getActivity().getSharedPreferences("name", 0);
        name.setText(settings.getString("name", "John"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences settings = getActivity().getSharedPreferences("name", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("name", name.getText().toString());
        editor.commit();
    }
}
