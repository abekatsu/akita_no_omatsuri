package com.damburisoft.android.yamalocationsrv.provider;

import java.util.HashMap;

import com.damburisoft.android.yamalocationsrv.model.OmatsuriEvent;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriRole;

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

public class EventInfoProvider extends ContentProvider {
    
    public static final String AUTHORITY = "com.damburisoft.android.yamalocationsrv.provider.eventinfoprovider";
    
    private EventInfoSQLiteOpenHelper mDbHelper;
    
    private static final int EVENT = 0; 
    private static final int EVENT_ID = 1; 
    private static final int ROLE = 2; 
    private static final int ROLE_ID = 3; 

    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> sEventProjectionMap;
    private static HashMap<String, String> sRoleProjectionMap;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, OmatsuriEvent.Columns.PATH, EVENT);
        sUriMatcher.addURI(AUTHORITY, OmatsuriEvent.Columns.PATH + "/#", EVENT_ID);
        sUriMatcher.addURI(AUTHORITY, OmatsuriEvent.Columns.PATH + "/#/" + OmatsuriRole.Columns.PATH, ROLE);
        sUriMatcher.addURI(AUTHORITY, OmatsuriEvent.Columns.PATH + "/#/" + OmatsuriRole.Columns.PATH + "/#", ROLE_ID);
        
        sEventProjectionMap = new HashMap<String, String>();
        sEventProjectionMap.put(OmatsuriEvent.Columns._ID, OmatsuriEvent.Columns._ID);
        sEventProjectionMap.put(OmatsuriEvent.Columns.ID, OmatsuriEvent.Columns.ID);
        sEventProjectionMap.put(OmatsuriEvent.Columns.USER_ID, OmatsuriEvent.Columns.USER_ID);
        sEventProjectionMap.put(OmatsuriEvent.Columns.CENTER, OmatsuriEvent.Columns.CENTER);
        sEventProjectionMap.put(OmatsuriEvent.Columns.CODE, OmatsuriEvent.Columns.CODE);
        sEventProjectionMap.put(OmatsuriEvent.Columns.CREATED_AT, OmatsuriEvent.Columns.CREATED_AT);
        sEventProjectionMap.put(OmatsuriEvent.Columns.DELAY_TIME, OmatsuriEvent.Columns.DELAY_TIME);
        sEventProjectionMap.put(OmatsuriEvent.Columns.END_AT, OmatsuriEvent.Columns.END_AT);
        sEventProjectionMap.put(OmatsuriEvent.Columns.SCALE, OmatsuriEvent.Columns.SCALE);
        sEventProjectionMap.put(OmatsuriEvent.Columns.SHARE_TYPE, OmatsuriEvent.Columns.SHARE_TYPE);
        sEventProjectionMap.put(OmatsuriEvent.Columns.START_AT, OmatsuriEvent.Columns.START_AT);
        sEventProjectionMap.put(OmatsuriEvent.Columns.SUMMARY, OmatsuriEvent.Columns.SUMMARY);
        sEventProjectionMap.put(OmatsuriEvent.Columns.TITLE, OmatsuriEvent.Columns.TITLE);
        sEventProjectionMap.put(OmatsuriEvent.Columns.UPDATED_AT, OmatsuriEvent.Columns.UPDATED_AT);
        
        sRoleProjectionMap = new HashMap<String, String>();
        sRoleProjectionMap.put(OmatsuriRole.Columns._ID, OmatsuriRole.Columns._ID);
        sRoleProjectionMap.put(OmatsuriRole.Columns.ID, OmatsuriRole.Columns.ID);
        sRoleProjectionMap.put(OmatsuriRole.Columns.CODE, OmatsuriRole.Columns.CODE);
        sRoleProjectionMap.put(OmatsuriRole.Columns.CREATED_AT, OmatsuriRole.Columns.CREATED_AT);
        sRoleProjectionMap.put(OmatsuriRole.Columns.DEVICE_ID, OmatsuriRole.Columns.DEVICE_ID);
        sRoleProjectionMap.put(OmatsuriRole.Columns.EVENT_ID, OmatsuriRole.Columns.EVENT_ID);
        sRoleProjectionMap.put(OmatsuriRole.Columns.NAME, OmatsuriRole.Columns.NAME);
        sRoleProjectionMap.put(OmatsuriRole.Columns.SUMMARY, OmatsuriRole.Columns.SUMMARY);
        sRoleProjectionMap.put(OmatsuriRole.Columns.UPDATED_AT, OmatsuriRole.Columns.UPDATED_AT);
        sRoleProjectionMap.put(OmatsuriRole.Columns.URL, OmatsuriRole.Columns.URL);

    }

    @Override
    public int delete(Uri uri, String initWhereClause, String[] whereArgs) {
        String db_table_name;
        String whereClause;
        switch (sUriMatcher.match(uri)) {
        case EVENT:
            db_table_name = EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME;
            whereClause = initWhereClause;
            break;
        case EVENT_ID:
            whereClause = OmatsuriEvent.Columns.ID + " = " + uri.getPathSegments().get(1)
                + (!TextUtils.isEmpty(initWhereClause) ? " AND (" + initWhereClause + ')' : "");
            db_table_name = EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME;
            break;
        case ROLE:
            db_table_name = EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME;
            whereClause = OmatsuriRole.Columns.EVENT_ID + " = " + uri.getPathSegments().get(1)
                + (!TextUtils.isEmpty(initWhereClause) ? " AND (" + initWhereClause + ')' : "");
            break;
        case ROLE_ID:
            db_table_name = EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME;
            whereClause = "(" + OmatsuriRole.Columns.EVENT_ID + " = " + uri.getPathSegments().get(1) + " AND "
                + OmatsuriRole.Columns.ID + " = " + uri.getPathSegments().get(3) + ")"
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
        case EVENT:
            ret = OmatsuriEvent.Columns.CONTENT_TYPE;
            break;
        case EVENT_ID:
            ret = OmatsuriEvent.Columns.CONTENT_ITEM_TYPE;
            break;
        case ROLE:
            ret = OmatsuriRole.Columns.CONTENT_TYPE;
            break;
        case ROLE_ID:
            ret = OmatsuriRole.Columns.CONTENT_ITEM_TYPE;
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
        case EVENT:
            content_uri = OmatsuriEvent.Columns.CONTENT_URI;
            db_table_name = EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME;
            break;
        case EVENT_ID:
            throw new IllegalArgumentException("invalid URI: " + uri.toString());
        case ROLE:
            content_uri = Uri.withAppendedPath(OmatsuriEvent.Columns.CONTENT_URI, 
                    uri.getPathSegments().get(1) + "/" + OmatsuriRole.Columns.PATH);
            db_table_name = EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME;
            break;
        case ROLE_ID:
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
        mDbHelper = new EventInfoSQLiteOpenHelper(getContext());
        return (mDbHelper == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, 
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
        case EVENT:
            qb.setTables(EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME);
            qb.setProjectionMap(sEventProjectionMap);
            break;
        case EVENT_ID:
            qb.setTables(EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME);
            qb.setProjectionMap(sEventProjectionMap);
            qb.appendWhere(OmatsuriEvent.Columns.ID + " = " + uri.getPathSegments().get(1));
            break;
        case ROLE:
            qb.setTables(EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME);
            qb.setProjectionMap(sRoleProjectionMap);
            qb.appendWhere(OmatsuriRole.Columns.EVENT_ID + " = " + uri.getPathSegments().get(1));
            break;
        case ROLE_ID:
            qb.setTables(EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME);
            qb.setProjectionMap(sRoleProjectionMap);
            qb.appendWhere(OmatsuriRole.Columns.EVENT_ID + " = " + uri.getPathSegments().get(1));
            qb.appendWhere(OmatsuriRole.Columns.ID + " = " + uri.getPathSegments().get(3));
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
    public int update(Uri uri, ContentValues values, 
            String initSelection, String[] selectionArgs) {
        String selection = null;
        String db_table = null;
        switch (sUriMatcher.match(uri)) {
        case EVENT:
            db_table = EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME;
            selection = initSelection;
            break;
        case EVENT_ID:
            db_table = EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME;
            selection = OmatsuriEvent.Columns.ID + "=" + uri.getPathSegments().get(1)
                + (!TextUtils.isEmpty(initSelection) ? " AND (" + initSelection + ")" : "");
            break;
        case ROLE:
            db_table = EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME;
            selection = OmatsuriRole.Columns.EVENT_ID + "=" + uri.getPathSegments().get(1)
                + (!TextUtils.isEmpty(initSelection) ? " AND (" + initSelection + ")" : "");
            break;
        case ROLE_ID:
            db_table = EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME;
            selection = "(" + OmatsuriRole.Columns.EVENT_ID + "=" + uri.getPathSegments().get(1) + " AND "
                + OmatsuriRole.Columns.ID + "=" + uri.getPathSegments().get(3) + ") "
                + (!TextUtils.isEmpty(initSelection) ? " AND (" + initSelection + ")" : "");
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.update(db_table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;

    }

}
