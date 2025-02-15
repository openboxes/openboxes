package org.pih.warehouse.databinding

/**
 * Constants relating to binding request input strings to objects.
 */
class DataBindingConstants {

    /*
     * Patterns that follow the ISO-8601 + RPC 3339 date formats while allowing for some slight variation in
     * the pattern.
     *
     * Ex: "2000-01-01", "2000/01/01", "20000101" are all valid strings according to FLEXIBLE_DATE_PATTERN.
     * Ex: both "2000-01-01" and "2000-01-01T00:00:00Z" are valid strings according to FLEXIBLE_DATE_TIME_ZONE_PATTERN.
     *
     * We do this mainly to support a wider range of user input, making our parsing logic more flexible.
     * Internally, we should strive to use a consistent structure and to always output the same format.
     *
     * Note that more specific optionals must be defined in front of less specific ones (ex: [HH:mm:ss][HH:mm],
     * not [HH:mm][HH:mm:ss]).
     *
     * Also note that depending on what date type you're parsing into, this can trigger an error. For example, Instant
     * requires time and timezone information, and so FLEXIBLE_DATE_TIME_ZONE_PATTERN will fail to parse "2000-01-01"
     * unless the associated DateTimeFormatter provides a default for time and zone (via "parseDefaulting").
     */
    static final String FLEXIBLE_DATE_PATTERN = "[yyyy-MM-dd][yyyy/MM/dd][yyyy MM dd][yyyyMMdd]"
    static final String FLEXIBLE_TIME_PATTERN =
            "[HH:mm:ss.SSS][HH:mm:ss:SSS][HHmmssSSS]" +
            "[HH:mm:ss.SS][HH:mm:ss:SS][HHmmssSS]" +
            "[HH:mm:ss.S][HH:mm:ss:S][HHmmssS]" +
            "[HH:mm:ss][HHmmss]" +
            "[HH:mm][HHmm]"
    static final String FLEXIBLE_DATE_TIME_ZONE_PATTERN =
            "${FLEXIBLE_DATE_PATTERN}[['T'][ ]${FLEXIBLE_TIME_PATTERN}][[XXX][XX][X]]"
}
