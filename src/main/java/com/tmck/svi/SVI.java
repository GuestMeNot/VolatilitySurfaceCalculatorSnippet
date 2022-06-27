package com.tmck.svi;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.MultivariateOptimizer;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.PowellOptimizer;

import com.tmck.svi.utils.FastDate;
import com.tmck.svi.utils.SimpleStatisticsUtils;
import com.tmck.svi.valueobjs.OptionData;


/**
 * Implements the SVI of Gatheral.
 *
 * @author GuestMeNot
 * @see "The Volatility Surface by Gatheral p. 36"
 * @see "Arbitrage-free SVI volatility Surfaces by Gatheral."
 */
public class SVI {


    private static final boolean debug = false;
    private double a, b, p, sigma, m = Double.NaN;
    private OptionData[] data;

    private double ssr = Double.MAX_VALUE;

    public SVI(OptionData[] data) {
        this.data = data;
        if (data.length < 3) {

            int dte = Integer.MAX_VALUE;
            String str = "";
            if (data.length > 0) {
                dte = (int) data[0].getDte();
                str = ":" + data[0].getDate() + " dte: " + dte + " root: " + data[0].getRoot();
            }

            if (dte < 200 && dte > 4) {
                System.err.println("WARNING: " + str + " ::: too little data for SVI!!!");
            }
        }

    }

    /**
     * @see "eq. 3.1 - p. 5"
     */
    public static double calculateVI(OptionData ref, double strike, double currentValue) {

        double w_t = calculateSVI(ref, strike, currentValue);

        if (Double.isInfinite(w_t) || w_t == Double.MAX_VALUE) {
            return Double.NaN;
        }

        double t = BlackScholes.getT(ref);

        return getIVFromWT(w_t, t);

    }

    /**
     * @see "p 6 of Quasi"
     */
    public static boolean isQuasySVI_OK(double a, double b, double p, double m, double sigma, double t, double[] w_t) {

        double c = getQuasiSVI_C(b, sigma, t);
        double d = getQuasiSVI_D(p, b, sigma, t);
        double absD = Math.abs(d);

        double sigma4 = 4 * sigma;

        if (c < 0 || c > sigma4) {
            return false;
        }

        return !(absD > c || absD > sigma4 - c);
    }

    public static double getQuasiSVI_AT(double a, double t) {
        return a * t;
    }

    public static double getQuasiSVI_C(double b, double sigma, double t) {
        return b * sigma * t;
    }

    public static double getQuasiSVI_D(double p, double b, double sigma, double t) {
        return p * b * sigma * t;
    }

    public static double getIVFromWT(double w_t, double t) {

        double ivSq = w_t / t;
        return Math.sqrt(ivSq);

    }

    /**
     * p.2 "of Gatheral."
     */
    public static double getWTFromIV(double iv, double t) {
        return iv * iv * t;
    }

    public static boolean parametersWithinTolerance(double a, double b, double p, double m, double sigma, double t) {

        // @see Friz, Peter and Gatheral, J. (2004), �Valuation of Volatility Derivatives as an Inverse Problem�
        if (a <= 0) {
            return false;
        }

        // @see Section 2 of Quasi-Explicit Calibration of Gatheral�s SVI model
        if (b <= 0) {
            return false;
        }

        // @see Section 2 of Quasi-Explicit Calibration of Gatheral�s SVI model
        if (sigma < 0) {
            return false;
        }

        // @see Section 2 of Quasi-Explicit Calibration of Gatheral�s SVI model
        if (p < -1 || p > 1) {
            return false;
        }

        // To Prevent Vertical Arbitrage:  @see Friz Gatheral 2004
        // @see Quasi-Explicit Calibration of Gatheral�s SVI model
        return !(b > (4d / ((1 + Math.abs(p)) * t)));
    }

    /**
     * Pass in the real strike and stock price. Calculate the log-strike and optimize SVI.
     */
    private static double calculateSVI(OptionData ref, double strike, double currentValue) {

        double a = ref.getValue(OptionData.SVI_A);
        double b = ref.getValue(OptionData.SVI_B);
        double p = ref.getValue(OptionData.SVI_P);
        double m = ref.getValue(OptionData.SVI_M);
        double sigma = ref.getValue(OptionData.SVI_SIGMA);

        double sigmasigma = sigma * sigma;

        double k = OptionData.getLogStrike(strike, currentValue);

        double t = BlackScholes.getT(ref);

        // We need to convert to Raw parameters.
        return calculateSVI(a * t, b * t, p, k, m, sigma, sigmasigma, t);

    }

    /**
     * @see "eq. 3.1 - p. 5"
     */
    public static double calculateSVI(double a, double b, double p, double k, double m, double sigma, double sigmasigma, double t) {


        if (!parametersWithinTolerance(a, b, p, m, sigma, t)) {
            return Double.MAX_VALUE;
        }

        double km = k - m;
        double kmkm = km * km;
        double sqrt = Math.sqrt(kmkm + sigmasigma);
        double pkm = p * km;
        double sqrtpkm = pkm + sqrt;
        double bsqrtpkm = b * sqrtpkm;

        // @see eq. 3.1 - p. 5
        double w_i = a + bsqrtpkm;

//		if(Math.abs(w_i) < .01d) {
//			return 0;
//		}

        if (w_i < 0) {
            return Double.MAX_VALUE;
        }

        if (Double.isInfinite(w_i)) {
            return Double.MAX_VALUE;
        }

        return w_i;

    }

    // @see Section 2.1 of Quasi-Explicit Calibration of Gatheral�s SVI model
    public static double getMinIVLogStrike(double m, double p, double sigma) {
        return m - (p * sigma / Math.sqrt(1 - p * p));
    }

    public static double getT(OptionData[] data) {
        return BlackScholes.getT(data[0]);
    }

    /**
     * @see "last paragraph - p. 6"
     */
    public static double getW_tJW(double v_t, double t) {
        return v_t * t;
    }

    public static double getSigma_JW(double alpha, double m_jw) {
        return alpha * m_jw;
    }

    /**
     * @see "eq. 3.5 - p. 6"
     */
    public static double getVT(double a, double b, double p, double m, double sigma, double t) {
        return (a + b * (-p * m + Math.sqrt(m * m + sigma * sigma))) / t;
    }

    /**
     * @see "eq. 3.5 - p. 6"
     */
    public static double getPhiT(double a, double b, double p, double m, double sigma, double w_t) {
        return (1d / Math.sqrt(w_t)) * (b / 2d) * ((-m / Math.sqrt(m * m + sigma * sigma)) + p);
    }

    /**
     * @see "eq. 3.5 - p. 6"
     */
    public static double getPT(double b, double p, double w_t) {
        return (b / Math.sqrt(w_t)) * (1 - p);
    }

    /**
     * @see "eq. 3.5 - p. 6"
     */
    public static double getCT(double b, double p, double w_t) {
        return (b / Math.sqrt(w_t)) * (1 + p);
    }

    /**
     * @see "eq. 3.5 - p. 6"
     */
    public static double getVMinT(double a, double b, double p, double sigma, double t) {
        return (a + b * sigma * Math.sqrt(1 - p * p)) / t;
    }

    /**
     * @see "page 19"
     */
    public static double getCTPrime(double p_t, double phi_t) {
        return p_t + 2d * phi_t;
    }

    /**
     * @see "page 19"
     */
    public static double getVMinTPrime(double p_t, double v_t, double ctPrime) {
        double ptCt = p_t + ctPrime;
        return v_t * (4d * p_t * ctPrime) / (ptCt * ptCt);
    }

    /**
     * @see "equations at the top of - p. 8"
     */
    public static double getB_JW(double w_t, double c_t, double p_t) {
        return (Math.sqrt(w_t) / 2d) * (c_t + p_t);
    }

    /**
     * @see "equations at the top of - p. 8"
     */
    public static double getP_JW(double w_t, double b_jw, double p_t) {
        return 1d - (p_t * Math.sqrt(w_t) / b_jw);
    }

    /**
     * @see "equations at the top of - p. 8"
     */
    public static double getA_JW(double vtMin, double b_jw, double p_jw, double sigma_jw, double t) {
        return vtMin * t - (b_jw * sigma_jw * Math.sqrt(1 - p_jw * p_jw));
    }

    /**
     * @see "equations at the top of - p. 8"
     */
    public static double getAlpha(double p_jw, double phiT, double w_t, double b_jw) {
        double beta = p_jw - (2d * phiT * Math.sqrt(w_t) / b_jw);
        return Math.signum(beta) * Math.sqrt((1d / (beta * beta)) - 1);
    }

    /**
     * @see "equations at the top of - p. 8"
     */
    public static double getM_JW(double vT, double vTMin, double t, double b_jw,
                                 double p_jw, double alpha) {

        return ((vT * vTMin) * t) / (b_jw * (-p_jw + (Math.signum(alpha) * Math.sqrt(alpha)) - (alpha * Math.sqrt(1 - p_jw * p_jw))));

    }

    public static double calculateIV(OptionData[] closestData, double currentStrike) {

        double[] ivs = OptionData.getIVs(closestData);
        double sum = SimpleStatisticsUtils.sum(ivs);
        if (sum > (closestData.length * .99)) {
            return 1d;
        }

        SVI svi = new SVI(closestData);
        svi.optimize();

        double w_i = svi.calculateSVI(currentStrike);
        double iv = getIVFromWT(w_i, getT(closestData));

        if (debug) {
            System.err.println("Calculating SVI for: datetime: " + closestData[0].getDate() + "  strike: " + currentStrike + "  IV: " + iv);
        }


        OptionData.setSVIValues(closestData, svi.a, svi.b, svi.p, svi.m, svi.sigma);

        return iv;

    }

    public static void calculateSVIParams(OptionData[] data) {

        List<Double> dtes = OptionData.getDtes(data);

        for (double dte : dtes) {

            OptionData[] dteData = OptionData.getOptionsByDte(data, dte);
            SVI svi = new SVI(dteData);

            try {
                svi.optimize();
                OptionData.setSVIValues(dteData, svi.a, svi.b, svi.p, svi.m, svi.sigma);
            } catch (IllegalStateException e) {
                // There likely wasn't enough data to do the calculations.
                e.printStackTrace();

            }
        }

    }

    public static OptionData createOptionDataFromSviParams(OptionData[] data, double currentStrike) {

        double impliedVolatility = calculateIV(data, currentStrike);

        double dte = data[0].getDte();
        FastDate date = data[0].getDate();
        if (Double.isNaN(impliedVolatility)) {
            System.err.println("Cannot calculate IV : " + date + " dte: " + dte + " currentStrike: " + currentStrike);
            return null;
        }

        if (impliedVolatility > 1) {
//			System.err.println("BAD IV : " + date + " dte: " + dte + " currentStrike: " + currentStrike + " IV: " + impliedVolatility);
            impliedVolatility = 1;
        }

        double r = BlackScholes.RISK_FREE_RATE_OF_RETURN;
        double currentValue = data[0].getCurrentStockPrice();
        boolean calls = data[0].isCall();

        double optionPrice = BlackScholes.optionPrice(calls, currentValue, currentStrike, dte, r, impliedVolatility);

       return OptionData.createOptionData(optionPrice, data[0].getStockTicker(), calls, date, data[0].getExpiryDate(),
                impliedVolatility, currentValue, dte, currentStrike, data[0].getRoot(), data[0], r);

    }

    public static OptionData createOptionDataFromSviParamsAndExactDelta(OptionData[] data, OptionData[] sviData, double delta) {

        double currentStrike = calculateCurrentStrikeForExactDelta(data, delta);
        return createOptionDataFromSviParams(sviData, currentStrike);

    }

    public static double calculateCurrentStrikeForExactDelta(OptionData[] data, double delta) {
        if (data.length != 2) {
            throw new IllegalArgumentException("Too many strikes...");
        }

        if (data[0].isCall() != data[1].isCall()) {
            throw new IllegalArgumentException("Either all puts or all calls...");
        }

        // Make sure the delta is the proper sign... it is common for users to mix up the sign.
        if (data[0].isCall() && delta < 0) {
            delta = -delta;
        }

        if (!data[0].isCall() && delta > 0) {
            delta = -delta;
        }

        OptionData data1 = data[0];
        OptionData data2 = data[data.length - 1];

        if (data1 == data2) {
            throw new IllegalStateException("Indentical data");
        }

        // Here we will use linear interpolation... which is within an acceptable error margin.
        double delta1 = data1.getDelta();
        double delta2 = data2.getDelta();
        double strike1 = data1.getStrikePrice();
        double strike2 = data2.getStrikePrice();
        double totalDeltaDistance = Math.abs(delta1 - delta2);
        double deltaDistanceFromLowestStrike = delta - data1.getDelta();
        double percentage = Math.abs(deltaDistanceFromLowestStrike / totalDeltaDistance);
        double totalStrikeDistance = strike2 - strike1;
        return strike1 + (percentage * totalStrikeDistance);

    }

    /**
     * @see "p 5 of Quasi-Explicit Calibration of Gatheral's SVI model"
     */
    public double calculateQuasiSVI_CDA(double a, double b, double p, double k, double m, double sigma, double t) {

        double a_t = getQuasiSVI_AT(a, t);
        double c = getQuasiSVI_C(b, sigma, t);
        double d = getQuasiSVI_D(p, b, sigma, t);

        double y = (k - m) / sigma;
        double dy = d * y;
        double cSqrtY2 = c * Math.sqrt(y * y + 1);

        return a_t + dy + cSqrtY2;

    }

    public double getCurrentValue() {
        return data[0].getCurrentStockPrice();
    }

    /**
     * Pass in the real strike. Calculate the log-strike and optimize SVI.
     *
     * @param strike - the strike price.
     * @return w_t the total variance.
     */
    public double calculateSVI(double strike) {

        double t = getT();
        double currentValue = getCurrentValue();

        double sigmasigma = sigma * sigma;
        double k = OptionData.getLogStrike(strike, currentValue);

        // We need to convert to Raw parameters: so we multiply a & b by t.
        return calculateSVI(a * t, b * t, p, k, m, sigma, sigmasigma, t);

    }

    public double getT() {
        return getT(data);
    }

    public double getDTE() {
        return data[0].getDte();
    }

    public double getAtmIV() {
        OptionData atmData = OptionData.getAtmOption(data);
        return atmData.getIV();
    }

    public double getMinimumIVLogStrike() {
        OptionData item = OptionData.getMinimiumIVOptionOptionData(data);
        return OptionData.getLogStrike(item);
    }

    private void optimizeQuasiSVI(MultivariateOptimizer optimizer, double[] initParams, double[] startParams, boolean ssr) {

        MinimizerQuasiSVI functQuasi = new MinimizerQuasiSVI(data, initParams, ssr);
        optimizeSVI(functQuasi, optimizer, initParams, startParams);

    }

    private void optimizeRawSVI(MultivariateOptimizer optimizer, double[] initParams, double[] startParams, boolean ssr) {

        MinimizerSVI functSVI = new MinimizerRawSVI(data, initParams, ssr);
        optimizeSVI(functSVI, optimizer, initParams, startParams);

    }

    private void optimizeJWSVI(MultivariateOptimizer optimizer, double[] initParams, boolean ssr) {

        MinimizerSVIJW functSVI = new MinimizerSVIJW(data, initParams, ssr);

        // get the starting values for optimizing JW.
        double p_t = functSVI.getPT();
        double phi_t = functSVI.getPhiT();
        double v_t = functSVI.getVT();
        double ctPrime = getCTPrime(p_t, phi_t);
        double vMinTPrime = getVMinTPrime(p_t, v_t, ctPrime);

        optimizeSVI(functSVI, optimizer, initParams, new double[]{ctPrime, vMinTPrime});

    }

    private void optimizeSVI(MinimizerSVI functSVI, MultivariateOptimizer optimizer, double[] initParams, double[] startParams) {

        PointValuePair pointValue = optimizer.optimize(2000, functSVI, GoalType.MINIMIZE, startParams);
        double[] param = functSVI.minParams;
        double ssr = functSVI.ssr;

        if (this.ssr > ssr) {

            this.ssr = ssr;

            a = functSVI.getA(param);
            b = functSVI.getB(param);
            p = functSVI.getP(param);
            m = functSVI.getM(param);
            sigma = functSVI.getSigma(param);

            if (debug) {
                functSVI.print(startParams);
            }

        }

    }

    private void optimize() {

        // We don't want to be off by more than .01 IV points where IV is between (0, 1] so
        // the Sum of Squared Residuals would be .01*.01=.0001
        // We use the Powell Method because it is more robust than Nelder-Mead.
        MultivariateOptimizer powell = new PowellOptimizer(.00001d, .00001d);


        double aStart = getWTFromIV(getAtmIV(), getT()) / 2d;
        double bStart = .25;
        double pStart = 0;

        double mStart = getMinimumIVLogStrike() / 2d;
        double sigmaStart = getWTFromIV(getAtmIV(), getT()) / 2d;

        // See Quasi-Explicit... or
        // See Issues of Nelder-Mead Simplex Optimization with Constraints by Floc'h
        if (sigmaStart < .005d) {
            sigmaStart = .005d;
        }

        optimizeQuasiSVI(powell, new double[]{aStart, bStart, Double.NaN, mStart, sigmaStart}, new double[]{pStart}, false);
        optimizeQuasiSVI(powell, new double[]{Double.NaN, Double.NaN, Double.NaN, mStart, sigmaStart}, new double[]{aStart, bStart, p}, true);

        double minMStart = mStart * 2;
        optimizeQuasiSVI(powell, new double[]{a, b, p, Double.NaN, Double.NaN}, new double[]{minMStart, sigmaStart}, true);
        optimizeQuasiSVI(powell, new double[]{Double.NaN, b, Double.NaN, mStart, sigmaStart}, new double[]{a, p}, true);

        // There aren't any parameters that we hold constant so the initial parameters array is empty.
        optimizeRawSVI(powell, new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN}, new double[]{a, b, p, m, sigma}, true);

        optimizeJWSVI(powell, new double[]{a, b, p, m, sigma}, true);

    }

    static class BoolHolder {
        boolean bool;
    }

    public abstract class MinimizerSVI implements MultivariateFunction {

        public double minSum = Double.NaN;
        public double[] minParams;
        private OptionData[] data;
        private double a = Double.NaN;
        private double b = Double.NaN;
        private double p = Double.NaN;
        private double m = Double.NaN;
        private double sigma = Double.NaN;
        private boolean calcSSR = true;
        private double ssr = Double.NaN;
        private long iterations = 0;
        private double[] logStrikes;
        private double[] ivs;


        public MinimizerSVI(OptionData[] data, double[] initParams, boolean ssr) {
            this.data = data;
            this.a = initParams[0];
            this.b = initParams[1];
            this.p = initParams[2];
            this.m = initParams[3];
            this.sigma = initParams[4];
            this.calcSSR = ssr;
        }

        @Override
        public final double value(double[] point) {
            return function(point);
        }

        public final double getA(double[] param) {
            return isASet() ? a : param[getIndex(0)];
        }

        public final double getB(double[] param) {
            return isBSet() ? b : param[getIndex(1)];
        }

        public final double getP(double[] param) {
            return isPSet() ? p : param[getIndex(2)];
        }

        public final double getM(double[] param) {
            return isMSet() ? m : param[getIndex(3)];
        }

        public final double getSigma(double[] param) {
            return isSigmaSet() ? sigma : param[getIndex(4)];
        }

        public final boolean isASet() {
            return !Double.isNaN(a);
        }

        public final boolean isBSet() {
            return !Double.isNaN(b);
        }

        public final boolean isPSet() {
            return !Double.isNaN(p);
        }

        public final boolean isMSet() {
            return !Double.isNaN(m);
        }

        public final boolean isSigmaSet() {
            return !Double.isNaN(sigma);
        }

        protected final int getIndex(int idx) {

            if (idx > 3 && isMSet()) {
                idx--;
            }

            if (idx > 2 && isPSet()) {
                idx--;
            }

            if (idx > 1 && isBSet()) {
                idx--;
            }

            if (idx > 0 && isASet()) {
                idx--;
            }

            return idx;

        }


        /**
         * @see "Quasi-Explicit Calibration of Gatheral's SVI model"
         */
        double function(double[] param) {

            iterations++;

            double a = getA(param);
            double b = getB(param);
            double p = getP(param);
            double m = getM(param);
            double sigma = getSigma(param);

            double t = getT();

            if (!parametersWithinTolerance(a, b, p, m, sigma, t)) {
                return Double.MAX_VALUE;
            }

            double[] w_ivs = getW_IVs(param);
            double[] ivs = getIVs();
            double ssr = SimpleStatisticsUtils.sumOfSquaredResiduals(w_ivs, ivs);
            double sum = ssr;
            if (!calcSSR) {
                sum = SimpleStatisticsUtils.getStandardErrorOfResiduals(w_ivs, ivs);
            }

            if (!Double.isNaN(sum)) {
                if (Double.isNaN(minSum) || sum < minSum) {
                    this.ssr = ssr;
                    minSum = sum;
                    minParams = Arrays.copyOf(param, param.length);
                }
            }

            return sum;

        }

        protected abstract double[] getW_IVs(double[] param);


        public double[] getLogStrikes() {
            if (logStrikes == null) {
                logStrikes = OptionData.getLogStrike(data);
            }
            return logStrikes;
        }

        public double[] getIVs() {
            if (ivs == null) {
                ivs = OptionData.getIVs(data);
            }
            return ivs;
        }

        public final void print(double[] startParams) {

            double[] w_ivs = getW_IVs(minParams);
            double[] ivs = getIVs();
            double[] strikes = OptionData.getStrikePrices(data);
            double[] currentValues = OptionData.getCurrentStockPrices(data);
            double[] dtes = OptionData.getDtesAsArray(data);
            double[] deltas = OptionData.getValues(data, OptionData.DELTA_PER_OPTION);
            boolean[] calls = OptionData.isCalls(data);

            double a = getA(minParams);
            double b = getB(minParams);
            double p = getP(minParams);
            double m = getM(minParams);
            double sigma = getSigma(minParams);


            System.out.println(" Date: " + data[0].getDate() + "  DTE: " + data[0].getValueAsInt(OptionData.DAYS_TO_EXPIRATION) + " Iterations to convergence: " + iterations);

            String comma = "";
            String parmStr = "";
            if (!isASet()) {
                parmStr += "a";
                comma = ",";
                double aStart = getA(startParams);
                System.out.println("Start: a: " + aStart + " -> " + a);
            } else {
                System.out.println("Constant: a: " + a);
            }

            if (!isBSet()) {
                parmStr += comma + "b";
                comma = ",";
                double bStart = getB(startParams);
                System.out.println("Start: b: " + bStart + " -> " + b);
            } else {
                System.out.println("Constant: b: " + b);
            }

            if (!isPSet()) {
                parmStr += comma + "p";
                comma = ",";
                double pStart = getP(startParams);
                System.out.println("Start: p: " + pStart + " -> " + p);
            } else {
                System.out.println("Constant: p: " + p);
            }

            if (!isMSet()) {
                parmStr += comma + "m";
                comma = ",";
                double mStart = getM(startParams);
                System.out.println("Start: m: " + mStart + " -> " + m);
            } else {
                System.out.println("Constant: m: " + m);
            }

            if (!isSigmaSet()) {
                parmStr += comma + "sigma";
                double sigmaStart = getSigma(startParams);
                System.out.println("Start: sigma: " + sigmaStart + " -> " + sigma);
            } else {
                System.out.println("Constant: sigma: " + sigma);
            }

            if (calcSSR) {
                double sum = SimpleStatisticsUtils.sumOfSquaredResiduals(w_ivs, ivs);
                System.out.println("SSR (" + parmStr + ")=" + sum + "  RMSE: " + Math.sqrt(sum / w_ivs.length));
            } else {
                double stderr = SimpleStatisticsUtils.getStandardErrorOfResiduals(w_ivs, ivs);
                System.out.println("StdErr (" + parmStr + ")=" + stderr);
            }

            for (int i = 0; i < w_ivs.length; i++) {
                String callPut = calls[i] ? "c" : "p";
                System.out.println("Check: stock: " + currentValues[i] + "  strike: " + strikes[i] + callPut + "  dte: " + dtes[i] + " delta: " + deltas[i]
                        + "  -->  ^" + w_ivs[i] + " ~= " + ivs[i] + "    e=" + (w_ivs[i] - ivs[i]));
            }

        }

    }

    public class MinimizerQuasiSVI extends MinimizerSVI {

        public MinimizerQuasiSVI(OptionData[] data, double[] initParams, boolean ssr) {
            super(data, initParams, ssr);
        }

        @Override
        protected double[] getW_IVs(double[] param) {

            double a = getA(param);
            double b = getB(param);
            double p = getP(param);
            double m = getM(param);
            double sigma = getSigma(param);

            double t = getT();

            double[] ks = getLogStrikes();
            double[] w_ivs = new double[data.length];

            for (int i = 0; i < data.length; i++) {

                double w_i = calculateQuasiSVI_CDA(a, b, p, ks[i], m, sigma, t);
                double w_i_svi = calculateSVI(a * t, b * t, p, ks[i], m, sigma, sigma * sigma, t);

                if (w_i_svi != Double.MAX_VALUE) {
                    double w_i_svi_diff = Math.abs(w_i - w_i_svi);
                    if (w_i_svi_diff > .0001d) {
                        throw new IllegalStateException();
                    }
                }

                w_ivs[i] = getIVFromWT(w_i, t);

            }

            return w_ivs;

        }


    }

    public class MinimizerRawSVI extends MinimizerSVI {

        public MinimizerRawSVI(OptionData[] data, double[] initParams, boolean ssr) {
            super(data, initParams, ssr);
        }

        @Override
        protected double[] getW_IVs(double[] params) {

            double t = getT();
            double[] ks = getLogStrikes();

            double[] w_ivs = new double[data.length];

            double sigmasigma = sigma * sigma;
            for (int i = 0; i < data.length; i++) {

                double w_i = calculateSVI(a * t, b * t, p, ks[i], m, sigma, sigmasigma, t);
                w_ivs[i] = getIVFromWT(w_i, t);

            }

            return w_ivs;
        }


    }

    public class MinimizerSVIJW extends MinimizerRawSVI {

        private double[] initParams;

        private double phiT = Double.NaN;
        private double vT = Double.NaN;
        private double pT = Double.NaN;
        private double wT = Double.NaN;

        public MinimizerSVIJW(OptionData[] data, double[] initParams, boolean ssr) {
            super(data, initParams, ssr);
            this.initParams = initParams;
        }

        private double getVT() {
            if (Double.isNaN(vT)) {
                vT = SVI.getVT(getAT(), getBT(), getP(initParams), getM(initParams), getSigma(initParams), getT());
            }
            return vT;
        }

        private double getWT() {
            if (Double.isNaN(wT)) {

                double vT = getVT();
                double t = getT();

                wT = vT * t;
            }

            return wT;
        }

        public double getAT() {
            return getA(initParams) * getT();
        }

        public double getBT() {
            return getB(initParams) * getT();
        }

        private double getPhiT() {
            if (Double.isNaN(phiT)) {
                phiT = SVI.getPhiT(getAT(), getBT(), getP(initParams), getM(initParams), getSigma(initParams), getWT());
            }
            return phiT;
        }

        private double getPT() {
            if (Double.isNaN(pT)) {
                pT = SVI.getPT(getBT(), getP(initParams), getWT());
            }
            return pT;
        }

        @Override
        public double function(double[] param) {

            // First we convert SVI raw to JW params

            double vT = getVT();
            double phiT = getPhiT();
            double p_t = getPT();

            // Now we use the new JW params we need to optimize!
            double c_t = param[0];
            double vTMin = param[1];

            double t = getT();

            // Now we convert back to the original raw params for calculation!
            double w_t = getWT();
            double b_jw = getB_JW(w_t, c_t, p_t);
            double p_jw = getP_JW(w_t, b_jw, p_t);

            double alpha = getAlpha(p_jw, phiT, w_t, b_jw);

            double m_jw = getM_JW(vT, vTMin, t, b_jw, p_jw, alpha);
            double sigma_jw = getSigma_JW(alpha, m_jw);
            double aJW = getA_JW(vTMin, b_jw, p_jw, sigma_jw, t);

            double[] paramRaw = new double[5];

            paramRaw[0] = aJW;
            paramRaw[1] = b_jw;
            paramRaw[2] = p_jw;
            paramRaw[3] = m_jw;
            paramRaw[4] = sigma_jw;

            // Optimize using the raw Equation!
            return super.function(paramRaw);

        }

    }

}
