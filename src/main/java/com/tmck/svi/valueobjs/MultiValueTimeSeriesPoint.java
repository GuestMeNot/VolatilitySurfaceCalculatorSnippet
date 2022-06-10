package com.tmck.svi.valueobjs;


/**
 * Corresponds for a TimeSeries that has multiple values.
 * An example would be Price Data for a stock which has 
 * a high, low, open and close price. Each of these (such as high) 
 * can be thought of as a separate TimeSeries or as part of a larger 
 * logical unit (in this case Price Data). 
 * 
 * @author tim
 *
 */
public abstract class MultiValueTimeSeriesPoint extends TimeSeriesPointGroup implements TimeSeriesPointGroupFactory {

	public MultiValueTimeSeriesPoint(TimeSeriesPoint defaultPoint, String defaultPointName) { 
		super(defaultPoint, defaultPointName); 
	}
	
	/** Create MultiValueTimeSeriesPoint that holds the minimum number of elements. */
	public MultiValueTimeSeriesPoint(TimeSeriesPoint defaultPoint, String defaultPointName, int maxNumPoints) {
		super(defaultPoint, defaultPointName, maxNumPoints); 
	}

}
