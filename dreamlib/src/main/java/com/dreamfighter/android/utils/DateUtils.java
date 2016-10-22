package com.dreamfighter.android.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by dreamfighter on 10/8/16.
 */
public class DateUtils extends GregorianCalendar{

    public static enum DiffType{
        SECOND,SECONDS,MINUTE,MINUTES,HOUR,HOURS,DAY,DAYS,DATE
    }

    public static class DiffDate{
        public DiffType diffType;
        public String diff;

        public DiffDate(String diff,DiffType diffType){
            this.diffType = diffType;
            this.diff = diff;
        }
    }

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

    public DiffDate getDiffDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        long diff = Math.abs(cal.getTimeInMillis() - getTimeInMillis());

        double hours =  (diff / (1000 * 60 * 60));
        double minutes = (diff / (1000 * 60));
        double seconds = (diff / 1000 );
        int diffDays =  (int) (hours / 24);
        int diffHours =  (int) (hours % 24);
        if(seconds==1){
            return new DiffDate("1", DiffType.SECOND);
        }else if(seconds<60){
            return new DiffDate("" + seconds, DiffType.SECOND);
        }else if(minutes==1){
            return new DiffDate("1", DiffType.MINUTE);
        }else if(minutes<60){
            return new DiffDate("" + minutes, DiffType.MINUTES);
        }else if(hours==1){
            return new DiffDate("1", DiffType.HOUR);
        }else if(hours<24){
            return new DiffDate("" + hours, DiffType.SECOND);
        }else if(diffDays==1){
            return new DiffDate("1", DiffType.DAY);
        }else if(diffDays<31){
            return new DiffDate("" + diffDays, DiffType.SECOND);
        }else{
            return new DiffDate("" , DiffType.DATE);
        }
    }
}
