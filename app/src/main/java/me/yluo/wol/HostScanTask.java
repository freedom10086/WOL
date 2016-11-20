package me.yluo.wol;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.yluo.wol.utils.NetUtil;

public class HostScanTask extends AsyncTask<Void, HostBean, Void> {

    private static final String TAG = "HostScanTask";
    public final static String NOMAC = "00:00:00:00:00:00";

    private final static String MAC_RE = "^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int BUF = 8 * 1024;

    private String ip;
    private ScanCallbak callbak;
    private ExecutorService mPool;
    private String localIp;

    public int start = 1;
    public int end = 255;

    public HostScanTask(String ip, ScanCallbak callbak) {
        this.callbak = callbak;
        if (!TextUtils.isEmpty(ip)) {
            this.ip = ip;
        } else {
            this.ip = NetUtil.getLocalIp();
        }
        if (TextUtils.isEmpty(this.ip)) {
            this.ip = NetUtil.DEFAULT_IP;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        localIp = NetUtil.getLocalIp();
        int thread = Runtime.getRuntime().availableProcessors();
        mPool = Executors.newFixedThreadPool(thread);
        String baseId = ip.substring(0, ip.lastIndexOf(".") + 1);
        for (int i = start; i < end; i++) {
            if (!mPool.isShutdown()) {
                mPool.execute(new CheckRunnable(baseId + String.valueOf(i)));
            }
        }
        mPool.shutdown();
        try {
            if (!mPool.awaitTermination(300, TimeUnit.SECONDS)) {
                mPool.shutdownNow();
                Log.e(TAG, "Shutting down pool");
                if (!mPool.awaitTermination(2, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            mPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return null;
    }


    @Override
    protected void onProgressUpdate(HostBean... host) {
        callbak.onScanProgress(host[0]);
    }

    @Override
    protected void onPostExecute(Void unused) {
        callbak.onScanFinish();
    }

    @Override
    protected void onCancelled() {
        if (mPool != null) {
            synchronized (mPool) {
                mPool.shutdownNow();
                // FIXME: Prevents some task to end (and close the Save DB)
            }
        }
        super.onCancelled();
    }


    private class CheckRunnable implements Runnable {
        private String addr;

        CheckRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            if (isCancelled()) {
                return;
            }
            HostBean bean = new HostBean(addr);
            if (!TextUtils.isEmpty(localIp) && localIp.equals(addr)) {
                bean.nickName = "本机";
                bean.macAddr = getMacAddress(bean.host);
                publishProgress(bean);
                return;
            }

            try {
                Log.e(TAG, "run=" + addr);
                InetAddress h = InetAddress.getByName(addr);
                String mac = getMacAddress(addr);

                if (!NOMAC.equals(mac)) {
                    Log.e(TAG, "found using arp #1 " + addr);
                    bean.macAddr = mac;
                    bean.nickName = h.getCanonicalHostName();
                    publishProgress(bean);
                    return;
                }

                // Native InetAddress check
                if (h.isReachable(300)) {
                    Log.e(TAG, "found using InetAddress ping " + addr);
                    bean.macAddr = getMacAddress(addr);
                    bean.nickName = h.getCanonicalHostName();
                    publishProgress(bean);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public String getMacAddress(String ip) {
        String hw = NOMAC;
        BufferedReader bufferedReader = null;
        try {
            if (ip != null) {
                String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
                Pattern pattern = Pattern.compile(ptrn);
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        hw = matcher.group(1);
                        break;
                    }
                }
            } else {
                Log.e(TAG, "host is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't open/read file ARP: " + e.getMessage());
            return hw;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return hw;
    }


    public interface ScanCallbak {
        void onScanProgress(HostBean bean);

        void onScanFinish();
    }

}
