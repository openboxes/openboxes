package org.pih.warehouse.core.date

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.app.ApplicationContextProvider

/**
 * A formatter that converts date objects to strings.
 */
abstract class TemporalAccessorFormatter<T extends TemporalAccessor> implements DateFormatter<T> {

    private static final String EMPTY_DISPLAY_DATE = ''

    /**
     * Required. The locale that we need to translate the date to. Needed for day (Monday) and month (January) fields.
     */
    Locale locale

    /**
     * Required. The format that the date will be displayed in.
     * Needed so that we can support different date patterns for each format.
     */
    DateDisplayFormat displayFormat

    /**
     * Optional. Overrides the pattern to format the date to.
     * Unlike displayStyleOverride, the pattern itself is not localized.
     * Will take priority over displayStyleOverride.
     */
    String patternOverride

    /**
     * Optional. Overrides the localized pattern (whereas patternOverride is not locale-specific) to format the date to.
     * If patternOverride is also set, it will take priority over this field.
     */
    DateDisplayStyle displayStyleOverride

    TemporalAccessorFormatter(
            final Locale locale,
            final DateDisplayFormat displayFormat,
            final String patternOverride,
            final DateDisplayStyle displayStyleOverride) {

        this.locale = locale
        this.displayFormat = displayFormat
        this.patternOverride = patternOverride
        this.displayStyleOverride = displayStyleOverride
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
        if (StringUtils.isNotBlank(patternOverride)) {
            return DateTimeFormatter.ofPattern(patternOverride)
        }

        // Similarly, if we're overriding the pattern by explicitly selecting a style, construct a formatter from it.
        if (displayStyleOverride) {
            return getFormatterFromDisplayStyle(displayStyleOverride)
        }

        // Otherwise choose the formatter that is relevant to the display format that we'll be formatting to. We opt to
        // not make any assumptions about the format to use, so if not format and no overrides are specified, error.
        if (!displayFormat) {
            throw new IllegalArgumentException(
                    'One (and only one) of the following fields must be set when formatting a date: patternOverride, ' +
                    'displayFormat, displayStyleOverride')
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
        return formatter
    }

    /**
     * Adds user-specific context to the given DateTimeFormatter
     * Can be overwritten by child implementations to add additional context.
     */
    protected DateTimeFormatter addContextToFormatter(DateTimeFormatter formatter) {
        return formatter
                .withLocale(locale)
    }

    /**
     * Converts the given date object to a String.
     */
    String format(T date) {
        if (!date) {
            return EMPTY_DISPLAY_DATE
        }

        return addContextToFormatter(getFormatter()).format(date)
    }

    /**
     * Localize a given display style, returning a formatter that wraps the locale-specific pattern to use.
     */
    protected DateTimeFormatter getFormatterFromDisplayStyle(DateDisplayStyle displayStyle) {
        String pattern = getLocalizer().localizePattern(displayStyle, locale)
        return DateTimeFormatter.ofPattern(pattern)
    }

    static DatePatternLocalizer getLocalizer() {
        // Better would be to have this be non-statically accessed but this is fine for now.
        return ApplicationContextProvider.getBean(DatePatternLocalizer.class)
    }
}
