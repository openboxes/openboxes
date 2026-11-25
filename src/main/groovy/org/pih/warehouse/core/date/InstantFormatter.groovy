package org.pih.warehouse.core.date

import java.time.Instant
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

/**
 * A formatter that converts Instants to Strings.
 */
@Component
class InstantFormatter extends AbstractDateFormatter<Instant> {

    @Override
    boolean isDefaultFormatterForType() {
        return true
    }

    @Override
    DateTimeFormatter getJsonFormatter() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    @Override
    DateTimeFormatter getGspFormatter() {
        return getFormatterFromDisplayStyle(DateDisplayStyle.DATE_TIME)
    }

    @Override
    DateTimeFormatter getCsvFormatter() {
        // We format our CSV exports using an ISO-like pattern (regardless of locale) because we want to ensure that if
        // the same CSV is later imported, it won't fail on date format errors. We could export to a locale-specific
        // pattern but we'd need to verify that each of those patterns can be handled by our data binders. For now,
        // we stick with ISO pattern because it's a universally accepted date format and is guaranteed to work.
        return CSV_DATE_TIME_FORMATTER
    }

    @Override
    DateTimeFormatter getFileNameFormatter() {
        return DateTimeFormatter.ofPattern('yyyyMMdd-HHmmss')
    }

    @Override
    protected DateTimeFormatter addContextToFormatter(DateTimeFormatter formatter, DateFormatterContext context) {
        return super.addContextToFormatter(formatter, context)
                .withZone(context?.timezoneOverride ?: getTimezone())
    }
}
