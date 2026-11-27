package org.pih.warehouse.core.date

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.session.SessionManager

@Unroll
class JavaUtilDateParserSpec extends Specification {

    private static final String SERVER_TIMEZONE = "America/Los_Angeles"
    private static final String USER_TIMEZONE = "Asia/Kolkata"

    JavaUtilDateParser javaUtilDateParser

    TimeZone originalServerTimezone

    void setup() {
        originalServerTimezone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(SERVER_TIMEZONE))

        javaUtilDateParser = new JavaUtilDateParser()
        javaUtilDateParser.sessionManager = Stub(SessionManager) {
            getTimezone() >> TimeZone.getTimeZone(USER_TIMEZONE)
        }
    }

    void cleanup() {
        TimeZone.setDefault(originalServerTimezone)
    }

    void 'parse should safely handle null dates'() {
        expect:
        assert javaUtilDateParser.parse(null) == null
    }

    void 'parse should fail when no dateFieldType is given'() {
        when:
        javaUtilDateParser.parse("2000-01-01")

        then:
        thrown(IllegalArgumentException)
    }

    void 'parse as a LOCAL_DATE fails for a String that contains a time'() {
        given:
        DateParserContext<Date> context = new DateParserContext<Date>(dateFieldType: DateFieldType.LOCAL_DATE)

        when:
        javaUtilDateParser.parse("2000-01-02T00:00:00Z", context)

        then:
        thrown(DateTimeParseException)
    }

    void 'parse as a LOCAL_DATE binds #scenario to midnight in the server timezone, not the user timezone'() {
        given:
        DateParserContext<Date> context = new DateParserContext<Date>(dateFieldType: DateFieldType.LOCAL_DATE)

        expect:
        assert formatInServerTimezone(javaUtilDateParser.parse(value, context)) == "2000-01-02T00:00:00-08:00"

        where:
        scenario                  | value
        "an ISO date String"      | "2000-01-02"
        "an Excel style String"   | "01/02/2000"
        "an Excel date (Double)"  | 36527d
        "an Excel date (decimal)" | 36527.0G
    }

    void 'parse should parse a LOCAL_DATE String to midnight in the server timezone for case: #scenario'() {
        when:
        Date date = javaUtilDateParser.parse(
                givenDate, new DateParserContext<Date>(dateFieldType: DateFieldType.LOCAL_DATE))

        then:
        assert formatInServerTimezone(date) == "2000-01-01T00:00:00-08:00"

        where:
        givenDate    || scenario
        "01/01/2000" || "Our format"
        "2000-01-01" || "ISO format"
        "2000/01/01" || "Slashes"
        "20000101"   || "No separator"
    }

    void 'parse as an INSTANT fails for a datetime String without a timezone, if not allowed'() {
        given:
        DateParserContext<Date> context = new DateParserContext<Date>(dateFieldType: DateFieldType.INSTANT)

        when:
        javaUtilDateParser.parse("2000-01-02 12:00:00", context)

        then:
        thrown(DateTimeParseException)
    }

    void 'parse as an INSTANT binds #scenario'() {
        given:
        DateParserContext<Date> context = new DateParserContext<Date>(dateFieldType: DateFieldType.INSTANT)

        expect:
        assert formatInUtc(javaUtilDateParser.parse(value, context)) == expectedDate

        where:
        scenario                                       | value                       || expectedDate
        "a String with an offset, using that offset"   | "2000-01-02T12:00:00+01:00" || "2000-01-02T11:00:00Z"
        "a String in UTC"                              | "2000-01-02T12:00:00Z"      || "2000-01-02T12:00:00Z"
        "a date-only String, at midnight for the user" | "2000-01-02"                || "2000-01-01T18:30:00Z"
        "an Excel datetime, in the user's timezone"    | 36527.5d                    || "2000-01-02T06:30:00Z"
    }

    void 'parse as an INSTANT binds datetime String #value without a timezone in the user timezone, if allowed'() {
        given:
        DateParserContext<Date> context = new DateParserContext<Date>(
                dateFieldType: DateFieldType.INSTANT,
                allowDateTimesWithoutTimezone: true,
        )

        expect: 'the format that we export to (and that Excel displays), and the ISO format'
        assert formatInUtc(javaUtilDateParser.parse(value, context)) == "2000-01-02T06:30:00Z"

        where:
        value << ["2000-01-02 12:00:00", "2000-01-02T12:00:00", "2000-01-02 12:00"]
    }

    void 'parse and asDate should parse an INSTANT String with a timezone, ignoring the user timezone, for case: #scenario'() {
        when:
        Date asDate = JavaUtilDateParser.asDate(givenDate, DateFieldType.INSTANT)
        Date parse = javaUtilDateParser.parse(givenDate, new DateParserContext<Date>(dateFieldType: DateFieldType.INSTANT))

        then:
        assert formatInUtc(asDate) == expectedDate
        assert formatInUtc(parse) == expectedDate

        where:
        givenDate                   || expectedDate               | scenario
        "2000-01-01T00:00:00Z"      || "2000-01-01T00:00:00Z"     | "Full string in proper ISO format"
        "2000-01-01T00:00Z"         || "2000-01-01T00:00:00Z"     | "No seconds"
        "2000-01-01T00:00:00.123Z"  || "2000-01-01T00:00:00.123Z" | "Non-zero millis"
        "2000-01-01T00:00:00-05"    || "2000-01-01T05:00:00Z"     | "Timezone conforming to X format w/ negative"
        "2000-01-01T00:00:00-0500"  || "2000-01-01T05:00:00Z"     | "Timezone conforming to XX format w/ negative"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z"     | "Timezone conforming to XXX format w/ negative"
        "2000-01-01T00:00:00+00"    || "2000-01-01T00:00:00Z"     | "Timezone conforming to X format w/ zero"
        "2000-01-01T00:00:00+0000"  || "2000-01-01T00:00:00Z"     | "Timezone conforming to XX format w/ zero"
        "2000-01-01T00:00:00+00:00" || "2000-01-01T00:00:00Z"     | "Timezone conforming to XXX format w/ zero"
        "2000-01-01T00:00:00+05"    || "1999-12-31T19:00:00Z"     | "Timezone conforming to X format w/ positive"
        "2000-01-01T00:00:00+0500"  || "1999-12-31T19:00:00Z"     | "Timezone conforming to XX format w/ positive"
        "2000-01-01T00:00:00+05:00" || "1999-12-31T19:00:00Z"     | "Timezone conforming to XXX format w/ positive"

        // The format that the frontend sometimes sends (ex: StockMovement.dateShipped)
        "01/01/2000 00:00:00 Z"     || "2000-01-01T00:00:00Z"     | "Full string in our format"
        "01/01/2000 00:00 Z"        || "2000-01-01T00:00:00Z"     | "No seconds our format"
        "01/01/2000 00:00 -05"      || "2000-01-01T05:00:00Z"     | "Timezone conforming to X format w/ negative our format"
        "01/01/2000 00:00 -0500"    || "2000-01-01T05:00:00Z"     | "Timezone conforming to XX format w/ negative our format"
        "01/01/2000 00:00 -05:00"   || "2000-01-01T05:00:00Z"     | "Timezone conforming to XXX format w/ negative our format"
        "01/01/2000 00:00 +00"      || "2000-01-01T00:00:00Z"     | "Timezone conforming to X format w/ zero our format"
        "01/01/2000 00:00 +0000"    || "2000-01-01T00:00:00Z"     | "Timezone conforming to XX format w/ zero our format"
        "01/01/2000 00:00 +00:00"   || "2000-01-01T00:00:00Z"     | "Timezone conforming to XXX format w/ zero our format"
        "01/01/2000 00:00 +05"      || "1999-12-31T19:00:00Z"     | "Timezone conforming to X format w/ positive our format"
        "01/01/2000 00:00 +0500"    || "1999-12-31T19:00:00Z"     | "Timezone conforming to XX format w/ positive our format"
        "01/01/2000 00:00 +05:00"   || "1999-12-31T19:00:00Z"     | "Timezone conforming to XXX format w/ positive our format"
    }

    void 'asDate should fail when no dateFieldType is given'() {
        when:
        JavaUtilDateParser.asDate("2000-01-01", null)

        then:
        thrown(IllegalArgumentException)
    }

    void 'asDate should return null for a blank String with dateFieldType #dateFieldType'() {
        expect:
        assert JavaUtilDateParser.asDate(" ", dateFieldType) == null

        where:
        dateFieldType << DateFieldType.values().toList()
    }

    void 'asDate should fail to parse an INSTANT String without a time and timezone for case: #scenario'() {
        when: 'asDate is static so has no user timezone to fall back to'
        JavaUtilDateParser.asDate(givenDate, DateFieldType.INSTANT)

        then:
        thrown(DateTimeException)

        where:
        givenDate          || scenario
        "01/01/2000 00:00" || "No timezone our format"
        "2000-01-01 00:00" || "No timezone ISO format"
        "01/01/2000"       || "No time or timezone our format"
        "2000-01-01"       || "No time or timezone ISO format"
        "01/01/2000 Z"     || "No time our format"
        "2000-01-01 Z"     || "No time ISO format"
    }

    void 'asDate should fail to parse a LOCAL_DATE String for case: #failureReason'() {
        when:
        JavaUtilDateParser.asDate(givenDate, DateFieldType.LOCAL_DATE)

        then:
        thrown(DateTimeParseException)

        where:
        givenDate              || failureReason
        "01 01 2000"           || "spaces not supported as separator"
        "01/01/00"             || "two digit year format not supported"
        "2000-01-01T00:00:00Z" || "time is not allowed for a date-only field"
    }

    void 'asDate should parse a LOCAL_DATE String to midnight in the server timezone for case: #scenario'() {
        when:
        Date date = JavaUtilDateParser.asDate(givenDate, DateFieldType.LOCAL_DATE)

        then:
        assert formatInServerTimezone(date) == "2000-01-01T00:00:00-08:00"

        where:
        givenDate    || scenario
        "01/01/2000" || "Our format"
        "2000-01-01" || "ISO format"
        "2000/01/01" || "Slashes"
        "20000101"   || "No separator"
    }

    void 'asDate should successfully convert an Instant to a Date for case: #scenario'() {
        when:
        Date date = JavaUtilDateParser.asDate(Instant.parse(givenDate))

        then:
        assert formatInUtc(date) == expectedDate

        where:
        givenDate                     || expectedDate               | scenario
        "2000-01-01T00:00:11Z"        || "2000-01-01T00:00:11Z"     | "seconds precision"
        "2000-01-01T00:00:11.111Z"    || "2000-01-01T00:00:11.111Z" | "millis precision"
        "2000-01-01T00:00:11.111999Z" || "2000-01-01T00:00:11.111Z" | "micros precision, which Date truncates to millis"
    }

    void 'asDate should successfully convert a ZonedDateTime to a Date for case: #scenario'() {
        when:
        Date date = JavaUtilDateParser.asDate(ZonedDateTime.parse(givenDate))

        then:
        assert formatInUtc(date) == expectedDate

        where:
        givenDate                   || expectedDate           | scenario
        "2000-01-01T00:00:00Z"      || "2000-01-01T00:00:00Z" | "UTC timezone"
        "2000-01-01T00:00:00+05:00" || "1999-12-31T19:00:00Z" | "+05 to UTC (backwards conversion)"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z" | "-05 to UTC (forwards conversion)"
    }

    void 'asDate should convert a LocalDate to midnight in the server timezone, using the offset of that date, for case: #scenario'() {
        when:
        Date date = JavaUtilDateParser.asDate(LocalDate.parse(givenDate))

        then:
        assert formatInServerTimezone(date) == expectedDate

        where:
        givenDate    || expectedDate                | scenario
        "2000-01-01" || "2000-01-01T00:00:00-08:00" | "standard time"
        "2000-07-01" || "2000-07-01T00:00:00-07:00" | "daylight savings time"
    }

    /**
     * Prints the instant that the Date represents (in UTC). Fractional seconds are only printed when non-zero.
     */
    private static String formatInUtc(Date date) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date.toInstant().atOffset(ZoneOffset.UTC))
    }

    /**
     * Prints the Date in the server timezone (see setup), including the offset of the server at that date.
     */
    private static String formatInServerTimezone(Date date) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date.toInstant().atZone(ZoneId.of(SERVER_TIMEZONE)))
    }
}
