package org.pih.warehouse.core.date

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Constants

/**
 * A formatter that converts ZonedDateTimes to Strings.
 */
@Component
class ZonedDateTimeFormatter extends AbstractDateFormatter<ZonedDateTime> {

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
        /*
         * We format CSV datetimes in the same way that we format dates for Excel (see ExcelCellStyles) because it
         * ensures consistency when doing an import. Excel does not support including timezone in the date format,
         * so when this datetime is imported we assume it is in the user's timezone.
         */
        return Constants.EXCEL_DATE_TIME_FORMATTER
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
