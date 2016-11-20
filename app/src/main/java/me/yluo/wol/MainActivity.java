package me.yluo.wol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

    public static final String TAG = "WakeOnLan";
    private EditText ip, mac, port;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ip = (EditText) findViewById(R.id.ip);
        mac = (EditText) findViewById(R.id.mac);
        port = (EditText) findViewById(R.id.port);

        mac.setOnFocusChangeListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendMagicPacket();
                break;
        }
    }

    private void sendMagicPacket() {
        String macStr = mac.getText().toString();
        String ipStr = ip.getText().toString();
        String portStr = port.getText().toString();

        if (!MagicPacket.validIp(ipStr)) {
            ip.setError("ip地址不合法");
            return;
        }

        if (!MagicPacket.validateMac(macStr)) {
            mac.setError("mac地址不合法");
            return;
        }

        int portInt = 9;
        if (!TextUtils.isEmpty(portStr)) {
            portInt = Integer.parseInt(portStr);
        }

        HostBean model = new HostBean(ipStr, ipStr, portInt, macStr);
        MySendTask task = new MySendTask();
        task.execute(model);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()) {
            case R.id.mac:
                if (!b) {
                    String input = mac.getText().toString();
                    mac.setText(MagicPacket.formatMac(input));
                }
                break;
        }
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
}
