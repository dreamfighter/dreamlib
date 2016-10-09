package com.dreamfighter.android.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by dreamfighter on 10/8/16.
 */
public class DateUtils extends GregorianCalendar{

    public static synchronized DateUtils getInstance() {
        return new DateUtils();
    }

    public int calculateDayDiff(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        long diff = cal.getTimeInMillis() - getTimeInMillis();

        double hours =  (diff / (1000 * 60 * 60));
        int diffDays =  (int) (hours / 24);
        //int diffHours =  (int) (hours % 24);
        return diffDays;
    }

    public int calculateHourDiff(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        long diff = cal.getTimeInMillis() - getTimeInMillis();

        double hours =  (diff / (1000 * 60 * 60));
        //int diffDays =  (int) (hours / 24);
        int diffHours =  (int) (hours % 24);
        return diffHours;
    }
}
