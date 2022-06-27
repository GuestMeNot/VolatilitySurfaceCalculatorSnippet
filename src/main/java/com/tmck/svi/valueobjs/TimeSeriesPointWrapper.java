package com.tmck.svi.valueobjs;

import com.tmck.svi.utils.FastDate;

/**
 * A TimeSeriesPoint object that holds another TimeSeriesPoint object.
 */
public class TimeSeriesPointWrapper extends TimeSeriesPoint implements PointWrapper {

    public static final String SYMBOL_NAME = "WrappedData";

    public static final String ROOT_SYMBOL_NAME = "RootData";

    private TimeSeriesPoint wrapped = null;

    /**
     * Constructor for TimeSeriesPointWrapper
     */
    public TimeSeriesPointWrapper(FastDate d, double v, TimeSeriesPoint wrapped) {

        super(d, v);
        setWrapped(wrapped);

    }

    public TimeSeriesPointWrapper(TimeSeriesPoint wrapped, double v) {
        this(wrapped.getDate(), v, wrapped);
    }

    @Override
    public void setCombinationStrategy(byte combinationStategy) {
        super.setCombinationStrategy(combinationStategy);
        getWrapped().setCombinationStrategy(combinationStategy);
    }

    public TimeSeriesPoint getWrapped() {
        return wrapped;
    }

    protected void setWrapped(TimeSeriesPoint wrapped) {
        this.wrapped = wrapped;
    }

    public TimeSeriesPoint getRoot() {

        TimeSeriesPoint data = this;
        TimeSeriesPoint prevData = data;
        while (data instanceof TimeSeriesPointWrapper) {
            prevData = data;
            data = ((TimeSeriesPointWrapper) data).getWrapped();
        }

        data = (data == null) ? prevData : data;

        return data;
    }

    @Override
    public String toString() {
        return getName() + "(" + getValue() + "):" + wrapped.toString();
    }

    public String getName() {
        return getClass().getSimpleName();
    }


    public TimeSeriesPoint combineRootData(TimeSeriesPoint[] data, int start, int end) {

        assert checkClass(data, start, end);

        TimeSeriesPoint[] rootData = new TimeSeriesPoint[end - start];

        for (int i = start; i < end; i++) {
            TimeSeriesPointWrapper wrapper = (TimeSeriesPointWrapper) data[i];
            rootData[i] = wrapper.getRoot();
        }

        return rootData[start].combine(rootData, start, end);

    }

    @Override
    public TimeSeriesPoint copy() {

        TimeSeriesPoint wrapped = getWrapped();
        FastDate d = getDate();
        double value = getValue();

        TimeSeriesPoint wrappedCopy = wrapped.copy();
        FastDate dateCopy = d.copy();

        return create(dateCopy, value, wrappedCopy);
    }

    public TimeSeriesPointWrapper create(FastDate d, double v, TimeSeriesPoint wrapped) {
        return new TimeSeriesPointWrapper(d, v, wrapped);
    }

}
