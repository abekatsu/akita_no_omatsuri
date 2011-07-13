package com.damburisoft.android.yamalocationsrv.provider;

import com.damburisoft.android.yamalocationsrv.model.OmatsuriEvent;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriRole;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class EventInfoSQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String DB_EVENT_TABLE_NAME = "eventTbl";
    public static final String DB_ROLE_TABLE_NAME = "roleTbl";
    private static final String DB_NAME = "eventInfo.db";
    private static final int DB_VERSION = 1;
    private static final String TAG = EventInfoSQLiteOpenHelper.class.getName();
    private static final boolean debug = false;
    
    public EventInfoSQLiteOpenHelper(Context context, String name,
            CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public EventInfoSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create TABLE " + DB_EVENT_TABLE_NAME + " (" +
                OmatsuriEvent.Columns._ID + " INTEGER PRIMARY KEY, " +
                OmatsuriEvent.Columns.ID + " " + OmatsuriEvent.Columns.ID_TYPENAME +  ", " +
                OmatsuriEvent.Columns.USER_ID +" " + OmatsuriEvent.Columns.USER_ID_TYPENAME +  ", " +
                OmatsuriEvent.Columns.CENTER + " " + OmatsuriEvent.Columns.CENTER_TYPENAME + ", " +
                OmatsuriEvent.Columns.CODE + " " + OmatsuriEvent.Columns.CODE_TYPENAME + ", " +
                OmatsuriEvent.Columns.CREATED_AT + " " + OmatsuriEvent.Columns.CREATED_AT_TYPENAME + ", " +
                OmatsuriEvent.Columns.DELAY_TIME + " " + OmatsuriEvent.Columns.DELAY_TIME_TYPENAME + ", " +
                OmatsuriEvent.Columns.END_AT + " " + OmatsuriEvent.Columns.END_AT_TYPENAME + ", " +
                OmatsuriEvent.Columns.START_AT + " " + OmatsuriEvent.Columns.START_AT_TYPENAME + ", " +
                OmatsuriEvent.Columns.SCALE + " " + OmatsuriEvent.Columns.SCALE_TYPENAME + ", " +
                OmatsuriEvent.Columns.SHARE_TYPE + " " + OmatsuriEvent.Columns.SHARE_TYPE_TYPENAME + ", " +
                OmatsuriEvent.Columns.SUMMARY + " " + OmatsuriEvent.Columns.SUMMARY_TYPENAME + ", " +
                OmatsuriEvent.Columns.TITLE + " " + OmatsuriEvent.Columns.TITLE_TYPENAME + ", " +
                OmatsuriEvent.Columns.UPDATED_AT + " " + OmatsuriEvent.Columns.UPDATED_AT_TYPENAME + "); ");
        db.execSQL("Create TABLE " + DB_ROLE_TABLE_NAME + " (" +
                OmatsuriRole.Columns._ID + " INTEGER PRIMARY KEY, " +
                OmatsuriRole.Columns.ID + " " + OmatsuriRole.Columns.ID_TYPENAME +  ", " +
                OmatsuriRole.Columns.CODE + " " + OmatsuriRole.Columns.CODE_TYPENAME +  ", " +
                OmatsuriRole.Columns.CREATED_AT + " " + OmatsuriRole.Columns.CREATED_AT_TYPENAME +  ", " +
                OmatsuriRole.Columns.DEVICE_ID + " " + OmatsuriRole.Columns.DEVICE_ID_TYPENAME +  ", " +
                OmatsuriRole.Columns.EVENT_ID + " " + OmatsuriRole.Columns.EVENT_ID_TYPENAME +  ", " +
                OmatsuriRole.Columns.NAME + " " + OmatsuriRole.Columns.NAME_TYPENAME +  ", " +
                OmatsuriRole.Columns.SUMMARY + " " + OmatsuriRole.Columns.SUMMARY_TYPENAME +  ", " +
                OmatsuriRole.Columns.UPDATED_AT + " " + OmatsuriRole.Columns.UPDATED_AT_TYPENAME +  ", " +
                OmatsuriRole.Columns.URL + " " + OmatsuriRole.Columns.URL_TYPENAME  + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (debug) {
            Log.i(TAG, "Upgrading database from version " + oldVersion 
                    + " to " + newVersion + ", which will destroy all old data");
        }
        // TODO more intelligent
        db.execSQL("DROP TABLE IF EXISTS " + DB_EVENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DB_ROLE_TABLE_NAME);
        onCreate(db);


    }

}
