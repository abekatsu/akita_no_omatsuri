package com.damburisoft.android.yamalocationsrv;

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
        SimpleDateFormat dfm = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); 
        return dfm.format(date);
    }
    
    public static final String getFilenameFromDateAndTime() {
        Date date = new Date();
        return DateTimeUtilities.getFilenameFromDateAndTime(date);
    }
    
    public static final String getFilenameFromDateAndTime(Date date) {
        SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss"); 
        return dfm.format(date);
    }
}
