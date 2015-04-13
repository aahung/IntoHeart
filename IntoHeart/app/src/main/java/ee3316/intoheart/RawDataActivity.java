package ee3316.intoheart;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee3316.intoheart.Data.HeartRateContract;
import ee3316.intoheart.Data.HeartRateStoreController;
import ee3316.intoheart.HTTP.JCallback;


public class RawDataActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_data);

        final ProgressDialog progressDialog = ProgressDialog.show(RawDataActivity.this, null, "Reading from database.");
        final HeartRateContract heartRateContract = new HeartRateContract(this);
        final RawDataListAdapter adapter = new RawDataListAdapter();
        Thread mThread = new Thread() {
            @Override
            public void run() {
                List<Long[]> ds = heartRateContract.getHRs();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                for (Long[] d : ds) {
                    Date time = new Date(d[0] * 1000);
                    adapter.addData(simpleDateFormat.format(time), String.format("ave: %s, [%s, %s], dev: %s",
                            d[1], d[3], d[2], d[4]));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    }
                });
            }
        };
        mThread.start();
        setListAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_raw_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class RawDataListAdapter extends BaseAdapter {
        private List<String[]> datas;
        private LayoutInflater mInflator;

        public RawDataListAdapter() {
            super();
            datas = new ArrayList<>();
            mInflator = getLayoutInflater();
        }

        public void addData (String time, String hr) {
            datas.add(new String[]{time, String.valueOf(hr)});
        }

        public String[] getData(int position) {
            return datas.get(position);
        }

        public void clear() {
            datas.clear();
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_raw_data, null);
                viewHolder = new ViewHolder();
                viewHolder.timestamp = (TextView) view.findViewById(R.id.timeText);
                viewHolder.hr = (TextView) view.findViewById(R.id.hrText);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            String[] data = datas.get(i);
            viewHolder.timestamp.setText(data[0]);
            viewHolder.hr.setText(data[1]);

            return view;
        }

        public class ViewHolder {
            TextView timestamp;
            TextView hr;
        }
    }
}
