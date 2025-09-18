package org.pih.warehouse.core.date

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import org.pih.warehouse.DateUtil

/**
 * A formatter that converts TemporalAccessor objects that have a date + time component to Strings.
 */
class TemporalAccessorDateTimeFormatter<T extends TemporalAccessor> extends TemporalAccessorFormatter<T> {

    /**
     * Required. The timezone that we should convert the datetime to be in.
     */
    ZoneId timezone

    TemporalAccessorDateTimeFormatter(
            final Locale locale,
            final DateDisplayFormat displayFormat,
            final String patternOverride,
            final DateDisplayStyle displayStyleOverride,
            final ZoneId timezone) {

        super(locale, displayFormat, patternOverride, displayStyleOverride)
        this.timezone = timezone
    }

    DateTimeFormatter getJsonFormatter() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    DateTimeFormatter getGspFormatter() {
        return getFormatterFromDisplayStyle(DateDisplayStyle.DATE_TIME)
    }

    DateTimeFormatter getCsvFormatter() {
        // We format our CSV exports using ISO pattern (regardless of locale) because we want to ensure that if
        // the same CSV is later imported, it won't fail on date format errors. We could export to a locale-specific
        // pattern but we'd need to verify that each of those patterns can be handled by our data binders. For now,
        // we stick with ISO pattern because it's a universally accepted date format and is guaranteed to work.
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    @Override
    protected DateTimeFormatter addContextToFormatter(DateTimeFormatter formatter) {
        return super.addContextToFormatter(formatter)
                .withZone(timezone)
    }
}
