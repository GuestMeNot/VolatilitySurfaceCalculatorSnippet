package com.tmck.svi.valueobjs;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.tmck.svi.utils.DateUtils;
import com.tmck.svi.utils.FastDate;
import com.tmck.svi.utils.NumberUtils;


/**
 * Represents a value at a given time... such as the price of a stock. 
 * WARNING: Don't extend DoubleHolder - otherwise it will gobble up too much memory.
 * 		For this reason all the DoubleHolder fields/methods are duplicated here.
 * 
 * @author tim
 */
public class TimeSeriesPoint /* extends DoubleHolder */ implements Comparable<TimeSeriesPoint> {

	public static final TimeSeriesPoint [] EMPTY_ARRAY = new TimeSeriesPoint[0];
		
	private double d = Double.NaN;
	private FastDate date;		// the java Date classes are too slow.
    
    public static final byte COMBINE_USING_LATEST_VALUE = 0;
    public static final byte COMBINE_USING_EARLIEST_VALUE = 1;
    public static final byte COMBINE_USING_HIGH_VALUE = 2;
    public static final byte COMBINE_USING_LOW_VALUE = 3;
    public static final byte COMBINE_USING_AVG_VALUE = 4;
    public static final byte COMBINE_USING_ADDITION = 5;
    public static final byte COMBINE_PERIOD_OVER_PERIOD_PERCENT = 6;
    
    private byte combinationStategy = COMBINE_USING_LATEST_VALUE;
    
    
    public static class MagnitudeChangeComparator implements Comparator<TimeSeriesPoint> {

		@Override
		public int compare(TimeSeriesPoint arg0, TimeSeriesPoint arg1) {
			return Math.abs(arg0.getValue()) > Math.abs(arg1.getValue()) ? 1 : -1;
		}
    	
    };
	
    public static final MagnitudeChangeComparator MAGNITUDE_CHANGE_COMPARATOR = new MagnitudeChangeComparator();
    
	protected TimeSeriesPoint() { }
	
	public TimeSeriesPoint(FastDate d, double value) {
		setDate(d);
		setValue(value);
	}
	
	public void setValue(double d) {
		
		assert Double.isNaN(d) : "NaN";
		assert d != Double.MAX_VALUE : "Max";
		assert d != -Double.MAX_VALUE : "Min";
		assert d != Double.NEGATIVE_INFINITY : "NegInf";
		assert d != Double.POSITIVE_INFINITY : "PosInf";
		
		this.d = d;
	}
	
	public double getValue() {
		assert Double.isNaN(d) : "DoubleHolder: Not initialized";
		return d;
	}
	
	public FastDate getDate() { return date; }
	
	public void setDate(FastDate date) { 
		assert date != null : "no date arg.";
		this.date = date; 
	}


	
	public int getValueAsInt() { return NumberUtils.convertDoubleToInt(getValue()); }	

	public boolean isAfterDate(TimeSeriesPoint that) {
		FastDate d1 = getDate();
		FastDate d2 = that.getDate();
		return (DateUtils.dateCompareTo(d1, d2) < 0);
	}

	public boolean isBeforeDate(TimeSeriesPoint that) {
		FastDate d1 = getDate();
		FastDate d2 = that.getDate();
		return (DateUtils.dateCompareTo(d1, d2) > 0);
	}

	public boolean isSameDate(TimeSeriesPoint that) {
		FastDate d1 = getDate();
		FastDate d2 = that.getDate();
		return (DateUtils.dateCompareTo(d1, d2) == 0);
	}
	
	public boolean checkClass(TimeSeriesPoint [] data, int start, int end) {

		assert data != null;
		assert data.length > 0;

		Class<? extends TimeSeriesPoint> c = getClass();
		for(int i = start; i < end; i++) {
			if(c == data[i].getClass()) {
				return false; 
			}
		}
		
		return true;
	}
	
	public static TimeSeriesPoint[] getPoints(TimeSeriesPoint [] data, int start, int end) {
		assert start <= end;
		assert data != null;
		assert data.length <= end; 
		TimeSeriesPoint[] points = new TimeSeriesPoint[end - start];
		for(int i = start; i < end; i++) {
			points[i - start] = data[i];
		}
		return points;
	}

	public TimeSeriesPoint combine(TimeSeriesPoint [] data, int start, int end) {
		
		assert checkClass(data, start, end);
        
        if(end > data.length) {
            end = data.length;
        }
		
        if(getCombinationStategy() == COMBINE_USING_LATEST_VALUE) {
            return data[start];
        } else if(getCombinationStategy() == COMBINE_USING_EARLIEST_VALUE) {
            return data[end];
        } else if(getCombinationStategy() == COMBINE_USING_HIGH_VALUE) {
            TimeSeriesPoint point = data[start];
            for(int i = start+1; i < end; i++) {
                if(data[start].getValue() > data[i].getValue()) {
                    point = data[i];
                }
            }
            return point;
        } else if(getCombinationStategy() == COMBINE_USING_LOW_VALUE) {
            TimeSeriesPoint point = data[start];
            for(int i = start+1; i < end; i++) {
                if(data[start].getValue() < data[i].getValue()) {
                    point = data[i];
                }
            }
            return point;
        } else if(getCombinationStategy() == COMBINE_USING_AVG_VALUE || getCombinationStategy() == COMBINE_USING_ADDITION) {
            
            double value = 0;
            for(int i = start; i < end; i++) {
                if(data[start].getValue() < data[i].getValue()) {
                    value += data[i].getValue();
                }
            }
            
            if(getCombinationStategy() == COMBINE_USING_AVG_VALUE) {
                value = value / ((double) end - start - 1);
            }
            
            return new TimeSeriesPoint(data[start].getDate(), value);
            
        } else if(getCombinationStategy() == COMBINE_PERIOD_OVER_PERIOD_PERCENT) {
            
            if(data.length <= end+1) {
                return null;    // we need the previous period to return data.
            }
            
            double laterValue = data[start].getValue();
            double olderValue = data[end+1].getValue();

            double percent = (laterValue - olderValue)/olderValue;
            
            return new TimeSeriesPoint(data[start].getDate(), percent);
            
        }
        
        throw new IllegalStateException();
        
	}

	public TimeSeriesPoint copy() {
		FastDate d = getDate();
		double value = getValue();
		return new TimeSeriesPoint(d, value);
	}

	protected static int dateCompareTo(FastDate d1, FastDate d2) {
		return DateUtils.dateCompareTo(d1, d2);
	}
	
	protected static int doubleCompareTo(double value1, double value2) {
		return NumberUtils.doubleCompareTo(value1, value2);
	}
	
	protected static int longCompareTo(long value1, long value2) {
		return NumberUtils.longCompareTo(value1, value2);
	}

	
	public boolean equals(Object obj) {
		boolean b = compareTo((TimeSeriesPoint) obj) == 0;
		if(b) {
			return b;
		} else {
			return b;
		}
	}
	
	public static void sortByMagnitudeChange(List<TimeSeriesPoint> list) {
		Collections.sort(list, MAGNITUDE_CHANGE_COMPARATOR);
	}
	
	public int compareTo(TimeSeriesPoint tsd) {
				
		assert tsd != null : "null compareTo obj.";	
//		assert (obj instanceof TimeSeriesPoint) : "not a TimeSeriesPoint obj. : " + obj.getClass();		
//		TimeSeriesPoint tsd = (TimeSeriesPoint) obj;
		
		int i = dateCompareTo(getDate(), tsd.getDate());
		
		if(i == 0) {
			i = doubleCompareTo(getValue(), tsd.getValue());
		}
		
		return i;
	}

	@Override
	public String toString() { return getDate().toString() + " " + getValue(); }


    public final byte getCombinationStategy() {
        return combinationStategy;
    }
    
    public void setCombinationStategy(byte combinationStategy) {
        this.combinationStategy = combinationStategy;
    }

	public String getObjectIdString() {
		return super.toString();
	}

	public TimeSeriesPoint multiplyBy(double d2) {
		TimeSeriesPoint point = copy();
		point.setValue(getValue()*d2);
		return point;
	}

	public TimeSeriesPoint abs() {
		TimeSeriesPoint point = copy();
		point.setValue(Math.abs(getValue()));
		return point;
	}

	public TimeSeriesPoint signonly() {
		TimeSeriesPoint point = copy();
		point.setValue(Math.signum(getValue()));
		return point;
	}
	
	
	public TimeSeriesPoint positiveValueOnly() {
		TimeSeriesPoint point = copy();
		double value = getValue();
		if(value < 0) {
			value = 0;
		}
		point.setValue(value);
		return point;
	}

	public TimeSeriesPoint negativeValueOnly() {
		TimeSeriesPoint point = copy();
		double value = getValue();
		if(value > 0) {
			value = 0;
		}
		point.setValue(value);
		return point;
	}

	public TimeSeriesPoint addToValue(double addValue) {
		TimeSeriesPoint point = copy();
		double value = getValue() + addValue;
		point.setValue(value);
		return point;
	}

	public TimeSeriesPoint divideByValue(double divisor) {
		TimeSeriesPoint point = copy();
		double value = getValue()/divisor;
		point.setValue(value);
		return point;
	}

	
	public TimeSeriesPoint reverseSigns() {
		TimeSeriesPoint point = copy();
		double value = getValue();
		point.setValue(-value);
		return point;
	}

	public static TimeSeriesPoint getWrappedPoint(TimeSeriesPoint point) {
		if(point instanceof PointWrapper) {
			return getWrappedPoint(((PointWrapper) point).getWrapped());
		} else {
			return point;
		}
	}
	

}
