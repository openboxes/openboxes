package org.pih.warehouse.core.date

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.core.localization.LocaleManager
import org.pih.warehouse.core.session.SessionManager

/**
 * A base class for formatters that convert date objects to Strings.
 */
abstract class AbstractDateFormatter<T extends TemporalAccessor> implements IDateFormatter<T> {

    @Autowired
    LocaleManager localeManager

    @Autowired
    SessionManager sessionManager

    @Autowired
    DatePatternLocalizer datePatternLocalizer

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
     * Returns the default DateTimeFormatter to use when formatting for use in file names.
     */
    abstract DateTimeFormatter getFileNameFormatter()

    /**
     * Converts the given date object to a String.
     */
    String format(T date, DateFormatterContext context=null) {
        if (date == null) {
            return context?.defaultValue  // If no default value is specified, will return null.
        }

        return addContextToFormatter(getFormatter(context), context).format(date)
    }

    /**
     * Returns the DateTimeFormatter that will be used to format the date to a String.
     */
    private DateTimeFormatter getFormatter(DateFormatterContext context) {
        // We default to the GSP format simply because we have more GSPs than we do CSV exporters.
        // (And because our JSON APIs probably won't even bother using this manager since they always use ISO format.)
        DateDisplayFormat displayFormat = context?.displayFormat ?: DateDisplayFormat.GSP

        // If we're overriding the pattern, construct a new formatter from that pattern and return it.
        if (StringUtils.isNotBlank(context?.patternOverride)) {
            return DateTimeFormatter.ofPattern(context.patternOverride)
        }

        // Similarly, if we're overriding the pattern by explicitly selecting a style, construct a formatter from it.
        if (context?.displayStyleOverride) {
            return getFormatterFromDisplayStyle(context.displayStyleOverride)
        }

        // Otherwise choose the formatter that is relevant to the display format that we'll be formatting to.
        DateTimeFormatter formatter = null
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
            case DateDisplayFormat.FILE_NAME:
                formatter = getFileNameFormatter()
                break
            // We opt to not make assumptions about the format, so if no format and no overrides are specified, error.
            case null:
                throw new IllegalArgumentException(
                        'One (and only one) of the following fields must be set when formatting a date: ' +
                        'patternOverride, displayFormat, displayStyleOverride')
        }
        return formatter
    }

    /**
     * Adds user-specific context to the given DateTimeFormatter
     * Can be overwritten by child implementations to add additional context.
     *
     * We do it this way because we want to let the individual formatters decide what context they need.
     * For example, if we (incorrectly) add a timezone to a formatter that is used for LocalDate, it first converts
     * the date to a ZonedDateTime then back to a LocalDate. This can change the resulting date (which we don't want).
     */
    protected DateTimeFormatter addContextToFormatter(DateTimeFormatter formatter, DateFormatterContext context) {
        return formatter.withLocale(context?.localeOverride ?: getLocale())
    }

    /**
     * Localize a given display style, returning a formatter that wraps the locale-specific pattern to use.
     */
    protected DateTimeFormatter getFormatterFromDisplayStyle(DateDisplayStyle displayStyle) {
        String pattern = datePatternLocalizer.localizePattern(displayStyle, locale)
        return DateTimeFormatter.ofPattern(pattern)
    }

    /**
     * @return Locale the locale of the requesting user.
     */
    protected Locale getLocale() {
        return localeManager.getCurrentLocale()
    }

    /**
     * @return ZoneId the timezone of the requesting user.
     */
    protected ZoneId getTimezone(){
        return sessionManager.timezone.toZoneId()
    }
}
