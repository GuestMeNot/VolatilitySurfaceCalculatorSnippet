package com.tmck.svi.utils;

public final class NumberUtils {

    public static int doubleCompareTo(double value1, double value2) {

        double d = value1 - value2;

        if (d < 0)
            return -1;

        if (d > 0)
            return 1;

        return 0;
    }

    public static int longCompareTo(long value1, long value2) {

        long d = value1 - value2;

        if (d < 0)
            return -1;

        if (d > 0)
            return 1;

        return 0;
    }

    public static double sum(double[] d, int start, int len) {

        int arrLen = d.length;

        if (start < 0 || start > arrLen) {
            throw new IllegalArgumentException("bad start index:max allowed(" + arrLen + ")" + start);
        }

        if (len < 0 || len > arrLen) {
            throw new IllegalArgumentException("bad length index:max allowed(" + arrLen + ")" + len);
        }

        double sum = 0;

        for (int i = start; i < len; i++) {
            sum += d[i];
        }

        return sum;
    }

    /**
     * For now this is a simple cast to int but in the future this cast may not work
     * properly and we don't want the cast throughout the code.
     * <p>
     * Maybe this is overkill.
     */
    public static int convertDoubleToInt(double d) {
        return (int) d;
    }

}
