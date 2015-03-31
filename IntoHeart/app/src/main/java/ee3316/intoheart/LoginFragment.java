package ee3316.intoheart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ee3316.intoheart.HTTP.Connector;
import ee3316.intoheart.HTTP.JCallback;
import ee3316.intoheart.HTTP.Outcome;
import ee3316.intoheart.UIComponent.SimpleAlertController;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aahung on 3/7/15.
 */
public class LoginFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private ProgressDialog progressDialog;

    Connector connector;

    public static LoginFragment newInstance(int sectionNumber) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        connector = new Connector();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this, rootView);
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


    @InjectView(R.id.email)
    EditText emailEdit;

    @InjectView(R.id.password)
    EditText passwordEdit;

    @OnClick(R.id.login)
    public void login(View view) {
        String email = emailEdit.getText().toString(),
        password = passwordEdit.getText().toString();
        if (email.isEmpty()) {
            SimpleAlertController.showSimpleMessage("Warning", "You should input your email.", getActivity());
            return;
        }
        if (password.isEmpty()) {
            SimpleAlertController.showSimpleMessage("Warning", "You should input your password.", getActivity());
            return;
        }
        progressDialog = ProgressDialog.show(getActivity(), null, "Log in");
        connector.login(email, password, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                progressDialog.dismiss();
                if (outcome.success) {
                    SimpleAlertController.showSimpleMessage("Welcome", outcome.getString(), getActivity());
                } else {
                    SimpleAlertController.showSimpleMessage("Error", outcome.getString(), getActivity());
                }
            }
        });
    }
}
