package com.tmck.svi.utils;

public final class SimpleStatisticsUtils {

    private SimpleStatisticsUtils() {
    }

    public static double sum(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }

    public static double getMean(double[] d) {
        if (d == null) {
            throw new IllegalArgumentException();
        }
        return getMean(d, d.length);
    }

    public static double getMean(double[] d, int len) {
        return getMean(d, 0, len);
    }

    public static double getMean(double[] d, int start, int end) {
        double sum = NumberUtils.sum(d, start, end);
        return sum / (end - start);
    }

    public static double getVariance(double[] d, int start, int end) {

        assert d != null : "no array.";

        double mean = getMean(d, start, end);
        double sumSqrs = 0;

        double n = 0;
        for (int i = start; i < end; i++) {
            double distance = d[i] - mean; // Euclidian distance from mean.
            sumSqrs += (distance * distance); // square to eliminate negative signs.
            n++;
        }

        double unbiasedN = (n - 1.0); // n - 1 is an unbiased estimator of population.
        return sumSqrs / unbiasedN;
    }

    public static double getVariance(double[] d, int len) {
        return getVariance(d, 0, len);
    }

    public static double getVariance(double[] d) {
        assert d != null : "no array.";
        return getVariance(d, d.length);
    }

    public static double getStandardDeviation(double[] d) {
        assert d != null : "no array.";
        return getStandardDeviation(d, d.length);
    }

    public static double getStandardDeviation(double[] d, int len) {
        return getStandardDeviation(d, 0, len);
    }

    public static double getStandardDeviation(double[] d, int start, int end) {
        double var = getVariance(d, start, end);
        return Math.sqrt(var);
    }

    public static double getStandardErrorOfMean(double[] data) {
        double stddev = getStandardDeviation(data);
        return stddev / Math.sqrt(data.length);
    }

    public static double sumOfSquaredResiduals(double[] d1, double[] d2) {
        double sumSq = 0;
        for (int i = 0; i < d1.length; i++) {
            double residual = d1[i] - d2[i];
            double residualResidual = residual * residual;
            sumSq += residualResidual;
        }
        return sumSq;
    }

    private static double[] getResiduals(double[] d1, double[] d2) {
        double[] residuals = new double[d1.length];
        for (int i = 0; i < d1.length; i++) {
            double residual = d1[i] - d2[i];
            residuals[i] = residual;
        }
        return residuals;
    }

    public static double getStandardErrorOfResiduals(double[] d1, double[] d2) {
        double[] residuals = getResiduals(d1, d2);
        return getStandardErrorOfMean(residuals);
    }

}
