package me.yluo.wol;

import android.text.TextUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * @desc Static WOL magic packet class
 */
public class MagicPacket {
    private static final String TAG = "MagicPacket";
    public static final String BROADCAST = "192.168.1.255";
    public static final int PORT = 9;

    public static void send(String mac, String ip) throws IOException {
        send(mac, ip, PORT);
    }

    public static boolean send(String mac, String ip, int port) throws IOException {
        if (!validateMac(mac)) {
            return false;
        }
        mac = mac.replaceAll("(:|\\-)", "");
        final byte[] bytes = new byte[102];
        //ff ff ff ff ff ff 固定
        //11 22 33 44 55 66 mac
        //....mac 直到填充满

        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }

        final byte[] macBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            macBytes[i] = (byte) Integer.parseInt(mac.substring(i * 2, i * 2 + 2), 16);
        }

        for (int i = 6; i < 102; i += 6) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        // create socket to IP
        final InetAddress address = InetAddress.getByName(ip);
        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
        final DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();

        return true;
    }

    public static boolean validateMac(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return false;
        }
        mac = mac.replaceAll("(:|\\-)", "");
        return mac.matches("([a-zA-Z0-9]){12}");
    }

    public static boolean validIp(String ip) {
        if (TextUtils.isEmpty(ip)) {
            return false;
        }
        return ip.matches("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
    }

    public static String formatMac(String inputMac) {
        String mac = inputMac.replaceAll("(:|\\-)", "").toUpperCase();
        if (mac.length() > 12) {
            mac = mac.substring(0, 12);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length(); i++) {
            if (i != 0 && i % 2 == 0) {
                sb.append(":");
            }
            sb.append(mac.charAt(i));
        }
        return sb.toString();
    }
}
