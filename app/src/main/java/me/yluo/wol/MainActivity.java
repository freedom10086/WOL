package me.yluo.wol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.yluo.wol.db.MyDB;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    public static final String TAG = "WakeOnLan";
    private ListView hostList;
    private MyDB myDB;
    private List<HostBean> hosts = new ArrayList<>();
    private HostAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hostList = (ListView) findViewById(R.id.hosts);
        myDB = new MyDB(this);
        adapter = new HostAdapter();
        hostList.setAdapter(adapter);
        hostList.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hosts = myDB.getHost();
        adapter.notifyDataSetChanged();
    }

    private void sendMagicPacket(HostBean bean) {
        String macStr = bean.macAddr;
        String ipStr = bean.host;

        if (!MagicPacket.validIp(ipStr)) {
            notifyUser(this, "ip地址不合法");
            return;
        }

        if (!MagicPacket.validateMac(macStr)) {
            notifyUser(this, "mac地址不合法");
            return;
        }

        MySendTask task = new MySendTask();
        task.execute(bean);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        sendMagicPacket(hosts.get(i));
    }

    private class MySendTask extends AsyncTask<HostBean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(HostBean... models) {
            HostBean model = models[0];
            try {
                return MagicPacket.send(model.macAddr, model.host, model.port);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                notifyUser(MainActivity.this, "发送成功");
            } else {
                notifyUser(MainActivity.this, "发送失败");
            }

        }
    }


    private class HostAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return hosts.size();
        }

        @Override
        public Object getItem(int i) {
            return hosts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_host, null);

            String nickName = hosts.get(i).nickName;
            if (TextUtils.isEmpty(nickName)) {
                nickName = hosts.get(i).host;
            } else {
                nickName = nickName + "(" + hosts.get(i).host + ")";
            }

            ((TextView) view.findViewById(R.id.name)).setText(nickName);
            ((TextView) view.findViewById(R.id.mac)).setText(hosts.get(i).macAddr);
            ((TextView) view.findViewById(R.id.index)).setText(String.valueOf(i + 1));

            int count = hosts.get(i).count;
            if (count > 0) {
                ((TextView) view.findViewById(R.id.count)).setText("连接" + count + "次");
            }

            return view;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case R.id.action_add:
                startActivity(new Intent(this, AddActivity.class));
                break;
        }

        mi.setChecked(true);
        return true;
    }


    private static Toast notification;

    public static void notifyUser(Context context, String message) {
        if (notification != null) {
            notification.setText(message);
            notification.show();
        } else {
            notification = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            notification.show();
        }
    }
}
