package org.pih.warehouse.core.date

import java.time.LocalDate
import java.time.ZoneId
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.databinding.DataBindingConstants

/**
 * Deserializes input objects into LocalDates.
 *
 * For use only by data importers. APIs should always use command objects which automatically handle data binding.
 */
@Component
class LocalDateParser extends AbstractDateParser<LocalDate> {

    @Override
    protected LocalDate parseImpl(Object date, DateParserContext<LocalDate> context) {
        switch (date) {
            case String:
                return asLocalDate(date as String)
            case Date:
                return asLocalDate(date as Date, currentTimezone)
            default:
                throw new UnsupportedOperationException("Cannot parse date of type [${date.class}]")
        }
    }

    /**
     * Parse a given String into a LocalDate of the given format.
     *
     * Because LocalDate is time and timezone agnostic, the String only expects day, month, and year elements. To avoid
     * any timezone related confusion, any additional data provided (such as time and timezone) will trigger an error.
     *
     * @param value "2000-12-31", "12/31/2000" for example
     */
    static LocalDate asLocalDate(String date) {
        return StringUtils.isBlank(date) ? null : LocalDate.parse(date.trim(), DataBindingConstants.DATE_FORMAT)
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to a LocalDate.
     * Useful when working with old code that uses the old format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static LocalDate asLocalDate(Date date, ZoneId zone) {
        if (!date) {
            return null
        }
        if (!zone) {
            throw new IllegalArgumentException('Cannot convert a Date to a LocalDate without specifying a timezone')
        }

        /*
         * We need to be very careful about which zoneId we provide to this conversion due to daylight savings. The
         * behaviour will be different if you pass a fixed offset (such as UTC-08) vs a timezone that is affected by
         * DST (such as "America/Vancouver").
         *
         * For example: The UTC-08 offset is always -8, no matter the time of year, but the "America/Vancouver"
         * timezone is PST (-08:00) from Nov-Mar and PDT (-07:00) from Mar-Nov while DST is in effect. So if it's
         * currently Jan 1st, your offset is -8, but if the given date is May 1st, the offset at the time would be -7.
         *
         * This impacts the day that the LocalDate resolves to. If given the "America/Vancouver" zoneId, and the
         * given Date is May 1st, 07:00 UTC (which in Vancouver that day would be in the -7 offset), then that would
         * get converted to May 1st, 00:00 UTC-07, and so would result in a LocalDate of May 1st. This is true even
         * if the current date is Jan 1st and so the current offset is -8.
         *
         * If instead the UTC-08 offset was provided as zoneId, that would get converted to April 30th, 23:00 UTC-08,
         * and so would result in a LocalDate of April 30th.
         */
        return InstantParser.asInstant(date).atZone(zone).toLocalDate()
    }
}
