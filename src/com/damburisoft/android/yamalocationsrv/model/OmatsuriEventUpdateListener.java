package com.damburisoft.android.yamalocationsrv.model;

import java.util.List;

public interface OmatsuriEventUpdateListener extends OmatsuriUpdateListenner {

    public void updateEvents(List<OmatsuriEvent> events);

}
