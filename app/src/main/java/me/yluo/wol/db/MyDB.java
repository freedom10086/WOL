package me.yluo.wol.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.yluo.wol.HostBean;


public class MyDB {
    private Context context;
    /**
     * 主机列表
     */
    static final String TABLE_HOST_LIST = "wol_host_list";
    /**
     * 搜索历史表
     */
    static final String TABLE_SCAN_LIST = "wol_scan_list";


    private SQLiteDatabase db = null;    //数据库操作


    //构造函数
    public MyDB(Context context) {
        this.context = context;
        this.db = new SQLiteHelper(context).getWritableDatabase();
    }

    private SQLiteDatabase initDb() {
        if (this.db == null || !this.db.isOpen()) {
            this.db = new SQLiteHelper(context).getWritableDatabase();
        }
        return this.db;
    }

    public void clearAllDataBase() {
        initDb();
        String sql = "DELETE FROM " + TABLE_SCAN_LIST;
        this.db.execSQL(sql);
        sql = "DELETE FROM " + TABLE_HOST_LIST;
        this.db.execSQL(sql);
        this.db.close();
        Log.e("mydb", "clear all TABLE_MESSAGE");
    }

    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());

        return format.format(curDate);
    }

    private Date getDate(String str) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return format.parse(str);
    }

    /*
        "id INTEGER 0"
        "nickname VARCHAR(20),  1"
        "host VARCHAR(25)   2"
        "mac VARCHAR(20) 3"
        "port  NOT NULL,4"
        "lastconnect  DATETIME  5"
     */
    public void addHost(HostBean bean) {
        initDb();
        if (TextUtils.isEmpty(bean.nickName)) {
            bean.nickName = bean.host;
        }

        String sql = "INSERT INTO " + TABLE_HOST_LIST + " (nickname,host,mac,port,lastconnect)"
                + " VALUES(?,?,?,?,?)";
        Object args[] = new Object[]{bean.nickName, bean.host, bean.macAddr, bean.port, getTime()};
        this.db.execSQL(sql, args);
        this.db.close();
        Log.e("mydb", bean.nickName + "addHost");
    }


    public void updateHost(HostBean bean) {
        initDb();
        if (TextUtils.isEmpty(bean.nickName)) {
            bean.nickName = bean.host;
        }
        String sql = "UPDATE " + TABLE_HOST_LIST + " SET nickName = ?,host = ?,mac = ?,port = ? WHERE id = ?";
        Object args[] = new Object[]{bean.nickName, bean.host, bean.macAddr, bean.port, bean.id};
        this.db.execSQL(sql, args);
        this.db.close();
        Log.e("mydb", bean.nickName + "updateHost");
    }

    public void deleteHost(int id) {
        initDb();
        String sql = "DELETE FROM " + TABLE_HOST_LIST + " WHERE id = ?";
        Object args[] = new Object[]{id};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void updateConnect(int id) {
        initDb();
        String sql = "UPDATE " + TABLE_HOST_LIST + " SET count = count +1,lastconnect = ? WHERE id=? ";
        Object args[] = new Object[]{getTime(), id};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public List<HostBean> getHost() {
        initDb();
        List<HostBean> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_HOST_LIST + " order by lastconnect desc";
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            //String nickName, String host, int port, String macAddr
            HostBean bean = new HostBean(result.getString(1), result.getString(2), result.getInt(4), result.getString(3));
            bean.id = result.getInt(0);
            datas.add(bean);
        }
        result.close();
        this.db.close();
        return datas;
    }

    public void clearHosts() {
        initDb();
        String sql = "DELETE FROM " + TABLE_HOST_LIST;
        this.db.execSQL(sql);
        this.db.close();
    }


    public boolean isHostExist(HostBean bean) {
        initDb();
        String sql = "SELECT * FROM " + TABLE_HOST_LIST + " WHERE host = ? AND mac = ? AND port =?";
        String args[] = new String[]{bean.host, bean.macAddr, String.valueOf(bean.port)};
        Cursor result = db.rawQuery(sql, args);
        int count = result.getCount();
        result.close();
        this.db.close();
        return count != 0;
    }

    /*
      host VARCHAR(25) PRIMARY KEY,
      nickname VARCHAR(20),
      mac VARCHAR(20)
    */

    public void addScanHost(HostBean bean) {
        initDb();
        if (TextUtils.isEmpty(bean.nickName)) {
            bean.nickName = bean.host;
        }
        String sql = "INSERT INTO " + TABLE_SCAN_LIST + " (host,nickname,mac)"
                + " VALUES(?,?,?)";
        Object args[] = new Object[]{bean.host, bean.nickName, bean.macAddr};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public List<HostBean> getScanHost() {
        initDb();
        List<HostBean> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_SCAN_LIST;
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            //String nickName, String host, int port, String macAddr
            HostBean bean = new HostBean(result.getString(1), result.getString(0), 9, result.getString(2));
            datas.add(bean);
        }
        result.close();
        this.db.close();
        return datas;
    }

    public void clearScanHosts() {
        initDb();
        String sql = "DELETE FROM " + TABLE_SCAN_LIST;
        this.db.execSQL(sql);
        this.db.close();
    }

}