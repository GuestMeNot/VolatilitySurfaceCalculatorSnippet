package com.tmck.svi.valueobjs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tmck.svi.utils.FastDate;
import com.tmck.svi.utils.NumberUtils;

/**
 * Holds Multiple TimeSeries. Ideally These Points should be 
 * related in some manner such as for the same Symbol on the 
 * same Date, but this is not strictly mandatory.
 * 
 * @author tim
 *
 */
public abstract class TimeSeriesPointGroup extends TimeSeriesPoint {

	private TimeSeriesPoint defaultPoint = null;
	private String defaultPointName = null;
	private Map<String, TimeSeriesPoint> points = null;
	private boolean displayNames = false; 
	private String [] orderedNames = null; 

    private DecimalFormat format = null;
	private boolean debugging = false;

	/** Create TimeSeriesPointGroup that holds the minimum number of elements. */
	public TimeSeriesPointGroup(TimeSeriesPoint defaultPoint, String defaultPointName, int maxNumPoints) {
		this(defaultPoint, defaultPointName);
		// Create a map that holds the minimum number of elements.
		points = new HashMap<String, TimeSeriesPoint>(maxNumPoints, 1);
	}
	
	public TimeSeriesPointGroup(TimeSeriesPoint defaultPoint, String defaultPointName) { 
		this(defaultPoint.getDate(), defaultPoint.getValue()); 
		setDefaultPoint(defaultPoint);
		setDefaultPointName(defaultPointName);
	}

	
	/**
     * Needed by PriceData for Speed.
     */
    protected TimeSeriesPointGroup(FastDate date, double value) {
        super(date, value);
    }


    public TimeSeriesPoint getDefaultPoint() { return defaultPoint; }
	public String getDefaultPointName() { return defaultPointName; }



	public String [] getNames() {
		String [] names = new String[size()];
        Map<String, TimeSeriesPoint> map = getMap();
        
        int i = 0;
		if(defaultPointName != null) {
            names[0] = defaultPointName;
            i = 1;
		}
        
		Iterator<String> itr = map.keySet().iterator();
		
        try {
    		while(itr.hasNext()) {
    			names[i] = itr.next();
    			i++;
    		}
        } catch(Exception e) {
            throw new IllegalStateException("bad array index: size(): " + size() + " i: " + i);
        }
		
		return names;
	}
	
	public TimeSeriesPoint get(String name) {
		return getInternal(name);
	}

	// Do not override this method or it could cause infinite recursion in PriceData.
	private final TimeSeriesPoint getInternal(String name) {
		if(isDefault(name)) {
			return getDefaultPoint();
		}
		Map<String, TimeSeriesPoint> points = getMap();
		return points.get(name);
	}


	protected Map<String, TimeSeriesPoint> getMap() {
		if(points == null) {
			points = new HashMap<String, TimeSeriesPoint>(); 
		}
		return points; 
	}

	public double getValue(String name) {
		Double value = getValueWithoutException(name);
		if(value == null) {
			FastDate date = getDate();
			throw new IllegalArgumentException("name not found: " + name + "  " + date);
		}
		return value;
	}

	public Double getValueWithoutException(String name) {
		TimeSeriesPoint point = getInternal(name);
		if(point == null) { 
			return null;
		}
		return point.getValue();
	}

    public int getValueAsInt(String name) { 
        TimeSeriesPoint point = getInternal(name);
        if(point == null) { 
            throw new IllegalArgumentException("name not found: " + name);
        }        
        return NumberUtils.convertDoubleToInt(point.getValue()); 
    }
    
    
	public FastDate getDate(String name) {
		TimeSeriesPoint point = getInternal(name);
		if(point == null) { 
			throw new IllegalArgumentException("name not found: " + name);
		}
		return point.getDate();
	}
	
	public static TimeSeriesPointGroup getTimeSeriesPointGroupFromWrappedChain(TimeSeriesPoint point) {
		TimeSeriesPoint wrapped = null;
		while(point instanceof TimeSeriesPointWrapper) { 
			TimeSeriesPointWrapper wrapper = (TimeSeriesPointWrapper) point;
			wrapped = (TimeSeriesPoint) wrapper.getWrapped();
			point = wrapped;
		}
		if(point instanceof TimeSeriesPointGroup) {
			return (TimeSeriesPointGroup) point;
		}
		return null;		
	}


	public void setDate(FastDate date) { 
		super.setDate(date);
		String [] names = getNames();
		if(names == null || names.length == 0) {
			return;
		}
		for(int i = 0; i < names.length; i++) {
			if(names[i] == null) {
				continue;	// needed for use in the constructor.
			}
			TimeSeriesPoint point = getInternal(names[i]);
			if(point == null) { 
				throw new IllegalArgumentException("name not found: " + names[i]);
			}			
			point.setDate(date);	
		}
	}

	
	protected void setValue(String name, double value) { 
		FastDate d = getDate();
		setValue(name, d, value);
	}
	
	protected void setValue(String name, FastDate d, double value) {

		// NOTE: Don't use contains(name) since will slow things down significantly.
		// NOTE: Don't use get(name) since it will cause infinite recursion.
		TimeSeriesPoint point = getInternal(name);
		if(point != null) {
			FastDate tspDate = point.getDate(); 
			if(!tspDate.equals(d)) {
				throw new IllegalArgumentException("Date for value has changed: " + name + " original: " + tspDate + " new: " + d);
			}
			point.setValue(value); 
		} else {
			add(new TimeSeriesPoint(d, value), name);
		}
	}

	protected void setDefaultPoint(TimeSeriesPoint point) { defaultPoint = point; }
	protected void setDefaultPointName(String string) { defaultPointName = string; }

	public boolean isDefault(String name) {
		if(name == null) {
			throw new IllegalArgumentException();
		}
		if(defaultPointName != null && name.equals(defaultPointName)) {
			return true;
		}
		return false;
	}
		
	public final boolean contains(String name) {
		
		// do not override for subclasses. PriceData needs this as is 
		// without pretend points for adjusted data.
		
		if(isDefault(name)) return true;
		Map<String, TimeSeriesPoint> points = getMap();
		return points.containsKey(name);
	}

	public int size() {
		int size = 1;
		if(defaultPoint == null) {
			size--;		
		}
		size += getMap().size(); 
		return  size;
	}

	public void add(TimeSeriesPoint point, String name) {

        point.setCombinationStategy(getCombinationStrategy(name));
        addInternal(point, name);
		
	}
	
	protected void addInternal(TimeSeriesPoint point, String name) {
		Map<String, TimeSeriesPoint> points = getMap();
		points.put(name, point);
	}

    protected byte getCombinationStrategy(String name) {
        return COMBINE_USING_LATEST_VALUE;
    }

    @Override
	public TimeSeriesPoint copy() {
		TimeSeriesPoint point = getDefaultPoint();
		TimeSeriesPoint copy = point.copy();
		TimeSeriesPointGroup newPoint = createPointGroup(copy);
		String [] names = getNames();
		for(String name : names) {
			if(newPoint.contains(name)) {
				continue;
			}
			point = getInternal(name);
			TimeSeriesPoint copy2 = point.copy();
			newPoint.add(copy2, name);
		}
		return newPoint;
	}
	
	public abstract TimeSeriesPointGroup createPointGroup(TimeSeriesPoint defaultPoint);
	
	public String toString() {
        return toString(isDisplayNames());
	}

    public String toString(boolean displayNames) {
        TimeSeriesPoint defaultPoint = getDefaultPoint();
        String defaultName = getDefaultPointName();
        
        String str = "";
        if(displayNames) {
            str += defaultName + " ";
        }
        
        str += defaultPoint.toString();
        String [] names = getNames();
        for(int i = 0; i < names.length; i++) {
            if(defaultName.equals(names[i]))
                continue;
            if(displayNames) {
                str += " " + names[i];
            }
            str += " " + getInternal(names[i]).getValue();
        }
        return str;
    }
    
	public boolean isDisplayNames() { return displayNames; }

	public void setDisplayNames(boolean displayNames) { this.displayNames = displayNames; }


	/**
	 * @return names ordered for external storage (e.g. a file).
	 */
	public String[] getOrderedNames() {
		if(orderedNames != null) {
			return orderedNames;
		}
		return getNames(); 
	}
	public void setOrderedNames(String[] names) { orderedNames = names; }


	public void put(Map<String, TimeSeriesPoint> points) {
		Iterator<String> itr = points.keySet().iterator();
		while(itr.hasNext()) {
			String name = itr.next();
			if(isDefault(name)) {
				continue;
			}
			TimeSeriesPoint point = points.get(name);
			add(point, name);
		}
	}
	
	public void merge(TimeSeriesPointGroup data) {
		String [] names = data.getNames();
		for(String name : names) {
			if(contains(name)) {
				continue;
			}
			TimeSeriesPoint point = data.get(name);
			add(point, name);
		}
	}


    public boolean hasPoint(String name) {
        return get(name) != null;
    }


    protected String getString(String name) {
        
        if(format == null) {
            format = new DecimalFormat("###,###,###,###,###.##");
        }

        if(hasPoint(name)) {
            return "(" + name + " : " + format.format(getValue(name)) + ")";
        }
        
        return "";
    }
	
    public final TimeSeriesPointGroup createNew(TimeSeriesPoint defaultPoint) {
        return null;
    }
    
    public TimeSeriesPoint combine(TimeSeriesPoint[] data, int start, int end) {

        List<TimeSeriesPoint> defaultPointList = new ArrayList<TimeSeriesPoint>(data.length);
        for(TimeSeriesPoint datum : data) {
            TimeSeriesPointGroup groupDatum = (TimeSeriesPointGroup) datum;
            if(groupDatum == null) {
                System.out.println("ERROR null while combining data");
                continue;
            }
            defaultPointList.add(groupDatum.getDefaultPoint());
        }
        
        TimeSeriesPoint[] defaultPoints = defaultPointList.toArray(new TimeSeriesPoint[defaultPointList.size()]);
        
        TimeSeriesPoint combinedDefaultPoint = defaultPoints[0].combine(defaultPoints, start, end);

        TimeSeriesPointGroup combinedPointGroup = createPointGroup(combinedDefaultPoint);

        
        String[] names = getNames();
        for(String name : names) {
            
            if(isDefault(name)) {
                continue;
            }
            
            List<TimeSeriesPoint> pointList = new ArrayList<TimeSeriesPoint>(data.length);
            for(TimeSeriesPoint datum : data) {
                TimeSeriesPointGroup groupDatum = (TimeSeriesPointGroup) datum;
                if(datum == null) {
                    System.out.println("ERROR null while combining data");
                    continue;
                }
                pointList.add(groupDatum.get(name));
            }
            
            TimeSeriesPoint[] dataPoints = pointList.toArray(new TimeSeriesPoint[pointList.size()]);
            
            TimeSeriesPoint combinedPoint = dataPoints[0].combine(dataPoints, start, end);
            
            combinedPointGroup.add(combinedPoint, name);
            
        }

        return combinedPointGroup;
    }
    
    public void setCombinationStategy(byte combinationStategy) {
        
        super.setCombinationStategy(combinationStategy);
        TimeSeriesPoint defaultPoint = getDefaultPoint();
        if(defaultPoint != null) {
            defaultPoint.setCombinationStategy(combinationStategy);
        }
        
        Map<String, TimeSeriesPoint> map = getMap();
        for(TimeSeriesPoint point : map.values()) {
            point.setCombinationStategy(combinationStategy);
        }
        
    }

	public boolean isDebugging() {
		return debugging;
	}

	public void setDebugging(boolean debugging) {
		this.debugging = debugging;
	}

	public static TimeSeriesPoint getPointByName(TimeSeriesPoint point, String pointName) {
		if(point instanceof TimeSeriesPointGroup) {
			return ((TimeSeriesPointGroup) point).get(pointName);
		} else {
			throw new IllegalStateException();
		}		
	}
	
	public static void setValues(TimeSeriesPointGroup[] data, String name, double value) {
		for(TimeSeriesPointGroup datum : data) {
			datum.setValue(name, value);
		}
	}

	public static boolean containsValues(TimeSeriesPointGroup[] data, String name) {
		for(TimeSeriesPointGroup datum : data) {
			if(!datum.contains(name)) {
				return false;
			}
		}
		return true;
	}

	public static double getChangeInValue(String name, TimeSeriesPointGroup newer, TimeSeriesPointGroup older) {
		double newerVal = newer.getValue(name);
		double olderVal = older.getValue(name);
		double diff = olderVal - newerVal;
		return diff;
	}

}
