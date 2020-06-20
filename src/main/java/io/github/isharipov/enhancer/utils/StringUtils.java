package io.github.isharipov.enhancer.utils;

public class StringUtils {
    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String uncapitalizeFirst(String str) {
        return str.substring(0, 1).toLowerCase();
    }
}
