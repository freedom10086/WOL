package me.yluo.wol;

import android.app.ActionBar;
import android.app.Activity;
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

import java.util.ArrayList;
import java.util.List;

import me.yluo.wol.db.MyDB;
import me.yluo.wol.utils.NetUtil;


public class AddActivity extends Activity implements AdapterView.OnItemClickListener, HostScanTask.ScanCallbak, AddHostDialog.AddHostListener {

    private ListView hostsList;
    private List<HostBean> hosts = new ArrayList<>();
    private HostScanTask hostScanTask;
    private MyHostAdapter adapter;
    private MyDB myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        hostsList = (ListView) findViewById(R.id.hosts);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        myDB = new MyDB(this);
        hosts = myDB.getScanHost();
        adapter = new MyHostAdapter();
        hostsList.setAdapter(adapter);
        hostsList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HostBean bean = hosts.get(i);
        AddHostDialog dialog = AddHostDialog.newInstance(bean, this);
        dialog.show(getFragmentManager(), "addHost");
    }

    @Override
    public void onScanProgress(HostBean bean) {
        hosts.add(bean);
        adapter.notifyDataSetChanged();
        myDB.addScanHost(bean);
    }

    @Override
    public void onScanFinish() {
        MainActivity.notifyUser(this, "扫描完成~~");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hostScanTask != null) {
            hostScanTask.cancel(true);
        }
    }

    @Override
    public void onAddHostOkClick(HostBean bean) {
        if (myDB.isHostExist(bean)) {
            MainActivity.notifyUser(this, "设备已经存在~~");
            return;
        }
        myDB.addHost(bean);
        MainActivity.notifyUser(this, "设备添加完成~~");
        this.finish();
    }

    public class MyHostAdapter extends BaseAdapter {

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
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_scan_host, null);
            String nickName = hosts.get(i).nickName;
            if (TextUtils.isEmpty(nickName)) {
                nickName = hosts.get(i).host;
            } else {
                nickName = nickName + "(" + hosts.get(i).host + ")";
            }
            if (hosts.get(i).host.equals(NetUtil.getLocalIp())) {
                hosts.get(i).macAddr = NetUtil.getLocalMacAddr(AddActivity.this);
            }
            ((TextView) view.findViewById(R.id.name)).setText(nickName);
            ((TextView) view.findViewById(R.id.mac)).setText(hosts.get(i).macAddr);

            return view;
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (NetUtil.isWiFi(this)) {
                    myDB.clearScanHosts();
                    hosts.clear();
                    adapter.notifyDataSetChanged();
                    String ip = NetUtil.getLocalIp();
                    hostScanTask = new HostScanTask(ip, this);
                    hostScanTask.execute();
                    MainActivity.notifyUser(this, "开始扫描局域网中的设备~~");
                } else {
                    MainActivity.notifyUser(this, "不在局域网无法搜索");
                }
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add:
                AddHostDialog dialog = AddHostDialog.newInstance(null, this);
                dialog.show(getFragmentManager(), "addHost");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
