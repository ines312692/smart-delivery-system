package com.delivery.common.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private ValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    private static final Pattern ZIPCODE_PATTERN =
            Pattern.compile("^\\d{5}(-\\d{4})?$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidZipCode(String zipCode) {
        return zipCode != null && ZIPCODE_PATTERN.matcher(zipCode).matches();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("<[^>]*>", "");
    }

    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null || min == null || max == null) {
            return false;
        }
        double val = value.doubleValue();
        return val >= min.doubleValue() && val <= max.doubleValue();
    }
}
