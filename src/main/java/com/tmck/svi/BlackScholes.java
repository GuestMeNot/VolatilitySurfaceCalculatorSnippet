package com.tmck.svi;

import java.util.HashMap;
import java.util.Map;

import com.tmck.svi.valueobjs.OptionData;


/**
 * Calculate Option values using the Black Scholes formula - No dividends for now!
 */
public class BlackScholes {

	public static final double TRADING_DAYS_IN_YEAR = 365d;
	
	/** 
	 * If we are more than 3 years out then it is a distinct possibility that DTE was expressed as calendars days not a fraction of a year!
	 * Besides if you are trading options with less than 3 DTE then the standard pricing models probably won't work.
	 * If you are trading options with more then 3 years to expiration then you are on your own.
	 */ 
	public static final double MIN_DTE = 3d;

	// TODO this should be parameterized!
	public static final double RISK_FREE_RATE_OF_RETURN = 0;
	
	/** From Wikipedia Greeks (Finance) */
	public static double gamma(OptionData data, double r, boolean setGamma) {

		double S = data.getCurrentStockPrice();
		double X = data.getStrikePrice(); 
		double dte = data.getDte();
		double v = data.getIV();

		double gamma = gamma(data.isCall(), S, X, dte, r, v);
		
		if(setGamma) {
			data.setValue(OptionData.GAMMA_PER_OPTION, gamma);
		}
		
		return gamma;
	}


	/** 
	 * Calculate the Gamma for an option.
	 * 
	 * @param S - the current price of the stock
	 * @param X - the strike price of the option
	 * @param T - The option's Days to Expiration DTE as a percentage of a calendar year (e.g. 30 DTE would be passed in as 30d/365d).
	 * @param r - the risk free rate of return as a decimal. (e.g. a 2.5% risk free rate would be passed in as 2.5d/100d or 0.0025)
	 * @param v - the implied volatility expressed as a decimal. (e.g. a 25 IV would be passed in as .25).
	 * 
	 * @return The Gamma expressed as a decimal (e.g. a 25 Gamma will be returned as .25).
	 *
	 * @see Wikipedia Greeks (Finance)
	 */

	public static double gamma(boolean isCallOption, double S, double X, double dte, double r, double v) {

		double T = convertDteToYears(dte);

		// IV should be between (0, 1) but is oftern quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		double d1 = d1(S, X, dte, r, v);
		double gamma = phi(d1)/(S*v*Math.sqrt(T));
		
		return gamma;
		
	}

	/** From Wikipedia Greeks (Finance) */
	public static double vega(OptionData data, double r, boolean setVega) {

		double S = data.getCurrentStockPrice();
		double X = data.getStrikePrice(); 
		double v = data.getIV();

		double vega = vega(data.isCall(), S, X, data.getDte(), r, v);

		if(setVega) {
			data.setValue(OptionData.VEGA_PER_OPTION, vega);
		}

		return vega;
	}
	
	public static double vomma(OptionData data, double r, boolean setVomma) {

		double v = data.getIV();

		double d1 = Double.NaN;
		if(!data.contains(OptionData.D1)) {
			d1 = d1(data, r, setVomma);
		} else {
			d1 = data.getValue(OptionData.D1);
		}

		double vega = Double.NaN;
		if(!data.contains(OptionData.VEGA_PER_OPTION)) {
			vega = vega(data, r, setVomma);
		} else {
			vega = data.getValue(OptionData.VEGA_PER_OPTION);
		}

		double vomma = vomma(d1, data.getDte(), v, vega);
		
		if(setVomma) {
			data.setValue(OptionData.VOMMA, vomma);
		}
		
		return vomma;
	}
	

	public static double vomma(double d1, double dte, double v, double vega) {
		
		// IV should be between (0, 1) but is often quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		double d2 = d2(d1, v, dte);
		
		return vega*d1*d2/v;
	}

	public static double vanna(OptionData data, double r, boolean setVanna) {

//		double S = data.getCurrentStockPrice();
//		double X = data.getStrikePrice(); 
		double dte = data.getDte();
		double v = data.getIV();

		double d1 = d1(data, r, setVanna);
		
		double vanna = vanna(d1, dte, v);
		
		if(setVanna) {
			data.setValue(OptionData.VANNA, vanna);
		}
		
		return vanna;
	}

	public static double vanna(double d1, double dte, double v) {
		
		// IV should be between (0, 1) but is often quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		double d2 = d2(d1, v, dte);
		
		double phiD1 = phi(d1);
		
		return -phiD1*d2/v;
	}

	/** 
	 * Calculate the vega for an option.
	 * 
	 * Vega is typically expressed as the amount of money per underlying share that the option's value 
	 * will gain or lose as volatility rises or falls by 1%.
	 * 
	 * The current implementation estimates the vega using first difference approximation.
	 * 
	 * @param S - the current price of the stock
	 * @param X - the strike price of the option
	 * @param T - The option's Days to Expiration DTE as a percentage of a calendar year (e.g. 30 DTE would be passed in as 30d/365d).
	 * @param r - the risk free rate of return as a decimal. (e.g. a 2.5% risk free rate would be passed in as 2.5d/100d or 0.0025)
	 * @param v - the implied volatility expressed as a decimal. (e.g. a 25 IV would be passed in as .25).
	 * 
	 * @return The vega expressed as a decimal (e.g. a 25 Vega will be returned as .25).
	 *
	 * @see Wikipedia Greeks (Finance)
	 */
	public static double vega(boolean isCallOption, double S, double X, double dte, double r, double v) {

		double T = convertDteToYears(dte);

		// IV should be between (0, 1) but is oftern quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		double d1 = d1(S, X, dte, r, v);

		double vega = S*phi(d1)*Math.sqrt(T)/100;
		
		return vega;

	}
	
	
	/** 
	 * Calculate the Delta for an option.
	 * 
	 * @param S - the current price of the stock
	 * @param X - the strike price of the option
	 * @param T - The option's Days to Expiration DTE as a percentage of a calendar year (e.g. 30 DTE would be passed in as 30d/365d).
	 * @param r - the risk free rate of return as a decimal. (e.g. a 2.5% risk free rate would be passed in as 2.5d/100d or 0.0025)
	 * @param v - the implied volatility expressed as a decimal. (e.g. a 25 IV would be passed in as .25).
	 * 
	 * @return The Delta expressed as a decimal (e.g. a 25 Delta will be returned as .25).
	 *
	 * @see Wikipedia Greeks (Finance)
	 */
	public static double delta(boolean isCallOption, double S, double X, double dte, double r, double v) {

		// IV should be between (0, 1) but is oftern quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		double d1 = d1(S, X, dte, r, v);
		return delta(isCallOption, d1);
	}
	
	/** From Wikipedia Greeks (Finance) */
	public static double delta(OptionData data, double r) {
		return delta(data.isCall(), d1(data, r, false));
	}

	public static double delta(OptionData data, double r, boolean setDelta) {
		double delta = delta(data.isCall(), d1(data, r, setDelta));
		if(setDelta) {
			data.setValue(OptionData.DELTA_PER_OPTION, delta);
		}
		return delta;
	}

	public static double delta(boolean isCallOption, double d1) {
	
		if (isCallOption) {
			return CND(d1);
		} else {
			return -1.0*CND(-d1);
		}
		
	}

	
	public static double d1(OptionData data, double r, boolean setD1) {
		
		if(data.contains(OptionData.D1)) {
			return data.getD1();
		}
		
		double S = data.getCurrentStockPrice();
		double X = data.getStrikePrice(); 
		double v = data.getIV();
		
		double d1 = d1(S, X, data.getDte(), r, v);
		if(setD1) {
			data.setD1(d1);
		}
		return d1;
	}
	
	/** T is expressed in years. */
	public static double d2(double d1, double v, double dte) {
		
		double T = convertDteToYears(dte);
		
		// IV should be between (0, 1) but is often quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		return d1-v*Math.sqrt(T);
	}
	
	public static double d1(double S, double X, double dte, double r, double v) {
		
		if(X < 0) {
			throw new IllegalStateException("Negative Strike: " + X);
		}

		if(Double.isNaN(X)) {
			throw new IllegalStateException("NaN Strike Price!");			
		}

		if(S < 0) {
			throw new IllegalStateException("Negative Stock Price: " + S);			
		}

		if(Double.isNaN(S)) {
			throw new IllegalStateException("NaN Stock Price!");			
		}

		if(v < 0) {
			throw new IllegalStateException("Negative IV: " + v);			
		}

		if(v > 1) {
			throw new IllegalStateException("IV should be between (0, 1): " + v);
		}
		
		if(Double.isNaN(v)) {
			throw new IllegalStateException("NaN IV!");
		}

		if(r < 0) {
			throw new IllegalStateException("Negative r: " + r);			
		}

		if(Double.isNaN(r)) {
			throw new IllegalStateException("NaN r!");		
		}

		if(dte < 0) {
			throw new IllegalStateException("Negative dte: " + dte);			
		}

		if(Double.isNaN(dte)) {
			throw new IllegalStateException("NaN dte!");		
		}

		double T = convertDteToYears(dte);
		
		// IV should be between (0, 1) but is oftern quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		double logSX = logStrike(S ,X);
		double rV2 = (r+v*v/2d);
		double sqrtT = sqrt(T);
		double denom = (v*sqrtT);
		double numerator = (logSX+rV2*T);
		
		double d1 = numerator/denom;
		
		if(Double.isNaN(d1)) {
			System.err.println("Error: Option Price D1 is NaN: call: S: " + S + " X: " + X + " T: " + T + " r: " + r + " iv: " + v);
		}
		
		return d1;
		
	}
	
	public static double sqrt(double T) {
		return Math.sqrt(T);
	}
	
	public static double logStrike(double S, double X) {
		
		return Math.log(S/X);
		
	}

	
	public static double impliedVolatility(OptionData data, double r, boolean setIV) {
		
		boolean isCallOption = data.isCall();
		double S = data.getCurrentStockPrice();
		double X = data.getStrikePrice(); 
		double price = data.getMid();
		
		double v = impliedVolatility(isCallOption, S, X, data.getDte(), r, price);
		
		if(setIV) {
			data.setIV(v);
		}
		
		return v;
		
	}


	/** 
	 * The Newton Raphson method for finding the IV using Black Scholes.
     *
	 * @param S - the current price of the stock
	 * @param X - the strike price of the option
	 * @param T - The Days to Expiration or DTE for the option expressed as a portion of a calendar year (e.g. 30 DTE would be passed in as 30d/365d).
	 * @param r - the risk free rate of return as a decimal. (e.g. a 2.5% risk free rate would be passed in as 2.5d/100d or 0.0025)
	 * @param optionPrice - the price of a single option not a contract.
	 * 
	 * @return The implied volatility expressed as a decimal (e.g. a 25 IV would be returned as .25)
	 * 
	 * @see http://www.softwareandfinance.com/Derivatives/OptionsPricingModel/ImpliedOptionsVolatility.html
	 */
	public static double impliedVolatility(boolean isCallOption, double S, double X, double T, double r, double optionPrice) {
		
		    double cpTest = 0;
		    
		    // An IV > 1 would represent an arbitrage opportunity!!!  
		    double IV = 1d;

		    double upper = 1d;
		    double lower = 0;
		    double range = Math.abs(lower - upper);
		 
		    // This algorithm uses the bisection method.
		    // It can be combined with the secant method to become Brent's method.
		    while(true) {
		 
		        // Get the option price using black scholes formula
		        cpTest = optionPrice(isCallOption, S, X, T, r, IV);
		 
		        // Implied Volatility - IV  has to go down
		        if(cpTest > optionPrice) {  
		            upper = IV;
		            IV = (lower + upper) / 2;
		        } else {
		            // Implied Volatility - IV has to go up
		            lower = IV;
		            IV = (lower + upper) / 2;
		        }
		        range = Math.abs(lower - upper);
		        if( range < 0.001) {
		            break;
		        }
		    }
		    
		    return IV;
		
	}
	
	/** @see http://finance.bi.no/~bernt/gcc_prog/algoritms_v1/algoritms/node8.html */
	public static double impliedVolatilityNewtonRaphson(
		     boolean call, double S, double X, double r, double T, double option_price)
		{
		  // check for arbitrage violations:
		  // if price at almost zero volatility greater than price, return 0
		  double sigma_low = 1e-5;
		  double price = optionPrice(call, S, X, T, r, sigma_low);
		  if (price > option_price) {
			  return 0.0;
		  }

		  final int MAX_ITERATIONS = 100;
		  final double ACCURACY    = 1.0e-4; 
		  double t_sqrt = Math.sqrt(T);

		  double callPrice = option_price;
		  if(!call) {
			  callPrice = putCallParity(call, S, X, T, r, option_price);
		  }
		  
		  double sigma = (callPrice/S)/(0.398*t_sqrt)/100;    // find initial value
		  for (int i=0;i<MAX_ITERATIONS;i++) {
			price = optionPrice(call, S, X, T, r, sigma);			  
		    double diff = option_price -price;
		    if (Math.abs(diff)<ACCURACY) {
		    	return sigma;
		    }
		    
		    double vega = vega(call, S, X, T, r, sigma);
		    sigma = sigma + diff/vega;
		  };
		  return -99e10;  // something bizarre happened, should throw exception
	}

	
	/** 
	 * The Black and Scholes (1973) Stock option formula
	 * 
	 * @param S - the current price of the stock
	 * @param X - the strike price of the option
	 * @param T - The option's Days to Expiration DTE as a percentage of a calendar year (e.g. 30 DTE would be passed in as 30d/365d).
	 * @param r - the risk free rate of return as a decimal. (e.g. a 2.5% risk free rate would be passed in as 2.5d/100d or 0.0025)
	 * @param v - the implied volatility expressed as a decimal. (e.g. a 25 IV would be passed in as .25).
	 * 
	 * @return The price for a single option rather than a contract.
	 *
	 * @see http://www.espenhaug.com/black_scholes.html
	 */
	public static double optionPrice(boolean isCallOption, double S, double X, double dte, double r, double v) {
					
		double T = convertDteToYears(dte);
		
		// IV should be between (0, 1) but is often quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1): " + v);
		}
		
		double d1 = d1(S, X, dte, r, v);
		double d2 = d2(d1, v, dte);
	
		double price = Double.NaN;
//		double et = Math.exp(-r*T);
//		double ket = X*et;
		if (isCallOption) {
			price = callOptionPrice(S, X, T, r, d1, d2);
		} else {
			price = putOptionPrice(S, X, T, r, d1, d2);
		}
	
		// There is some rounding error that allows a price to be less than zero!
		// So we set the option price to zero if we are within this rounding error!
		if(Math.abs(price) < .01d) {
			price = 0d;
		}
		
		if(Double.isNaN(price) || price < 0) {
			System.err.println("Error: Option Price is NaN or Negative: call: " + isCallOption + " S: " + S + " X: " + X + " T: " + T + " r: " + r + " iv: " + v);
		}

		return price;
	}

	public static double putOptionPrice(double S, double X, double T, double r, double d1, double d2) {
		double ket = X*Math.exp(-r*T);
		double cndD2 = CND(-d2);
		double cndD1 = CND(-d1);
		return ket*cndD2-S*cndD1;
	}
	
	public static double callOptionPrice(double S, double X, double T, double r, double d1, double d2) {
		double ket = X*Math.exp(-r*T);
		return S*CND(d1)-ket*CND(d2);
	}
	
	public static double optionPrice(OptionData data, double r) {
		
		double iv = data.getIV();
		if(iv > 1d) {
			iv = 1d;
		}
		
		return optionPrice(data, iv, r);
		
	}
	
	public static double optionPrice(OptionData data, double iv, double r) {

		boolean isCallOption = data.isCall();
		
		double S = data.getCurrentStockPrice();
		double X = data.getStrikePrice(); 
		
		double price = optionPrice(isCallOption, S, X, data.getDte(), r, iv);
				
		return price;
		
	}
	
	/** From Wikipedia Greeks (Finance) */
	public static double theta(OptionData data, double r, boolean setTheta) {

		double S = data.getCurrentStockPrice();
		double X = data.getStrikePrice(); 
		double v = data.getIV();

		double theta = theta(data.isCall(), S, X, data.getDte(), r, v);

		if(setTheta) {
			data.setValue(OptionData.THETA_PER_OPTION_PER_DAY, theta);
		}

		return theta;
	}
	
	public static double theta(boolean isCallOption, double S, double X, double dte, double r, double v) {

		double T = convertDteToYears(dte);
		
		// IV should be between (0, 1) but is oftern quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		double d1 = d1(S, X, dte, r, v);
		double d2 = d2(d1, v, dte);
		
		double term1 = (-S*phi(d1)*v)/(2*Math.sqrt(T));
		if (isCallOption) {
			double term2 = r*X*Math.exp(-r*T)*CND(d2);
			double yearlyTheta = term1-term2; 
			
			// Divide by the number of trading days in a year by convention to get theta per day.
			return yearlyTheta / TRADING_DAYS_IN_YEAR;
			
		}
		
		double term2 = (r*X*Math.exp(-r*T)*CND(-d2));
		double yearlyTheta = term1+term2;
		
		// Divide by the number of trading days in a year by convention to get theta per day.
		double dailyTheta = yearlyTheta / TRADING_DAYS_IN_YEAR;
		if(Math.abs(dailyTheta) < .001d) {
			dailyTheta = 0d; 
		}
		
		return dailyTheta;
	}

	public static double rho(boolean isCallOption, double S, double X, double dte, double r, double v) {
		
		double T = convertDteToYears(dte);
		
		// IV should be between (0, 1) but is often quoted between (0, 100)
		// it would be extremely rare for an IV quoted from 0,100 to be below 1 
		// so we convert it.
		if(v > 1d) {
			throw new IllegalStateException("IV should be between (0, 1)");
		}

		
		Double optionPrice = optionPrice(isCallOption, S, X, T, r, v);
		Double optionPriceDelta = optionPrice(isCallOption, S, X, T, r + .001d, v);
		
		if(isCallOption) {
			return  (optionPriceDelta - optionPrice) * 1000d;
		} 
		return  Math.abs(optionPrice - optionPriceDelta) * 1000d;
	}
	
	/** The cumulative normal distribution function. */ 
	public static double CND(double x) {
	    int neg = (x < 0d) ? 1 : 0;
	    if ( neg == 1) 
	        x *= -1d;

	    double k = (1d / ( 1d + 0.2316419 * x));
	    double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
	                   k - 0.356563782) * k + 0.319381530) * k;
	    y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

	    return (1d - neg) * y + neg * (1d - y);
	}
	
	/** 
	 * The standard normal distribution density function.
	 * @see http://introcs.cs.princeton.edu/java/22library/Gaussian.java.html
	 * @see http://www.itl.nist.gov/div898/handbook/eda/section3/eda3661.htm
	 */ 
    protected static double phi(double x) {
        return Math.exp( (-(x*x)) / 2) / Math.sqrt(2 * Math.PI);
    }
    
    /** @see http://en.wikipedia.org/wiki/Put%E2%80%93call_parity */
    public static double putCallParity(OptionData data, double r) {

    	double optionPrice = data.getMid();
    	double S = data.getCurrentStockPrice();
		double dte = data.getDte();
		double X = data.getStrikePrice();
		boolean call = data.isCall();
		
		return putCallParity(call, S, X, dte, r, optionPrice);
		
    }
    
    /** @see http://en.wikipedia.org/wiki/Put%E2%80%93call_parity */
    public static double putCallParity(boolean isCallOptionPricePassed, double S, double X, double dte, double r, double optionPrice) {

    	double T = convertDteToYears(dte);
    	
    	double KET = X*Math.exp(-1*r*T);
		if(isCallOptionPricePassed) {
			return KET - S + optionPrice;
		} else {
			return optionPrice - KET + S;    		
		}
		
    }
    
	public static void setGreeksByOptionPrice(OptionData data, double interestRate) {
		
        double asset = data.getCurrentStockPrice();
        double strike = data.getStrikePrice();
        double price = data.getMid();
        
        // This can happen after the data suppliers have cleared the previous day's data but before the market makers supply bid/ask prices!
        if(price < .00001d) {
        	price = data.get(OptionData.LAST_TRADED_OPTION_PRICE).getValue();
        }
        
        if(Double.isNaN(interestRate)) {
        	interestRate = RISK_FREE_RATE_OF_RETURN;
        }
        
        data.setInterestRate(interestRate);
        
        double time = data.getDte();
        boolean isCall = data.isCall();
        
        double iv = Double.NaN;
        if(data.contains(OptionData.IMPLIED_VOLATILITY_PER_OPTION)) {
			iv = data.getIV();        	
        } else {
			iv = impliedVolatility(isCall, asset, strike, time, interestRate, price);
			data.setIV(iv);
        }
        
        if(Double.isNaN(price) && !Double.isNaN(iv)) {
        	price = optionPrice(data, interestRate);
        	data.setAsk(price);
        	data.setBid(price);
        }
        
        if(!data.contains(OptionData.DELTA_PER_OPTION)) {
        	double delta =  delta(isCall, asset, strike, time, interestRate, iv);
    		data.setValue(OptionData.DELTA_PER_OPTION, delta);
        }
        
        if(!data.contains(OptionData.VEGA_PER_OPTION)) {
        	double vega = vega(isCall, asset, strike, time, interestRate, iv);
    		data.setValue(OptionData.VEGA_PER_OPTION, vega);
        }
        
        if(!data.contains(OptionData.GAMMA_PER_OPTION)) {
        	double gamma = gamma(isCall, asset, strike, time, interestRate, iv);
    		data.setValue(OptionData.GAMMA_PER_OPTION, gamma);        	
        }
		
        double theta = Double.NaN;
		double existingTheta = Double.NaN;
		if(data.contains(OptionData.THETA_PER_OPTION_PER_DAY)) {
			existingTheta = data.getValue(OptionData.THETA_PER_OPTION_PER_DAY);
			theta = existingTheta;
		}
		
		// Theta is always negative!
		if(Double.isNaN(existingTheta) || existingTheta > 0) {
			theta = theta(isCall, asset, strike, time, interestRate, iv);
		}
		data.setValue(OptionData.THETA_PER_OPTION_PER_DAY, theta);
		
		
        if(!data.contains(OptionData.VANNA)) {
        	vanna(data, interestRate, true);
        }
        if(!data.contains(OptionData.VOMMA)) {
        	vomma(data, interestRate, true);
        }

		if(theta > 0) {
			// TODO double check this!
			data.setValue(OptionData.THETA_PER_OPTION_PER_DAY, 0d);			
		} else {
			data.setValue(OptionData.ORIGINAL_THETA_PER_OPTION_PER_DAY, theta);
		}

	}

	public static void setOriginalGreeksByOptionPrice(OptionData data, double interestRate) {
		
        double asset = data.getCurrentStockPrice();
        double currentDte = data.getDte();
		double dit = 0;
		
		if(data.contains(OptionData.DAYS_IN_TRADE)) {
			dit = data.getValue(OptionData.DAYS_IN_TRADE);
		}

		double dte = currentDte + dit;
		
        double strike = data.getStrikePrice();
        double price = data.get(OptionData.ORIGINAL_OPTION_PRICE).getValue();
        
        double time = dte;
        boolean isCall = data.isCall();
        
		double iv = impliedVolatility(isCall, asset, strike, time, interestRate, price);
		double delta =  delta(isCall, asset, strike, time, interestRate, iv);
		double vega = vega(isCall, asset, strike, time, interestRate, iv);
		double gamma = gamma(isCall, asset, strike, time, interestRate, iv);
		double theta = theta(isCall, asset, strike, time, interestRate, iv);
		
		
		data.setValue(OptionData.ORIGINAL_VEGA_PER_OPTION, vega);
		data.setValue(OptionData.ORIGINAL_GAMMA_PER_OPTION, gamma);
		data.setValue(OptionData.ORIGINAL_DELTA_PER_OPTION, delta);
		if(!data.contains(OptionData.ORIGINAL_IMPLIED_VOLATILITY_PER_OPTION)) {
			data.setValue(OptionData.ORIGINAL_IMPLIED_VOLATILITY_PER_OPTION, iv);
		}

		if(isCall && delta < 0) {
			System.err.println("BAD Call Delta: delta: " +  delta + "  OptionData: " + data.toString(true));			
		}

		if(!isCall && delta > 0) {
			System.err.println("BAD Put Delta: delta: " +  delta + "  OptionData: " + data.toString(true));			
		}
		
		// For Bad LiveVol Data.
		if(theta > 0.072d) {
			System.err.println("BAD Theta: theta: " +  theta + "  OptionData: " + data.toString(true));
		} else {
			theta = (theta > 0d) ? 0 : theta;
			data.setValue(OptionData.ORIGINAL_THETA_PER_OPTION_PER_DAY, theta);
		}

	}

	/**
	 * Get the DTE expressed not as days but in years. which is needed for Black-Scholes model. 
	 */
	public static double getT(OptionData data) {
	    double dte = data.getDte();
	    return convertDteToYears(dte);
	}
	
	/** Cached for performance. */
	private static Map<Double, Double> dteToYearsMap = new HashMap<Double, Double>();

	/**
	 * Convert the DTE expressed in days to time expressed in years. This is needed for Black-Scholes model. 
	 */
	public static double convertDteToYears(double dte) {
		
		if(dteToYearsMap.containsKey(dte)) {
			return dteToYearsMap.get(dte);
		}
		
		// If we are more than 3 years out then it is a distinct possibility that DTE was expressed as calendars days not a fraction of a year!
		// Besides if you are trading options with less than 3 DTE then the standard pricing models probably won't work.
		// If you are trading options with more then 3 years to expiration then you are on your own.
		if(dte % 1.0 > 0) {
			System.err.println("WARNING: perhaps DTE is in years: " + dte);
		}
	    double T = dte/TRADING_DAYS_IN_YEAR;
		dteToYearsMap.put(dte, T);
	    return T;
	}

	public static double[] getResampledOptionPrices(OptionData data, double[] resampledStockPrices, double[] resampledIVs, double dte, double r) {
			
			double X = data.getStrikePrice();
			
			if(X < 0) {
				System.err.println("Error: bad strike: " + X);
			}
			
			if(resampledStockPrices.length != resampledIVs.length) {
				int len1 = resampledStockPrices.length;
				int len2 = resampledIVs.length;
				throw new IllegalArgumentException("resampledStockPrices.length(" + len1 + ") = resampledIVs.length(" + len2 + ")");
			}

			double[] resampledOptionPrices = new double[resampledStockPrices.length];
			boolean call = data.isCall();
			for(int i = 0; i < resampledStockPrices.length; i++) {
				
				double S = resampledStockPrices[i];
				
				if(resampledIVs[i] > 1) {
					System.err.println("Warning: resetting IV to 1 rather than: " + resampledIVs[i]);
					resampledIVs[i] = 1d;
				}
				
				double resampledOptionPrice = optionPrice(call, S, X, dte, r, resampledIVs[i]);
							
				resampledOptionPrices[i] = resampledOptionPrice;
				
			}
			
			return resampledOptionPrices;
	}


	public static double[] getResampledThetas(OptionData data, double[] resampledStockPrices, double[] resampledIVs, double dte, double r) {
	
		double X = data.getStrikePrice();
	
		double[] resampledThetas = new double[resampledStockPrices.length];
		boolean call = data.isCall();
		for(int i = 0; i < resampledStockPrices.length; i++) {
			
			double S = resampledStockPrices[i];
			double resampledTheta = theta(call, S, X, dte, r, resampledIVs[i]);
			
			resampledThetas[i] = resampledTheta;
			
		}
		
		return resampledThetas;
	
	}


	public static void calculateValuesFromIV(OptionData datum) {
		
		double iv = datum.getIV();
		
		double price = optionPrice(datum, RISK_FREE_RATE_OF_RETURN);
		datum.setValue(OptionData.ORIGINAL_OPTION_PRICE, price);
		
		double bid = price - .01;
		double ask = price + .01;
		datum.setBidAsk(bid, ask);
		
		setOriginalGreeksByOptionPrice(datum, RISK_FREE_RATE_OF_RETURN);
		
		double newIv = datum.getIV();
		
		if(Math.abs(iv - newIv) > .001d) {
			throw new IllegalStateException();
		}
		 
	}

	

}