package org.pih.warehouse.databinding

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

import org.pih.warehouse.DateUtil

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
            .appendPattern(DataBindingConstants.FLEXIBLE_DATE_TIME_ZONE_PATTERN)
            // Default to HH:mm:00.000 if seconds and milliseconds are excluded in the string because we often don't
            // require that amount of precision.
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .toFormatter()

    /**
     * @deprecated for use only by existing endpoints that mix and match date formats (and so need a more flexible
     *             formatter). New endpoints should avoid Date and should use DATE_FORMAT or DATE_TIME_ZONE_FORMAT.
     *
     * A DateTimeFormatter used to parse a String to a java.util.Date.
     *
     * Note that the behaviour isn't consistent when handling input that doesn't provide a timezone. This is due
     * to weirdness around DST (daylight savings time).
     *
     * Ex: Due to DST, the "America/Vancouver" timezone is PST (-08:00) from November-March and PDT (-07:00) from
     *     March-November while DST is in effect. So if the server is in the "America/Vancouver" timezone, and we're
     *     given a string "2000-01-01", which is in PST (-08:00), if the current date is within March-November, it'll
     *     parse that date as PDT (-07:00). This means we'll get the Date equivalent of "2000-01-01T00:00:00-07:00",
     *     aka "1999-12-31T23:00:00-08:00". And if the current date is within November-March it'll parse that date as
     *     PST (-08:00), resulting in a Date like "2000-01-01T00:00:00-08:00", aka "2000-01-01T01:00:00-07:00.
     *
     * For date-only fields, the above is fine because time and zone get stripped out so as long as the frontend has
     * the same timezone as the backend, we'll always end up with "2000-01-01", but for datetime fields that don't also
     * specify timezone, the behaviour is inconsistent. This is a main reason why we avoid using Date, why we should
     * always pass full date + time + zone for datetime fields, and why we suggest configuring the server to use UTC.
     */
    @Deprecated
    static final DateTimeFormatter FLEXIBLE_DATE_TIME_ZONE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern(DataBindingConstants.FLEXIBLE_DATE_TIME_ZONE_PATTERN)
            // Defaults to 00:00:00.000 (aka midnight) if time is not provided in the String.
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            // TODO: This is imperfect because if the server is in a timezone that has multiple offsets depending on the
            //       the time of year (due to daylight savings time), this will break. It's unlikely that a server would
            //       be configured this way, but still worth noting. Once we upgrade to Java 9+, remove this offset
            //       default and replace it with "withZone" when parsing (see DateUtil.asDate).
            //       https://stackoverflow.com/questions/41999421/how-does-datetimeformatters-override-zone-work-when-parsing
            // Defaults to the current timezone offset of the system if timezone is not provided in the String.
            .parseDefaulting(ChronoField.OFFSET_SECONDS, DateUtil.getSystemZoneOffset().getTotalSeconds())
            .toFormatter()
}
