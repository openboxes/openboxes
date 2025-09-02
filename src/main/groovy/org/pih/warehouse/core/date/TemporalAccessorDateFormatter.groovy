package org.pih.warehouse.core.date

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import org.pih.warehouse.DateUtil

/**
 * A formatter that converts TemporalAccessor objects that have a date component to Strings.
 */
class TemporalAccessorDateFormatter<T extends TemporalAccessor> extends TemporalAccessorFormatter<T> {

    TemporalAccessorDateFormatter(Locale locale, String pattern, DateDisplayFormat displayFormat) {
        super(locale, pattern, displayFormat)
    }

    DateTimeFormatter getJsonFormatter() {
        // For consistency, our APIs should always return ISO formatted dates.
        return DateTimeFormatter.ISO_DATE
    }

    DateTimeFormatter getGspFormatter() {
        // TODO: We should support a localized default pattern for GSPs. Certain languages such as Arabic
        //       and Chinese display dates in unique formats, and so we should change the format depending on
        //       the locale that is provided. We can achieve this by having the pattern be a translated field in
        //       message.properties (and in fact we already do this via the default.date.format field) instead of a
        //       constant value (though we can still use the constant as a fallback if the message isn't defined).
        return DateUtil.DEFAULT_DISPLAY_DATE_FORMATTER
    }

    DateTimeFormatter getCsvFormatter() {
        // We format our CSV exports using ISO pattern (regardless of locale) because we want to ensure that if
        // the same CSV is later imported, it won't fail on date format errors. We could export to a locale-specific
        // pattern but we'd need to verify that each of those patterns can be handled by our data binders. For now,
        // we stick with ISO pattern because it's the only universal date format and is guaranteed to work.
        return DateTimeFormatter.ISO_DATE
    }
}
