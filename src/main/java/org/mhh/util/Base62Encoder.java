package org.mhh.util;

/**
 * @auther:MHEsfandiari
 */

public class Base62Encoder {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = CHARACTERS.length();

    public static String encode(long number) {
        if (number == 0) {
            return String.valueOf(CHARACTERS.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(CHARACTERS.charAt((int) (number % BASE)));
            number /= BASE;
        }

        return sb.reverse().toString();
    }
}