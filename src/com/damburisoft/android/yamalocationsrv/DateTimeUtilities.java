package com.damburisoft.android.yamalocationsrv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtilities {
    public static final String getDateAndTime() {
        Date date = new Date();
        return DateTimeUtilities.getDateAndTime(date);
    }
    
    public static final String getDateAndTime(long theDate) {
        Date date = new Date(theDate);
        return DateTimeUtilities.getDateAndTime(date);
    }
    
    public static final String getDateAndTime(Date date) {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(date);
    }
    
    public static final String getFilenameFromDateAndTime() {
        Date date = new Date();
        return DateTimeUtilities.getFilenameFromDateAndTime(date);
    }
    
    public static final String getFilenameFromDateAndTime(Date date) {
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        return dfm.format(date);
    }
}
