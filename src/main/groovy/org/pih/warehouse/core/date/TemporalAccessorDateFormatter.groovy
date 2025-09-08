package org.pih.warehouse.core.date

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * A formatter that converts TemporalAccessor objects that have a date component to Strings.
 */
class TemporalAccessorDateFormatter<T extends TemporalAccessor> extends TemporalAccessorFormatter<T> {

    TemporalAccessorDateFormatter(
            final Locale locale,
            final DateDisplayFormat displayFormat,
            final String patternOverride,
            final DateDisplayStyle displayStyleOverride) {

        super(locale, displayFormat, patternOverride, displayStyleOverride)
    }

    DateTimeFormatter getJsonFormatter() {
        return DateTimeFormatter.ISO_DATE
    }

    DateTimeFormatter getGspFormatter() {
        return getFormatterFromDisplayStyle(DateDisplayStyle.DATE)
    }

    DateTimeFormatter getCsvFormatter() {
        // We format our CSV exports using ISO pattern (regardless of locale) because we want to ensure that if
        // the same CSV is later imported, it won't fail on date format errors. We could export to a locale-specific
        // pattern but we'd need to verify that each of those patterns can be handled by our data binders. For now,
        // we stick with ISO pattern because it's a universally accepted date format and is guaranteed to work.
        return DateTimeFormatter.ISO_DATE
    }
}
