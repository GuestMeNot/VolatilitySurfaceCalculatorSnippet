package com.tmck.svi.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class NumberUtils {

	private static final int POW10[] = { 1, 10, 100, 1000, 10000, 100000, 1000000 };

	public static String format(double val, int precision) {
		StringBuilder sb = new StringBuilder();
		return format(sb, val, precision).toString();
	}

	public static StringBuilder format(StringBuilder sb, double val, int precision) {
		if (val < 0) {
			sb.append('-');
			val = -val;
		}
		int exp = POW10[precision];
		long lval = (long) (val * exp + 0.5);
		sb.append(lval / exp).append('.');
		long fval = lval % exp;
		for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
			sb.append('0');
		}
		sb.append(fval);
		return sb;
	}

	public static double extractDollarAmount(String line) {
		int firstDigit = indexOfMonetaryAmount(line);
		if (firstDigit < 0) {
			throw new IllegalArgumentException("no currency amount in line: " + line);
		}

		int lastDigit = StringUtils.indexOfWhitespace(line, firstDigit);
		String amount = line.substring(firstDigit, lastDigit);
		try {
			return formatCurrency(amount);
		} catch (ParseException e) {
			throw new IllegalStateException("no currency amount found in line:" + line);
		}
	}

	public static double formatCurrency(String amount) throws ParseException {

		NumberFormat f = NumberFormat.getInstance();
		if (f instanceof DecimalFormat) {
			((DecimalFormat) f).setDecimalSeparatorAlwaysShown(true);
		}

		Number number = f.parse(amount);
		return number.doubleValue();
	}

	public static int indexOfMonetaryAmount(String line) {
		int indexOf = indexOfDigit(line);
		return StringUtils.newIndexOf(line, '(', indexOf);
	}

	public static int indexOfDigit(String line) {
		int indexOf = line.indexOf('1');

		indexOf = StringUtils.newIndexOf(line, '2', indexOf);
		indexOf = StringUtils.newIndexOf(line, '3', indexOf);
		indexOf = StringUtils.newIndexOf(line, '4', indexOf);
		indexOf = StringUtils.newIndexOf(line, '5', indexOf);
		indexOf = StringUtils.newIndexOf(line, '6', indexOf);
		indexOf = StringUtils.newIndexOf(line, '7', indexOf);
		indexOf = StringUtils.newIndexOf(line, '8', indexOf);
		indexOf = StringUtils.newIndexOf(line, '9', indexOf);
		indexOf = StringUtils.newIndexOf(line, '0', indexOf);

		return indexOf;
	}

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

	public static double sum(double[] d, int len) {
		return sum(d, 0, len);
	}

	public static double sum(double[] d) {
		assert d != null : "no array.";
		return sum(d, d.length);
	}

	public static double sumOfProducts(double[] d1, double[] d2, int xStart, int yStart, int len) {

		assert d1 != null : "no d1 array.";
		assert (xStart >= 0) : "bad xStart: cannot be negative" + xStart;
		assert (xStart <= d1.length) : "bad xStart index:max allowed(" + d1.length + ")" + xStart;
		if (len < 0 || (xStart + len) > d1.length)
			throw new IllegalArgumentException(
					"bad X length index:max allowed(" + d1.length + "): len:" + len + ": xStart:" + xStart);

		if (d2 == null)
			throw new NullPointerException("no d2 array.");
		if (yStart < 0 || yStart > d2.length)
			throw new IllegalArgumentException("bad yStart index:max allowed(" + d2.length + ")" + yStart);
		if (len < 0 || (yStart + len) > d2.length)
			throw new IllegalArgumentException(
					"bad Y length index:max allowed(" + d2.length + "): len:" + len + ": yStart:" + yStart);

		double sum = 0;

		for (int i = 0; i < len; i++) {
			sum += (d1[i + xStart] * d2[i + yStart]);
		}

		return sum;
	}

	/**
	 * @returns a random int between 0 and upperBound. It will return a value lower
	 *          than upperBound.
	 */
	public static int getRandom(int upperBound) {
		double rand = Math.random();
		return convertDoubleToInt(rand * upperBound);
	}

	public static boolean isDigit(char c) {
		return Character.isDigit(c);
	}

	public static double[] sort(double[] data) {
		ArrayList<Double> list = new ArrayList<Double>(data.length);

		for (int i = 0; i < data.length; i++) {
			list.add(data[i]);
		}

		Collections.sort(list);

		double[] d = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			Double obj = (Double) list.get(i);
			d[i] = obj.doubleValue();
		}

		return d;
	}

	public static boolean isUnknownDecimalValue(String decimal) {

		return decimal == null || decimal.equals("?") || decimal.equals("NaN") || decimal.equals("Infinity")
				|| decimal.equals("-Infinity");
	}

	/**
	 * @param comparedValue the value to compare to the targetValue
	 * @param targetValue   the value which is expected.
	 * @param tolerance     the allowed error.
	 * @return true if the comparedValue matches the targetValue within the
	 *         tolerance.
	 */
	public static boolean equals(double comparedValue, double targetValue, double tolerance) {
		double diff = comparedValue - targetValue;
		tolerance = (tolerance > 0) ? tolerance : -tolerance;
		return diff < tolerance && diff > -tolerance;
	}

	/**
	 * For now this is a simple cast to int but in the future this cast may not work
	 * properly and we don't want the cast throughout the code.
	 * 
	 * Maybe this is overkill.
	 * 
	 * @param d
	 * @return
	 */
	public static int convertDoubleToInt(double d) {
		return (int) d;
	}

	/**
	 * @param arr
	 * @return
	 */
	public static double[] copy(double[] arr) {
		double[] copy = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			copy[i] = arr[i];
		}
		return copy;
	}

	public static double[] shuffle(double[] arr) {

		double[] a = copy(arr);
		int n = a.length;
		Random random = new Random();
		random.nextInt();

		for (int i = 0; i < n; i++) {
			int change = i + random.nextInt(n - i);
			swap(a, i, change);
		}

		return a;

	}

	private static void swap(double[] a, int i, int change) {

		double helper = a[i];
		a[i] = a[change];
		a[change] = helper;
	}

	public static double max(double[] values) {
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
			}
		}
		return max;
	}

	public static double min(double[] values) {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] < min) {
				min = values[i];
			}
		}
		return min;
	}

	public static int min(int[] values) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] < min) {
				min = values[i];
			}
		}
		return min;
	}

	public static int max(int[] values) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
			}
		}
		return max;
	}

	public static double[] convertToArray(List<Double> list) {
		double[] d = new double[list.size()];
		for (int i = 0; i < list.size(); i++) {
			d[i] = list.get(i);
		}
		return d;
	}

	public static int[] convertToArray(List<Integer> list, int ii) {
		int[] d = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			d[i] = list.get(i);
		}
		return d;
	}

	public static int[] convertToArray(Collection<Integer> set) {
		int[] d = new int[set.size()];
		Iterator<Integer> itr = set.iterator();
		int i = 0;
		while (itr.hasNext()) {
			d[i] = itr.next();
			i++;
		}
		return d;
	}

	public static double[] convertToDoubleArray(Collection<Double> set) {
		double[] d = new double[set.size()];
		Iterator<Double> itr = set.iterator();
		int i = 0;
		while (itr.hasNext()) {
			d[i] = itr.next();
			i++;
		}
		return d;
	}

	public static List<Integer> convertToList(int[] array) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
		return list;
	}

	public static List<Double> convertToList(double[] array) {
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
		return list;
	}

	/**
	 * Divide two integers, rounding towards -Inf and returning quotient and
	 * remainder.
	 *
	 * @param n the numerator
	 * @param d the denominator
	 * @return the quotient and remainder
	 * @throws ArithmeticException if <code>d == 0</code>
	 */
	public static long[] div(long n, long d) {
		long q = n / d;
		long r = n % d;
		// n == q * d + r == (q - 1) * d + d + r
		if (r < 0) {
			q--;
			r += d;
		}
		return new long[] { q, r, };
	}

	public static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
