package ee3316.intoheart;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import ee3316.intoheart.Data.UserStore;
import ee3316.intoheart.HTTP.Connector;
import ee3316.intoheart.HTTP.JCallback;
import ee3316.intoheart.HTTP.Outcome;
import ee3316.intoheart.UIComponent.SimpleAlertController;

/**
 * Created by Lily on 2015/4/7.
 */
public class AddFriendActivity extends Activity{
    String tEmail = null;
    UserStore userStore;
    @InjectView(R.id.search_view)
    SearchView searchView;

    @InjectView(R.id.result_block)
    LinearLayout resultBlock;

    @InjectView(R.id.user_name)
    TextView userNameText;

    @InjectView(R.id.user_email)
    TextView userEmailText;

    Connector connector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        userStore = new UserStore(this);
        connector = new Connector();

        ButterKnife.inject(this);
        resultBlock.setVisibility(View.GONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                connector.search(query, new JCallback<Outcome>() {
                    @Override
                    public void call(Outcome outcome) {
                        if (outcome.success) {
                            String name = outcome.getString();
                            resultBlock.setVisibility(View.VISIBLE);
                            userNameText.setText(name);
                            userEmailText.setText(query);
                            tEmail = query;
                        } else {
                            resultBlock.setVisibility(View.GONE);
                            SimpleAlertController.showSimpleMessage("Sorry",
                                    outcome.getString(), AddFriendActivity.this);
                        }
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @OnClick(R.id.add_new_friends)
    public void addFriend(View view) {
        String email = tEmail;
        if (email == userStore.email) {
            SimpleAlertController.showSimpleMessage("Sorry",
                    "You cannot add self as friend.", AddFriendActivity.this);
            return;
        }
        connector.sendRequest(userStore.email, userStore.password, email, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                if (outcome.success) {
                    SimpleAlertController.showSimpleMessage("Cool",
                            "Friend request sent.", AddFriendActivity.this);
                } else {
                    SimpleAlertController.showSimpleMessage("Sorry",
                            outcome.getString(), AddFriendActivity.this);
                }
            }
        });
    }
}
