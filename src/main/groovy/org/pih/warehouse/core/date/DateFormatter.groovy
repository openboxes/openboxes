package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A simple wrapper component on the individual formatters.
 * Exists purely for convenience so that we only need to wire in a single component.
 *
 * For use only by data exporters and GSPs. APIs should always return dates in UTC ISO format.
 */
@Component
class DateFormatter {

    private static final String EMPTY_DISPLAY_DATE = ""

    @Autowired
    InstantFormatter instantFormatter

    @Autowired
    ZonedDateTimeFormatter zonedDateTimeFormatter

    @Autowired
    LocalDateFormatter localDateFormatter

    @Autowired
    JavaUtilDateFormatter javaUtilDateFormatter

    /**
     * Convenience method for converting the current date as a String for use in file names.
     */
    String formatCurrentDateForFileName() {
        return formatForFileName(Instant.now())
    }

    /**
     * Convenience method for converting the given date object to a String for use in file names.
     */
    String formatForFileName(Object date) {
        return format(date, DateFormatterContext.builder()
                .withDisplayFormat(DateDisplayFormat.FILE_NAME)
                // Simply exclude the date in the file name if we aren't given one
                .withDefaultValue(EMPTY_DISPLAY_DATE)
                .build())
    }

    /**
     * Convenience method for converting the given date object to a String for use by file exporters.
     */
    String formatForExport(Object date) {
        return format(date, DateFormatterContext.builder()
                .withDisplayFormat(DateDisplayFormat.CSV)
                // Return an empty string when given no date because CSVs don't handle nulls well.
                .withDefaultValue(EMPTY_DISPLAY_DATE)
                .build())
    }

    /**
     * Converts the given date object to a String in the locale and timezone of the requesting user.
     */
    String format(Object date, DateFormatterContext context=null) {
        switch (date) {
            case Instant:
                return instantFormatter.format(date, context)
                break
            case ZonedDateTime:
                return zonedDateTimeFormatter.format(date, context)
            case LocalDate:
                return localDateFormatter.format(date, context)
            // Supporting this case allows us to refactor old code to use this formatter before we actually
            // change the date field types. New fields should not use Date.
            case Date:
                return javaUtilDateFormatter.format(date, context)
            case null:
                return context?.defaultValue  // If no default value is specified, will return null.
            default:
                throw new UnsupportedOperationException("Cannot format date of type [${date.class}]")
        }
    }
}
