package ee3316.intoheart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

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
public class UserinfoFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    UserStore userStore;
    Connector connector;

    public static UserinfoFragment newInstance(int sectionNumber) {
        UserinfoFragment fragment = new UserinfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserinfoFragment() {

    }

    @InjectView(R.id.login_block)
    LinearLayout login_block;
    @InjectView(R.id.logout_block)
    LinearLayout logout_block;

    private void setVisibility() {
        if (userStore.getLogin()) {
            login_block.setVisibility(View.GONE);
            logout_block.setVisibility(View.VISIBLE);
        } else {
            login_block.setVisibility(View.VISIBLE);
            logout_block.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_userinfo, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this, rootView);
        {
            userStore = new UserStore(getActivity());
            setVisibility();
            updateContent();
            if (userStore.getLogin())
                userStore.fetchFromOnline(new JCallback<Outcome>() {
                    @Override
                    public void call(Outcome outcome) {
                        if (outcome.success) updateContent();
                    }
                });
        }
        connector = new Connector();
        return rootView;
    }

    private void updateContent() {
        nameEdit.setText(userStore.name);
        if (userStore.getAge() != null)
            ageEdit.setText(userStore.getAge().toString());
        if (userStore.getHeight() != null)
            heightEdit.setText(userStore.getHeight().toString());
        weightPicker.setMaxValue(9);weightPicker.setMinValue(0);
        weightPicker2.setMaxValue(9);weightPicker2.setMinValue(0);
        weightPicker3.setMaxValue(9);weightPicker3.setMinValue(0);
        if (userStore.getWeight() != null) {
            int weight = userStore.getWeight().intValue();
            weightPicker.setValue(weight / 100);
            weightPicker2.setValue(weight % 100 / 10);
            weightPicker3.setValue(weight % 10);
        }
        emergencyEdit.setText(userStore.emergencyTel);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @OnClick(R.id.save_button)
    public void save() {
        userStore.name = nameEdit.getText().toString().trim();
        userStore.emergencyTel = emergencyEdit.getText().toString().trim();
        try {
            userStore.age = Integer.valueOf(ageEdit.getText().toString());
        } catch (Exception ex) {}
        try {
            userStore.height = Integer.valueOf(heightEdit.getText().toString());
        } catch (Exception ex) {}
        int weight = weightPicker.getValue() * 100 + weightPicker2.getValue() * 10 + weightPicker3.getValue();
        userStore.weight = Integer.valueOf(weight);
        userStore.save();
    }

    @InjectView(R.id.name_edit) EditText nameEdit;
    @InjectView(R.id.age_edit) EditText ageEdit;
    @InjectView(R.id.height_edit) EditText heightEdit;
    @InjectView(R.id.weightPicker)
    NumberPicker weightPicker;
    @InjectView(R.id.weightPicker2)
    NumberPicker weightPicker2;
    @InjectView(R.id.weightPicker3)
    NumberPicker weightPicker3;
    @InjectView(R.id.emergency_edit) EditText emergencyEdit;

    @OnClick(R.id.login_button)
    public void loginPrompt(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_login, null);
        final EditText emailEdit = ((EditText)dialogView.findViewById(R.id.email_edit));
        final EditText passwordEdit = ((EditText)dialogView.findViewById(R.id.password_edit));
        emailEdit.setText(tmpEmail);
        passwordEdit.setText(tmpPassword);
        builder.setView(dialogView)
                .setTitle("Log in")
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Log in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = emailEdit
                                .getText().toString();
                        String password = passwordEdit
                                .getText().toString();
                        dialog.dismiss();
                        login(email, password);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    String tmpEmail, tmpPassword;

    private void login(final String email, final String password) {
        tmpEmail = email;
        tmpPassword = password;
        connector.login(email, password, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                if (outcome.success) {
                    nameEdit.setText(outcome.getString());
                    userStore.fetch();
                    userStore.name = outcome.getString();
                    userStore.email = email;
                    userStore.password = password;
                    userStore.saveUserLogin();
                    userStore.fetchFromOnline(new JCallback<Outcome>() {
                        @Override
                        public void call(Outcome outcome) {
                            userStore.save();
                            updateContent();
                        }
                    });
                    SimpleAlertController.showSimpleMessage("Log in successfully!",
                            String.format("Welcome back, %s", outcome.getString()), getActivity());
                    setVisibility();
                } else {
                    SimpleAlertController.showSimpleMessageWithHandler("Failed to log in.Please try again.",
                            outcome.getString(), getActivity(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loginPrompt(null);
                        }
                    });
                }
            }
        });
    }

    @OnClick(R.id.register_button)
    public void registerPrompt(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_signup, null);
        final EditText emailEdit = ((EditText)dialogView.findViewById(R.id.email_edit));
        final EditText nameEdit = ((EditText)dialogView.findViewById(R.id.name_edit));
        nameEdit.setText(this.nameEdit.getText().toString());
        final EditText passwordEdit = ((EditText)dialogView.findViewById(R.id.password_edit));
        builder.setView(dialogView)
                .setTitle("Register")
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameEdit.getText().toString();
                        String email = emailEdit
                                .getText().toString();
                        String password = passwordEdit
                                .getText().toString();
                        dialog.dismiss();
                        register(name, email, password);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void register(final String name, final String email, final String password) {
        connector.register(name, email, password, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                if (outcome.success) {
                    nameEdit.setText(name);
                    userStore.fetch();
                    userStore.name = name;
                    userStore.email = email;
                    userStore.password = password;
                    userStore.save();
                    SimpleAlertController.showSimpleMessage("Register successfully!",
                            String.format("Welcome %s", name), getActivity());
                    setVisibility();
                } else {
                    SimpleAlertController.showSimpleMessage("Failed to register",
                            "Please try again.", getActivity());
                }
            }
        });
    }

    @OnClick(R.id.logout_button)
    public void logout() {
        tmpEmail = null;
        tmpPassword = null;
        userStore.fetch();
        userStore.email = null;
        userStore.password = null;
        userStore.save();
        setVisibility();
    }
}
