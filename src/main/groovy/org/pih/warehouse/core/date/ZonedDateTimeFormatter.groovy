package org.pih.warehouse.core.date

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

/**
 * A formatter that converts ZonedDateTimes to Strings.
 */
@Component
class ZonedDateTimeFormatter extends AbstractDateFormatter<ZonedDateTime> {

    DateTimeFormatter getJsonFormatter() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    DateTimeFormatter getGspFormatter() {
        return getFormatterFromDisplayStyle(DateDisplayStyle.DATE_TIME)
    }

    DateTimeFormatter getCsvFormatter() {
        // TODO: Preserve the existing display format until we can start the date refactor (scheduled for 0.9.7).
        //       Once we decide on a format/style to use for CSVs, update this line.
        return DateTimeFormatter.ofPattern('dd/MMM/yyyy HH:mm:ss')

        // We format our CSV exports using ISO pattern (regardless of locale) because we want to ensure that if
        // the same CSV is later imported, it won't fail on date format errors. We could export to a locale-specific
        // pattern but we'd need to verify that each of those patterns can be handled by our data binders. For now,
        // we stick with ISO pattern because it's a universally accepted date format and is guaranteed to work.
        //return DateTimeFormatter.ISO_OFFSET_DATE_TIME
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
