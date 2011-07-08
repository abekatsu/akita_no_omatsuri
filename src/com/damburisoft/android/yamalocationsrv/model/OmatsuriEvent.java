package com.damburisoft.android.yamalocationsrv.model;

import org.json.JSONException;
import org.json.JSONObject;

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

}
