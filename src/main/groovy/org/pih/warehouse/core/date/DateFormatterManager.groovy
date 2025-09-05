package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.session.SessionManager

/**
 * Formats date objects for display. For use only by data exporters and GSPs. APIs should always return dates in
 * ISO format using UTC.
 */
@Component
class DateFormatterManager {

    @Autowired
    SessionManager sessionManager

    /**
     * Convenience method for converting the given date object to a String for use by file exporters.
     */
    String formatForExport(Object date) {
        return format(date, DateFormatterContext.builder()
                .withDisplayFormat(DateDisplayFormat.CSV)
                .build())
    }

    /**
     * Converts the given date object to a String in the locale and timezone of the requesting user.
     */
    String format(Object date, DateFormatterContext context=null) {

        Locale locale = context?.localeOverride ?: getLocale()
        ZoneId timezone = context?.timezoneOverride ?: getTimezone()
        DateDisplayFormat displayFormat = context?.displayFormat ?: DateDisplayFormat.GSP

        DateFormatter<?> formatter
        switch (date) {
            case Instant:
                formatter = new TemporalAccessorDateTimeFormatter<Instant>(
                        locale, context?.patternOverride, displayFormat, timezone)
                break
            case ZonedDateTime:
                formatter = new TemporalAccessorDateTimeFormatter<ZonedDateTime>(
                        locale, context?.patternOverride, displayFormat, timezone)
                break
            case LocalDate:
                formatter = new TemporalAccessorDateFormatter<LocalDate>(
                        locale, context?.patternOverride, displayFormat)
                break
            default:
                throw new UnsupportedOperationException("Cannot format date of type [${date.class}]")
        }

        return formatter.format(date)
    }

    private Locale getLocale() {
        // Extracts the locale from the request. We need a fallback for when we're not in the context of an HTTP
        // request, such as when running unit tests or console commands.
        return GrailsWebRequest.lookup()?.getLocale() ?: Locale.getDefault()
    }

    private ZoneId getTimezone(){
        // We trust session manager to provide a default timezone if there is no valid session.
        return sessionManager.timezone.toZoneId()
    }

    /**
     * Context objects containing the configuration fields for formatting dates.
     * For a majority of cases the default settings can be used and so this object will not be required.
     */
    static class DateFormatterContext {
        Locale localeOverride
        String patternOverride
        ZoneId timezoneOverride
        DateDisplayFormat displayFormat

        static DateFormatterContextBuilder builder() {
            return new DateFormatterContextBuilder()
        }
    }

    private static class DateFormatterContextBuilder {

        DateFormatterContext context = new DateFormatterContext()

        DateFormatterContext build() {
            return context
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
    }
}
