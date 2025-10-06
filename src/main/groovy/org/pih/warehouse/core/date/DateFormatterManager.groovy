package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.LocaleDeterminer
import org.pih.warehouse.core.session.SessionManager

/**
 * Formats date objects for display. Acts as a factory class for the individual formatters.
 *
 * For use only by data exporters and GSPs. APIs should always return dates in ISO format using UTC.
 */
@Component
class DateFormatterManager {

    @Autowired
    SessionManager sessionManager

    @Autowired
    LocaleDeterminer localeDeterminer

    /**
     * Convenience method for converting the given date object to a String for use by file exporters.
     */
    String formatForExport(Object date) {
        return format(date, DateFormatterContext.builder()
                .withDisplayFormat(DateDisplayFormat.CSV)
                // Return an empty string when given no date because CSVs don't handle nulls well.
                .withDefaultValue('')
                .build())
    }

    /**
     * Converts the given date object to a String in the locale and timezone of the requesting user.
     */
    String format(Object date, DateFormatterContext context=null) {
        if (date == null) {
            return context?.defaultValue ?: null
        }

        Locale locale = context?.localeOverride ?: getLocale()
        ZoneId timezone = context?.timezoneOverride ?: getTimezone()

        // We default to the GSP format simply because we have more GSPs than we do CSV exporters.
        // (And because our JSON APIs probably won't even bother using this manager since they always use ISO format.)
        DateDisplayFormat displayFormat = context?.displayFormat ?: DateDisplayFormat.GSP

        DateFormatter<?> formatter
        switch (date) {
            case Instant:
                formatter = initInstantFormatter(locale, displayFormat, context, timezone)
                break
            case ZonedDateTime:
                formatter = initZonedDateTimeFormatter(locale, displayFormat, context, timezone)
                break
            case LocalDate:
                formatter = initLocalDateFormatter(locale, displayFormat, context)
                break
            default:
                throw new UnsupportedOperationException("Cannot format date of type [${date.class}]")
        }

        return formatter.format(date)
    }

    private Locale getLocale() {
        // We trust the locale resolver to provide a default locale if there is no valid session.
        return localeDeterminer.currentLocale
    }

    private ZoneId getTimezone(){
        // We trust the session manager to provide a default timezone if there is no valid session.
        return sessionManager.timezone.toZoneId()
    }

    // Pull the init logic for the formatters into methods so they can be easily stubbed in tests.
    TemporalAccessorDateTimeFormatter<Instant> initInstantFormatter(
            Locale locale, DateDisplayFormat displayFormat, DateFormatterContext context, ZoneId timezone) {
        return new TemporalAccessorDateTimeFormatter<Instant>(
                locale, displayFormat, context?.patternOverride, context?.displayStyleOverride, timezone)
    }
    TemporalAccessorDateTimeFormatter<ZonedDateTime> initZonedDateTimeFormatter(
            Locale locale, DateDisplayFormat displayFormat, DateFormatterContext context, ZoneId timezone) {
        return new TemporalAccessorDateTimeFormatter<ZonedDateTime>(
                locale, displayFormat, context?.patternOverride, context?.displayStyleOverride, timezone)
    }
    TemporalAccessorDateFormatter<LocalDate> initLocalDateFormatter(
            Locale locale, DateDisplayFormat displayFormat, DateFormatterContext context) {
        return new TemporalAccessorDateFormatter<LocalDate>(
                locale, displayFormat, context?.patternOverride, context?.displayStyleOverride)
    }
}
