package com.tmck.svi.utils;


/**
 * Holds more advanced (but still common) statistics routines. 
 * 
 * @author tim
 *
 */
public class StatisticsUtils {

	public static double correlate(double [] data1, double [] data2, int len) {
	
		assert data1 != null : "no data1.";
		assert data2 != null : "no data2.";
		
		double [] x = data1;
		double [] y = data2;
		int n = len;
		
		double yt=0.0, xt=0.0, syy=0.0, sxy=0.0, sxx=0.0, ay=0.0, ax=0.0;
				
		boolean allZeroXs = true;
		boolean allZeroYs = true;
		
		for(int j = 0; j < n; j++) {
			
			if(Double.isNaN(x[j])) {
				throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for x[" + j + "]");
			}			
			if(Double.isNaN(y[j])) {
				throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for y[" + j + "]");
			}
			
			ax += x[j];
			ay += y[j];
			
			if(allZeroXs && x[j] != 0.0) {
				allZeroXs = false;
			}
			
			if(allZeroYs && y[j] != 0.0) {
				allZeroYs = false;
			}
		}
		
		if(allZeroXs) {
			return 0.0;
//			throw new IllegalArgumentException("StatisticsUtils.correlate(): Xs are all zero: so sxx will be become zero.");
		}			

		if(allZeroYs) {
			return 0.0;
//			throw new IllegalArgumentException("StatisticsUtils.correlate(): Ys are all zero: so syy will be become zero.");
		}			
		
		ax /= n;
		ay /= n;
		
		for(int j = 0;  j < n; j++) {
			xt=x[j]-ax;
			if(Double.isNaN(xt)) {
				throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for xt[" + j + "]");
			}			
			yt=y[j]-ay;
			if(Double.isNaN(yt)) {
				throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for yt[" + j + "]");
			}			
			sxx += xt*xt;
			if(Double.isNaN(sxx)) {
				throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for sxx[" + j + "]");
			}			
			syy += yt*yt;
			if(Double.isNaN(syy)) {
				throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for syy[" + j + "]");
			}			
			sxy += xt*yt;
			if(Double.isNaN(sxy)) {
				throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for sxy[" + j + "]");
			}			
		}

		if(sxx == 0.0 || syy == 0.0) {
			return 0.0;
//			throw new IllegalArgumentException("StatisticsUtils.correlate(): neither number should be zero: sxx: " + sxx + " syy:" + syy);
		}			
		
		double sqrt = Math.sqrt(sxx*syy);
		if(Double.isNaN(sqrt)) {
			throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for sqrt");
		}			

		double correl = sxy/sqrt;
		if(Double.isNaN(correl)) {
			throw new IllegalArgumentException("StatisticsUtils.correlate(): is NaN for sxy/sqrt : sxy: " + sxy + " sqrt:" + sqrt);
		}			
		
		return correl;
		
	}
	
	public static double correlate(double [] data1, double [] data2) {
	
		assert data1 != null : "no data1.";
		assert data2 != null : "no data2.";
		
		int len = data1.length;
		
		if(data1.length > data2.length) {
			len = data2.length;
		}
		
		return correlate(data1, data2, len);
	}		


	public static double meanAbsoluteError(double[] residuals) {
		double sum = 0.0d;
		for(double d : residuals) {
			sum += Math.abs(d);
		}
		return sum/(1.0*residuals.length);
	}

	public static double rootMeanSquaredError(double[] residuals) {
		double sum = 0.0d;
		for(double d : residuals) {
			sum += d*d;
		}		
		return Math.sqrt(sum/(1.0*residuals.length));
	}

	public static double min(double[] values) {
		double min = Double.MAX_VALUE;
		for(int i = 0; i < values.length; i++) {
			if(values[i] < min) {
				min = values[i];
			}
		}
		return min;
	}

	public static double max(double[] values) {
		double max = -Double.MAX_VALUE;
		for(int i = 0; i < values.length; i++) {
			if(values[i] > max) {
				max = values[i];
			}
		}
		return max;
	}

	public static double sum(double[] values) {
		double sum = 0;
		for(int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		return sum;
	}

	public static double[] reverseSigns(double[] values) {
		
		if(values == null) {
			return null;
		}
		
		double[] reversed = new double[values.length];
		for(int i = 0; i < values.length; i++) {
			reversed[i] = -values[i];
		}

		return reversed;
		
	}

	public static double[] convertToDoubleArray(int[] values) {
		
		double[] d = new double[values.length];
		for(int i = 0; i < values.length; i++) {
			d[i] = values[i];
		}
		
		return d;
		
	}

	public static double semicovariance(double[] values1, double[] values2, int length, double cutoff, boolean upperSemiVar, boolean corrected) {
        double sumsq = 0.0;
        for (int i = 0; i < length; i++) {
        	double val1 = values1[i];
        	double val2 = values2[i];
            if ((val1 > cutoff) == upperSemiVar && (val2 > cutoff) == upperSemiVar) {
               double dev1 = val1 - cutoff;
               double dev2 = val2 - cutoff;
               sumsq += dev1 * dev2;
            }
        }

        if (corrected) {
            return sumsq / (length - 1.0);
        } else {
            return sumsq / length;
        }
    }

}
