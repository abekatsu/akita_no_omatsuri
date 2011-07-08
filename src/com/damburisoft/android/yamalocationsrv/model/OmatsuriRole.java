package com.damburisoft.android.yamalocationsrv.model;

import org.json.JSONException;
import org.json.JSONObject;

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
    
    
}
