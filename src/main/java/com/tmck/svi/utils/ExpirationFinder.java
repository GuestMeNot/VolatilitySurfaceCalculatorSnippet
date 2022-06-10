package com.tmck.svi.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class ExpirationFinder {
	
	public static boolean isExpirationWeek(Date date) {
      FastDate expDate = ExpirationFinder.findCurrentMonthlyExpiration(date);
      int days = DateUtils.getDaysBetween(date, expDate);
      if(days <= 5) {
      	return true;
      }
      return false;
	}
	
	/** 
	 * Find the DTE (in Calendar days) that is at least minimumDaysBeforeExpiration calendar days before expiration. 
	 * @param minimumCalendarDaysBeforeExpiration the minimum number of calendar days before expiration.
	 */
	public static int getCalendarDaysToExpirationForCurrentMonthlyExpiration(int minimumDaysBeforeExpiration) {
		return getCalendarDaysToExpiration(findCurrentMonthlyExpiration(minimumDaysBeforeExpiration));
	}
	
	/** 
	 * Find the DTE that is at least minimumDaysBeforeExpiration days before expiration. 
	 * @param minimumCalendarDaysBeforeExpiration the minimum number of calendar days before expiration.
	 */
	public static int getTradingDaysToExpirationForCurrentMonthlyExpiration(int minimumCalendarDaysBeforeExpiration) {
		FastDate expiryDate = findCurrentMonthlyExpiration(minimumCalendarDaysBeforeExpiration);
		return DateUtils.getWorkingDaysBetween(new FastDate(), expiryDate);
	}

	/**
	 * Find the date of the current monthly expiration Date that is at least minimumDaysBeforeExpiration before expiration.
	 * @param minimumCalendarDaysBeforeExpiration the minimum number of calendar days before expiration.
	 */
	public static FastDate findCurrentMonthlyExpiration(int minimumDaysBeforeExpiration) {
		return findCurrentMonthlyExpiration(new Date(), minimumDaysBeforeExpiration);
	}	
	
	public static FastDate findCurrentMonthlyExpiration(Date currentDate, int minimumDaysBeforeExpiration) {
		Date lastDate = DateUtils.addCalendarDays(currentDate, minimumDaysBeforeExpiration);
		return findCurrentMonthlyExpiration(lastDate);
	}
	
	public static int findCurrentMonthlyExpirationDte(Date currentDate, int minimumDaysBeforeExpiration) {
		FastDate expiryDate = findCurrentMonthlyExpiration(currentDate, minimumDaysBeforeExpiration);
		return getCalendarDaysToExpiration(expiryDate, currentDate);
	}

	
	public static FastDate findCurrentMonthlyExpiration(Date lastDate) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(lastDate);		
		cal.set(GregorianCalendar.DAY_OF_WEEK, Calendar.FRIDAY);
		cal.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, 3);

		
		Date time = cal.getTime();
		if(time.after(lastDate) || DateUtils.matchDayMonthAndYear(time, lastDate)) {
			return new FastDate(time);
		} else {
			
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(lastDate);		
			cal2.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
			cal2.set(GregorianCalendar.DAY_OF_WEEK, Calendar.FRIDAY);
			cal2.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, 3);
			
			return new FastDate(cal2.getTime());
			
		}
		
	}
	
	public static int getCalendarDaysToExpiration(Date expirationDate) {
		return getCalendarDaysToExpiration(new FastDate(), expirationDate);
	}
	
	public static int getCalendarDaysToExpiration(Date expiryDate,
			Date currentDate) {
		return DateUtils.getDaysBetween(currentDate, expiryDate);
	}

	public static FastDate findNextMonthExpiration(int minimumDaysBeforeExpiration, int numMonthsForward) {
		return findNextMonthExpiration(new Date(), minimumDaysBeforeExpiration, numMonthsForward);
	}
	
	public static FastDate findNextMonthExpiration(Date currentDate, int minimumDaysBeforeExpiration, int numMonthsForward) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(currentDate);
		cal.add(Calendar.MONTH, numMonthsForward + 1);
		cal.add(Calendar.DAY_OF_MONTH, minimumDaysBeforeExpiration);
		Date lastDate = cal.getTime();
		
		return findCurrentMonthlyExpiration(lastDate);
		
	}

	public static int getDteJustPriorToDesiredDte(Date currentDate, int minimumDaysBeforeExpiration) {
		
		Date date = findCurrentMonthlyExpiration(currentDate, minimumDaysBeforeExpiration);
		return DateUtils.getDaysBetween(currentDate, date);
	
	}

	public static int getCalendarDaysToExpirationForMonthlyExpiration(
			FastDate currentDate, int minimumDaysBeforeExpiration) {
		return getCalendarDaysToExpiration(findCurrentMonthlyExpiration(currentDate, minimumDaysBeforeExpiration), currentDate);
	}

}
