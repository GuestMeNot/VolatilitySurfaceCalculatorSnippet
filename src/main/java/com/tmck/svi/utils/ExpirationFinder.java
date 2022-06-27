package com.tmck.svi.utils;

import java.util.Date;


public final class ExpirationFinder {

    private ExpirationFinder() {
    }

    public static int getCalendarDaysToExpiration(Date expirationDate) {
        return getCalendarDaysToExpiration(new FastDate(), expirationDate);
    }

    public static int getCalendarDaysToExpiration(Date expiryDate,
                                                  Date currentDate) {
        return DateUtils.getDaysBetween(currentDate, expiryDate);
    }

}
