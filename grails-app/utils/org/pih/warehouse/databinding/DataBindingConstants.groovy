package org.pih.warehouse.databinding

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Constants relating to binding request input strings to objects.
 */
class DataBindingConstants {

    /*
     * Patterns that follow the ISO-8601 + RPC 3339 date formats while allowing for slight variation in the pattern
     * for the sake of flexibility. For example, we also allow MM/dd/yyyy, which is the default date format for Excel.
     *
     * Ex: "2000-01-01", "01/01/2000", "20000101" are all valid strings according to FLEXIBLE_DATE_PATTERN.
     * Ex: "2000-01-01T00:00:00Z", "01/01/2000 00:00Z" are valid strings according to FLEXIBLE_DATE_TIME_ZONE_PATTERN.
     *
     * We do this mainly to support a wider range of user input, making our parsing logic more flexible.
     * Internally, we should strive to use a consistent structure and to always output the same format.
     *
     * Note that more specific optionals must be defined in front of less specific ones (ex: [HH:mm:ss][HH:mm],
     * not [HH:mm][HH:mm:ss]).
     *
     * Also note that depending on what date type you're parsing into, this can trigger an error. For example, Instant
     * and ZonedDateTime require time and timezone information, and so FLEXIBLE_DATE_TIME_ZONE_PATTERN will fail to
     * parse "2000-01-01" unless the DateTimeFormatter provides a default for time and zone (via "parseDefaulting").
     */
    static final String FLEXIBLE_DATE_PATTERN = "[yyyy-MM-dd][MM/dd/yyyy][yyyy/MM/dd][yyyy MM dd][yyyyMMdd]"
    static final String FLEXIBLE_TIME_PATTERN =
            "[HH:mm:ss.SSS][HH:mm:ss:SSS][HHmmssSSS]" +
            "[HH:mm:ss.SS][HH:mm:ss:SS][HHmmssSS]" +
            "[HH:mm:ss.S][HH:mm:ss:S][HHmmssS]" +
            "[HH:mm:ss][HHmmss]" +
            "[HH:mm][HHmm]"
    static final String FLEXIBLE_DATE_TIME_ZONE_PATTERN =
            "${FLEXIBLE_DATE_PATTERN}[['T'][ ]${FLEXIBLE_TIME_PATTERN}][[ ][XXX][XX][X]]"

    /**
     * A DateTimeFormatter used to parse a String to a date-only class, such as java.time.LocalDate.
     *
     * If a full datetime string containing time and zone information, parsing will fail. Use DATE_TIME_ZONE_FORMAT
     * instead for strings of that format.
     */
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(FLEXIBLE_DATE_PATTERN)

    /**
     * A DateTimeFormatter used to parse a String to a datetime class, such as java.time.Instant or ZonedDateTime.
     *
     * Note that this formatter does not provide defaults for time or zone. If either is missing, parsing will fail.
     * We do this because we've had timezone issues in the past, and so have decided to force the given string to
     * contain all the information required to build the datetime.
     */
    static final DateTimeFormatter DATE_TIME_ZONE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern(FLEXIBLE_DATE_TIME_ZONE_PATTERN)
            // Default to HH:mm:00.000 if seconds and milliseconds are excluded in the string because we often don't
            // require that amount of precision.
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            // Must default to NANO_OF_SECOND (not MILLI_OF_SECOND) because that's what the "S" pattern uses.
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter()

    /**
     * A DateTimeFormatter used to parse a String that contains a date and time but no timezone to a
     * java.time.LocalDateTime. Ex: "2000-01-31 13:00:00", which is how Excel displays datetimes.
     *
     * The transformation to LocalDateTime should be an intermediary step. The caller is responsible for deciding
     * which timezone to apply (which will convert the LocalDateTime into a ZonedDateTime or Instant).
     */
    static final DateTimeFormatter DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("${FLEXIBLE_DATE_PATTERN}['T'][ ]${FLEXIBLE_TIME_PATTERN}")
            // Default to HH:mm:00.000 if seconds and milliseconds are excluded in the string because we often don't
            // require that amount of precision.
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            // Must default to NANO_OF_SECOND (not MILLI_OF_SECOND) because that's what the "S" pattern uses.
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter()
}
