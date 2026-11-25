package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.session.SessionManager

@Unroll
class DateParserSpec extends Specification {

    DateParser dateParser

    void setup() {
        JavaUtilDateParser javaUtilDateParser = new JavaUtilDateParser(sessionManager: Stub(SessionManager) {
            getTimezone() >> TimeZone.getTimeZone("Asia/Kolkata")
        })
        dateParser = new DateParser(javaUtilDateParser: javaUtilDateParser)
    }

    void "parseToDate parses #value as a #dateFieldType"() {
        expect:
        assert dateParser.parseToDate(value, dateFieldType) == expected

        where:
        value                     | dateFieldType              || expected
        "01/31/2000"              | DateFieldType.LOCAL_DATE   || JavaUtilDateParser.asDate(LocalDate.parse("2000-01-31"))
        "01/31/2000 13:00 -05:00" | DateFieldType.INSTANT      || Date.from(Instant.parse("2000-01-31T18:00:00Z"))
        null                      | DateFieldType.LOCAL_DATE   || null
    }

    void "parseToDate fails when no dateFieldType is given"() {
        when:
        dateParser.parseToDate("01/31/2000", null)

        then:
        thrown(IllegalArgumentException)
    }
}
