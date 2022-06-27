package com.tmck.svi.utils;

/**
 * Generic String Utilities
 *
 * @author tim
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static String substitute(String source, char charToReplace, char substituteChar) {

        assert source != null : "no source String";

        StringBuilder buf = new StringBuilder(source.length());
        int len = source.length();

        for (int i = 0; i < len; i++) {
            char c = source.charAt(i);
            if (c == charToReplace) {
                c = substituteChar;
            }
            buf.append(c);
        }

        return buf.toString();

    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

}
