package com.damburisoft.android.yamalocationsrv.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.provider.BaseColumns;

public class OmatsuriRole extends AbstractOmatsuriObject {

    public OmatsuriRole(JSONObject obj) throws JSONException {
        super();
        this.id = obj.getInt("id");
        this.code = getJSONObjectStringValue("code", obj);
        this.created_at = getJSONObjectStringValue("created_at", obj);
        this.device_id = obj.getInt("device_id");
        this.event_id = obj.getInt("event_id");
        this.name = getJSONObjectStringValue("name", obj);
        this.summary = getJSONObjectStringValue("summary", obj);
        this.updated_at = getJSONObjectStringValue("updated_at", obj);
        this.url = getJSONObjectStringValue("url", obj);
    }

    public OmatsuriRole() {
        super();
    }

    @Key
    public String code;
    
    @Key
    public String created_at;
    
    @Key
    public int device_id;
    
    @Key
    public int event_id;
    
    @Key
    public int id;
    
    @Key
    public String name;
    
    @Key
    public String summary;
    
    @Key
    public String updated_at;
    
    @Key
    public String url;
    
    public static final class Columns implements BaseColumns {
        private Columns() {};
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of roles.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.damburisoft.yamalocation.role";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single role.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.damburisoft.yamalocation.role";

        /**
         * The paths for ContentProvider. 
         */
        public static final String PATH = "roles";
        
        /**
         * The content:// style URI for all data records of infos.
         */
        // public static final Uri CONTENT_URI = Uri.parse("content://" +  EventInfoProvider.AUTHORITY + "/" + PATH);

        public static final String CODE = "code";
        public static final String CODE_TYPENAME = "TEXT";

        public static final String CREATED_AT = "created_at";
        public static final String CREATED_AT_TYPENAME = "INTEGER";
        
        public static final String DEVICE_ID = "device_id";
        public static final String DEVICE_ID_TYPENAME = "INTEGER";
        
        public static final String EVENT_ID = "event_id";
        public static final String EVENT_ID_TYPENAME = "INTEGER";
        
        public static final String ID = "id";
        public static final String ID_TYPENAME = "INTEGER";
        
        public static final String NAME = "name";
        public static final String NAME_TYPENAME = "TEXT";

        public static final String SUMMARY = "summary";
        public static final String SUMMARY_TYPENAME = "TEXT";
        
        public static final String UPDATED_AT = "updated_at";
        public static final String UPDATED_AT_TYPENAME = "INTEGER";
        
        public static final String URL = "url";
        public static final String URL_TYPENAME = "TEXT";

    }
    
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(OmatsuriRole.Columns.CODE, code);
        cv.put(OmatsuriRole.Columns.CREATED_AT, created_at);
        cv.put(OmatsuriRole.Columns.DEVICE_ID, device_id);
        cv.put(OmatsuriRole.Columns.EVENT_ID, event_id);
        cv.put(OmatsuriRole.Columns.ID, id);
        cv.put(OmatsuriRole.Columns.NAME, name);
        cv.put(OmatsuriRole.Columns.SUMMARY, summary);
        cv.put(OmatsuriRole.Columns.UPDATED_AT, updated_at);
        cv.put(OmatsuriRole.Columns.URL, url);

        return cv;
    }

}
