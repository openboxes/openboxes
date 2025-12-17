package org.pih.warehouse.core.date

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import org.pih.warehouse.DateUtil

/**
 * A formatter that converts Date objects to Strings.
 *
 * Designed to be temporary. We aim to eventually fully switch to java.time classes, which would deprecate this class.
 */
class JavaUtilDateFormatter implements IDateFormatter<Date> {

    private static final String EMPTY_DISPLAY_DATE = ''

    private static final DateTimeFormatter DEFAULT_DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy")
    private static final DateTimeFormatter DEFAULT_DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss XXX")
    private static final DateTimeFormatter DEFAULT_DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")

    @Override
    String format(Date date) {
        return formatAsDateTime(date)
    }

    /**
     * Converts a Date to a date-only string for display. This method should only be used by GSPs and file exporters.
     * Otherwise we should return the date object as is and let the frontend decide the display format.
     * Useful when working with old code that uses the old Date format.
     *
     * NOTE: For backwards compatibility purposes, the date will always be in the system timezone.
     *
     * @return a formatted date String. Ex: "01/Jan/2025"
     */
    static String formatAsDate(Date date) {
        return date
                ? DEFAULT_DISPLAY_DATE_FORMATTER.format(LocalDateParser.asLocalDate(date, DateUtil.systemZoneId))
                : EMPTY_DISPLAY_DATE
    }

    /**
     * Converts a Date to a date + time + offset string for display. This method should only be used by GSPs and file
     * exporters. Otherwise we should return the date object as is and let the frontend decide the display format.
     * Useful when working with old code that uses the old Date format.
     *
     * NOTE: For backwards compatibility purposes, the date will always be in the system timezone.
     *
     * @return a formatted datetime String. Ex: "01/Jan/2025 00:00:00 +05:00"
     */
    static String formatAsDateTime(Date date) {
        return date
                ? DEFAULT_DISPLAY_DATE_TIME_FORMATTER.format(ZonedDateTimeParser.asZonedDateTime(date, DateUtil.systemZoneId))
                : EMPTY_DISPLAY_DATE
    }

    /**
     * Converts a Date to a time-only string for display. This method should only be used by GSPs and file
     * exporters. Otherwise we should return the date object as is and let the frontend decide the display format.
     * Useful when working with old code that uses the old Date format.
     *
     * NOTE: For backwards compatibility purposes, the date will always be in the system timezone.
     *
     * @return a formatted time String. Ex: "12:34:56"
     */
    static String formatAsTime(Date date) {
        return date
                ? DEFAULT_DISPLAY_TIME_FORMATTER.format(asLocalTime(date))
                : EMPTY_DISPLAY_DATE
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to a LocalTime.
     * Useful when working with old code that uses the old format.
     *
     * NOTE: For backwards compatibility purposes, the date will always be in the system timezone.
     */
    private static LocalTime asLocalTime(Date date) {
        return date
                ? InstantParser.asInstant(date).atZone(DateUtil.getSystemZoneId()).toLocalTime()
                : null
    }
}
