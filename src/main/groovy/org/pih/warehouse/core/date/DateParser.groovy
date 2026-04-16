package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A simple wrapper component on the individual parsers.
 * Exists purely for convenience so that we only need to wire in a single component.
 */
@Component
class DateParser {

    @Autowired
    InstantParser instantParser

    @Autowired
    JavaUtilDateParser javaUtilDateParser

    @Autowired
    LocalDateParser localDateParser

    @Autowired
    ZonedDateTimeParser zonedDateTimeParser

    /**
     * Parses a given date object into an Instant.
     */
    Instant parseToInstant(Object date, DateParserContext<Instant> context=null) {
        return instantParser.parse(date, context)
    }

    /**
     * Parses a given date object into a LocalDate.
     */
    LocalDate parseToLocalDate(Object date, DateParserContext<LocalDate> context=null) {
        return localDateParser.parse(date, context)
    }

    /**
     * Parses a given date object into a ZonedDateTime.
     */
    ZonedDateTime parseToZonedDateTime(Object date, DateParserContext<ZonedDateTime> context=null) {
        return zonedDateTimeParser.parse(date, context)
    }

    /**
     * Parses a given date object into a java.util.Date.
     */
    Date parseToDate(Object date, DateParserContext<Date> context=null) {
        return javaUtilDateParser.parse(date, context)
    }
}
