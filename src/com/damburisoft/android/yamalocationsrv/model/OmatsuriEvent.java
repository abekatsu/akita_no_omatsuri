package com.damburisoft.android.yamalocationsrv.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.damburisoft.android.yamalocationsrv.provider.EventInfoProvider;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class OmatsuriEvent extends AbstractOmatsuriObject {

    public OmatsuriEvent(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.user_id = obj.getInt("user_id");
        this.center = getJSONObjectStringValue("center", obj);
        this.code = getJSONObjectStringValue("code", obj);
        this.created_at = getJSONObjectStringValue("created_at", obj);
        this.delay_time = getJSONObjectStringValue("delay_time", obj);
        this.end_at = getJSONObjectStringValue("end_at", obj);
        this.scale = getJSONObjectStringValue("scale", obj);
        this.share_type = obj.getInt("share_type");
        this.start_at = getJSONObjectStringValue("start_at", obj);
        this.summary = getJSONObjectStringValue("summary", obj);
        this.title = getJSONObjectStringValue("title", obj);
        this.updated_at = getJSONObjectStringValue("updated_at", obj);
        
    }
    
    public OmatsuriEvent() {
        // TODO Auto-generated constructor stub
    }

    @Key
    public int id;
    
    @Key
    public int user_id;
    
    @Key
    public String center;
    
    @Key
    public String code;
    
    @Key
    public String created_at;
    
    @Key
    public String delay_time;
    
    @Key
    public String end_at;
    
    @Key
    public String scale;
    
    @Key
    public int share_type;
    
    @Key
    public String start_at;
    
    @Key
    public String summary;
    
    @Key
    public String title;
    
    @Key
    public String updated_at;
    
    
    public static final class Columns implements BaseColumns {
        private Columns() {};
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of events.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.damburisoft.yamalocation.event";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single event.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.damburisoft.yamalocation.event";

        /**
         * The paths for ContentProvider. 
         */
        public static final String PATH = "events";
        
        /**
         * The content:// style URI for all data records of infos.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +  EventInfoProvider.AUTHORITY + "/" 
                + PATH);

        public static final String ID = "id";
        public static final String ID_TYPENAME = "INTEGER";
        
        public static final String USER_ID = "user_id";
        public static final String USER_ID_TYPENAME = "INTEGER";
        
        public static final String CENTER = "center";
        public static final String CENTER_TYPENAME = "TEXT";
        
        public static final String CODE = "code";
        public static final String CODE_TYPENAME = "TEXT";
        
        public static final String CREATED_AT = "created_at";
        public static final String CREATED_AT_TYPENAME = "INTEGER";
        
        public static final String DELAY_TIME = "delay_time";
        public static final String DELAY_TIME_TYPENAME = "INTEGER";
        
        public static final String END_AT = "end_at";
        public static final String END_AT_TYPENAME = "INTEGER";
        
        public static final String SCALE = "scale";
        public static final String SCALE_TYPENAME = "TEXT";
        
        public static final String SHARE_TYPE = "share_type";
        public static final String SHARE_TYPE_TYPENAME = "INTEGER";
        
        public static final String START_AT = "start_at";
        public static final String START_AT_TYPENAME = "INTEGER";
        
        public static final String SUMMARY = "summary";
        public static final String SUMMARY_TYPENAME = "TEXT";
        
        public static final String TITLE = "title";
        public static final String TITLE_TYPENAME = "TEXT";
        
        public static final String UPDATED_AT = "updated_at";
        public static final String UPDATED_AT_TYPENAME = "INTEGER";

    }


    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(OmatsuriEvent.Columns.ID, id);
        cv.put(OmatsuriEvent.Columns.USER_ID, user_id);
        cv.put(OmatsuriEvent.Columns.CODE, code);
        cv.put(OmatsuriEvent.Columns.CREATED_AT, created_at);
        cv.put(OmatsuriEvent.Columns.DELAY_TIME, delay_time);
        cv.put(OmatsuriEvent.Columns.END_AT, end_at);
        cv.put(OmatsuriEvent.Columns.SCALE, scale);
        cv.put(OmatsuriEvent.Columns.SHARE_TYPE, share_type);
        cv.put(OmatsuriEvent.Columns.START_AT, start_at);
        cv.put(OmatsuriEvent.Columns.SUMMARY, summary);
        cv.put(OmatsuriEvent.Columns.TITLE, title);
        cv.put(OmatsuriEvent.Columns.UPDATED_AT, updated_at);
        return cv;
    }

}
