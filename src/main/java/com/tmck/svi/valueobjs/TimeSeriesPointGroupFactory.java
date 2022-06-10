package com.tmck.svi.valueobjs;

/**
 * @author tim
 *
 */
public interface TimeSeriesPointGroupFactory {
	public abstract TimeSeriesPointGroup createPointGroup(TimeSeriesPoint defaultPoint);
}
