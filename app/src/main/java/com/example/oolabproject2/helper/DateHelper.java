package com.example.oolabproject2.helper;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateHelper {
    public static Date cleanDate(@NonNull Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        return cal.getTime();
    }
    public static Date cleanGMTDate(@NonNull Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        return cal.getTime();
    }
    public static Pair<Long, Long> getTimestampRangeForDay(@NonNull Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        cal.add(Calendar.HOUR_OF_DAY, -11);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, 23);
        long end = cal.getTimeInMillis();

        return new Pair<>(start, end);
    }
}
