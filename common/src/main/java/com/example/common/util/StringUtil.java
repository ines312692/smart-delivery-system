package com.delivery.common.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class StringUtil {

    private StringUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ALPHANUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateShortUUID() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public static String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + generateShortUUID();
    }

    public static String generatePaymentNumber() {
        return "PAY-" + System.currentTimeMillis() + "-" + generateShortUUID();
    }

    public static String generateDeliveryNumber() {
        return "DEL-" + System.currentTimeMillis() + "-" + generateShortUUID();
    }

    public static String generateTrackingNumber() {
        return "TRK-" + generateRandomString(12);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 2) {
            return "***@" + parts[1];
        }
        return localPart.substring(0, 2) + "***@" + parts[1];
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        return "***" + phone.substring(phone.length() - 4);
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}