package com.tmck.svi.utils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.tmck.svi.valueobjs.TimeInterval;


public class DateUtils {

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
	public static final long MONTHLY = DAILY * 30;
	public static final long QUARTERLY = WEEKLY * WEEKS_PER_QUARTER;
	public static final long YEARLY = QUARTERLY * 4;

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

	public static final int SUNDAY = GregorianCalendar.SUNDAY - 1;
	public static final int MONDAY = GregorianCalendar.MONDAY - 1;
	public static final int TUESDAY = GregorianCalendar.TUESDAY - 1;
	public static final int WEDNESDAY = GregorianCalendar.WEDNESDAY - 1;
	public static final int THURSDAY = GregorianCalendar.THURSDAY - 1;
	public static final int FRIDAY = GregorianCalendar.FRIDAY - 1;
	public static final int SATURDAY = GregorianCalendar.SATURDAY - 1;

	public static final int JANUARY = FastDate.JANUARY + 1;
	public static final int FEBRUARY = FastDate.FEBRUARY + 1;
	public static final int MARCH = FastDate.MARCH + 1;
	public static final int APRIL = FastDate.APRIL + 1;
	public static final int MAY = FastDate.MAY + 1;
	public static final int JUNE = FastDate.JUNE + 1;
	public static final int JULY = FastDate.JULY + 1;
	public static final int AUGUST = FastDate.AUGUST + 1;
	public static final int SEPTEMBER = FastDate.SEPTEMBER + 1;
	public static final int OCTOBER = FastDate.OCTOBER + 1;
	public static final int NOVEMBER = FastDate.NOVEMBER + 1;
	public static final int DECEMBER = FastDate.DECEMBER + 1;

	public static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

	public static int getDaysInMonth(Date d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/** @return number of minutes that have passed since the day started. */
	@SuppressWarnings("deprecation")
	public static int getCurrentMinutesIntoDay() {
		Date d = new Date();
		int hours = d.getHours();
		int minutes = d.getMinutes();
		return (hours * 60) + minutes;
	}

	/** if saturday or sunday move to Friday. */
	@SuppressWarnings({ "deprecation", "unused" })
	public static Date getLastCompletedWorkDay(Date d) {

		int dow = getDayOfWeek(d);

		int month = d.getMonth();
		int day = d.getDate();
		int year = d.getYear();

		Date newDate = (Date) d.clone();

		if (dow == Calendar.SATURDAY) {
			newDate.setDate(day - 1);
		} else if (dow == Calendar.SUNDAY) {
			newDate.setDate(day - 2);
		}

		return newDate;
	}

	/** if saturday or sunday move to monday. */
	@SuppressWarnings("deprecation")
	public static Date getNextWorkDayIfTodayIsSatOrSun(Date d) {
		
		int dow = getDayOfWeek(d);
		int day = d.getDate();

		Date newDate = (Date) d.clone();

		if (dow == Calendar.SATURDAY) {
			newDate.setDate(day + 2);
		} else if (dow == Calendar.SUNDAY) {
			newDate.setDate(day + 1);
		}

		return newDate;
	}

	/** if saturday or sunday move to Friday. */
	@SuppressWarnings("deprecation")
	public static Date getPriorWorkDayIfTodayIsSatOrSun(Date d) {
		
		int dow = getDayOfWeek(d);
		int day = d.getDate();

		Date newDate = (Date) d.clone();

		if (dow == Calendar.SATURDAY) {
			newDate.setDate(day - 1);
		} else if (dow == Calendar.SUNDAY) {
			newDate.setDate(day - 2);
		}

		return newDate;
	}

	/**
	 * if saturday or sunday move to tuesday. if friday move to monday. if m-th
	 * advance 2 days.
	 * 
	 */
	@SuppressWarnings("deprecation")
	public static Date getNextNextWorkDay(Date d) {
		
		int dow = getDayOfWeek(d);
		int day = d.getDate();

		Date newDate = (Date) d.clone();

		if (dow == Calendar.SATURDAY || dow == Calendar.FRIDAY) {
			newDate.setDate(day + 3);
		} else if (dow == Calendar.SUNDAY) {
			newDate.setDate(day + 2);
		} else {
			newDate.setDate(day + 1);
		}

		return newDate;
	}

	/**
	 * if saturday or sunday move to monday. if friday move to monday. if m-th
	 * advance 1 days.
	 * 
	 * NOTE: Does not copy the time by design!
	 * 
	 */
	@SuppressWarnings({ "deprecation" })
	public static Date getNextWorkDay(Date d) {
		int dow = getDayOfWeek(d);

		int day = d.getDate();

		Date newDate = (Date) d.clone();

		if (dow == Calendar.FRIDAY) {
			newDate.setDate(day + 3);
		} else if (dow == Calendar.SATURDAY) {
			newDate.setDate(day + 2);
		} else {
			newDate.setDate(day + 1);
		}

		return newDate;
	}

	final static int[] DAYS_OF_WEEK = { Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
			Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY };

	@SuppressWarnings("deprecation")
	public static Date getPriorWorkDay(Date d) {
		int dow = getDayOfWeek(d);

		int day = d.getDate();

		Date newDate = (Date) d.clone();

		if (dow == Calendar.MONDAY) {
			newDate.setDate(day - 3);
		} else if (dow == Calendar.SUNDAY) {
			newDate.setDate(day - 2);
		} else {
			newDate.setDate(day - 1);
		}

		return newDate;
	}

	@SuppressWarnings("deprecation")
	public static int getDayOfWeek(Date d) {
		return getDayOfWeek(d.getYear() + 1900, d.getMonth() + 1, d.getDate()) + 1;
	}

	/**
	 * Calculate the day of the week using Zeller's Congruence.
	 * 
	 * @param yy is the year as four digits (e.g. 1987)
	 * @param mm is the month from 1 to 12
	 * @param dd is the day from 1 to 31
	 * @see http://www.bsdg.org/SWAG/DATETIME/0027.PAS.html
	 * @return the day of the week as a number between 0 and 6 with 0 being Sunday
	 *         and 6 being Saturday.
	 */
	public static int getDayOfWeek(int yy, int mm, int dd) {

		if (yy < 200) {
			yy += 1900;
		}

		// First test For error conditions on input values:
		if (yy < 0) {
			throw new IllegalArgumentException("bad year: " + yy);
		}

		if ((mm < 1) || (mm > 12)) {
			throw new IllegalArgumentException("bad month: " + mm);
		}

		if ((dd < 1) || (dd > 31)) {
			throw new IllegalArgumentException("bad day of month: " + dd);
		}

		// Do the Zeller's Congruence calculation as Zeller himself
		// described it in "Acta Mathematica" #7, Stockhold, 1887.

		// First we separate out the year and the century figures:

		int century = yy / 100;
		yy = yy % 100;

		// Next we adjust the month such that March remains month #3,
		// but that January and February are months #13 and #14,
		// *but of the previous year*:

		if (mm < 3) {
			mm += 12;
			if (yy > 0) {
				yy--; // The year before 2000 is
			} else { // 1999, not 20-1...
				yy = 99;
				century--;
			}
		}

		// Here's Zeller's seminal black magic:
		int dow = dd; // Start With the day of month
		dow += (((mm + 1) * 26) / 10); // Calc the increment
		dow += yy; // Add in the year
		dow += (yy / 4); // Correct For leap years
		dow += (century / 4); // Correct For century years
		dow = dow - century - century; // DON'T KNOW WHY HE DID THIS!
		// ***********************KLUDGE ALERT!***************************
		while (dow < 0) { // turn negative values into positive values
			dow += 7;
		}

		dow = dow % 7; // Divide by 7 and keep the remainder
		// ***********************KLUDGE ALERT!***************************
		// Here we "wrap" Saturday around to be the last day:
		if (dow == 0) {
			dow = 7;
		}

		// Zeller kept the Sunday = 1 origin; computer weenies prefer to
		// start everything With 0, so here's a 20th century kludge:
		dow--;

		return dow; // Return the end product!
	}

	/**
	 * Determine the date that Easter falls on given a year.
	 * 
	 * @see http://aa.usno.navy.mil/faq/docs/easter.html
	 * @see Knuth Vol. 1, p. 155
	 * @see http://www.cs.caltech.edu/courses/cs11/material/c/mike/lab2/lab2.html
	 * @param yy is the year to determin easter for.
	 * @return the date of easter.
	 */
	@SuppressWarnings("deprecation")
	public static Date getEasterDay(int yy) {

		int y = yy;

		if (y < 200) {
			y += 1900;
		} else {
			yy -= 1900;
		}

		int c = y / 100;
		int n = y - 19 * (y / 19);
		int k = (c - 17) / 25;
		int i = c - c / 4 - (c - k) / 3 + 19 * n + 15;
		i = i - 30 * (i / 30);
		i = i - (i / 28) * (1 - (i / 28) * (29 / (i + 1)) * ((21 - n) / 11));
		int j = y + y / 4 + i + 2 - c + c / 4;
		j = j - 7 * (j / 7);
		int l = i - j;
		int mm = 3 + (l + 40) / 44;
		int dd = l + 28 - 31 * (mm / 4);

		// calendar stuff.
//		int century = (yy / 100) + 1;  
//		int skippedLeapYears = (3*century / 4) - 12; // skip every 100 years
//		int sunday = (5*yy / 4) - skippedLeapYears - 10; // March (-D mod 7) is a Sunday.
//
//		// moon stuff.
//		int goldenNumber = (yy % 19) + 1;   // "golden year" in the 19-year Metonic cycle.
//		int Z = ((8*century + 5) / 25) - 5;          // Z is a correction factor for the moon's orbit.
//		int epact = (11*goldenNumber + 20 + Z - skippedLeapYears) % 30;	// age of moon at beginning of year.
//		if((epact == 25 && goldenNumber > 11) || (epact == 24)) {
//			epact++;
//		}
//		int dd = 44 - epact;  // March N is a "calendar full moon".
//		if(dd < 21) { dd += 30; }
//		
//		dd = dd + 7 - ((sunday + dd) % 7);  // N is a Sunday after full moon.
//		int mm = 4; // april.
//		if(dd > 31) {
//			dd -= 31;
//		} else {
//			mm = 2;
//		}	

		Date date = new Date(yy, mm - 1, dd);
		return date;
	}

	public static int getDaysInMonth(int yy, int mm) {

		switch (mm) {
		case 1: {
			return 31;
		} // subtract 30 days for jan.
		case 2: { // subtract 28 or 29 days for feb
			int dd = 28;
			if (isLeapYear(yy)) {
				dd++;
			}
			return dd;
		}
		case 3: {
			return 31;
		} // add 30 days for march
		case 4: {
			return 30;
		} // add 30 days for april
		case 5: {
			return 31;
		} // add 30 days for may
		case 6: {
			return 30;
		} // add 30 days for june
		case 7: {
			return 31;
		} // add 30 days for july
		case 8: {
			return 31;
		} // add 30 days for august
		case 9: {
			return 30;
		} // add 30 days for sept.
		case 10: {
			return 31;
		} // add 30 days for oct
		case 11: {
			return 30;
		} // add 30 days for nov
		case 12: {
			return 31;
		} // add 30 days for nov

		}

		throw new IllegalArgumentException("not a valid month: " + mm);

	}

//	public static int getDaysSinceJanuaryFirst(int yy, int mm) {
//		int dd = 0;
//		for(int i = 2; i <= mm; i++) {
//			dd += getDaysInMonth(yy, i);
//		}
//		return dd;
//	}

	/**
	 * Determine the first Sunday of a month.
	 * 
	 * @param yy the year to determine the first sunday for.
	 * @param mm the month to determine the first sunday for. 1 is January.
	 * @return the day that the first Sunday falls on.
	 */
	public static int getFirstSunday(int yy, int mm) {
		return getFirstDateForDayOfWeek(yy, mm, 0);
	}

	/**
	 * Determine the last Sunday of a month.
	 * 
	 * @param yy the year to determine the last sunday for.
	 * @param mm the month to determine the last sunday for. 1 is January.
	 * @return the day that the last Sunday falls on.
	 */
	public static int getLastSunday(int yy, int mm) {
		return getLastDateForDayOfWeek(yy, mm, 0);
	}

	/**
	 * 
	 * @param yy  the year as four digits.
	 * @param mm  month of year in the range 1-12.
	 * @param dow 0-6 with sunday as 0.
	 * @return the first date in the month for the specified day of week.
	 */
	public static int getFirstDateForDayOfWeek(int yy, int mm, int dow) {

		int firstDow = getDayOfWeek(yy, mm, 1);
		int date = (firstDow + dow) % 7;
//		int dateOffset = dow - firstDow;
//		dateOffset = (dateOffset < 0) ? dateOffset + 7 : dateOffset;	// bring into positive territory.
//		int date = (firstDow + dateOffset) % 7;

		return date;

	}

	/**
	 * Determine the last date in a month for a given DOW.
	 * 
	 * @param yy the year (four digits) to determine the last dow for.
	 * @param mm the month to determine the last dow for. 1 is January.
	 * @return the first date in the month for the specified day of week.
	 */
	public static int getLastDateForDayOfWeek(int yy, int mm, int dow) {
		int first = getFirstDateForDayOfWeek(yy, mm, dow);
		int totalDays = getDaysInMonth(yy, mm);
		int last = first + 28;
		while (last > totalDays) {
			last -= 7;
		}
		return last;
	}

	/**
	 * Is the year a leap year.
	 * 
	 * @see http://www.dickinson.edu/~braught/courses/cs132f01/classes/code/LeapYear.src.html
	 * @param yy the year to calculate.
	 * @return true if it is a leap year.
	 */
	public static boolean isLeapYear(int yy) {

		// Check for a 2 digit year and adjust the year
		// using a windowing technique. If the date has
		// more than 2 digits we'll assume it is a complete
		// year.
		if (yy < 100) {
			// If the year is greater than 40 assume it
			// is from 1900's. If the year is less than
			// 40 assume it is from 2000's.
			if (yy > 40) {
				yy = yy + 1900;
			} else {
				yy = yy + 2000;
			}
		}

		// Is theYear Divisible by 4?
		if (yy % 4 == 0) {

			// Is theYear Divisible by 4 but not 100?
			if (yy % 100 != 0) {
				return true;
			} else if (yy % 400 == 0) {
				// Is theYear Divisible by 4 and 100 and 400?
				return true;
			} else {
				// It is Divisible by 4 and 100 but not 400!
				return false;
			}
		} else {
			// It is not divisible by 4.
			return false;
		}
	}

	/** handles DST from 1920 forwards. */
	private static boolean isAfterDSTEnd(int yy, int mm, int dd) {

		if (yy < 1920) {
			throw new IllegalStateException("cannot handle DST before 1920");
		}

		// DST did not end during these years.
		if (yy == 1942 || yy == 1944 || yy == 1943) {
			return false;
		}

		// DST ended Sept. 10, 1945
		if (yy == 1945) {
			if (mm > 9)
				return true;
			if (mm == 9 && dd >= 10)
				return true;
			return false;
		}

		// other than WWII there was no DST from 1920 to 1965.
		if (yy < 1966) {
			return false;
		}

		int dow = DateUtils.getDayOfWeek(yy, mm, dd);
		dow = (dow == 0) ? 7 : 0;
		if (yy > 1966 && mm > 10 && dd - dow > 1) {
			return true;
		}
		return false;
	}

	/** handles DST from 1920 forwards. */
	public static boolean isAfterDSTStart(int yy, int mm, int dd) {

		if (yy < 1920) {
			throw new IllegalStateException("cannot handle DST before 1920");
		}

		// There was no DST from 1920 to 1942.
		if (yy < 1942) {
			return false;
		}

		// DST started Feb. 9, 1942
		if (yy == 1942) {
			if (mm > 2)
				return true;
			if (mm == 2 && dd > 9)
				return true;
			return false;
		}

		// DST continued during these years.
		if (yy == 1943 || yy == 1944 || yy == 1945) {
			return true;
		}

		// There was no DST from 1946 to 1965.
		if (yy < 1966) {
			return false;
		}

		// handle Jan. 6, 1974 - energy crisis
		if (yy == 1974) {
			if (mm > 1) {
				return true;
			}
			if (mm == 1 && dd > 6) {
				return true;
			}
			return false;
		}

		// handle Feb. 2, 1975 - energy crisis
		if (yy == 1975) {
			if (mm > 2) {
				return true;
			}
			if (mm == 2 && dd > 2) {
				return true;
			}
			return false;
		}

		if (yy > 1966 && mm < 4) {
			return false;
		} // before April
		if (yy > 1966 && mm > 4) {
			return true;
		} // after April
		if (yy <= 1986 && dd < 23) {
			return false;
		} // in the first 3 weeks of the month
		if (yy > 1986 && dd > 7) {
			return true;
		} // in the last 3 weeks of the month.

		// DST change is on 1st monday after 1986.
		int firstSunday = DateUtils.getFirstSunday(yy, mm);
		if (yy > 1986 && dd >= firstSunday) {
			return true;
		}

		// DST change is on last monday prior to 1986.
		int lastSunday = DateUtils.getLastSunday(yy, mm);
		if (yy <= 1986 && dd >= lastSunday) {
			return true;
		}

		return false;
	}

	/**
	 * The idea of daylight saving time was first put into practice by the German
	 * government during the First World War between April 30, 1916 and October 1,
	 * 1916. Shortly afterwards, the United Kingdom followed suit, first adopting
	 * DST between the 21st of May, 1916 and the 1st of October, 1916.
	 * 
	 * Then on March 19, 1918 the United States Congress established several time
	 * zones (which were already in use by railroads since 1883) and made daylight
	 * saving time official (which went into effect on March 31) for the remainder
	 * of World War I. It was observed for seven months in 1918 and 1919. The law,
	 * however, proved so unpopular (mostly because people rose earlier and went to
	 * bed earlier than in modern times) that the law was later repealed on August
	 * 20 (or 29?), 1919. However, NYC and Philadelphia continued to observe DST.
	 * 
	 * Daylight saving time was reinstated in the United States on February 9, 1942,
	 * again as a wartime measure to conserve resources, this time in order to fight
	 * World War II. This remained in effect until the war began winding down and
	 * the requirement was removed on September 30, 1945. From 1945 to 1966, there
	 * was no federal law about daylight saving time. States and localities were
	 * free to observe daylight saving time or not.
	 * 
	 * The Uniform Time Act of 1966 mandated that daylight saving time begin
	 * nationwide on the last Sunday of April and end on the last Sunday of October.
	 * Any state that wanted to be exempt from daylight saving time could do so by
	 * passing a state law, provided that it exempts the entire state. The law was
	 * amended in 1972 to permit states that straddle a time zone boundary to exempt
	 * the entire area of the state lying in one time zone. In response to the 1973
	 * energy crisis, daylight saving was begun earlier in both 1974 and 1975,
	 * commencing on the first Sunday in January in the former year and the last
	 * Sunday in February in the latter. The law was amended again in 1986 to begin
	 * daylight saving time on the first Sunday in April, to take effect the
	 * following year.
	 * 
	 * Under the Uniform Time Act of 1966 the change over occurs at 2 am.
	 */
	public static boolean isDaylightSavingsDay(int yy, int mm, int dd) {
		if (DateUtils.isAfterDSTStart(yy, mm, dd) && !DateUtils.isAfterDSTEnd(yy, mm, dd)) {
			return true;
		}
		return false;
	}

	public static boolean isGreaterThan(Date d1, Date d2) {
		return dateCompareTo(d1, d2) < -1;
	}

	public static boolean isEqual(Date d1, Date d2) {
		return dateCompareTo(d1, d2) == 0;
	}

	@SuppressWarnings("deprecation")
	public static boolean matchDayMonthAndYear(Date d1, Date d2) {

		// Added for performance!
		if (d1 == d2) {
			return true;
		}

		return d1.getDate() == d2.getDate() && d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear();
	}

	public static boolean matchHoursAndMinutes(Date d1, Date d2) {

		// Added for performance!
		if (d1 == d2) {
			return true;
		}

		int minutes1 = getMinutesSinceStartOfDay(d1);
		int minutes2 = getMinutesSinceStartOfDay(d2);

		return minutes1 == minutes2;

	}

	/**
	 * Is the time component of the first parameter (hours and minutes only) after
	 * the time component of the second parameter (hours and minutes only)?
	 */
	public static boolean afterHoursAndMinutes(Date reference, Date d2) {

		// Added for performance!
		if (reference == d2) {
			return true;
		}

		int minutes1 = getMinutesSinceStartOfDay(reference);
		int minutes2 = getMinutesSinceStartOfDay(d2);

		return minutes1 > minutes2;

	}

	@SuppressWarnings("deprecation")
	public static int getMinutesSinceStartOfDay(Date date) {
		int hours = date.getHours();
		int minutes = date.getMinutes();
		return (hours * 60) + minutes;
	}

	@SuppressWarnings("deprecation")
	public static Date parseFullDateString(String fullDateStr) {

		StringTokenizer tok = new StringTokenizer(fullDateStr, " \t,");

		String monthStr = tok.nextToken();
		String dayStr = tok.nextToken();
		String yearStr = tok.nextToken();

		int month = monthFromMonthString(monthStr);
		int day = Integer.parseInt(dayStr);
		int year = Integer.parseInt(yearStr);

		return new Date(year - 1900, month - 1, day);

	}

	public static int monthFromMonthString(String monthStr) {

		monthStr = monthStr.toLowerCase();
		if (monthStr.startsWith("-")) {
			monthStr = monthStr.substring(1, 4);
		}
		char m2 = monthStr.charAt(1);
		char m3 = monthStr.charAt(2);

		switch (m3) {
		case 'n':
			return (m2 == 'u') ? JUNE : JANUARY;
		case 'b':
			return FEBRUARY;
		case 'r':
			return (m2 == 'a') ? MARCH : APRIL;
		case 'y':
			return MAY;
		case 'l':
			return JULY;
		case 'g':
			return AUGUST;
		case 'p':
			return SEPTEMBER;
		case 't':
			return OCTOBER;
		case 'v':
			return NOVEMBER;
		case 'c':
			return DECEMBER;
		}
		throw new IllegalArgumentException("bad month string: " + monthStr);
	}

	public static long[] convertToLongArray(FastDate[] d) {

		long[] vals = new long[d.length];
		for (int i = 0; i < d.length; i++) {
			vals[i] = d[i].getTime();
		}

		return vals;
	}

	public static double[] convertToDoubleArray(FastDate[] d) {

		long first = Long.MAX_VALUE;
		for (int i = 0; i < d.length; i++) {
			long time = d[i].getTime();
			if (time < first) {
				first = time;
			}
		}

		double[] vals = new double[d.length];
		for (int i = 0; i < d.length; i++) {
			long current = d[i].getTime();
			vals[i] = (current - first);
		}

		return vals;
	}

	/**
	 * NOTE: This is intentionally backward so that more recent dates are sorted to
	 * lower index values in a List.
	 */
	public static int dateCompareTo(Date d1, Date d2) {

		assert d1 != null : "null d1 passed to dateCompareTo";
		assert d2 != null : "null d2 passed to dateCompareTo";

		if (d2 == null) {
			System.out.println();
		}

		long time1 = d1.getTime();
		long time2 = d2.getTime();
//		int val1 = d2.compareTo(d1);
		int val2 = NumberUtils.doubleCompareTo(time2, time1);
		return val2;

//		return d2.compareTo(d1);
	}

	@SuppressWarnings("deprecation")
	public static boolean isBeforeMarketOpen(Date d) {
		int hours = d.getHours();
		if (hours < 9)
			return true;
		if (hours == 9 && d.getMinutes() < 30) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static FastDate nextQuarter(Date d) {
		int dd = d.getDate();
		int mm = d.getMonth();
		int yy = d.getYear();
		mm = mm + 3;
		if (mm > 11) {
			mm = mm - 12;
			yy++;
		}
		return new FastDate(yy, mm, dd);
	}

	@SuppressWarnings("deprecation")
	public static FastDate nextMonth(Date d) {
		int dd = d.getDate();
		int mm = d.getMonth();
		int yy = d.getYear();
		mm++;
		if (mm > 11) {
			mm = 0;
			yy++;
		}
		return new FastDate(yy, mm, dd);
	}

	@SuppressWarnings("deprecation")
	public static FastDate nextYear(Date d) {

		int dd = d.getDate();
		int mm = d.getMonth();
		int yy = d.getYear();

		yy++;

		return new FastDate(yy, mm, dd);
	}

	@SuppressWarnings("deprecation")
	public static FastDate priorMonth(Date d) {

		int dd = d.getDate();
		int mm = d.getMonth();
		int yy = d.getYear();
		mm--;
		if (mm < 0) {
			mm = 11;
			yy--;
		}
		return new FastDate(yy, mm, dd);
	}

	/**
	 * @param d
	 * @param interval
	 * @return
	 */
	public static FastDate priorInterval(FastDate d, TimeInterval interval) {
		if (interval.equals(TimeInterval.MONTHLY_INTERVAL)) {
			return priorMonth(d);
		}
		if (interval.equals(TimeInterval.DAILY_INTERVAL)) {
			return (FastDate) getPriorWorkDay(d);
		}
		throw new IllegalArgumentException("interval not currently supported: " + interval);
	}

	public static List<Date> getDatesBetween(Date start, Date end) {
		return getDatesBetween(start, end, Calendar.DATE, 1);
	}

	public static List<Date> getWeeklyDatesBetween(Date start, Date end) {
		return getDatesBetween(start, end, Calendar.DATE, 7);
	}

	public static List<Date> getMonthlyDatesBetween(Date start, Date end) {
		return getDatesBetween(start, end, Calendar.MONTH, 1);
	}

	public static List<Date> getQuarterlyDatesBetween(Date start, Date end) {
		return getDatesBetween(start, end, Calendar.MONTH, 3);
	}

	public static List<Date> getYearlyDatesBetween(Date start, Date end) {
		return getDatesBetween(start, end, Calendar.YEAR, 1);
	}

	public static List<Date> getDatesBetween(Date start, Date end, int calendarInterval, int numPeriods) {
		List<Date> dates = new ArrayList<Date>();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(start);
		while (calendar.getTime().before(end)) {
			Date result = calendar.getTime();
			dates.add(result);
			calendar.add(calendarInterval, numPeriods);
		}
		return dates;
	}

	/**
	 * @return the calendar days between two dates. Always a positive number as the
	 *         two dates are swapped if needed.
	 */
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
		int daysBetween1 = (yearsElapsed1 * 365) + daysElapsed1;

		return daysBetween1;
	}

	/** 0<month<=12 */
	private static int dayOfYear(int year, int month, int day) {
		short[] daysInMonth = new short[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		short[] daysInMonthLeap = new short[] { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

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

	public static int getWorkingDaysBetweenFast(Date start, Date end) {

		int calendarDays = getDaysBetween(start, end);
		int rem = calendarDays % 7;
		int days = calendarDays / 7;
		return days = (days * 5) + rem;
	}

	public static int getWorkingDaysBetween(Date start, Date end) {

		Date current = start;
		int count = 0;
		while (!matchDayMonthAndYear(current, end)) {
			count++;
			current = getNextCalendarDate(current);
			current = getNextWorkDayIfTodayIsSatOrSun(current);
		}

		return count;

	}


	@SuppressWarnings("deprecation")
	public static Date getNextCalendarDate(Date date) {
		return new Date(date.getYear(), date.getMonth(), date.getDate() + 1);
	}


	public static boolean isTomorrow(Date date) {
		Date tomorrow = getTomorrow();
		return isMatchingDates(tomorrow, date);
	}

	public static Date getTomorrow() {
		return getTomorrow(new Date());
	}

	public static Date getTomorrow(Date date) {
		return addCalendarDays(date, 1);
	}

	public static Date getYesterday() {
		return getYesterday(new Date());
	}

	public static Date getYesterday(Date now) {
		return addCalendarDays(now, -1);
	}

	public static boolean isToday(Date date) {
		return isMatchingDates(new Date(), date);
	}

	@SuppressWarnings("deprecation")
	public static boolean isMatchingDates(Date date1, Date date2) {
		return date2.getDate() == date1.getDate() && date2.getMonth() == date1.getMonth()
				&& date2.getYear() == date1.getYear();
	}

	@SuppressWarnings("deprecation")
	public static FastDate stripTimeFromDate(Date day) {
		int dayOfMonth = day.getDate();
		return new FastDate(day.getYear(), day.getMonth(), dayOfMonth);
	}

	public static FastDate earliestDate(FastDate[] dates) {
		if (dates == null || dates.length == 0) {
			return null;
		}

		FastDate date = dates[0];
		for (int i = 1; i < dates.length; i++) {
			if (dates[i].before(date)) {
				date = dates[i];
			}
		}

		return date;
	}

	public static Date addCalendarYears(Date date, int yearsToAdvance) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, yearsToAdvance);

		Date d = cal.getTime();
		return d;

	}

	public static FastDate addCalendarDays(FastDate date, int daysToAdvance) {
		return new FastDate(addCalendarDays((Date) date, daysToAdvance));
	}

	public static Date addCalendarDays(Date date, int daysToAdvance) {
		// return new Date(date.getYear(), date.getMonth(), date.getDate() +
		// daysToAdvance);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, daysToAdvance);

		Date d = cal.getTime();
		return d;

	}

	public static Date addToCalendar(Date date, long offsetMilliseconds) {

		long milliseconds = date.getTime() + offsetMilliseconds;
		Date d = new Date(milliseconds);
		return d;

	}

	public static long convertDaysToMilliseconds(double days) {
		// Casting will truncate to decimal places rather than round...
		return (long) days + DAILY;
	}

	public static boolean isTimeAvailable(Date date) {

		if (date == null) {
			return false;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		int ms = cal.get(Calendar.MILLISECOND);
		if (ms > 0) {
			return true;
		}

		int ss = cal.get(Calendar.SECOND);
		if (ss > 0) {
			return true;
		}

		int mm = cal.get(Calendar.MINUTE);
		if (mm > 0) {
			return true;
		}

		int hh = cal.get(Calendar.HOUR_OF_DAY);
		return hh > 0;

	}

	@SuppressWarnings("deprecation")
	public static boolean isDifferentMonths(Date currentDate, Date priorDate) {
		return currentDate.getMonth() != priorDate.getMonth();
	}

	@SuppressWarnings("deprecation")
	public static boolean isDifferentYears(Date currentDate, Date priorDate) {
		return currentDate.getYear() != priorDate.getYear();
	}

	@SuppressWarnings("deprecation")
	public static Date startOfMonth(Date currentDatetime) {
		Calendar cal = new GregorianCalendar(currentDatetime.getYear() + 1900, currentDatetime.getMonth(),
				currentDatetime.getDate());
		return cal.getTime();
	}


	public static Date getNextMonthFirstDayOfWeek(Date date, int dayOfWeek) {
		Calendar c = GregorianCalendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		// search for day fo week!
		while (c.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
			c.add(Calendar.DAY_OF_MONTH, 1);
		}
		return c.getTime();
	}

	public static boolean isWeekendDay(Date date) {
		int dayOfWeek = getDayOfWeek(date);
		return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
	}

	public static List<FastDate> mergeByMonthDayYear(List<FastDate> dates1, List<FastDate> dates2) {

		for (FastDate date : dates1) {
			if (isTimeAvailable(date)) {
				throw new IllegalArgumentException("Error: date should only have yyyy-mm-dd : " + date);
			}
		}
		Set<FastDate> set = new HashSet<FastDate>(dates1);
		for (FastDate date : dates2) {
			if (isTimeAvailable(date)) {
				throw new IllegalArgumentException("Error: date should only have yyyy-mm-dd : " + date);
			}
			// this will remove duplicates
			set.add(date);
		}

		return new ArrayList<FastDate>(set);
	}

	@SuppressWarnings("deprecation")
	public static int getQuarter(FastDate date) {
		int month = date.getMonth();
		if (month <= 2) {
			return 1;
		}
		if (month <= 5) {
			return 2;
		}
		if (month <= 8) {
			return 3;
		}
		return 4;
	}


	public static boolean isLaterDate(Date endDate, Date currentDate) {
		Date end = DateUtils.stripTimeFromDate(endDate);
		Date current = DateUtils.stripTimeFromDate(currentDate);
		boolean b = end.after(current) || matchDayMonthAndYear(end, current);
		return b;
	}

	public static boolean isLaterDateStrict(Date endDate, Date currentDate) {
		Date end = DateUtils.stripTimeFromDate(endDate);
		Date current = DateUtils.stripTimeFromDate(currentDate);
		boolean b = end.after(current);
		return b;
	}

	@SuppressWarnings("deprecation")
	public static boolean afterHoursAndMinutes(Date time, int hours, int minutes) {
		Date date = new Date();
		date.setHours(hours);
		date.setMinutes(minutes);
		return afterHoursAndMinutes(date, time);
	}

	@SuppressWarnings("deprecation")
	public static boolean matchHoursAndMinutes(Date time, int hours, int minutes) {
		Date date = new Date();
		date.setHours(hours);
		date.setMinutes(minutes);
		return matchHoursAndMinutes(date, time);
	}

	public static Date addWorkingDays(Date date, int daysInTradeExDiv) {
		int rem = daysInTradeExDiv % 5;
		int days = daysInTradeExDiv / 5;
		days *= 7;
		int daysForward = days + rem;
		Date d = addCalendarDays(date, daysForward);
		if (rem == 0) {
			return d;
		}
		int dow = getDayOfWeek(date);
		int toAdd = 0;
		if (dow == Calendar.FRIDAY && rem > 0) {
			toAdd = 2;
		} else if (dow == Calendar.THURSDAY && rem > 1) {
			toAdd = 2;
		} else if (dow == Calendar.WEDNESDAY && rem > 2) {
			toAdd = 2;
		} else if (dow == Calendar.TUESDAY && rem > 3) {
			toAdd = 2;
		}

		d = addCalendarDays(d, toAdd);
		return d;
	}

	/**
	 * Calculate the difference between two dates, ignoring weekends.
	 *
	 * @param d1 the first day of the interval
	 * @param d2 the day after the end of the interval
	 * @return the number of days in the interval, excluding weekends
	 */
	public static long countWorkingDaysBetween(Date d1, Date d2) {
		return countWorkingDaysSinceJanFirst1970(d2) - countWorkingDaysSinceJanFirst1970(d1);
	}

	/**
	 * Return the number of week days between Monday, 29 December, 1969 and the
	 * given date.
	 *
	 * @param d a date
	 * @return the number of week days since Monday, 29 December, 1969
	 */
	private static long countWorkingDaysSinceJanFirst1970(Date date) {
		long l = NumberUtils.div(date.getTime(), 1000 * 60 * 60 * 24)[0] + 3;
		long d[] = NumberUtils.div(l, 7);
		return 5 * d[0] + Math.min(d[1], 5);
	}

	@SuppressWarnings("deprecation")
	public static int getNumYearsSince(Date date, int year) {
		if (year > 1900) {
			year -= 1900;
		}
		return date.getYear() - year;
	}

	@SuppressWarnings("deprecation")
	public static boolean beforeYear(Date date, int year) {
		if (year > 1900) {
			year -= 1900;
		}
		return date.getYear() < year;
	}

	public static int getMonthAsInteger(String month) {
		String[] months = DateFormatSymbols.getInstance().getMonths();
		String monthUp = month.toUpperCase();
		for (int i = 0; i < months.length; i++) {
			if (months[i].toUpperCase().startsWith(monthUp)) {
				return i;
			}
		}
		return -1;
	}

	public static int countCalendarDaysBetween(FastDate date, FastDate date2) {

		long time = date.getTime();
		long time2 = date2.getTime();
		long diff = time2 - time;

		long days = diff / MILLISECONDS_PER_DAY;

		return (int) days;

	}

}
