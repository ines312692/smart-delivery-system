package com.delivery.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtil {

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : null;
    }

    public static String formatISO(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_FORMATTER) : null;
    }

    public static LocalDateTime parse(String dateTimeString) {
        return dateTimeString != null ?
                LocalDateTime.parse(dateTimeString, DEFAULT_FORMATTER) : null;
    }

    public static LocalDateTime parseISO(String dateTimeString) {
        return dateTimeString != null ?
                LocalDateTime.parse(dateTimeString, ISO_FORMATTER) : null;
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    public static boolean isAfter(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isAfter(dateTime2);
    }

    public static boolean isBefore(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isBefore(dateTime2);
    }
}