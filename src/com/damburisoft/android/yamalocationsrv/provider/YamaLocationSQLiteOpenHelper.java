package com.damburisoft.android.yamalocationsrv.provider;

import com.damburisoft.android.yamalocationsrv.model.YamaLocationColumn;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class YamaLocationSQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String DB_TABLE_INFO_NAME = "info";
    
    private static final String DB_NAME = "location.db";

    private static final int DB_VERSION = 1;
    private static final String TAG = YamaLocationSQLiteOpenHelper.class.getName();
    private boolean debug = true;

    public YamaLocationSQLiteOpenHelper(Context context, String name,
            CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    
    public YamaLocationSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private void createInfoTable(SQLiteDatabase db) {
        db.execSQL("Create TABLE " + DB_TABLE_INFO_NAME + " (" +
                YamaLocationColumn.Info._ID + " INTEGER PRIMARY KEY, " +
                YamaLocationColumn.Info.BATTERY_LEVEL + " REAL, " +
                YamaLocationColumn.Info.NICKNAME + " TEXT, " +
                YamaLocationColumn.Info.HEADING + " REAL, " +
                YamaLocationColumn.Info.HEADING_ACCURACY + " REAL, " +
                YamaLocationColumn.Info.HORIZONTAL_ACCURACY + " REAL, " +
                YamaLocationColumn.Info.LATITUDE + " REAL, " +
                YamaLocationColumn.Info.LONGITUDE + " REAL, " +
                YamaLocationColumn.Info.ALTITUDE + " REAL, " +
                YamaLocationColumn.Info.TIMESTAMP + " INTEGER, " +
                YamaLocationColumn.Info.PUSHED + " INTEGER);");
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        createInfoTable(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (debug) {
            Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        }

        if (oldVersion == 0) {
            db.execSQL("Drop TABLE " + DB_TABLE_INFO_NAME + ";");
        }
        
    }

}
