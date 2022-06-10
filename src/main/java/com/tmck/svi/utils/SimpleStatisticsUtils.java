package com.tmck.svi.utils;

import java.util.Arrays;


public class SimpleStatisticsUtils {

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
		double variance = sumSqrs / unbiasedN;
		return variance;
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

	/**
	 * @param data data to add up.
	 * @return Double.NaN of data is null or cumulative value of the data.
	 * 
	 * @deprecated use NumberUtils.sum() instead.
	 */
	public static double getCumulativeValue(double[] data) {

		if (data == null) {
			return Double.NaN;
		}

		return NumberUtils.sum(data);
	}

	public static int getQuadrant(double[] values, double d) {
		double mean = getMean(values);
		double stddev = getStandardDeviation(values);
		if (d <= mean - stddev)
			return 1;
		if (d <= mean)
			return 2;
		if (d <= mean + stddev)
			return 3;
		return 4;
	}


	public static double getMin(double[] data) {

		double min = Double.MAX_VALUE;

		for (int i = 0; i < data.length; i++) {
			if (data[i] < min) {
				min = data[i];
			}
		}

		return min;
	}

	public static double getMax(double[] data) {

		double max = -Double.MAX_VALUE;

		for (int i = 0; i < data.length; i++) {
			if (data[i] > max) {
				max = data[i];
			}
		}

		return max;
	}

	public static int getMaxIndex(double[] data) {

		int idx = -1;
		double max = -Double.MAX_VALUE;

		for (int i = 0; i < data.length; i++) {
			if (data[i] > max) {
				max = data[i];
				idx = i;
			}
		}

		return idx;
	}

	/**
	 * @see A No-Arbitrage Approach to Range-Based Estimation of Return Covariances
	 *      and Correlations - Brandt and Diebold
	 */
	public static double getExtremeValueVariance(double[] data) {
		int ups = 0;
		int downs = 0;
		for (double d : data) {
			if (d > 0d) {
				ups++;
			} else if (d < 0) {
				downs++;
			}
		}

		double[] upData = new double[ups];
		double[] downData = new double[downs];
		int upcount = 0;
		int downcount = 0;
		for (double d : data) {
			if (d > 0d) {
				upData[upcount] = d;
				upcount++;
			} else if (d < 0) {
				downData[downcount] = d;
				downcount++;
			}
		}

		return getExtremeValueVariance(downData, upData);

	}

	/**
	 * @see The Extreme Value Method for Estimating the Variance of the Rate of
	 *      Return - Michael Parkinson
	 */
	public static double getExtremeValueVariance(double[] downData, double[] upData) {

		double sum = 0;

		for (int i = 0; i < downData.length; i++) {
			// extreme diffusion.
			double l = upData[i] - downData[i];
			double l2 = l * l;
			sum += l2;
		}

		return .361 * sum / downData.length;

	}

	/**
	 * Values of 2 standard errors of skewness (ses) or more (regardless of sign)
	 * are probably skewed to a significant degree.
	 * 
	 * @return the value for ses.
	 * @see Tabachnick, B. G., & Fidell, L. S. (1996). Using multivariate statistics
	 *      (3rd ed.)
	 */
	public static double getStandardErrorOfSkew(double[] data) {
		return Math.sqrt(6.0 / (1.0 * data.length));
	}

	/**
	 * Values of 2 standard errors of kurtosis (sek) or more (regardless of sign)
	 * are probably have excess kurtosis to a significant degree.
	 * 
	 * @return the value for sek.
	 * @see Tabachnick, B. G., & Fidell, L. S. (1996). Using multivariate statistics
	 *      (3rd ed.)
	 */
	public static double getStandardErrorOfKurtosis(double[] data) {
		return Math.sqrt(24.0 / (1.0 * data.length));
	}

	public static double getStandardErrorOfMean(double[] data) {
		double stddev = getStandardDeviation(data);
		return stddev / Math.sqrt(data.length);
	}

	public static double getStandardErrorOfMeanDownside(double[] data) {
		double stddev = getDownsideRisk(data);
		return stddev / Math.sqrt(data.length);
	}

	public static double getSortinoStderr(double[] data) {
		double errOfMean = getStandardErrorOfMeanDownside(data);
		double stddev = getDownsideRisk(data);
		return errOfMean / stddev;
	}

	public static double getSharpeStderr(double[] data) {
		double errOfMean = getStandardErrorOfMean(data);
		double stddev = getStandardDeviation(data);
		return errOfMean / stddev;
	}

	public static double getExtremeValueSimpleVariance(double[] data) {

		double sum = 0;
		for (int i = 0; i < data.length; i++) {
			// extreme diffusion.
			double l2 = data[i] * data[i];
			sum += l2;
		}

		return (sum * .361) / data.length;

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

	public static int getNumPointsWithLargeStddevMoves(double[] data, int length, double maxNumStddevs, int daysBack) {

		if (data == null || data.length == 0) {
			return 0;
		}

		double[] roc = getRoc(data);
		if (length > roc.length) {
			length = roc.length;
		}

		double stddev = getStandardDeviation(roc, length);
		double maxStddev = stddev * maxNumStddevs;

		return countGreaterThanAbsThreshold(roc, maxStddev, daysBack);

	}

	public static int getNumPointsWithLargeStddevMovesUp(double[] data, int length, double maxNumStddevs,
			int daysBack) {

		if (data == null || data.length == 0) {
			return 0;
		}

		double[] roc = getRoc(data);
		if (length > roc.length) {
			length = roc.length;
		}

		double stddev = getStandardDeviation(roc, length);
		double maxStddev = stddev * maxNumStddevs;

		return countGreaterThanThreshold(roc, maxStddev, daysBack);

	}

	public static int countGreaterThanAbsThreshold(double[] data, double max, int daysBack) {

		if (daysBack > data.length) {
			daysBack = data.length;
		}

		int count = 0;
		for (int i = 0; i < daysBack; i++) {
			double val = Math.abs(data[i]);
			if (val > max) {
				count++;
			}
		}

		return count;
	}

	public static int countGreaterThanThreshold(double[] data, double max, int daysBack) {

		if (daysBack > data.length) {
			daysBack = data.length;
		}

		int count = 0;
		for (int i = 0; i < daysBack; i++) {
			double val = data[i];
			if (val > max) {
				count++;
			}
		}

		return count;
	}

	public static double[] getRoc(double[] data) {

		double[] roc = new double[data.length - 1];
		for (int i = 0; i < data.length - 1; i++) {
			// Newer data is in the smaller indices.
			roc[i] = data[i] - data[i + 1];
		}

		return roc;
	}

	/** The rate of return above the expected rate of return. */
	public static double sharpeRatio(double[] roR) {
		double mean = getMean(roR);
		double stddev = getStandardDeviation(roR);

		return mean / stddev;
	}

	/** The rate of return above the expected rate of return. */
	public static double sortinoRatio(double[] roR) {
		return sortinoRatio(roR, 0);
	}

	public static double sortinoRatio(double[] roR, double targetRoR) {

		double mean = sortinoMean(roR, targetRoR);
		double stddev = getDownsideRisk(roR);
		if (stddev == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return mean / stddev;
	}

	public static double sortinoMean(double[] roR) {
		return sortinoMean(roR, 0);
	}

	public static double sortinoMean(double[] roR, double targetRoR) {
		return getMean(roR) - targetRoR;
	}
	
	public static double farinelliTibiletteRatio(double[] roR) {

		double mean = getUpsideReward(roR);
		double stddev = getDownsideRisk(roR);
		if (stddev == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return mean / stddev;
	}

	public static double momentumSortinoRatio(double[] roR, int momentumLen, double targetRoR) {
		try {

			momentumLen /= 2;

			if (momentumLen > roR.length) {
				return Double.NaN;
			}

			double[] momentumRor = Arrays.copyOf(roR, momentumLen);
			double mean = getMean(momentumRor) - targetRoR;
			double stddev = getDownsideRisk(roR);
			if (stddev == 0) {
				return Double.POSITIVE_INFINITY;
			}
			return mean / stddev;
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/** The rate of return above the expected rate of return. */
	public static double getDownsideRisk(double[] roR) {

		assert roR != null : "no array.";

		double sumSqrs = 0;
		double n = 0;
		for (int i = 0; i < roR.length; i++) {
			if (roR[i] <= 0) {
				double distance = roR[i]; // Euclidian distance from mean.
				sumSqrs += (distance * distance); // square to eliminate negative signs.
			}
			n++; // This is correct. See https://en.wikipedia.org/wiki/Downside_risk
		}

		if (n == 0) {
			return 0;
		}

		double variance = sumSqrs / n;
		return Math.sqrt(variance);

	}

	public static double getUpsideReward(double[] roR) {

		assert roR != null : "no array.";

		double sumSqrs = 0;
		double n = 0;
		for (int i = 0; i < roR.length; i++) {
			if (roR[i] > 0) {
				double distance = roR[i]; // Euclidian distance from mean.
				sumSqrs += (distance * distance); // square to eliminate negative signs.
			}
			n++; // This is correct. See https://en.wikipedia.org/wiki/Downside_risk
		}

		if (n == 0) {
			return 0;
		}

		double variance = sumSqrs / n;
		return Math.sqrt(variance);

	}

	public static double kellyCriteria(double[] roR) {
		double mean = getMean(roR);
		double stddev = getStandardDeviation(roR);

		return mean / (stddev * stddev);
	}

	public static double kellySortinoCriteria(double[] roR) {
		double mean = getMean(roR);
		double stddev = getDownsideRisk(roR);

		return mean / (stddev * stddev);
	}

	public static double cagr(double initialValue, double endingValue, double years) {
		return Math.pow((endingValue / initialValue), 1 / years) - 1d;
	}

	public static double getExtremeValueSimpleStddev(double[] data) {
		return Math.sqrt(getExtremeValueSimpleVariance(data));
	}

}
