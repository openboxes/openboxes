package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DateFormatterSpec extends Specification {

    // We don't care that this isn't a real date since we're not testing the formatters themselves
    static final String FORMATTED_DATE_STRING = 'FORMATTED!'

    @Shared
    DateFormatter dateFormatter

    void setup() {
        dateFormatter = new DateFormatter()

        // We're not testing the formatters themselves so it doesn't matter what string we format to.
        dateFormatter.instantFormatter = Stub(InstantFormatter) {
            format(_ as Instant, _ as DateFormatterContext) >> FORMATTED_DATE_STRING
        }
        dateFormatter.zonedDateTimeFormatter = Stub(ZonedDateTimeFormatter) {
            format(_ as ZonedDateTime, _ as DateFormatterContext) >> FORMATTED_DATE_STRING
        }
        dateFormatter.localDateFormatter = Stub(LocalDateFormatter) {
            format(_ as LocalDate, _ as DateFormatterContext) >> FORMATTED_DATE_STRING
        }
        dateFormatter.javaUtilDateFormatter = Stub(JavaUtilDateFormatter) {
            format(_ as Date, _ as DateFormatterContext) >> FORMATTED_DATE_STRING
        }
    }

    void 'formatForExport does not error for for type: #type'() {
        expect:
        dateFormatter.formatForExport(date) == FORMATTED_DATE_STRING

        where:
        date                | type
        Instant.now()       | "Instant"
        ZonedDateTime.now() | "ZonedDateTime"
        LocalDate.now()     | "LocalDate"
        new Date()          | "Date"
    }

    void 'formatForFileName does not error for for type: #type'() {
        expect:
        dateFormatter.formatForExport(date) == FORMATTED_DATE_STRING

        where:
        date                | type
        Instant.now()       | "Instant"
        ZonedDateTime.now() | "ZonedDateTime"
        LocalDate.now()     | "LocalDate"
        new Date()          | "Date"
    }

    void 'formatCurrentDateForFileName does not error'() {
        expect:
        dateFormatter.formatCurrentDateForFileName() == FORMATTED_DATE_STRING
    }
}
