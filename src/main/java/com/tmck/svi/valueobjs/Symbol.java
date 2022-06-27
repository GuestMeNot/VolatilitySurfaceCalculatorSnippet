package com.tmck.svi.valueobjs;

import com.tmck.svi.utils.StringUtils;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Collection;


/**
 * A Symbol has a name and a type.
 * <p>
 * Examples:
 * a ticker
 * a company name.
 * a Cusip.
 * a SEC CIK #.
 * a Fred series abbreviation.
 *
 * @author tim
 */
public class Symbol implements Comparable<Symbol>, Serializable {


    public static final String STANDARDIZED_DATA = "Standardized Data";
    public static final String EARNINGS_SUPRISE = "EARNINGS_SUPRISE";

    // NOTE : Don't make these an enumerated type otherwise
    //			it won't be able to be passed in from the command
    //			line.
    public static final String COMPANY_NAME = "COMPANY_NAME";
    public static final String TICKER = "TICKER";
    public static final String CIK = "CIK";
    public static final String CUSIP = "CUSIP";
    public static final String NAME = "NAME";
    public static final String FRED_NAME = "FRED_NAME";
    public static final String LABOR_NAME = "LABOR_NAME";
    public static final String BUS_CYCLE = "BUS_CYCLE";
    public static final String DERIVED_DATA = "DERIVED_DATA";
    public static final String OVERALL_MARKET = "OVERALL_MARKET";
    public static final String COMMODITY = "COMMODITY";
    public static final String CURRENCY_FUTURE = "CURRENCY_FUTURE";
    public static final String BOND = "BOND";
    public static final String FUNDAMENTAL = "FUNDAMENTAL";
    public static final String FUNCTION_VALUE = "FUNCTION_VALUE";
    public static final String REGRESSION_RESPONSE = "RESPONSE";
    public static final String USER_DEFINED = "USER_DEFINED";
    public static final String FORECLOSURE = "FORCLOSURE";
    public static final String ADDRESS = "ADDRESS";
    public static final String PERSON = "Person";
    public static final String FOMCBullBear = "FOMCBullBear";
    public static final String QUANDL_NAME = "Quandl";
    public static final String PORTFOLIO = "PORTFOLIO";
    public static final String[] VALID_DERIVED_SYMBOL_NAMES = {"close", "open", "volume"};
    public static final String SECTOR = "Sector";
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4051044159848984631L;
    private static final String DUMMY_TYPE = "Dummy Type";
    public static final String[] TYPES = {
            TICKER,
            CIK,
            COMPANY_NAME,
            CUSIP,
            NAME,
            EARNINGS_SUPRISE,
            FRED_NAME,
            LABOR_NAME,
            BUS_CYCLE,
            DERIVED_DATA,
            OVERALL_MARKET,
            COMMODITY,
            CURRENCY_FUTURE,
            BOND,
            FUNDAMENTAL,
            FUNCTION_VALUE,
            DUMMY_TYPE,
            STANDARDIZED_DATA,
            REGRESSION_RESPONSE,
            PORTFOLIO,
            USER_DEFINED
    };
    private static final String DUMMY_NAME = "Dummy";
    public static final Symbol DUMMY_SYMBOL = new Symbol(DUMMY_NAME, DUMMY_TYPE);
    private String name;
    private String type;
    private Symbol relatedSymbol = null;


    public Symbol(String symbol, String type) {
        this(symbol, type, null);
    }

    public Symbol(String symbol, String type, Symbol relatedSymbol) {
        setName(symbol);
        setType(type);
        setRelatedSymbol(relatedSymbol);
    }

    public static boolean isValidType(String type) {

        assert type != null : "no type";

        for (String to_check : TYPES) {
            if (type.equals(to_check)) {
                return true;
            }
        }

        return false;

    }

    public static boolean isValidDerivedSymbol(Symbol symbol) {

        if (symbol == null) {
            return false;
        }

        if (symbol.getType() == null) {
            return false;
        }

        if (DERIVED_DATA.equals(symbol.getType())) {
            return true;
        }

        for (String name : VALID_DERIVED_SYMBOL_NAMES) {
            if (symbol.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public Symbol getRelatedSymbol() {
        return relatedSymbol;
    }

    public void setRelatedSymbol(Symbol symbol) {
        assert symbol != null : "no name obj.";
        relatedSymbol = symbol;
    }

    public String getType() {
        return type;
    }

    protected void setType(String type) {
        assert isValidType(type) : "invalid type";
        this.type = type;
    }

    public String getName() {
        return name;
    }

    protected void setName(String string) {
        assert string != null : "no name string.";
        name = string;
    }

    public Symbol getRootSymbol() {

        Symbol root = this;

        Symbol wrapped = null;
        while ((wrapped = root.getRelatedSymbol()) != null) {
            root = wrapped;
        }

        return root;
    }

    public String getFileName() {
        return StringUtils.substitute(getCompleteName(), '^', '-');
    }

    public String getCompleteName() {
        String str = getName();
        Symbol related = getRelatedSymbol();
        if (related != null) {
            str += "(" + related.getCompleteName() + ")";
        }
        return str;
    }

    public boolean isType(String string) {
        return getType().equals(string);
    }

    /**
     * @param parentSymbol the symbol to check relation of.
     * @return true if this symbol (or one of its relates symbols) is related to the parentSymbol.
     */
    public boolean isRelatedSymbol(Symbol parentSymbol) {
        if (parentSymbol.equals(this))
            return true;

        Symbol related = getRelatedSymbol();
        while (related != null) {
            if (related.equals(parentSymbol)) {
                return true;
            }
            related = related.getRelatedSymbol();
        }

        return false;
    }

    public boolean matchesFileName(String name) {
        String fileName = getFileName();
        return fileName.equals(name);
    }

    public boolean matchesSubstituteSpecialChars(String name) {
        String fileName = substituteSpecialChars();
        return fileName.equals(name);
    }

    public int deepCompareTo(Symbol that) {
        int value = compareTo(that);
        if (value != 0) {
            return value;
        }
        Symbol related = getRelatedSymbol();
        Symbol thatRelated = that.getRelatedSymbol();

        if (related == null && thatRelated == null)
            return 0;
        if (thatRelated == null)
            return -1;
        if (related == null)
            return 1;

        return related.deepCompareTo(thatRelated);
    }

    public boolean deepEquals(Symbol that) {

        int value = deepCompareTo(that);
        return value == 0;

    }

    public String substituteSpecialChars() {
        return StringUtils.substitute(getCompleteName(), '^', '!');
    }

    public Symbol[] convertCollectionToArray(Collection<? extends Symbol> c) {
        return c.toArray(new Symbol[0]);
    }

    public Symbol copy() {

        String name = getName();
        String type = getType();
        Symbol related = getRelatedSymbol();

        if (related == null) {
            return new Symbol(name, type);
        }

        return new Symbol(name, type, related);

    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) {
            return false;
        }

        return compareTo((Symbol) obj) == 0;
    }

    /**
     * a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
     */
    public int compareTo(Symbol that) {

        if (that == null) {
            throw new NullPointerException("no that");
        }

        if (that.getName() == null) {
            throw new NullPointerException("no that.getName()");
        }

        if (this.getName() == null) {
            throw new NullPointerException("no this.getName()");
        }

        // NOTE: relatedSymbol and Type have not been added here intentionally.
        //  The reason is that most likely we are doing lookups or comparisons
        //  of an object in a map or list. When this is the case we probably
        //  don't care about the relatedSymbol and we may not know (or care about)
        //  the type.
        return this.getName().compareTo(that.getName());
    }

    public String toString() {
        String str = getName() + ":" + getType();
        Symbol related = getRelatedSymbol();
        if (related != null) {
            str += ":" + related;
        }
        return str;
    }

    @SuppressWarnings("deprecation")
    public String getURLEncodedName() {
        String name = getName();
        return URLEncoder.encode(name);
    }

}
