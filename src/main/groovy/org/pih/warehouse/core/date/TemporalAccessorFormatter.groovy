package org.pih.warehouse.core.date

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import org.apache.commons.lang.StringUtils

/**
 * A formatter that converts date objects to strings.
 */
abstract class TemporalAccessorFormatter<T extends TemporalAccessor> implements DateFormatter<T> {

    private static final String EMPTY_DISPLAY_DATE = ''

    /**
     * The locale that we need to translate the date to. Needed for day (Monday) and month (January) fields.
     */
    Locale locale

    /**
     * An optional override of the pattern to format the date to.
     */
    String pattern

    /**
     *
     */
    DateDisplayFormat displayFormat

    TemporalAccessorFormatter(Locale locale, String pattern, DateDisplayFormat displayFormat) {
        this.locale = locale
        this.pattern = pattern
        this.displayFormat = displayFormat
    }

    /**
     * Returns the default DateTimeFormatter to use when formatting for JSON APIs.
     */
    abstract DateTimeFormatter getJsonFormatter()

    /**
     * Returns the default DateTimeFormatter to use when formatting for frontend GSPs.
     */
    abstract DateTimeFormatter getGspFormatter()

    /**
     * Returns the default DateTimeFormatter to use when formatting for CSV exports.
     */
    abstract DateTimeFormatter getCsvFormatter()

    /**
     * Returns the DateTimeFormatter that will be used to format the date to a String.
     */
    DateTimeFormatter getFormatter() {
        // If we're overriding the pattern, construct a new formatter from that pattern and return it.
        if (StringUtils.isNotBlank(pattern)) {
            return addContextToFormatter(DateTimeFormatter.ofPattern(pattern))
        }

        DateTimeFormatter formatter
        switch (displayFormat) {
            case DateDisplayFormat.JSON:
                formatter = getJsonFormatter()
                break
            case DateDisplayFormat.GSP:
                formatter = getGspFormatter()
                break
            case DateDisplayFormat.CSV:
                formatter = getCsvFormatter()
                break
        }
        return addContextToFormatter(formatter)
    }

    /**
     * Adds user-specific context to the given DateTimeFormatter
     * Can be overwritten by child implementations to add additional context.
     */
    protected DateTimeFormatter addContextToFormatter(DateTimeFormatter formatter) {
        return formatter
                .withLocale(locale)
    }

    String format(T date) {
        if (!date) {
            return EMPTY_DISPLAY_DATE
        }

        return getFormatter().format(date)
    }
}
