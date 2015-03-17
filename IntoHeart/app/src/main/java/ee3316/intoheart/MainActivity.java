package ee3316.intoheart;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee3316.intoheart.BLE.BluetoothLeService;
import ee3316.intoheart.BLE.SensorConnectionManager;
import ee3316.intoheart.Data.HeartRateContract;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String PREFS_NAME_RECENT_ADDRESS = "PrefsRecentAddr";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    public HeartRateContract heartRateContract = new HeartRateContract(this);
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public List<String> sectionTitles;

    public SensorConnectionManager sensorConnectionManager;

    public MainActivity() {
        sectionTitles = new ArrayList<>();
        sectionTitles.add("Dashboard");
        sectionTitles.add("Analysis");
        sectionTitles.add("Sensors");
        sectionTitles.add("Log in");
        sectionTitles.add("Ranking");
        sectionTitles.add("My Info");
        sectionTitles.add("Lifestyle");

        sensorConnectionManager = new SensorConnectionManager(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, sensorConnectionManager.mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(sensorConnectionManager.mGattUpdateReceiver,
                sensorConnectionManager.makeGattUpdateIntentFilter());
        sensorConnectionManager.mBluetoothLeService = new BluetoothLeService();

        // read device address
        SharedPreferences settings = getSharedPreferences(PREFS_NAME_RECENT_ADDRESS, 0);
        sensorConnectionManager.mDeviceAddress = settings.getString(PREFS_NAME_RECENT_ADDRESS, null);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_NAME_RECENT_ADDRESS, null);
        editor.commit();
        if (sensorConnectionManager.mBluetoothLeService != null) {
            final boolean result
                = sensorConnectionManager.mBluetoothLeService.
                connect(sensorConnectionManager.mDeviceAddress);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME_RECENT_ADDRESS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_NAME_RECENT_ADDRESS, sensorConnectionManager.mDeviceAddress);
        editor.commit();
        unbindService(sensorConnectionManager.mServiceConnection);
        sensorConnectionManager.mBluetoothLeService = null;
        unregisterReceiver(sensorConnectionManager.mGattUpdateReceiver);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        Fragment fragment;
        switch (position) {
            default:
            case 0:
                fragment = DashboardFragment.newInstance(position + 1);
                break;
            case 1:
                fragment = AnalysisFragment.newInstance(position + 1);
                break;
            case 2:
                fragment = SensorsFragment.newInstance(position + 1);
                break;
            case 3:
                final Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return;
            case 4:
                fragment = RankingFragment.newInstance(position + 1);
                break;
            case 5:
                fragment = UserinfoFragment.newInstance(position + 1);
                break;
            case 6:
                fragment = LifestyleFragment.newInstance(position + 1);
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        int index = number - 1;
        if (index >= sectionTitles.size()) {
            index = 0;
        }
        mTitle = sectionTitles.get(index);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
