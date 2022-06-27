package com.tmck.svi.valueobjs;

/**
 * @author tim
 */
public interface TimeSeriesPointGroupFactory {
    TimeSeriesPointGroup createPointGroup(TimeSeriesPoint defaultPoint);
}
