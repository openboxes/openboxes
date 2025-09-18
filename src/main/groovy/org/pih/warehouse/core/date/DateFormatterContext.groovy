package org.pih.warehouse.core.date

import java.time.ZoneId

/**
 * Context object containing the configuration fields for formatting dates.
 * For a majority of cases the default settings can be used and so this context object will not be required.
 */
class DateFormatterContext {

    /**
     * Overrides the locale to use when formatting the date.
     */
    Locale localeOverride

    /**
     * Overrides the pattern to use when formatting the date.
     * Unlike displayStyleOverride, the pattern itself is not localized.
     */
    String patternOverride

    /**
     * Overrides the timezone to use when formatting the date.
     */
    ZoneId timezoneOverride

    /**
     * The format that the date will be displayed in.
     * Needed so that we can support different date patterns for each format.
     */
    DateDisplayFormat displayFormat

    /**
     * Overrides the localized pattern (whereas patternOverride is not locale-specific) to format the date to.
     */
    DateDisplayStyle displayStyleOverride

    static DateFormatterContextBuilder builder() {
        return new DateFormatterContextBuilder()
    }

    DateFormatterContext validate() {
        if (!(patternOverride != null ^ displayFormat != null ^ displayStyleOverride != null)) {
            throw new IllegalArgumentException(
                    'One (and only one) of the following fields must be set when formatting a date: patternOverride, ' +
                            'displayFormat, displayStyleOverride')
        }
        return this
    }

    private static class DateFormatterContextBuilder {

        DateFormatterContext context = new DateFormatterContext()

        DateFormatterContext build() {
            return context.validate()
        }

        DateFormatterContextBuilder withLocaleOverride(Locale localeOverride) {
            context.localeOverride = localeOverride
            return this
        }

        DateFormatterContextBuilder withPatternOverride(String patternOverride) {
            context.patternOverride = patternOverride
            return this
        }

        DateFormatterContextBuilder withTimezoneOverride(ZoneId timezoneOverride) {
            context.timezoneOverride = timezoneOverride
            return this
        }

        DateFormatterContextBuilder withDisplayFormat(DateDisplayFormat displayFormat) {
            context.displayFormat = displayFormat
            return this
        }

        DateFormatterContextBuilder withDisplayStyleOverride(DateDisplayStyle displayStyleOverride) {
            context.displayStyleOverride = displayStyleOverride
            return this
        }
    }
}
