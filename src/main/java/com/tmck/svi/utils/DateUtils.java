package com.tmck.svi.utils;

import java.util.Calendar;
import java.util.Date;


public final class DateUtils {

    public static final int DAYS_PER_WEEK = 7;
    public static final int WEEKS_PER_QUARTER = 13;
    public static final int DAYS_PER_QUARTER = DAYS_PER_WEEK * WEEKS_PER_QUARTER;
    public static final int MONTHS_PER_QUARTER = 3;

    public static final long ONE_SECOND = 1000; // 1000 milliseconds
    public static final long ONE_MINUTE = ONE_SECOND * 60;
    public static final long FIVE_MINUTES = ONE_MINUTE * 5;
    public static final long FIFTEEN_MINUTES = FIVE_MINUTES * 3;
    public static final long HOURLY = FIFTEEN_MINUTES * 4;
    public static final long DAILY = HOURLY * 24;
    public static final long WEEKLY = DAILY * 7;
    public static final long QUARTERLY = WEEKLY * WEEKS_PER_QUARTER;
    public static final long YEARLY = QUARTERLY * 4;
    public static final long MONTHLY = DAILY * 30;
    public static final int MONTHS_PER_YEAR = 12;
    public static final int MAX_DAYS_PER_MONTH = 31;
    public static final int JAVA_YEAR_OFFSET = 1900;

    public static final int TRADING_DAYS_PER_YEAR = 252;

    /*
     * Known Leap Seconds:
     *
     * 30 June 1972 31 December 1972 31 December 1973 31 December 1974 31 December
     * 1975 31 December 1976 31 December 1977 31 December 1978 31 December 1979 30
     * June 1981 30 June 1982 30 June 1983 30 June 1985 31 December 1987 31 December
     * 1989 31 December 1990 30 June 1992 30 June 1993 30 June 1994 31 December 1995
     * 30 June 1997 31 December 1998
     *
     */
    public static final double AVERAGE_NUM_DAYS_PER_MONTH = 30.4375;

    public static final int GREGORIAN_CALENDAR_BEGIN = 1583; // Before this the julian calendar must be used.

    private DateUtils() {
    }


    /**
     * NOTE: This is intentionally backward so that more recent dates are sorted to
     * lower index values in a List.
     */
    public static int dateCompareTo(Date d1, Date d2) {

        assert d1 != null : "null d1 passed to dateCompareTo";
        assert d2 != null : "null d2 passed to dateCompareTo";

        long time1 = d1.getTime();
        long time2 = d2.getTime();
        return NumberUtils.doubleCompareTo(time2, time1);

    }

    /**
     * @return the calendar days between two dates. Always a positive number as the
     * two dates are swapped if needed.
     */
    // Keep
    @SuppressWarnings("deprecation")
    public static int getDaysBetween(Date start, Date end) {

        // swap if start is after end.
        if (start.after(end)) {

            // DO NOT REMOVE THIS SWAP CODE.
            Date tmp = start;
            start = end;
            end = tmp;

        }

        // for performance we don't use the Calendar code even though it is the proper
        // way calculate dates.
        int startYear1 = start.getYear() + 1900;
        int endYear1 = end.getYear() + 1900;
        int startDayOfYear1 = dayOfYear(startYear1, start.getMonth(), start.getDate());
        int endDayOfYear1 = dayOfYear(endYear1, end.getMonth(), end.getDate());

        int daysElapsed1 = endDayOfYear1 - startDayOfYear1;
        int yearsElapsed1 = endYear1 - startYear1;
        return (yearsElapsed1 * 365) + daysElapsed1;
    }

    /**
     * 0<month<=12
     */
    private static int dayOfYear(int year, int month, int day) {
        short[] daysInMonth = new short[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        short[] daysInMonthLeap = new short[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);

        int yearDay = 0;
        for (int i = 0; i < month; i++) {
            if (isLeapYear) {
                yearDay += daysInMonthLeap[i];
            } else {
                yearDay += daysInMonth[i];
            }
        }

        yearDay += day;
        return yearDay;
    }

    public static Date addCalendarDays(Date date, int daysToAdvance) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToAdvance);

        return cal.getTime();

    }

}
