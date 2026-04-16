package org.pih.warehouse.core.date

import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.session.SessionManager

@Unroll
class InstantParserSpec extends Specification {

    @Shared
    InstantParser instantParser

    @Shared
    SessionManager sessionManagerStub

    void setup() {
        sessionManagerStub = Stub(SessionManager)
        instantParser = new InstantParser()
        instantParser.sessionManager = sessionManagerStub
    }

    void 'parse should safely handle null dates'() {
        expect:
        assert instantParser.parse(null) == null
    }

    void 'parse should successfully parse valid strings for case: #scenario'() {
        given: 'no default timezone to fallback to'
        sessionManagerStub.timezone >> null

        expect:
        assert instantParser.parse(givenDate).toString() == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00.000Z"  || "2000-01-01T00:00:00Z" | "Full string in proper ISO format"
        "2000-01-01T00:00:00.000 Z" || "2000-01-01T00:00:00Z" | "Full string, space before timezone"
        "2000-01-01 00:00:00.000Z"  || "2000-01-01T00:00:00Z" | "Replace 'T' with space"
        "2000-01-01T000000000Z"     || "2000-01-01T00:00:00Z" | "Time with no separator"
        "2000-01-01T00000000Z"      || "2000-01-01T00:00:00Z" | "Time with no separator, shorter millis"
        "2000-01-01T0000000Z"       || "2000-01-01T00:00:00Z" | "Time with no separator, shorter millis"
        "2000-01-01T000000Z"        || "2000-01-01T00:00:00Z" | "Time with no separator, no millis"
        "2000-01-01T0000Z"          || "2000-01-01T00:00:00Z" | "Time with no separator, no seconds"
        "2000-01-01T00:00:00:000Z"  || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':'"
        "2000-01-01T00:00:00:00Z"   || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':', shorter millis"
        "2000-01-01T00:00:00:0Z"    || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':', shorter millis"

        // Note that even though timezone-specific dates are given, Instant is always in reference to UTC so timezone
        // information is lost (unlike ZonedDateTime, where it is preserved).
        "2000-01-01T00:00:00-05"    || "2000-01-01T05:00:00Z" | "Timezone conforming to X format w/ negative"
        "2000-01-01T00:00:00-0500"  || "2000-01-01T05:00:00Z" | "Timezone conforming to XX format w/ negative"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z" | "Timezone conforming to XXX format w/ negative"
        "2000-01-01T00:00:00+00"    || "2000-01-01T00:00:00Z" | "Timezone conforming to X format w/ zero"
        "2000-01-01T00:00:00+0000"  || "2000-01-01T00:00:00Z" | "Timezone conforming to XX format w/ zero"
        "2000-01-01T00:00:00+00:00" || "2000-01-01T00:00:00Z" | "Timezone conforming to XXX format w/ zero"
        "2000-01-01T00:00:00+05"    || "1999-12-31T19:00:00Z" | "Timezone conforming to X format w/ positive"
        "2000-01-01T00:00:00+0500"  || "1999-12-31T19:00:00Z" | "Timezone conforming to XX format w/ positive"
        "2000-01-01T00:00:00+05:00" || "1999-12-31T19:00:00Z" | "Timezone conforming to XXX format w/ positive"
    }

    void 'parse should successfully parse valid strings with a default for case: #scenario'() {
        given: 'a default timezone to fallback to'
        sessionManagerStub.timezone >> TimeZone.getTimeZone('America/Montevideo')  // -03:00 (always, no DST)

        expect:
        assert instantParser.parse(givenDate).toString() == expectedConvertedDate

        where:
        // When the string has a timezone already, use that when converting (though note that the expected
        // date is still stringified to UTC because Instants have no timezone information)
        givenDate                       || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00.000Z"      || "2000-01-01T00:00:00Z" | "UTC Full string in proper ISO format"
        "2000-01-01T00:00:00Z"          || "2000-01-01T00:00:00Z" | "UTC No millis"
        "2000-01-01T00:00Z"             || "2000-01-01T00:00:00Z" | "UTC No seconds"
        "2000-01-01T00:00:00.000-05:00" || "2000-01-01T05:00:00Z" | "-5 Full string in proper ISO format"
        "2000-01-01T00:00:00-05:00"     || "2000-01-01T05:00:00Z" | "-5 No millis"
        "2000-01-01T00:00-05:00"        || "2000-01-01T05:00:00Z" | "-5 No seconds"

        // When the string has no time or zone information, default to midnight in the timezone in the user's session
        "2000-01-01"                    || "2000-01-01T03:00:00Z" | "No TZ Full string"
    }

    void 'parse should fail to parse invalid strings for case: #failureReason'() {
        given: 'no default timezone to fallback to'
        sessionManagerStub.timezone >> null

        when:
        instantParser.parse(givenDate)

        then:
        thrown(exception)

        where:
        givenDate                 || exception              | failureReason
        // Invalid formats
        "2000-01-32T00:00Z"       || DateTimeParseException | "day out of range"
        "2000-13-01T00:00Z"       || DateTimeParseException | "month out of range"
        "10000-13-01T00:00Z"      || DateTimeParseException | "year out of range"
        "00-01-01T00:00Z"         || DateTimeParseException | "two digit year format not supported"
        // If we don't have a timezone to fallback to, we always need to be given a full date + time + zone string
        "2000-01-01T00:00:00.000" || DateTimeException      | "full datetime, no timezone"
        "2000-01-01T00:00:00.00"  || DateTimeException      | "Shorter millis, no timezone"
        "2000-01-01T00:00:00.0"   || DateTimeException      | "Ever shorter millis, no timezone"
        "2000-01-01T00:00:00"     || DateTimeException      | "No millis, no timezone"
        "2000-01-01T00:00"        || DateTimeException      | "No seconds, no timezone"
        "2000-01-01"              || DateTimeException      | "No time"
        "2000 01 01"              || DateTimeException      | "Date with spaces, no time"
        "2000/01/01"              || DateTimeException      | "Date with slashes, no time"
        "01/01/2000"              || DateTimeException      | "Date with slashes, Excel format, no time"
        "20000101"                || DateTimeException      | "Date with no separator, no time"
    }

    void 'parse should fail to parse invalid strings with a default for case: #failureReason'() {
        given: 'a default timezone to fallback to'
        sessionManagerStub.timezone >> TimeZone.getTimeZone('America/Montevideo')  // -03:00 (always, no DST)

        when:
        instantParser.parse(givenDate)

        then:
        thrown(exception)

        where:
        givenDate                 || exception              | failureReason
        // Invalid formats
        "2000-01-32T00:00Z"       || DateTimeParseException | "day out of range"
        "2000-13-01T00:00Z"       || DateTimeParseException | "month out of range"
        "10000-13-01T00:00Z"      || DateTimeParseException | "year out of range"
        "00-01-01T00:00Z"         || DateTimeParseException | "two digit year format not supported"
        // Even if we have a timezone to fallback to, we treat a date + time (with no timezone) string as invalid
        "2000-01-01T00:00:00.000" || DateTimeException      | "full datetime, no timezone"
        "2000-01-01T00:00:00.00"  || DateTimeException      | "Shorter millis, no timezone"
        "2000-01-01T00:00:00.0"   || DateTimeException      | "Ever shorter millis, no timezone"
        "2000-01-01T00:00:00"     || DateTimeException      | "No millis, no timezone"
        "2000-01-01T00:00"        || DateTimeException      | "No seconds, no timezone"
    }

    void 'parse should successfully convert a LocalDate to an Instant for case: #scenario'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone(timezoneOfDate)

        and:
        LocalDate localDate = LocalDate.of(year, month, day)

        expect:
        assert instantParser.parse(localDate).toString() == expectedConvertedDate

        where:
        year | month | day | timezoneOfDate || expectedConvertedDate  | scenario
        2000 | 1     | 1   | "Z"            || "2000-01-01T00:00:00Z" | "UTC timezone"
        2000 | 1     | 1   | "GMT+05:00"    || "1999-12-31T19:00:00Z" | "timezone ahead of UTC"
        2000 | 1     | 1   | "GMT-05:00"    || "2000-01-01T05:00:00Z" | "timezone behind UTC"
    }

    void 'parse should successfully convert a Double to an Instant when given no workbook for case: #scenario'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone(timezoneOfDate)

        and: 'no workbook (which defaults to the windows 1900 based date system)'
        DateParserContext<Instant> context = new DateParserContext(excelWorkbook: null)

        expect:
        assert instantParser.parse(givenDate, context).toString() == expectedConvertedDate

        where:
        givenDate | timezoneOfDate || expectedConvertedDate  | scenario
        1.0       | "Z"            || '1900-01-01T00:00:00Z' | 'midnight UTC on day 1'
        1.5       | "Z"            || '1900-01-01T12:00:00Z' | 'noon UTC on day 1'
        367.0     | "Z"            || '1901-01-01T00:00:00Z' | 'midnight UTC on day 367'
        1.0       | "GMT+05:00"    || '1899-12-31T19:00:00Z' | 'midnight +05 on day 1'
        1.0       | "GMT-05:00"    || '1900-01-01T05:00:00Z' | 'midnight -05 on day 1'
    }

    void 'parse should successfully convert a Date to an Instant for case: #scenario'() {
        given:
        Date date = newDate(givenDate)

        expect:
        assert instantParser.parse(date).toString() == expectedConvertedDate

        where:
        givenDate                || expectedConvertedDate  | scenario
        "2000-01-01T00:00Z"      || "2000-01-01T00:00:00Z" | "UTC timezone"
        "2000-01-01T00:00+05:00" || "1999-12-31T19:00:00Z" | "timezone ahead of UTC"
        "2000-01-01T00:00-05:00" || "2000-01-01T05:00:00Z" | "timezone behind UTC"
    }

    void 'parse should successfully convert a Calendar to an Instant for case: #scenario'() {
        given:
        Calendar calendar = new Calendar.Builder()
                .setDate(year, month, day)
                .setTimeOfDay(hour, min, sec)
                .setTimeZone(TimeZone.getTimeZone(tz))
                .build()

        expect:
        assert instantParser.parse(calendar).toString() == expectedConvertedDate

        where:
        year | month | day | hour | min | sec | tz          || expectedConvertedDate  | scenario
        2000 | 0     | 1   | 0    | 0   | 0   | "Z"         || "2000-01-01T00:00:00Z" | "UTC timezone"
        2000 | 0     | 1   | 0    | 0   | 0   | "GMT+05:00" || "1999-12-31T19:00:00Z" | "timezone ahead of UTC"
        2000 | 0     | 1   | 0    | 0   | 0   | "GMT-05:00" || "2000-01-01T05:00:00Z" | "timezone behind UTC"
    }

    void 'parse should successfully convert a ZonedDateTime to an Instant for case: #scenario'() {
        given:
        ZonedDateTime zdt = ZonedDateTime.parse(givenDate)

        expect:
        assert instantParser.parse(zdt).toString() == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00Z"      || "2000-01-01T00:00:00Z" | "Full string in proper ISO format"
        "2000-01-01T00:00Z"         || "2000-01-01T00:00:00Z" | "No seconds"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z" | "Timezone conforming to XXX format w/ negative"
        "2000-01-01T00:00:00+00:00" || "2000-01-01T00:00:00Z" | "Timezone conforming to XXX format w/ zero"
        "2000-01-01T00:00:00+05:00" || "1999-12-31T19:00:00Z" | "Timezone conforming to XXX format w/ positive"
    }

    /**
     * Convenience method to build a Date for tests.
     */
    Date newDate(String date){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX").parse(date)
    }
}
