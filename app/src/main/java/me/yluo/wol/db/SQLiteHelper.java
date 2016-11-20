package me.yluo.wol.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by free2 on 16-5-20.
 * 数据库操作类
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "wol.db";

    //更改版本后数据库将重新创建
    private static final int DATABASE_VERSION = 1;


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);//继承父类
    }


    /**
     * 该函数是在第一次创建数据库时执行，只有当其调用getreadabledatebase()
     */
    public void onCreate(SQLiteDatabase db) {

        /**
         *  主机列表
         */
        String sql1 = "CREATE TABLE " + MyDB.TABLE_HOST_LIST + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nickname VARCHAR(20),"
                + "host VARCHAR(25) NOT NULL,"
                + "mac VARCHAR(20) NOT NULL,"
                + "port INTEGER DEFAULT '9' NOT NULL,"
                + "count INTEGER DEFAULT '0' NOT NULL,"
                + "lastconnect  DATETIME NOT NULL"
                + ")";
        db.execSQL(sql1);
        Log.e("DATABASE", "主机列表创建成功");

        /**
         * 搜索历史
         */
        String sql2 = "CREATE TABLE " + MyDB.TABLE_SCAN_LIST + "("
                + "host VARCHAR(25) PRIMARY KEY,"
                + "nickname VARCHAR(20),"
                + "mac VARCHAR(20) NOT NULL"
                + ")";
        db.execSQL(sql2);
        Log.e("DATABASE", "搜索历史表创建成功");
    }


    /**
     * 数据库更新函数，当数据库更新时会执行此函数
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + MyDB.TABLE_HOST_LIST;
        db.execSQL(sql);

        String sql2 = "DROP TABLE IF EXISTS " + MyDB.TABLE_SCAN_LIST;
        db.execSQL(sql2);

        this.onCreate(db);
        Log.e("DATABASE", "数据库已更新");
    }

}