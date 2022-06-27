package com.tmck.svi.valueobjs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.tmck.svi.BlackScholes;
import com.tmck.svi.utils.ExpirationFinder;
import com.tmck.svi.utils.FastDate;
import com.tmck.svi.utils.StringUtils;

/**
 * @author tim
 */
public class OptionData extends MultiValueTimeSeriesPoint implements SymbolHolder {

    public static final Date OPRA_CODE_CHANGE_DATE = new Date(110, Calendar.FEBRUARY, 12);
    public static final Date OSI_PHASE_TWO_DATE = new Date(110, Calendar.APRIL, 1);
    
    public static final String D1 = "D1";
    public static final String SVI_A = "SVI_A";
    public static final String SVI_B = "SVI_B";
    public static final String SVI_P = "SVI_P";
    public static final String SVI_M = "SVI_M";
    public static final String SVI_SIGMA = "SVI_SIG";
    public static final String LAST_TRADED_OPTION_PRICE = "Current Option Price";
    public static final String CURRENT_STOCK_PRICE = "Current Stock Price";
    /**
     * Vega per option - NOT per contract.
     */
    public static final String VEGA_PER_OPTION = "Vega";
    public static final String ORIGINAL_VEGA_PER_OPTION = "Starting Vega";
    /**
     * Theta per option - NOT per contract. Per day on a 365 day year.
     */
    public static final String THETA_PER_OPTION_PER_DAY = "Theta";
    public static final String TRADE_VOLUME = "TradeVolume";
    public static final String ORIGINAL_THETA_PER_OPTION_PER_DAY = "Starting Theta";
    /**
     * Gamma per option - NOT per contract.
     */
    public static final String GAMMA_PER_OPTION = "Gamma";
    public static final String ORIGINAL_GAMMA_PER_OPTION = "Starting Gamma";
    /**
     * Delta per option - NOT per contract.
     */
    public static final String DELTA_PER_OPTION = "Delta";
    public static final String ORIGINAL_DELTA_PER_OPTION = "Starting Delta";
    /**
     * IV per option - NOT per contract.
     */
    public static final String IMPLIED_VOLATILITY_PER_OPTION = "IV";
    public static final String ORIGINAL_IMPLIED_VOLATILITY_PER_OPTION = "Starting IV";
    public static final String ORIGINAL_OPTION_MID = "Starting Mid";
    public static final String OPEN_INTEREST = "Open Interest";
    public static final String VOLUME = "Volume";
    public static final String EXPECTED_PROFIT = "Expected Profit";
    public static final String MODEL_PROFIT = "Model Profit";
    public static final String DAYS_TO_EXPIRATION = "Days to Expiry";
    public static final String STRIKE_PRICE = "Strike Price";
    public static final String COST = "Cost";
    public static final String MAX_PROFIT = "Max. Profit";
    public static final String MAX_RISK = "Max. Risk";
    public static final String RANK = "Rank";
    public static final String BID = "Bid";
    public static final String ASK = "Ask";
    public static final String ASK_SIZE = "AskSize";
    public static final String BID_SIZE = "BidSize";
    public static final String DAYS_IN_TRADE = "DIT";
    public static final String ORIGINAL_OPTION_PRICE = "Starting Price";
    public static final String VOMMA = "Vomma";
    public static final String VANNA = "Vanna";
    public static final String INTEREST_RATE = "R";
    public static final String[] OPRA_PRICE_CODES = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T"};
    public static final String[] OPRA_PRICE_CODES_NON_STANDARD = {"U", "V", "W", "X", "Y", "Z"};
    public static String[] OLD_OPRA_MONTLY_CALL_CODES = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};
    public static String[] OLD_OPRA_MONTLY_PUT_CODES = {"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X"};
    private Symbol stockTicker = null;
    private Symbol optionTicker = null;
    private boolean call = true;
    private String root;
    private double dte = -1;
    private double currentStockPrice = Double.NaN;
    private double strikePrice = Double.NaN;
    private double iv = Double.NaN;
    private double originalIV = Double.NaN;
    private double d1 = Double.NaN;
    private double originalMid = Double.NaN;
    private int tradeVolume = -1;
    private double volArbOffset = .01d;            // TODO this should be set somewhere!
    private double bid = Double.NaN;
    private double oi = Double.NaN;
    private double ask = Double.NaN;
    /**
     * Needs to be cached for speed!
     */
    private double mid = Double.NaN;
    private Date expiryDate;
    private double interestRate;
    private boolean underlying = false;

	private OptionData[] EMPTY_OPTIONDATA_ARR = new OptionData[0];

    public OptionData(double currentPrice) {
        this(currentPrice, new FastDate());
    }

    public OptionData(double currentPrice, FastDate date) {
        this(new TimeSeriesPoint(date, currentPrice));
    }

    public OptionData(TimeSeriesPoint defaultPoint) {
        super(defaultPoint, LAST_TRADED_OPTION_PRICE);
    }

    public static List<OptionData> getCalls(OptionData[] data) {
        List<OptionData> list = new ArrayList<OptionData>();
        if (data == null) {
            return list;
        }
        for (OptionData od : data) {
            if (od.isCall()) {
                list.add(od);
            }
        }
        return list;
    }

    public static List<OptionData> getPuts(OptionData[] data) {
        List<OptionData> list = new ArrayList<OptionData>();
        if (data == null) {
            return list;
        }
        for (OptionData od : data) {
            if (!od.isCall()) {
                list.add(od);
            }
        }
        return list;
    }

    public static OptionData getAtmOption(OptionData[] data) {

        if (data == null || data.length == 0) {
            return null;
        }

        double stockPrice = data[0].getCurrentStockPrice();
        return getAtmOptionForStockPrice(data, stockPrice);

    }

    public static OptionData getAtmOptionForStockPrice(OptionData[] data, double stockPrice) {

        if (data == null) {
            return null;
        }

        String ticker = data[0].getStockTicker().getName().replace('^', ' ').trim();
        String desiredRoot = ticker;
        if (ticker.length() > 1) {
            desiredRoot = ticker.substring(0, 2);
        }

        if (ticker.equals("SPX")) {
            desiredRoot = ticker.substring(0, 1);
        }

        OptionData atm = null;
        double distance = Double.MAX_VALUE;

        for (int i = 0; i < data.length; i++) {
            if (atm == null) {
                atm = data[i];
            }

            double strikePrice = data[i].getStrikePrice();

            double currentDistance = Math.abs(stockPrice - strikePrice);

            String foundRoot = atm.getRoot();
            if (foundRoot != null && !foundRoot.startsWith(desiredRoot)) {
                continue;
            }

            if (distance > currentDistance) {
                atm = data[i];
                distance = currentDistance;
            }

        }

        return atm;
    }

    public static OptionData getOptionDataByStrike(OptionData[] data, double targetStrike) {

        if (data == null) {
            return null;
        }

        for (OptionData od : data) {
            double strikePrice = od.getStrikePrice();
            if (areStrikesEqual(strikePrice, targetStrike)) {
                return od;
            }
        }

        return null;

    }

    public static OptionData getNextOptionStrike(OptionData[] data, double currentStrike, boolean call, boolean otm) {

        if (data == null) {
            return null;
        }

        OptionData next = null;
        double nextStrike = -Double.MAX_VALUE;
        if (call) {
            nextStrike = Double.MAX_VALUE;
        }


        for (int i = 0; i < data.length; i++) {

            if (call != data[i].isCall()) {
                continue;
            }

            double strikePrice = data[i].getStrikePrice();


            if (next != null) {
                nextStrike = next.get(STRIKE_PRICE).getValue();
            }

            if (call && strikePrice > currentStrike && nextStrike > strikePrice) {
                next = data[i];
            }

            if (!call && strikePrice < currentStrike && nextStrike < strikePrice) {
                next = data[i];
            }

        }

        return next;

    }

    public static void removeLowOpenInterestOptions(List<OptionData> optionDataList, double openInterest) {

        if (optionDataList == null || optionDataList.size() == 0) {
            return;
        }

        ListIterator<OptionData> itr = optionDataList.listIterator();
        Symbol symbol = optionDataList.get(0).getStockTicker();

        while (itr.hasNext()) {

            OptionData data = itr.next();

            // If we interpolated the data there won't be open interest.
            if (!data.contains(OPEN_INTEREST)) {
                continue;
            }

            double oi = data.getValue(OPEN_INTEREST);

            if (Math.abs(oi) < openInterest) {
                double strike = data.getStrikePrice();
                boolean call = data.isCall();
                itr.remove();
            }
        }

        if (optionDataList.size() == 0) {
            System.err.println("Oops! removed all OptionData due to low Open Interest: " + symbol);
        }

    }

    /**
     * Remove options below a specified Delta.
     */
    public static void removeOptionsBelowDelta(List<OptionData> optionDataList, double optionDelta) {

        if (optionDataList == null || optionDataList.size() == 0) {
            return;
        }

        Symbol symbol = optionDataList.get(0).getStockTicker();

        ListIterator<OptionData> itr = optionDataList.listIterator();
        while (itr.hasNext()) {

            OptionData data = itr.next();
            double delta = data.getValue(DELTA_PER_OPTION);

            if (Math.abs(delta) < optionDelta) {
                double strike = data.getStrikePrice();
                boolean call = data.isCall();
                itr.remove();
            }
        }

        if (optionDataList.size() == 0) {
            System.err.println("Oops! removed all OptionData due to low Delta: " + symbol);
        }
    }

    /**
     * remove options above a given Delta.
     */
    public static void removeOptionsAboveDelta(List<OptionData> optionDataList, double optionDelta) {

        if (optionDataList == null || optionDataList.size() == 0) {
            return;
        }

        Symbol symbol = optionDataList.get(0).getStockTicker();
        ListIterator<OptionData> itr = optionDataList.listIterator();
        while (itr.hasNext()) {
            OptionData data = itr.next();
            double delta = data.getValue(DELTA_PER_OPTION);
            if (Math.abs(delta) > optionDelta) {
                double strike = data.getStrikePrice();
                boolean call = data.isCall();
                itr.remove();
            }
        }

        if (optionDataList.size() == 0) {
            System.err.println("Oops! removed all OptionData due to high Delta: " + symbol);
        }

    }

    public static OptionData getByOpra(OptionData[] options, String opra) {
        for (OptionData option : options) {
            String currentOpra = option.getOptionTicker().getName();
            if (currentOpra.equals(opra)) {
                return option;
            }
        }
        return null;
    }

    public static OptionData[] removeOptionsWithDteStrictlyLessThan(OptionData[] data, double maxDte) {

        List<OptionData> list = new ArrayList<OptionData>();
        for (OptionData item : data) {
            if (item.getDte() >= maxDte) {
                list.add(item);
            }
        }

        return list.toArray(new OptionData[0]);

    }

    /**
     * Currently doesn't work with old OPRA codes.
     */
    public static OptionData[] keepOptionsExpiringOnDate(OptionData[] data, Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
        String yymmdd = format.format(date);
        int dateint = Integer.parseInt(yymmdd);

        List<OptionData> list = Arrays.asList(data);

        ListIterator<OptionData> itr = list.listIterator();
        while (itr.hasNext()) {
            OptionData item = itr.next();
            String callput = "P";
            if (item.isCall()) {
                callput = "C";
            }


            String ticker = item.getStockTicker().getName();
            String opra = item.getOptionTicker().getName();

            // These lines don't work with old OPRA codes.
            String temp = opra.replace(ticker, "");
            String current = temp.substring(0, temp.indexOf(callput));
            int currentInt = Integer.parseInt(current);
            // the expiration dates could be off by 1.
            if (Math.abs(dateint - currentInt) > 2) {
                itr.remove();
            }
        }

        return list.toArray(new OptionData[0]);

    }

    public static double[] getPercentAboveTheMoney(OptionData[] data) {
        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].getPercentAboveTheMoney();
        }
        return d;
    }

    public static double[] getIVs(OptionData[] data) {
        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].getIV();
        }
        return d;
    }

    public static double[] getStrikePrices(OptionData[] data) {
        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].getStrikePrice();
        }
        return d;
    }

    public static double[] getDeltas(OptionData[] data) {
        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].getDelta();
        }
        return d;
    }

    public static double[] getStrikePrices(List<OptionData> data) {
        double[] d = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            d[i] = data.get(i).getStrikePrice();
        }
        return d;
    }

    public static double[] getDtesAsArray(OptionData[] data) {
        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].getDte();
        }
        return d;
    }

    public static double[] getCurrentStockPrices(OptionData[] data) {
        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i].getCurrentStockPrice();
        }
        return d;
    }

    public static OptionData[] copy(OptionData[] data) {
        OptionData[] copy = new OptionData[data.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = data[i].copy();
        }
        return copy;
    }

    public static Date getExpiryDate(OptionData leg) {
        return leg.getExpiryDate();
    }

    public static double getIVDistance(OptionData put, OptionData call) {

        if (put == null || call == null) {
            return Double.NaN;
        }

        double putAt25DeltaIV = put.getIV();
        double callAt25DeltaIV = call.getIV();

        return putAt25DeltaIV - callAt25DeltaIV;

    }

    public static OptionData[] getCallOptionData(OptionData[] data) {

        List<OptionData> callList = OptionData.getCalls(data);
        return callList.toArray(new OptionData[0]);

    }

    public static OptionData[] getPutOptionData(OptionData[] data) {

        List<OptionData> putList = OptionData.getPuts(data);
        return putList.toArray(new OptionData[0]);

    }

    public static void copySVIParams(OptionData from, OptionData to) {

        if (!from.contains(OptionData.SVI_A)) {
            throw new IllegalStateException();
        }

        to.setValue(SVI_A, from.getValue(SVI_A));
        to.setValue(SVI_B, from.getValue(SVI_B));
        to.setValue(SVI_P, from.getValue(SVI_P));
        to.setValue(SVI_M, from.getValue(SVI_M));
        to.setValue(SVI_SIGMA, from.getValue(SVI_SIGMA));

    }

    public static void setSVIValues(OptionData[] data, double a, double b,
                                    double p, double m, double sigma) {

        if (!Double.isNaN(a)) {
            setValues(data, SVI_A, a);
        }
        if (!Double.isNaN(b)) {
            setValues(data, SVI_B, b);
        }
        if (!Double.isNaN(p)) {
            setValues(data, SVI_P, p);
        }
        if (!Double.isNaN(m)) {
            setValues(data, SVI_M, m);
        }
        if (!Double.isNaN(sigma)) {
            setValues(data, SVI_SIGMA, sigma);
        }
    }

    public static void setSVIValues(OptionData data, double a, double b,
                                    double p, double m, double sigma) {

        if (!Double.isNaN(a)) {
            data.setValue(SVI_A, a);
        }
        if (!Double.isNaN(b)) {
            data.setValue(SVI_B, b);
        }
        if (!Double.isNaN(p)) {
            data.setValue(SVI_P, p);
        }
        if (!Double.isNaN(m)) {
            data.setValue(SVI_M, m);
        }
        if (!Double.isNaN(sigma)) {
            data.setValue(SVI_SIGMA, sigma);
        }
    }

    public static double[] getValues(OptionData[] data, String fieldName) {

        double[] values = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = data[i].getValue(fieldName);
        }

        return values;
    }

    public static boolean areStrikesEqual(double strike1, double strike2) {
        return Math.abs(strike1 - strike2) < .01d;
    }

    public static <T> List<T> convertToList(T[] arr) {
        return Arrays.asList(arr);
    }

    public static OptionData[] convertToArray(List<OptionData> list) {
		return list.toArray(new OptionData[0]);
    }

    public static String getOptionRoot(OptionData currentData) {

        String optionRoot = currentData.getRoot();
        if (optionRoot != null) {
            return optionRoot;
        }

        Date expiry = currentData.getExpiryDate();
        Symbol ticker = currentData.getStockTicker();

        return getOptionRoot(ticker, expiry);
    }

    public static String getOptionRoot(Symbol ticker, Date expiry) {

        if (isOldOpraExpiration(expiry)) {
            return getOldOpraUnderlyingSecuritySymbol(ticker);
        }

        return getOsiRoot(ticker, expiry);

    }

    public static String createOpraCode(OptionData currentData) {

        Date expiry = currentData.getExpiryDate();
        Symbol ticker = currentData.getStockTicker();

        String optionRoot = currentData.getRoot();

        if (isOldOpraExpiration(expiry)) {

            // Use the OLD Opra codes.
            //
            // A Motorola October 2009 7.00 CALL is represented as:
            // MOT JJ
            // Where:
            // Root Symbol = MOT
            // Month Code = J (October Call)
            // Price Code = J (7.00 Strike Price Code)

            String letter = getOldOpraExpiryModeCallPutLetter(expiry, currentData.isCall());

            if (optionRoot == null) {
                optionRoot = getOldOpraUnderlyingSecuritySymbol(ticker);
            }

            try {
                String priceCode = getOldOpraPriceCode(currentData.getStrikePrice());
                return optionRoot + letter + priceCode;
            } catch (ArrayIndexOutOfBoundsException e) {
                // We could get this Exception when we try to create an Opra code for a fictitious strike price.
                return null;
            }

        }

        if (optionRoot == null) {
            optionRoot = getOsiRoot(ticker, expiry);
        }

        SimpleDateFormat fmt = new SimpleDateFormat("yyMMdd");
        String expiryStr = fmt.format(expiry);
        String callPut = currentData.isCall() ? "C" : "P";

        double strike = currentData.getStrikePrice();
//		String value = String.format("%05f", strike);
        String dollars = String.format("%05d", (int) strike);

        double centsVal = (strike - Math.floor(strike)) * 1000d;
        String cents = String.format("%3d", (int) centsVal).replace(' ', '0');

        return optionRoot + expiryStr + callPut + dollars + cents;

    }

    public static String getOsiRoot(Symbol stockTicker, Date expiry) {

        String symbol = stockTicker.getName();
        if (isOSIPhaseOneExpirationDate(expiry)) {
            symbol = getOldOpraUnderlyingSecuritySymbol(stockTicker);
        }

        if (symbol.length() > 6) {
            symbol = symbol.substring(0, 6);
        }

        if (symbol.length() < 6) {
            symbol = StringUtils.padRight(symbol, 6);
        }

        return symbol;
    }

    public static boolean isOSIPhaseOneExpirationDate(Date expiry) {
        return OPRA_CODE_CHANGE_DATE.before(expiry) &&
                OSI_PHASE_TWO_DATE.after(expiry);
    }

    public static boolean isOldOpraExpiration(Date expiry) {
        return OPRA_CODE_CHANGE_DATE.after(expiry);
    }

    @SuppressWarnings("deprecation")
    public static String getOldOpraExpiryModeCallPutLetter(Date expiry, boolean call) {

        int month = expiry.getMonth();
        String letter = OLD_OPRA_MONTLY_CALL_CODES[month];
        if (call) {
            letter = OLD_OPRA_MONTLY_PUT_CODES[month];
        }

        return letter;

    }

    public static String getOldOpraPriceCode(double price) {

        double priceMod100 = price % 100;
        double increment = priceMod100 / 5d;
        int intValue = (int) Math.round(increment);
        if (Math.abs(increment - intValue) < .01d) {
            return OPRA_PRICE_CODES[intValue];
        }

        double priceDiv75 = priceMod100 / 7.5;
        intValue = (int) Math.round(priceDiv75);

        return OPRA_PRICE_CODES_NON_STANDARD[intValue];

    }

    public static String getOldOpraUnderlyingSecuritySymbol(Symbol stockTicker) {

        String symbol = stockTicker.getName();

        if (symbol.startsWith("^")) {
            symbol = symbol.replace("^", "");
        }

        if (symbol.equals("MSFT")) {
            return "VMF";
        }

        if (symbol.length() <= 3) {
            return symbol;
        }

        return symbol.substring(0, 3);
    }

    /**
     * @see "A note on sufficient conditions for no arbitrage by Peter Carr, Dilip B. Madan"
     */
    public static void isArbitragePresent(OptionData[] data) {
        isArbitragePresent(data, true);
    }

    /**
     * @see "A note on sufficient conditions for no arbitrage by Peter Carr, Dilip B. Madan"
     */
    public static void isArbitragePresent(OptionData[] data, boolean calc) {

        String str = !calc ? "" : " Surface-Calculated: " + calc;

        if (calc) {

            if (hasVerticalArbitrage(data)) {
                System.err.println("Vertical Arbitrage!!!!" + str);
            }

            if (hasButterflyArbitrage(data)) {
                System.err.println("Butterfly Arbitrage!!!!" + str);
            }

            if (hasCalendarArbitrage(data)) {
                System.err.println("Call, Calendar Arbitrage!!!!" + str);
            }

        }

    }

    public static boolean hasButterflyArbitrage(OptionData[] data) {
        return hasButterflyArbitrage(data, true) || hasButterflyArbitrage(data, false);
    }

    public static boolean hasButterflyArbitrage(OptionData[] data, boolean calls) {

        if (calls) {
            data = OptionData.getCallOptionData(data);
        } else {
            data = OptionData.getPutOptionData(data);
        }

        List<Double> dtes = OptionData.getDtes(data);

        for (Double dte : dtes) {

            OptionData[] dteData = getOptionsByDte(data, dte);

            for (int i = 0; i < dteData.length; i++) {
                double mid1 = dteData[i].getMid();
                double k1 = dteData[i].getStrikePrice();
                for (int j = i + 1; j < dteData.length; j++) {
                    double mid2 = dteData[j].getMid();
                    double k2 = dteData[j].getStrikePrice();
                    for (int k = i + 1; k < dteData.length; k++) {
                        double mid3 = dteData[k].getMid();
                        double k3 = dteData[k].getValue(OptionData.STRIKE_PRICE);

                        boolean arb = false;
                        if (calls) {

                            // The more ITM calls should collect more premium than the OTM calls.
                            // otherwise this is an Arbitrage opportunity.
                            arb = (mid1 - mid2) / (k2 - k1) < (mid2 - mid3) / (k3 - k2);
                        } else {

                            // The more ITM puts should collect more premium than the OTM puts
                            // otherwise this is an Arbitrage opportunity.
                            arb = (mid2 - mid1) / (k2 - k1) > (mid3 - mid2) / (k3 - k2);
                        }

                        if (arb) {
                            return true;
                        }

                    }
                }
            }
        }

        return false;
    }

    public static boolean hasCalendarArbitrage(OptionData[] data) {
        return hasCalendarArbitrage(data, true) || hasCalendarArbitrage(data, false);
    }

    public static boolean hasVerticalArbitrage(OptionData[] data) {
        return hasVerticalArbitrage(data, true) || hasVerticalArbitrage(data, false);
    }

    public static boolean hasCalendarArbitrage(OptionData[] data, boolean calls) {

        if (calls) {
            data = OptionData.getCallOptionData(data);
        } else {
            data = OptionData.getPutOptionData(data);
        }

        List<Double> dtes = OptionData.getDtes(data);

        for (int i = 0; i < dtes.size(); i++) {

            OptionData[] dte1Data = OptionData.getOptionsByDte(data, dtes.get(i));
            for (int j = i + 1; j < dtes.size(); j++) {

                OptionData[] dte2Data = OptionData.getOptionsByDte(data, dtes.get(i));
                for (OptionData data1 : dte1Data) {

                    double k1 = data1.getStrikePrice();
                    OptionData data2 = OptionData.getOptionDataByStrike(dte2Data, k1);

                    if (data2 == null) {
                        continue;
                    }

                    if (data1.getMid() > data2.getMid()) {
                        return true;
                    }

                }
            }
        }

        return false;
    }

    public static OptionData[] getOptionsByDte(OptionData[] data, double dte) {

        List<OptionData> list = new ArrayList<OptionData>();
        for (OptionData item : data) {
            double currentDte = item.getDte();
            if (Math.abs(currentDte - dte) < .01d) {
                list.add(item);
            }
        }

        if (data.length == list.size() && data.length > 5) {
            System.err.println("Warning: getOptionsByDTE() : All data is from the same DTE: " + data[0].getDate() + "  dte: " + dte);
        }

        return list.toArray(new OptionData[0]);
    }

    public static boolean hasVerticalArbitrage(OptionData[] data, boolean calls) {

        if (calls) {
            data = OptionData.getCallOptionData(data);
        } else {
            data = OptionData.getPutOptionData(data);
        }

        List<Double> dtes = OptionData.getDtes(data);
        for (double dte : dtes) {

            OptionData[] dteData = getOptionsByDte(data, dte);

            for (int i_1 = 0; i_1 < dteData.length; i_1++) {

                OptionData data1_1 = dteData[i_1];
                double mid_1 = data1_1.getMid();
                double k_1 = data1_1.getStrikePrice();
                for (int i = i_1 + 1; i < dteData.length; i++) {

                    OptionData data1 = dteData[i];
                    double mid1 = data1.getMid();
                    double k1 = data1.getStrikePrice();

                    // We make free money (NOTE: the value is normalized to the range [0, 1] )

                    // An ITM call should be worth more than an OTM call and credit should be less than strike width.
                    // i.e. the numerator must be positive and the ratio must be less than 1
                    double cost = (mid_1 - mid1) / (k1 - k_1);
                    if (!calls) {

                        // An ITM put should be worth more than an OTM put and credit should be less than strike width.
                        // i.e. the numerator must be positive and the ratio must be less than 1.
                        cost = (mid1 - mid_1) / (k1 - k_1);
                    }

                    if (cost < 0 || cost > 1) {
                        return true;
                    }

                }
            }
        }

        return false;
    }

    public static List<Double> getDtes(OptionData[] data) {

        if (data == null) {
            throw new IllegalArgumentException();
        }

        List<Double> list = new ArrayList<>();
        for (OptionData item : data) {
            double dte = item.getDte();
            if (containsDte(list, dte)) {
                continue;
            }
            list.add(dte);
        }

        Collections.sort(list);

        return list;
    }

    private static boolean containsDte(List<Double> list, double dte) {
        for (Double val : list) {
            if (Math.abs(val - dte) < .01d) {
                return true;
            }
        }
        return false;
    }

    public static OptionData[] getOptionsAboveTheMoney(OptionData[] data) {
        List<OptionData> list = new ArrayList<OptionData>();

        for (OptionData item : data) {
            if (item.getPercentAboveTheMoney() >= 0) {
                list.add(item);
            }
        }

        return list.toArray(new OptionData[0]);
    }

    public static OptionData[] getOptionsBelowTheMoney(OptionData[] data) {
        List<OptionData> list = new ArrayList<OptionData>();

        for (OptionData item : data) {
            if (item.getPercentAboveTheMoney() < 0) {
                list.add(item);
            }
        }

        return list.toArray(new OptionData[0]);
    }

    /**
     * @see "p.21 of The Volatility Surface by Gatheral"
     */
    public static double[] getLogStrike(OptionData[] data) {

        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = getLogStrike(data[i]);
        }

        return d;
    }

    /**
     * @see "p.21 of The Volatility Surface by Gatheral"
     */
    public static double getLogStrike(OptionData data) {
        double currentValue = data.getCurrentStockPrice();
        double strike = data.getStrikePrice();
        return getLogStrike(strike, currentValue);
    }

    /**
     * @see "p.21 of The Volatility Surface by Gatheral"
     */
    public static double getLogStrike(double strike, double currentValue) {
        return Math.log(strike / currentValue);
    }

    public static boolean isImpliedVolatilityValid(OptionData[] data) {

        for (OptionData item : data) {
            if (!isImpliedVolatilityValid(item)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isImpliedVolatilityValid(OptionData data) {

        double iv = data.getIV();
        return !(iv < 0 || iv > 1 || Double.isInfinite(iv) || Double.isNaN(iv));
    }

    public static OptionData[] getOTMOptions(OptionData[] data) {

        List<OptionData> list = new ArrayList<OptionData>();
        for (OptionData item : data) {
            if (item.isOTM()) {
                list.add(item);
            }
        }

        return list.toArray(new OptionData[0]);
    }

    public static boolean[] isCalls(OptionData[] data) {

        boolean[] b = new boolean[data.length];
        for (int i = 0; i < data.length; i++) {
            b[i] = data[i].isCall();
        }

        return b;

    }

    public static OptionData getMinimiumIVOptionOptionData(OptionData[] data) {

        double minIV = Double.MAX_VALUE;
        OptionData min = null;

        for (OptionData item : data) {

            double iv = item.getIV();
            if (iv < minIV) {
                minIV = iv;
                min = item;
            }

        }

        return min;

    }

    public static boolean hasSviCalculations(List<OptionData> data) {
        for (OptionData datum : data) {
            if (!datum.hasAllSviCalculations()) {
                return false;
            }
        }
        return true;
    }

    public static OptionData createOptionData(double optionPrice, Symbol ticker, boolean call, FastDate expiryDate, double currentValue, double dte, double strike, double r) {
        return createOptionData(optionPrice, ticker, null, call, currentValue, Double.NaN, Double.NaN, expiryDate, strike, Double.NaN, Double.NaN, r);
    }

    public static OptionData createOptionData(double last, Symbol ticker, Symbol opraTicker, boolean call, double currentStockPrice, double openInt, double vol, FastDate expiryDate, double strike, double bid, double ask, double r) {

        // TODO the executionDate needs to be passed in as a parameter to the ctor!
        return createOptionData(last, ticker, null, call, null, expiryDate,
                Double.NaN, currentStockPrice, Double.NaN, strike, bid, ask, openInt, vol, null, null, r);

    }

    public static OptionData createOptionData(double optionPrice, Symbol stockTicker, boolean calls, FastDate executionDate, Date expiryDate,
                                              double impliedVolatility, double currentStockPrice, double dte, double strike, String optionRoot, OptionData sviParamData, double r) {
        return createOptionData(optionPrice, stockTicker, null, calls, executionDate, expiryDate,
                impliedVolatility, currentStockPrice, dte, strike, Double.NaN, Double.NaN, Double.NaN, Double.NaN, optionRoot, sviParamData, r);
    }

    public static OptionData createOptionData(double optionPrice, Symbol stockTicker, Symbol opraTicker, boolean calls, FastDate executionDate, Date expiryDate,
                                              double impliedVolatility, double currentStockPrice, double dte, double strike, double bid, double ask, double openInt, double vol, String optionRoot, OptionData sviParamData, double r) {

        OptionData strikeData = new OptionData(optionPrice, executionDate);
        strikeData.setStockTicker(stockTicker);
        strikeData.setCall(calls);
        strikeData.setCurrentStockPrice(currentStockPrice);
        if (executionDate != null) {
            strikeData.setDate(executionDate);
        }
        strikeData.setExpiryDate(expiryDate);

        strikeData.setValue(OPEN_INTEREST, openInt);
        strikeData.setValue(VOLUME, vol);
        strikeData.setBid(bid);
        strikeData.setAsk(ask);

        if (!Double.isNaN(dte)) {
            strikeData.setDte(dte);
        } else {
            strikeData.setDte(ExpirationFinder.getCalendarDaysToExpiration(expiryDate));
        }
        strikeData.setStrikePrice(strike);

        if (optionRoot != null) {
            strikeData.setRoot(optionRoot);
        } else {
            strikeData.setRoot(getOptionRoot(strikeData));
        }

        if (opraTicker == null) {
            String opraCode = OptionData.createOpraCode(strikeData);
            // It is possible that we could be creating an OptionData object for a fictitious strike... in which case the Opra code could be null.
            if (opraCode != null) {
                opraTicker = new Symbol(opraCode, Symbol.TICKER);
            }
        }
        strikeData.setOptionTicker(opraTicker);

        if (!Double.isNaN(impliedVolatility)) {
            strikeData.setIV(impliedVolatility);
        }

        if (sviParamData != null) {
            copySVIParams(sviParamData, strikeData);
            if (!strikeData.contains(SVI_A)) {
                throw new IllegalStateException();
            }
        }

        BlackScholes.setGreeksByOptionPrice(strikeData, r);
        return strikeData;

    }

    public static OptionData createOptionData(double mid, Date execDatetime, boolean call, double bid, int bidsize, double ask, int asksize, double stockPrice, int dte, double iv, double originalIV, double originalMid, int tradeVolume, double delta, double gamma, double theta, double vega, double strike, Date expiryDate, String underlying, String root, double r) {

        OptionData data = new OptionData(mid, new FastDate(execDatetime));
        data.setCall(call);
        data.setBid(bid);
        data.setBidSize(bidsize);
        data.setAsk(ask);
        data.setAskSize(asksize);
        data.setTradeVolume(tradeVolume);
        data.setCurrentStockPrice(stockPrice);

        if (!Double.isNaN(originalIV)) {
            data.setOriginalIV(originalIV);
        }

        if (!Double.isNaN(originalMid)) {
            data.setOriginalMid(originalMid);
        }

        if (dte < 0) {
            throw new IllegalStateException("Illegal DTE: " + dte + " exec: " + execDatetime + "  expiry: " + expiryDate + " strike: " + strike + (call ? "c" : "p"));
        }

        data.setDte(dte);
        data.setStrikePrice(strike);
        data.setExpiryDate(expiryDate);
        data.setRoot(root);

        data.setStockTicker(new Symbol(underlying, Symbol.TICKER));

        // This should be before the calls to vanna() and vomma()

        if (Double.isNaN(iv)) {

            data.setValue(THETA_PER_OPTION_PER_DAY, theta);
            BlackScholes.setGreeksByOptionPrice(data, r);

        } else {

            data.setIV(iv);
            data.setValue(DELTA_PER_OPTION, delta);
            data.setValue(GAMMA_PER_OPTION, gamma);
            data.setValue(VEGA_PER_OPTION, vega);
            data.setValue(THETA_PER_OPTION_PER_DAY, theta);
            BlackScholes.vanna(data, BlackScholes.RISK_FREE_RATE_OF_RETURN, true);
            BlackScholes.vomma(data, BlackScholes.RISK_FREE_RATE_OF_RETURN, true);
        }

        return data;

    }

    public boolean isITM() {
        return !isOTM();
    }

    public boolean isOTM() {

        double current = getCurrentStockPrice();
        return isOTM(current);

    }

    public boolean isOTM(double current) {
        double strike = getStrikePrice();

        if (isCall()) {
            return current < strike;
        }

        return current > strike;
    }

    public boolean isCall() {
        return call;
    }

    public void setCall(boolean call) {
        this.call = call;
    }

    @Override
    public TimeSeriesPointGroup createPointGroup(TimeSeriesPoint defaultPoint) {
        return new OptionData(defaultPoint);
    }

    public final Symbol getOptionTicker() {

        if (optionTicker == null) {
            optionTicker = new Symbol(getOpraCode(), Symbol.TICKER);
        }

        return optionTicker;
    }

    public final void setOptionTicker(Symbol optionTicker) {
        this.optionTicker = optionTicker;
    }

    public String getOpraCode() {
        return OptionData.createOpraCode(this);
    }

    public final Symbol getStockTicker() {
        return stockTicker;
    }

    public final void setStockTicker(Symbol stockTicker) {
        this.stockTicker = stockTicker;
    }

    public Symbol getSymbol() {
        return getOptionTicker();
    }

    @Override
    public void setValue(String name, double value) {
        super.setValue(name, value);
        resetCachedValues();
    }

    public void resetCachedValues() {
        iv = Double.NaN;
    }

    /**
     * sort an option chain by Strike rather than a TimeSeries.
     */
    public int compareTo(OptionData tsd) {

        assert tsd != null : "null compareTo obj.";

        if (isCall() && !tsd.isCall()) {
            return -1;
        }
        if (!isCall() && tsd.isCall()) {
            return 1;
        }

        int i = dateCompareTo(getDate(), tsd.getDate());

        // We compare strikes rather than option prices since option prices can be skewed.
        if (i == 0) {
            i = doubleCompareTo(getStrikePrice(), tsd.getStrikePrice());
        }

        return i;

    }

    public double getAsk() {
        if (Double.isNaN(ask)) {
            ask = getValue(ASK);
        }
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
        setValue(ASK, ask);
        mid = Double.NaN;
    }

    public double getBid() {
        if (Double.isNaN(bid)) {
            bid = getValue(BID);
        }
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
        setValue(BID, bid);
        mid = Double.NaN;
    }

    public double getOpenInterest() {
        if (Double.isNaN(oi)) {
            oi = getValue(OPEN_INTEREST);
        }
        return oi;
    }

    public double getMid() {

        if (!Double.isNaN(mid)) {
            return mid;
        }

        if (isBidAskAvailable()) {

            double bid = getBid();
            double ask = getAsk();

//				bid = roundIfWithinTolerance(bid);

            double bidask = (bid + ask);

            double d = 50d * bidask; // 100 contracts*(bid+ask)/2.0d;

            double floor = Math.floor(d);
            double decimal = d - floor;
            if (decimal < .25) {
                decimal = 0;
            } else if (decimal < .75) {
                decimal = .5;
            } else {
                decimal = 1;
            }
            mid = (floor + decimal) / 100d;
        } else {
            mid = getValue(LAST_TRADED_OPTION_PRICE);
        }

        if (Double.isNaN(mid)) {
            // If the option is in the last week of trading and it is far OTM there might not be a quote...
            if (getDte() < 5 && Double.isNaN(getIV())) {
                return 0;
            }
            // For debugging.
            mid = Double.NaN;
        }

        return mid;
    }

    public void setBidAsk(double bid, double ask) {

        setAsk(ask);
        setBid(bid);

    }

    public boolean isBidAskAvailable() {
        return isAskAvailable() && isBidAvailable();
    }

    public boolean isAskAvailable() {
        try {
            double ask = getAsk();
            return !Double.isNaN(ask) && ask != 0d;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBidAvailable() {
        try {
            double bid = getBid();
            return !Double.isNaN(bid) && bid != 0d;
        } catch (Exception e) {
            return false;
        }
    }

    public double getPercentAboveTheMoney() {
        double strike = getStrikePrice();
        double stock = getCurrentStockPrice();
        return (strike - stock) / stock;
    }

    public OptionData copy() {
        OptionData copy = (OptionData) super.copy();
        copy.setCall(isCall());
        copy.setCombinationStrategy(getCombinationStrategy());
        copy.setDate(getDate().copy());
        copy.setOptionTicker(getOptionTicker().copy());
        copy.setStockTicker(getStockTicker().copy());
        copy.setOrderedNames(getOrderedNames());
        return copy;
    }

    public Date getExpiryDate() {

        if (expiryDate == null) {

            double dte = getDte();
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, (int) dte);

            expiryDate = c.getTime();

        }

        return expiryDate;
    }

    public void setExpiryDate(Date date) {
        expiryDate = date;
    }

    public double getDelta() {
        return getValue(OptionData.DELTA_PER_OPTION);
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public boolean hasAllSviCalculations() {
        return contains(OptionData.SVI_A) && contains(OptionData.SVI_B) && contains(OptionData.SVI_P) && contains(OptionData.SVI_M) && contains(OptionData.SVI_SIGMA);
    }

    public double getDte() {
        if (dte == -1) {
            dte = getValue(OptionData.DAYS_TO_EXPIRATION);
        }
        return dte;
    }

    public void setDte(double dte) {
        setValue(OptionData.DAYS_TO_EXPIRATION, dte);
        setValue(OptionData.D1, Double.NaN);
        this.dte = dte;
    }

    public double getCurrentStockPrice() {
        if (Double.isNaN(currentStockPrice)) {
            currentStockPrice = getValue(OptionData.CURRENT_STOCK_PRICE);
        }
        return currentStockPrice;
    }

    public void setCurrentStockPrice(double currentStockPrice) {
        setValue(OptionData.CURRENT_STOCK_PRICE, currentStockPrice);
        setValue(OptionData.D1, Double.NaN);
        this.currentStockPrice = currentStockPrice;
    }

    public double getStrikePrice() {
        if (Double.isNaN(strikePrice)) {
            strikePrice = getValue(OptionData.STRIKE_PRICE);
        }
        return strikePrice;
    }

    public void setStrikePrice(double strikePrice) {
        setValue(OptionData.STRIKE_PRICE, strikePrice);
        this.strikePrice = strikePrice;
    }

    public double getIV() {
        if (Double.isNaN(iv)) {
            iv = getValue(OptionData.IMPLIED_VOLATILITY_PER_OPTION);
        }
        return iv;
    }

    public void setIV(double v) {
        setValue(OptionData.IMPLIED_VOLATILITY_PER_OPTION, v);
        setValue(OptionData.D1, Double.NaN);
        iv = v;
    }

    public double getOriginalIV() {
        if (Double.isNaN(originalIV)) {
            originalIV = getValue(OptionData.ORIGINAL_IMPLIED_VOLATILITY_PER_OPTION);
        }
        return originalIV;
    }

    public void setOriginalIV(double v) {
        setValue(OptionData.ORIGINAL_IMPLIED_VOLATILITY_PER_OPTION, v);
        originalIV = v;
    }

    public double getD1() {
        if (Double.isNaN(d1)) {
            d1 = getValue(OptionData.D1);
        }
        return d1;
    }

    public void setD1(double v) {
        setValue(OptionData.D1, v);
        d1 = v;
    }

    public double getOriginalMid() {
        if (Double.isNaN(originalMid)) {
            if (contains(ORIGINAL_OPTION_MID)) {
                originalMid = getValue(ORIGINAL_OPTION_MID);
            }
        }
        return originalMid;
    }

    public void setOriginalMid(double v) {
        setValue(OptionData.ORIGINAL_OPTION_MID, v);
        originalMid = v;
    }

    public int getTradeVolume() {
        if (tradeVolume < 0) {
            tradeVolume = getValueAsInt(OptionData.TRADE_VOLUME);
        }
        return tradeVolume;
    }

    public void setTradeVolume(int v) {
        setValue(OptionData.TRADE_VOLUME, v);
        tradeVolume = v;
    }

    public double getVolArbOffset() {
        return volArbOffset;
    }

    public void setVolArbOffset(double v) {
        volArbOffset = v;
    }

    public int getBidSize() {
        return getValueAsInt(OptionData.BID_SIZE);
    }

    public void setBidSize(int bidsize) {
        setValue(OptionData.BID_SIZE, bidsize);
    }

    public int getAskSize() {
        return getValueAsInt(OptionData.ASK_SIZE);
    }

    public void setAskSize(int size) {
        setValue(OptionData.ASK_SIZE, size);
    }

    public double getInterestRate() {
        if (Double.isNaN(interestRate)) {
            interestRate = getValue(OptionData.INTEREST_RATE);
        }
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        setValue(OptionData.INTEREST_RATE, interestRate);
        this.interestRate = interestRate;
    }

    public boolean isUnderlying() {
        return underlying;
    }

    public void setUnderlying(double multiplier) {
        this.underlying = true;
        setOptionTicker(getStockTicker());
        mid = Double.NaN;
        setBid(getCurrentStockPrice() * multiplier);
        setAsk(getCurrentStockPrice() * multiplier);

        setDte(Double.NaN);
        setExpiryDate(null);

        if (contains(ORIGINAL_IMPLIED_VOLATILITY_PER_OPTION)) {
            setOriginalIV(0d);
        }

        if (contains(ORIGINAL_OPTION_MID)) {
            setOriginalMid(0d);
        }

        setValueOnlyIfAlreadySet(ORIGINAL_DELTA_PER_OPTION, 1d);
        setValueOnlyIfAlreadySet(ORIGINAL_GAMMA_PER_OPTION, Double.NaN);
        setValueOnlyIfAlreadySet(ORIGINAL_VEGA_PER_OPTION, Double.NaN);
        setValueOnlyIfAlreadySet(ORIGINAL_THETA_PER_OPTION_PER_DAY, Double.NaN);
        setValueOnlyIfAlreadySet(DELTA_PER_OPTION, 1d);
        setValueOnlyIfAlreadySet(GAMMA_PER_OPTION, Double.NaN);
        setValueOnlyIfAlreadySet(VEGA_PER_OPTION, Double.NaN);
        setValueOnlyIfAlreadySet(VANNA, Double.NaN);
        setValueOnlyIfAlreadySet(VOMMA, Double.NaN);
        setValueOnlyIfAlreadySet(THETA_PER_OPTION_PER_DAY, Double.NaN);
        setValueOnlyIfAlreadySet(SVI_A, Double.NaN);
        setValueOnlyIfAlreadySet(SVI_B, Double.NaN);
        setValueOnlyIfAlreadySet(SVI_P, Double.NaN);
        setValueOnlyIfAlreadySet(SVI_M, Double.NaN);
        setValueOnlyIfAlreadySet(SVI_SIGMA, Double.NaN);

        // IV should be the last value set.
        setIV(Double.NaN);

    }

    protected void setValueOnlyIfAlreadySet(String name, double value) {
        if (contains(name)) {
            setValue(name, value);
        }

    }


}
