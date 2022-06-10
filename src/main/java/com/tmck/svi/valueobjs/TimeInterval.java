package com.tmck.svi.valueobjs;

import java.util.GregorianCalendar;

import com.tmck.svi.utils.DateUtils;
import com.tmck.svi.utils.FastDate;
import com.tmck.svi.utils.NumberUtils;


/**
 * A class that specifies how frequently something reoccurs.
 * 
 * @author tim
 *
 */
public class TimeInterval implements Comparable<TimeInterval> {

	private static final int FEBRUARY = FastDate.FEBRUARY;
	private static final int MAY = FastDate.MAY;
	private static final int JUNE = FastDate.JUNE;
	private static final int JULY = FastDate.JULY;
	private static final int AUGUST = FastDate.AUGUST;
	private static final int SEPTEMBER = FastDate.SEPTEMBER;
	private static final int OCTOBER = FastDate.OCTOBER;
	private static final int NOVEMBER = FastDate.NOVEMBER;
	private static final int DECEMBER = FastDate.DECEMBER;
	public static final long INTERVAL_NOT_SET = Long.MIN_VALUE;
	public static final long INVALID_TIME_INTERVAL = Long.MIN_VALUE + 1;
	private static final int WEEKS_PER_QUARTER = DateUtils.WEEKS_PER_QUARTER;
	private static final int DAYS_PER_QUARTER = DateUtils.DAYS_PER_QUARTER;
	
	public static final long [] VALID_INTERVALS = { 
							DateUtils.ONE_MINUTE,
							DateUtils.FIVE_MINUTES,
							DateUtils.FIFTEEN_MINUTES,
							DateUtils.HOURLY,
							DateUtils.DAILY,
							DateUtils.WEEKLY,
							DateUtils.MONTHLY,
							DateUtils.QUARTERLY,
							DateUtils.YEARLY
							};


	public static TimeInterval ONE_MINUTE_INTERVAL = new TimeInterval(DateUtils.ONE_MINUTE);
	public static TimeInterval FIVE_MINUTE_INTERVAL = new TimeInterval(DateUtils.FIVE_MINUTES);
	public static TimeInterval FIFTEEN_MINUTE_INTERVAL = new TimeInterval(DateUtils.FIFTEEN_MINUTES);
	public static TimeInterval HOURLY_INTERVAL = new TimeInterval(DateUtils.HOURLY);
	public static TimeInterval DAILY_INTERVAL = new TimeInterval(DateUtils.DAILY);
	public static TimeInterval WEEKLY_INTERVAL = new TimeInterval(DateUtils.WEEKLY);
	public static TimeInterval MONTHLY_INTERVAL = new TimeInterval(DateUtils.MONTHLY);
	public static TimeInterval QUARTERLY_INTERVAL = new TimeInterval(DateUtils.QUARTERLY);
	public static TimeInterval YEARLY_INTERVAL = new TimeInterval(DateUtils.YEARLY);
	public static TimeInterval NOT_SET_INTERVAL = new TimeInterval();
	public static TimeInterval INVALID_INTERVAL = new TimeInterval(INVALID_TIME_INTERVAL);

    public static final TimeInterval [] VALID_TIME_INTERVALS = { 
                    ONE_MINUTE_INTERVAL,
                    FIVE_MINUTE_INTERVAL,
                    FIFTEEN_MINUTE_INTERVAL,
                    HOURLY_INTERVAL,
                    DAILY_INTERVAL,
                    WEEKLY_INTERVAL,
                    MONTHLY_INTERVAL,
                    QUARTERLY_INTERVAL,
                    YEARLY_INTERVAL
                    };
    
    
	private long timeInterval = INTERVAL_NOT_SET;

			
	public TimeInterval() { }

	public TimeInterval(FastDate start, FastDate end) { adjustTimeInterval(start, end); }

	public TimeInterval(TimeInterval interval) {
		setTimeInterval(interval.getTimeInterval()); 
	}

	private TimeInterval(long interval) {
		setTimeInterval(interval); 
	}
	
	public boolean isTimeIntervalSet() {
		return timeInterval == INTERVAL_NOT_SET;
	}
							
	private long getTimeInterval() {
		return timeInterval;
	}

	private void setTimeInterval(long interval) {
//		if(isAllowableTimeIntervalValue(interval)) {
//			timeInterval = interval;
//		}
		if(interval == INVALID_TIME_INTERVAL || interval == INTERVAL_NOT_SET) {
			timeInterval = interval;
		} else {
			timeInterval = getAdjustedTimeInterval(interval);
		}
	}

	public static boolean isValidTimeInterval(long interval) {
		for(int i = 0; i < VALID_INTERVALS.length; i++) {
			if(interval == VALID_INTERVALS[i]) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isAllowableTimeIntervalValue(long interval) {
		return isValidTimeInterval(interval) 
			|| interval == INTERVAL_NOT_SET
			|| interval == INVALID_TIME_INTERVAL;
	}
	
//	private boolean isComparable(long interval) {
//	
//		if(!isValidTimeInterval(interval))
//			return false;
//			
//		if(!isValidTimeInterval(getTimeInterval()))
//			return false;
//			
//		return true;		
//	}
	
	/** @return the adjusted time interval. Will not work for overnight, holidays and weekends for daily or intra-day intervals. */
	private static long getAdjustedTimeInterval(long interval) {
		
		if(interval < 0) {
			return INVALID_TIME_INTERVAL;
		}
			 
		
		for(int i = 0; i < VALID_INTERVALS.length - 1; i++) {
			
			long first = VALID_INTERVALS[i];
			long second = VALID_INTERVALS[i + 1];
			
			long thresholdInterval = (long) ((second - first)/1.5 + first);
			
			// handle weekends for daily and weekly data.
			if(first != DateUtils.DAILY && first != DateUtils.WEEKLY && second > (2 * first)) {
				thresholdInterval = 2 * first;
			}
			
			if(interval <= first || interval < thresholdInterval)
				return first;
		}
		
		return VALID_INTERVALS[VALID_INTERVALS.length - 1];
										
	}

	private static long getAdjustedTimeInterval(FastDate start, FastDate end) {
		long rawInterval = getRawInterval(start, end);
		return getAdjustedTimeInterval(rawInterval);
	}		

	
	protected void adjustTimeInterval(FastDate start, FastDate end) {
		
		long interval = getAdjustedTimeInterval(start, end);
		
		if(getTimeInterval() == INTERVAL_NOT_SET) {
			setTimeInterval(interval);
		} else if(!matchesTimeInterval(interval)) {	
			if(!isMissingTimeInterval(start, end)) {
				setTimeInterval(INVALID_TIME_INTERVAL);
			}
		}	
		
	}


	public static boolean isMissingTimeInterval(FastDate start, FastDate end) {
		return isMissingDate(start) || isMissingDate(end);		
	}

	public static boolean isMissingDate(FastDate start) {
		return isMatch(SEPTEMBER, 10, 2001, start) 
					|| isMatch(SEPTEMBER, 7, 2001, start) 
					|| isMatch(OCTOBER, 24, 2000, start) 
					|| isMatch(AUGUST, 25, 2000, start) 
					|| isMatch(MAY, 24, 2002, start) 
					|| isMatch(MAY, 24, 2001, start) 
					|| isMatch(FEBRUARY, 11, 1971, start) 
					|| isMatch(MAY, 26, 1978, start) 
					|| isMatch(FEBRUARY, 11, 1982, start)
					|| isMatch(JUNE, 30, 1995, start) 
					|| isMatch(JUNE, 30, 2000, start) 
					|| isMatch(JUNE, 30, 1989, start) 
					|| isMatch(JULY, 3, 1996, start) 
					|| isMatch(JULY, 3, 1991, start) 
					|| isMatch(JULY, 2, 1986, start) 
					|| isMatch(JULY, 3, 1985, start) 
					|| isMatch(JULY, 2, 2004, start) 
					|| isMatch(JULY, 29, 1999, start) 
					|| isMatch(NOVEMBER, 24, 2004, start) 
					|| isMatch(NOVEMBER, 26, 2003, start) 
					|| isMatch(NOVEMBER, 27, 2002, start) 
					|| isMatch(NOVEMBER, 22, 2000, start) 
					|| isMatch(NOVEMBER, 24, 1999, start) 
					|| isMatch(NOVEMBER, 25, 1998, start) 
					|| isMatch(NOVEMBER, 26, 1997, start) 
					|| isMatch(NOVEMBER, 27, 1996, start) 
					|| isMatch(NOVEMBER, 22, 1995, start) 
					|| isMatch(NOVEMBER, 23, 1994, start) 
					|| isMatch(NOVEMBER, 24, 1993, start) 
					|| isMatch(NOVEMBER, 25, 1992, start) 
					|| isMatch(NOVEMBER, 23, 1988, start) 
					|| isMatch(NOVEMBER, 25, 1987, start) 
					|| isMatch(NOVEMBER, 26, 1986, start) 
					|| isMatch(NOVEMBER, 27, 1985, start) 
					|| isMatch(DECEMBER, 31, 2003, start) 
					|| isMatch(DECEMBER, 30, 1999, start) 
					|| isMatch(DECEMBER, 24, 2003, start) 
					|| isMatch(DECEMBER, 23, 1992, start) 
					|| isMatch(DECEMBER, 21, 1990, start) 
					|| isMatch(DECEMBER, 24, 1986, start) 
					|| isMatch(DECEMBER, 21, 1973, start) 
					|| isMatch(DECEMBER, 21, 2001, start);
		
	}

	private static boolean isMatch(int mm, int dd, int yy, FastDate start) {
		FastDate d = getDate(mm, dd, yy);
		boolean b = DateUtils.matchDayMonthAndYear(start, d);
		return b;
	}


//	private boolean isMatch(int mm, int dd, int yy, Date start, Date end) {
//		Date d = getDate(mm, dd, yy);
//		boolean b = DateUtils.matchDayMonthAndYear(start, d) || DateUtils.matchDayMonthAndYear(end, d);
//		return b;
//	}

	private static FastDate getDate(int mm, int dd, int yy) {
		
		FastDate d = new FastDate(yy, mm, dd);
		
//		d.setYear(yy - 1900);
//		d.setMonth(mm);
//		d.setDate(dd);	
		
		return d;
	}

	
	/** @return true if time interval matches. NOTE: this will not work for 9/11. */
	private boolean matchesTimeInterval(long interval) {
		
		long current = getTimeInterval(); 
		long invalid = INVALID_TIME_INTERVAL;
		
		if(interval == invalid && current == invalid) {
			return true;
		}

		if(interval == INTERVAL_NOT_SET && current == INTERVAL_NOT_SET) {
			return true;
		}
		
		long adjusted = getAdjustedTimeInterval(interval);
		
		if(adjusted == current)
			return true;
			
		// handle overnight, weekend and holiday hours.
		if(isIntraDayInterval(current)) {
			return true;
		}

		return false;
	}

	private boolean isIntraDayInterval(TimeInterval interval) {
		return interval.matchesTimeInterval(DateUtils.HOURLY);
	}

	// needed to prevent a circular series of method calls.
	private boolean isIntraDayInterval(long interval) {
		
		long adjusted = getAdjustedTimeInterval(interval);
		
		return adjusted == DateUtils.HOURLY  
				|| adjusted == DateUtils.FIFTEEN_MINUTES 
				|| adjusted == DateUtils.FIVE_MINUTES
				|| adjusted == DateUtils.ONE_MINUTE;
				
	}

	public boolean isIntraDayInterval() {
		return isIntraDayInterval(this);
	}
		
	public static boolean isDailyInterval(TimeInterval interval) {
		return interval.matchesTimeInterval(DateUtils.DAILY);
	}

	public boolean isDailyInterval() {
		return isDailyInterval(this);
	}

	public boolean matchesTimeInterval(FastDate start, FastDate end) {

		long interval = getRawInterval(start, end); 

		if(matchesTimeInterval(interval)) {
			return true;
		}
		
		return false;
	}

	public boolean matchesTimeInterval(TimeInterval interval) {

		if(matchesTimeInterval(interval.getTimeInterval())) {
			return true;
		}
		
		return false;
	}

	
	private static long getRawInterval(FastDate start, FastDate end) {
		
		assert start != null;
		assert end != null;
		
		long startTime = start.getTime();
		long endTime = end.getTime();
		
		if(startTime > endTime) {
			System.err.println("startTime > endTime: " + start + " : " + end + " using absolute value instead.");			
		}
		
		long interval = end.getTime() - start.getTime();
		return Math.abs(interval);
		
	}

	
	public boolean isSmallerTimeInterval(TimeInterval interval) {
		return isSmallerTimeInterval(interval.getTimeInterval());
	}

	private boolean isSmallerTimeInterval(long interval) {
//		if(!isComparable(interval))
//			return false;
		
		return getAdjustedTimeInterval(interval) < getTimeInterval();
	}

	public boolean isLargerTimeInterval(TimeInterval interval) {
		if(interval == null) {
			throw new NullPointerException();
		}
		return isLargerTimeInterval(interval.getTimeInterval());
	}
	
	private boolean isLargerTimeInterval(long interval) {
		
//		if(!isComparable(interval))
//			return false;
		
		return getAdjustedTimeInterval(interval) > getTimeInterval();
		 
	}

	public boolean isSameTimeInterval(TimeInterval interval) {
		return isSameTimeInterval(interval.getTimeInterval());
	}

	private boolean isSameTimeInterval(long interval) {
		
//		if(!isComparable(interval))
//			return false;
			
		return getAdjustedTimeInterval(interval) == getTimeInterval();
		
	}
	
	public boolean startsNewInterval(FastDate reference, FastDate toCheck, boolean useRelativeStartTime) {
		return startsNewInterval(this, reference, toCheck, useRelativeStartTime);
	}

	
	private static boolean startsNewInterval(TimeInterval interval, FastDate param1, FastDate param2, boolean useRelativeStartTime) {
		
		FastDate reference = param1;
		FastDate toCheck = param2;
		
		if(param1.getTime() > param2.getTime()) {
			reference = param2;
			toCheck = param1;
		}
		
		// TODO check is this sequence is too slow.
		
		if(interval.matchesTimeInterval(DateUtils.DAILY)) return isStartOfNewDay(reference, toCheck);
		if(interval.matchesTimeInterval(DateUtils.WEEKLY)) return isStartOfNewWeek(reference, toCheck, useRelativeStartTime);
		if(interval.matchesTimeInterval(DateUtils.MONTHLY)) return isStartOfNewMonth(reference, toCheck, useRelativeStartTime);
		if(interval.matchesTimeInterval(DateUtils.QUARTERLY)) return isStartOfNewQuarter(reference, toCheck, useRelativeStartTime);
        if(interval.matchesTimeInterval(DateUtils.YEARLY)) return isStartOfNewYear(reference, toCheck, useRelativeStartTime);
		
		throw new IllegalStateException("invalid interval: " + interval);
	}


    @SuppressWarnings("deprecation")
	private static boolean isStartOfNewYear(FastDate reference, FastDate toCheck, boolean useRelativeStartTime) {
        
        if(useRelativeStartTime) {
            
            GregorianCalendar referenceCal = (GregorianCalendar) reference.convertToCalendar().clone();
            GregorianCalendar toCheckCal = toCheck.convertToCalendar();
            
            referenceCal.add(GregorianCalendar.YEAR, 1);
            
            boolean b = toCheckCal.after(referenceCal) || toCheckCal.equals(referenceCal);
            
            return b;
        }
        
        int ref = reference.getYear();
        int to = toCheck.getYear();
        
        if(ref < to) {
            return true;
        } 

        return false;
        
    }

    private static boolean isStartOfNewQuarter(FastDate reference, FastDate toCheck, boolean useRelativeStartTime) {
		
        if(useRelativeStartTime) {
            
            GregorianCalendar referenceCal = (GregorianCalendar) reference.convertToCalendar().clone();
            GregorianCalendar toCheckCal = toCheck.convertToCalendar();
    		
    		referenceCal.add(GregorianCalendar.MONTH, 3);
    		
    		boolean b = toCheckCal.after(referenceCal) || toCheckCal.equals(referenceCal);
            
            return b;
        }
        
        int ref = reference.getQuarter();
        int to = toCheck.getQuarter();
        
        if(ref < to) {
            return true;
        } 

        if(to <= 1 && ref >= 2)
            return true;
    
        return false;
			
	}


	@SuppressWarnings("deprecation")
	private static boolean isStartOfNewMonth(FastDate reference, FastDate toCheck, boolean useRelativeStartTime) {

        if(useRelativeStartTime) {
            
            GregorianCalendar referenceCal = (GregorianCalendar) reference.convertToCalendar().clone();
            GregorianCalendar toCheckCal = toCheck.convertToCalendar();
            
            referenceCal.add(GregorianCalendar.DAY_OF_YEAR, 7);
            
            boolean b = toCheckCal.after(referenceCal) || toCheckCal.equals(referenceCal);
            
            return b;
            
        }        
        
        int ref = reference.getMonth();
        int to = toCheck.getMonth();
        
        if(ref < to) {
            return true;
        } 

        if(to <= 1 && ref >= 2)
            return true;
	
		return false;
			
	}

	private static boolean isStartOfNewDay(FastDate reference, FastDate toCheck) {
		
		int ref = reference.getDayOfYear();
		int to = toCheck.getDayOfYear();
		
		if(ref < to) {
			return true;
		} 

		// if the new date is the in the first quarter and 
		//		the reference week is in the last quarter
		if(to <= DAYS_PER_QUARTER || ref >= DAYS_PER_QUARTER*3)
			return true;

		return false;
			
	}


	private static boolean isStartOfNewWeek(FastDate reference, FastDate toCheck, boolean useRelativeStartTime) {
		
        
        if(useRelativeStartTime) {
            
            int refDay = reference.getDayOfYear();
            int toDay = toCheck.getDayOfYear();

            // if the new date is the in the first quarter and 
            //      the reference day is in the last quarter
            if(toDay <= DAYS_PER_QUARTER && refDay >= DAYS_PER_QUARTER*3) {
                int daysInYear = 365;
                if(reference.isLeapYear()) {
                    daysInYear = 366;
                }
                toDay += daysInYear;
            }
            
            // if the new week is first.
            if(refDay+6 < toDay) {
                return true;
            } 
            
            return false;            
            
        }
        
        
		int ref = reference.getWeekOfYear();
		int to = toCheck.getWeekOfYear();
		
		// if the new week is first.
		if(ref < to) {
			return true;
		} 

		// if the new date is the in the first quarter and 
		//		the reference week is in the last quarter
		if(to <= WEEKS_PER_QUARTER && ref >= WEEKS_PER_QUARTER*3)
			return true;
			
		return false;

	}

    public static int getNextIntervalIndex(TimeSeriesPoint [] data, int start, TimeInterval interval) {
		FastDate startDate = data[start].getDate();
		for(int i = start + 1; i < data.length; i++) {
			FastDate endDate = data[i].getDate();
			if(interval.matchesTimeInterval(startDate, endDate)) {
				return i + 1;
			}	
		}
		return data.length;
	}

	public String toString() {
		long interval = getTimeInterval();
		String str = "" +  interval;

		if(interval == DateUtils.ONE_MINUTE) {
			str += ":1 Minute";
		} else if(interval == DateUtils.FIVE_MINUTES) {
			str += ":5 Minutes";
		} else if(interval == DateUtils.FIFTEEN_MINUTES) {
			str += ":15 Minutes";
		} else if(interval == DateUtils.HOURLY) {
			str += ":Hourly";
		} else if(interval == DateUtils.DAILY) {
			str += ":Daily";
		} else if(interval == DateUtils.WEEKLY) {
			str += ":Weekly";
		} else if(interval == DateUtils.MONTHLY) {
			str += ":Monthly";
		} else if(interval == DateUtils.QUARTERLY) {
			str += ":Quarterly";
		} else if(interval == DateUtils.YEARLY) {
			str += ":Yearly";
		}
		
		return  str;
				 
	}
	
	public void markInvalid() {
		setTimeInterval(TimeInterval.INVALID_TIME_INTERVAL);		
	}

	public boolean isNotSet() {
		return getTimeInterval() == TimeInterval.INTERVAL_NOT_SET;		
	}
	
	public boolean isInvalid() {
		return getTimeInterval() == TimeInterval.INVALID_TIME_INTERVAL;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof TimeInterval)) {
			return false;
		} 
		return equals((TimeInterval) obj);
	}

	public boolean equals(TimeInterval interval) {
		return compareTo(interval) == 0;
	}

	public int compareTo(TimeInterval interval) {
		
		if(interval.timeInterval < timeInterval) return -1; 
		if(interval.timeInterval > timeInterval) return 1;
		 
		return 0;
		
	}

	public String convertToString() {

		if(matchesTimeInterval(DateUtils.WEEKLY)) {
			return "WEEK";
		}

		if(matchesTimeInterval(DateUtils.DAILY)) {
			return "DAILY";
		}

		if(matchesTimeInterval(DateUtils.HOURLY)) {
			return "HOURLY";
		}

		if(matchesTimeInterval(DateUtils.MONTHLY)) {
			return "MONTHLY";
		}

		if(matchesTimeInterval(DateUtils.QUARTERLY)) {
			return "QUARTERLY";
		}
		
		if(matchesTimeInterval(DateUtils.YEARLY)) {
			return "YEARLY";
		}

		return "UNKNOWN_INTERVAL";
		
	}

	public static String convertToString(long timeInterval) {
		
		TimeInterval interval = new TimeInterval(timeInterval);
		return interval.convertToString();
		
	}

	public TimeInterval copy() {
		TimeInterval interval = new TimeInterval();
		interval.setTimeInterval(this.timeInterval);
		return interval;
	}

	public boolean isWeeklyInterval() {
		return matchesTimeInterval(DateUtils.WEEKLY);
	}

	public boolean isMonthlyInterval() {
		return matchesTimeInterval(DateUtils.MONTHLY);
	}

	public boolean isQuarterlyInterval() {
		return matchesTimeInterval(DateUtils.QUARTERLY);
	}

	public static boolean isWeeklyInterval(TimeInterval reference) {
		return matchesInterval(reference, TimeInterval.WEEKLY_INTERVAL);
	}

	public static boolean isMonthlyInterval(TimeInterval reference) {
		return matchesInterval(reference, TimeInterval.MONTHLY_INTERVAL);
	}

	public static boolean isQuarterlyInterval(TimeInterval reference) {
		return matchesInterval(reference, TimeInterval.QUARTERLY_INTERVAL); 
	}

	public static boolean matchesInterval(TimeInterval reference, TimeInterval toMatch) {
		if(reference == null) {
			return false;
		}
		return reference.equals(toMatch);
	}

	/**
	 *  TODO find a better way to do this because it is used for date manipulation.
	 * 
	 * @return the rough (but not always accurate) number of milliseconds that will elapse during this interval. 
	 */
	public long milliseconds() { return timeInterval; }

	public static void main(String [] args) {

		FastDate monday = new FastDate(104, 8, 2);
//		monday.setYear(104);
//		monday.setMonth(9);
//		monday.setDate(1);

		FastDate friday = new FastDate(104, 8, 6);
//		friday.setYear(104);
//		friday.setMonth(9);
//		friday.setDate(4);
		
System.out.println("monday: " + monday);		
System.out.println("friday: " + friday);		
		
		TimeInterval interval = new TimeInterval();
		interval.setTimeInterval(DateUtils.DAILY + DateUtils.DAILY/5);

		boolean b = interval.matchesTimeInterval(monday, friday);
		System.out.println("=monday-friday(true):" + b);

		b = interval.matchesTimeInterval(DateUtils.WEEKLY);
		System.out.println("=WEEKLY(false):" + b);		
		
		b = interval.matchesTimeInterval(DateUtils.DAILY);
		System.out.println("=DAILY(true):" + b);
		
		b = interval.matchesTimeInterval(DateUtils.HOURLY);
		System.out.println("=HOURY(false):" + b);
		

		b = interval.matchesTimeInterval(DateUtils.MONTHLY);
		System.out.println("=MONTHLY(false):" + b);		

		b = interval.matchesTimeInterval(DateUtils.QUARTERLY);
		System.out.println("=QUARTERLY(false):" + b);		

		b = interval.matchesTimeInterval(DateUtils.YEARLY);
		System.out.println("=YEARLY(false):" + b);		
		
	}
	
	public static TimeInterval getClosestMatchingTimeInterval(FastDate start, FastDate end) {
		return new TimeInterval(start, end);
	}
	
	public int hashCode() { return NumberUtils.convertDoubleToInt(getTimeInterval()); }

    public boolean isYearlyInterval() {
        return matchesTimeInterval(DateUtils.YEARLY);
    }	
	
}
