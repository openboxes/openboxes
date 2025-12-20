package org.pih.warehouse.core.date

import java.time.Instant
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

/**
 * A formatter that converts Instants to Strings.
 */
@Component
class InstantFormatter extends AbstractDateFormatter<Instant> {

    DateTimeFormatter getJsonFormatter() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    DateTimeFormatter getGspFormatter() {
        return getFormatterFromDisplayStyle(DateDisplayStyle.DATE_TIME)
    }

    DateTimeFormatter getCsvFormatter() {
        /*
         * We format our CSV exports using ISO pattern (regardless of locale) because we want to ensure that if
         * the same CSV is later imported, it won't fail on date format errors. We could export to a locale-specific
         * pattern but we'd need to verify that each of those patterns can be handled by our data binders. For now,
         * we stick with ISO pattern because it's a universally accepted date format and is guaranteed to work.
         *
         * We format our datetime fields to date-only when exporting for CSV because:
         *
         * 1) Date-only fields are easier to work with in Excel (there's no datetime picker)
         * 2) Users were reportedly confused by datetime fields
         * 3) We don't have a need to import dates with time precision
         *
         * If we need to import dates with time precision, modify this formatter to use the "yyyy-MM-dd hh:mm" pattern.
         */
        return DateTimeFormatter.ISO_LOCAL_DATE
    }

    DateTimeFormatter getFileNameFormatter() {
        return DateTimeFormatter.ofPattern('yyyyMMdd-HHmmss')
    }

    @Override
    protected DateTimeFormatter addContextToFormatter(DateTimeFormatter formatter, DateFormatterContext context) {
        return super.addContextToFormatter(formatter, context)
                .withZone(context?.timezoneOverride ?: getTimezone())
    }
}
