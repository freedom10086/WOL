package me.yluo.wol;


public class HostBean {
    public int id;
    public String nickName;
    public String host;
    public String macAddr;
    public int port = 9;
    public String lastConnect;

    public HostBean(String host) {
        this.host = host;
    }

    public HostBean(String nickName, String host, int port, String macAddr) {
        this.nickName = nickName;
        this.host = host;
        this.macAddr = macAddr;
        this.port = port;
    }
}
