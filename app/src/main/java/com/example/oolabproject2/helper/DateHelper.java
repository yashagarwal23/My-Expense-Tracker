package com.example.oolabproject2.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.example.oolabproject2.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    public static List<Date> getListOfMonthsAvailableForUser(@NonNull Context context)
    {
        long initDate = Parameters.getInstance(context).getLong(ParameterKeys.INIT_DATE, System.currentTimeMillis());

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(initDate);

        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        Date today = new Date();

        List<Date> months = new ArrayList<>();

        while( cal.getTime().before(today) )
        {
            months.add(cal.getTime());
            cal.add(Calendar.MONTH, 1);
        }

        return months;
    }

    public static String getMonthTitle(@NonNull Context context, @NonNull Date date)
    {
        SimpleDateFormat format = new SimpleDateFormat(context.getResources().getString(R.string.monthly_report_month_title_format), Locale.getDefault());
        return format.format(date);
    }
}
