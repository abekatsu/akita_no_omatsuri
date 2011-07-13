package com.damburisoft.android.yamalocationsrv.model;

import java.util.List;

public interface OmatsuriRoleUpdateListener extends OmatsuriUpdateListenner {
    
    public void updateRoles(int event_id, List<OmatsuriRole> roles);

}
