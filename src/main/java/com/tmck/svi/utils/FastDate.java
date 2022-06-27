package com.tmck.svi.utils;

import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Speeds up the java date class.
 * Joda-Time and newer Java Date code was not generally used when this class was created.
 *
 * @author tim
 */
public class FastDate extends Date {

    public static final int JANUARY = GregorianCalendar.JANUARY;
    public static final int FEBRUARY = GregorianCalendar.FEBRUARY;
    public static final int MARCH = GregorianCalendar.MARCH;
    public static final int APRIL = GregorianCalendar.APRIL;
    public static final int MAY = GregorianCalendar.MAY;
    public static final int JUNE = GregorianCalendar.JUNE;
    public static final int JULY = GregorianCalendar.JULY;
    public static final int AUGUST = GregorianCalendar.AUGUST;
    public static final int SEPTEMBER = GregorianCalendar.SEPTEMBER;
    public static final int OCTOBER = GregorianCalendar.OCTOBER;
    public static final int NOVEMBER = GregorianCalendar.NOVEMBER;
    public static final int DECEMBER = GregorianCalendar.DECEMBER;
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3834869178779054390L;
    private SoftReference<GregorianCalendar> cal = null;


    public FastDate() {
        super();
    }

    public FastDate(long date) {
        super(date);
    }

    public FastDate(Date date) {
        this(date.getTime());
    }

    public FastDate(FastDate date) {
        this(date.getJavaDate());
    }

    /**
     * @param year  if greater than 1584 it is assumed to be in yyyy form rather than Java form.
     * @param month - specified in the range 0-11
     * @param day   - specified in the range 1-31
     */
    @SuppressWarnings("deprecation")
    public FastDate(int year, int month, int day) {
        super(year < DateUtils.GREGORIAN_CALENDAR_BEGIN ? year : year - DateUtils.JAVA_YEAR_OFFSET, month, day);
    }

    public static boolean isLeapSecondCompliant() {
        FastDate june30 = new FastDate(97, 5, 30);
        FastDate july1 = new FastDate(97, 6, 1);
        long juneTime = june30.getTime();
        long newTime = juneTime + DateUtils.DAILY + DateUtils.ONE_SECOND;    // add a day plus one leap second.
        Date testDate = new Date(newTime);
        Date verifyDate = new Date(juneTime + DateUtils.DAILY);
        boolean leapSecondCompliant = testDate.equals(july1);
        return !leapSecondCompliant && !verifyDate.equals(july1);
    }

    public static boolean isDaylightSavingsCompliant() {
        FastDate april3 = new FastDate(104, 3, 3);
        FastDate april4 = new FastDate(104, 3, 4);
        long daylightTime = april3.getTime();
        long newTime = daylightTime + DateUtils.DAILY + DateUtils.HOURLY;
        Date testDate = new Date(newTime);
        Date verifyDate = new Date(daylightTime + DateUtils.DAILY);
        boolean daylightSavingsCompliant = testDate.equals(april4);
        return !daylightSavingsCompliant && !verifyDate.equals(april4);
    }

    /**
     * @return a Java Date. Used for compatibility with other Date superclasses such as BigDate.
     */
    public Date getJavaDate() {
        return this;
    }

    @Override
    public Object clone() {
        // modify if fields are added/subtracted.
        return super.clone();
    }

    public FastDate copy() {
        return (FastDate) clone();
    }

    public int getWeekOfYear() {
        return convertToCalendar().get(Calendar.WEEK_OF_YEAR);
    }

    public int getCalendarYear() {
        return convertToCalendar().get(Calendar.YEAR);
    }

    public int getCalendarMonth() {
        return convertToCalendar().get(Calendar.MONTH);
    }

    public int getDayOfYear() {
        return convertToCalendar().get(Calendar.DAY_OF_YEAR);
    }

    @SuppressWarnings("deprecation")
    public boolean isLeapYear() {
        return convertToCalendar().isLeapYear(getYear());
    }

    public synchronized GregorianCalendar convertToCalendar() {
        GregorianCalendar greg = null;
        if (cal != null) {
            greg = cal.get();
        }
        if (greg == null) {
            greg = new GregorianCalendar();
            greg.setTime(getJavaDate());
            cal = new SoftReference<GregorianCalendar>(greg);
        }
        return greg;
    }

    @SuppressWarnings("deprecation")
    public int getQuarter() {
        int month = getMonth();
        if (month < 4) {
            return 1;
        }
        if (month < 7) {
            return 2;
        }
        if (month < 10) {
            return 3;
        }
        return 4;
    }


}
