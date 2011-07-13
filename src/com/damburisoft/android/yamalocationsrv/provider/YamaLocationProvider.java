package com.damburisoft.android.yamalocationsrv.provider;

import java.util.HashMap;

import com.damburisoft.android.yamalocationsrv.model.YamaLocationColumn;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class YamaLocationProvider extends ContentProvider {

    public static final String AUTHORITY = "com.damburisoft.android.yamalocationsrv.provider.yamalocationprovider";

    private YamaLocationSQLiteOpenHelper mDbHelper;
    
    private static final int INFO = 0; 
    private static final int INFO_ID = 1; 

    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> sInfosProjectionMap;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(YamaLocationProvider.AUTHORITY, YamaLocationColumn.Info.PATH, INFO);
        sUriMatcher.addURI(YamaLocationProvider.AUTHORITY, YamaLocationColumn.Info.PATH + "/#", INFO_ID);
        
        sInfosProjectionMap = new HashMap<String, String>();
        sInfosProjectionMap.put(YamaLocationColumn.Info._ID, YamaLocationColumn.Info._ID);
        sInfosProjectionMap.put(YamaLocationColumn.Info.BATTERY_LEVEL, YamaLocationColumn.Info.BATTERY_LEVEL);
        sInfosProjectionMap.put(YamaLocationColumn.Info.NICKNAME, YamaLocationColumn.Info.NICKNAME);
        sInfosProjectionMap.put(YamaLocationColumn.Info.HEADING, YamaLocationColumn.Info.HEADING);
        sInfosProjectionMap.put(YamaLocationColumn.Info.HEADING_ACCURACY, YamaLocationColumn.Info.HEADING_ACCURACY);
        sInfosProjectionMap.put(YamaLocationColumn.Info.HORIZONTAL_ACCURACY, YamaLocationColumn.Info.HORIZONTAL_ACCURACY);
        sInfosProjectionMap.put(YamaLocationColumn.Info.LATITUDE, YamaLocationColumn.Info.LATITUDE);
        sInfosProjectionMap.put(YamaLocationColumn.Info.LONGITUDE, YamaLocationColumn.Info.LONGITUDE);
        sInfosProjectionMap.put(YamaLocationColumn.Info.ALTITUDE, YamaLocationColumn.Info.ALTITUDE);
        sInfosProjectionMap.put(YamaLocationColumn.Info.TIMESTAMP, YamaLocationColumn.Info.TIMESTAMP);
        sInfosProjectionMap.put(YamaLocationColumn.Info.PUSHED, YamaLocationColumn.Info.PUSHED);

    }
    
    @Override
    public int delete(Uri uri, String initWhereClause, String[] whereArgs) {
        String db_table_name;
        String whereClause;
        String _id;
        switch (sUriMatcher.match(uri)) {
        case INFO:
            db_table_name = YamaLocationSQLiteOpenHelper.DB_TABLE_INFO_NAME;
            whereClause = initWhereClause;
            break;
        case INFO_ID:
            _id = uri.getPathSegments().get(1); 
            db_table_name = YamaLocationSQLiteOpenHelper.DB_TABLE_INFO_NAME;
            whereClause = YamaLocationColumn.Info._ID + "=" + _id 
                + (!TextUtils.isEmpty(initWhereClause) ? " AND (" + initWhereClause + ')' : "");
            break;
        default:
            throw new IllegalArgumentException("unknown URI: " + uri.toString());
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.delete(db_table_name, whereClause, whereArgs);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        String ret = null;
        switch (sUriMatcher.match(uri)) {
        case INFO:
            ret = YamaLocationColumn.Info.CONTENT_TYPE;
            break;
        case INFO_ID:
            ret = YamaLocationColumn.Info.CONTENT_ITEM_TYPE;
            break;
        default:
            throw new IllegalArgumentException("unknown URI: " + uri.toString());
        }
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        Uri content_uri = null;
        String db_table_name;
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        switch (sUriMatcher.match(uri)) {
        case INFO:
            content_uri = YamaLocationColumn.Info.CONTENT_URI;
            db_table_name = YamaLocationSQLiteOpenHelper.DB_TABLE_INFO_NAME;
            YamaLocationColumn.Info.checkInsertValues(values);
            break;
        case INFO_ID:
            throw new IllegalArgumentException("invalid URI: " + uri.toString());
        default:
            throw new IllegalArgumentException("unknown URI: " + uri.toString());
        }
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (db_table_name != null) {
            long rowId = db.insertOrThrow(db_table_name, null, values);
            if (rowId > 0 & content_uri != null) {
                Uri retUri = ContentUris.withAppendedId(content_uri, rowId);
                getContext().getContentResolver().notifyChange(retUri, null);
                return retUri;
            }
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new YamaLocationSQLiteOpenHelper(getContext());
        return (mDbHelper == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
        case INFO:
            qb.setTables(YamaLocationSQLiteOpenHelper.DB_TABLE_INFO_NAME);
            qb.setProjectionMap(sInfosProjectionMap);
            break;
        case INFO_ID:
            qb.setTables(YamaLocationSQLiteOpenHelper.DB_TABLE_INFO_NAME);
            qb.setProjectionMap(sInfosProjectionMap);
            qb.appendWhere(YamaLocationColumn.Info._ID + "=" + uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String initSelection, String[] selectionArgs) {
        String selection = null;
        String db_table = null;
        String _id = null;

        switch (sUriMatcher.match(uri)) {
        case INFO:
            db_table = YamaLocationSQLiteOpenHelper.DB_TABLE_INFO_NAME;
            selection = initSelection;
            break;
        case INFO_ID:
            db_table = YamaLocationSQLiteOpenHelper.DB_TABLE_INFO_NAME;
            _id = uri.getPathSegments().get(1);
            selection = YamaLocationColumn.Info._ID + "=" + _id 
                + (!TextUtils.isEmpty(initSelection) ? "AND (" + initSelection + ")" : "");
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.update(db_table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
