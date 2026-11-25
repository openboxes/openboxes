package org.pih.warehouse.core.date

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.DateUtil

@Unroll
class JavaUtilDateFormatterSpec extends Specification {

    @Shared
    JavaUtilDateFormatter javaUtilDateFormatter

    @Shared
    ZoneOffset systemZoneOffset

    void setupSpec() {
        // All of these tests use January 1st, 2000 as their date. We fetch the timezone offset that was active at that
        // time so that we can factor in daylight savings. This is needed because different timezones can have different
        // offsets depending on the time of year. Since date formatting is based on the system timezone, computing
        // the offset here prevents flakey tests that depend on the timezone that your system is in when running them.
        Instant instantForOffset = Instant.parse("2000-01-01T00:00:00Z")
        systemZoneOffset = DateUtil.getSystemZoneOffset(instantForOffset)
    }

    void setup() {
        javaUtilDateFormatter = new JavaUtilDateFormatter()
    }

    void 'format should successfully convert a Date to a String for display style #displayStyle'() {
        given: 'a Date in the current timezone offset of the system'
        Date date = newDate("2000-01-01T00:00${systemZoneOffset}")

        and: 'a display style to use'
        DateFormatterContext context = DateFormatterContext.builder()
                .withDisplayStyleOverride(displayStyle)
                .build()

        expect:
        assert javaUtilDateFormatter.format(date, context) == expectedDate

        where:
        displayStyle                    | expectedDate
        null                            | "01/Jan/2000 00:00:00 ${systemZoneOffset}"
        DateDisplayStyle.DATE_TIME      | "01/Jan/2000 00:00:00 ${systemZoneOffset}"
        DateDisplayStyle.DATE_TIME_ZONE | "01/Jan/2000 00:00:00 ${systemZoneOffset}"
        DateDisplayStyle.DATE           | "01/Jan/2000"
        DateDisplayStyle.TIME           | "00:00:00"
    }

    void 'formatAsDateTime should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert javaUtilDateFormatter.formatAsDateTime(date) == ''
    }

    void 'formatAsDateTime should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        Date date = newDate("2000-01-01T00:00${systemZoneOffset}")

        expect:
        assert javaUtilDateFormatter.formatAsDateTime(date) == "01/Jan/2000 00:00:00 ${systemZoneOffset}"
    }

    void 'formatAsDate should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert javaUtilDateFormatter.formatAsDate(date) == ''
    }

    void 'formatAsDate should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        Date date = newDate("2000-01-01T00:00${systemZoneOffset}")

        expect:
        assert javaUtilDateFormatter.formatAsDate(date) == "01/Jan/2000"
    }

    void 'formatAsTime should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert javaUtilDateFormatter.formatAsTime(date) == ''
    }

    void 'formatAsTime should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        Date date = newDate("2000-01-01T12:34${systemZoneOffset}")

        expect:
        assert javaUtilDateFormatter.formatAsTime(date) == "12:34:00"
    }

    /**
     * Convenience method to build a Date for tests.
     */
    Date newDate(String date){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX").parse(date)
    }
}
