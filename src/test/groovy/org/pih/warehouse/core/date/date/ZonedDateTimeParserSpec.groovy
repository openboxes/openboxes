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

import org.pih.warehouse.core.date.ZonedDateTimeParser
import org.pih.warehouse.core.session.SessionManager

@Unroll
class ZonedDateTimeParserSpec extends Specification {

    @Shared
    ZonedDateTimeParser zonedDateTimeParser

    @Shared
    SessionManager sessionManagerStub

    void setup() {
        sessionManagerStub = Stub(SessionManager)
        zonedDateTimeParser = new ZonedDateTimeParser()
        zonedDateTimeParser.sessionManager = sessionManagerStub
    }

    void 'parse should safely handle null dates'() {
        expect:
        assert zonedDateTimeParser.parse(null) == null
    }

    void 'parse should successfully parse valid strings for case: #scenario'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('Z')

        expect:
        assert zonedDateTimeParser.parse(givenDate).toString() == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate    | scenario
        "2000-01-01T00:00:00.000Z"  || "2000-01-01T00:00Z"      | "Full string in proper ISO format"
        "2000-01-01T00:00:00.000 Z" || "2000-01-01T00:00Z"      | "Full string, space before timezone"
        "2000-01-01 00:00:00.000Z"  || "2000-01-01T00:00Z"      | "Replace 'T' with space"
        "2000-01-01T000000000Z"     || "2000-01-01T00:00Z"      | "Time with no separator"
        "2000-01-01T00000000Z"      || "2000-01-01T00:00Z"      | "Time with no separator, shorter millis"
        "2000-01-01T0000000Z"       || "2000-01-01T00:00Z"      | "Time with no separator, shorter millis"
        "2000-01-01T000000Z"        || "2000-01-01T00:00Z"      | "Time with no separator, no millis"
        "2000-01-01T0000Z"          || "2000-01-01T00:00Z"      | "Time with no separator, no seconds"
        "2000-01-01T00:00:00:000Z"  || "2000-01-01T00:00Z"      | "Time with millis replace '.' with ':'"
        "2000-01-01T00:00:00:00Z"   || "2000-01-01T00:00Z"      | "Time with millis replace '.' with ':', shorter millis"
        "2000-01-01T00:00:00:0Z"    || "2000-01-01T00:00Z"      | "Time with millis replace '.' with ':', shorter millis"

        // Note that (unlike Instant) timezone information is preserved.
        "2000-01-01T00:00:00-05"    || "2000-01-01T00:00-05:00" | "Timezone conforming to X format w/ negative"
        "2000-01-01T00:00:00-0500"  || "2000-01-01T00:00-05:00" | "Timezone conforming to XX format w/ negative"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T00:00-05:00" | "Timezone conforming to XXX format w/ negative"
        "2000-01-01T00:00:00+00"    || "2000-01-01T00:00Z"      | "Timezone conforming to X format w/ zero"
        "2000-01-01T00:00:00+0000"  || "2000-01-01T00:00Z"      | "Timezone conforming to XX format w/ zero"
        "2000-01-01T00:00:00+00:00" || "2000-01-01T00:00Z"      | "Timezone conforming to XXX format w/ zero"
        "2000-01-01T00:00:00+05"    || "2000-01-01T00:00+05:00" | "Timezone conforming to X format w/ positive"
        "2000-01-01T00:00:00+0500"  || "2000-01-01T00:00+05:00" | "Timezone conforming to XX format w/ positive"
        "2000-01-01T00:00:00+05:00" || "2000-01-01T00:00+05:00" | "Timezone conforming to XXX format w/ positive"

        // When given a date with no time, we default to midnight in the current timezone
        "2000-01-01"                || "2000-01-01T00:00Z[GMT]" | "No time"
        "2000 01 01"                || "2000-01-01T00:00Z[GMT]" | "Date with spaces, no time"
        "2000/01/01"                || "2000-01-01T00:00Z[GMT]" | "Date with slashes, no time"
        "01/01/2000"                || "2000-01-01T00:00Z[GMT]" | "Date with slashes, Excel format, no time"
        "20000101"                  || "2000-01-01T00:00Z[GMT]" | "Date with no separator, no time"
    }

    void 'parse should fail to parse invalid strings for case: #failureReason'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('Z')

        when:
        zonedDateTimeParser.parse(givenDate)

        then:
        thrown(exception)

        where:
        givenDate                 || exception              | failureReason
        "2000-01-32T00:00Z"       || DateTimeParseException | "day out of range"
        "2000-13-01T00:00Z"       || DateTimeParseException | "month out of range"
        "10000-13-01T00:00Z"      || DateTimeParseException | "year out of range"
        "00-01-01T00:00Z"         || DateTimeParseException | "two digit year format not supported"
        "2000-01-01T00:00:00.000" || DateTimeException      | "full datetime, no timezone"
        "2000-01-01T00:00:00.00"  || DateTimeException      | "Shorter millis, no timezone"
        "2000-01-01T00:00:00.0"   || DateTimeException      | "Ever shorter millis, no timezone"
        "2000-01-01T00:00:00"     || DateTimeException      | "No millis, no timezone"
        "2000-01-01T00:00"        || DateTimeException      | "No seconds, no timezone"
    }

    void 'parse should successfully convert an Instant to a ZonedDateTime for case: #scenario'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone(timezoneToDisplay)

        and:
        Instant instant = Instant.parse(givenDate)

        when:
        ZonedDateTime zdt = zonedDateTimeParser.parse(instant)

        then:
        assert zdt.toString() == expectedConvertedDate

        where:
        givenDate                  | timezoneToDisplay || expectedConvertedDate               | scenario
        "2000-01-01T00:00:00Z"     | 'Z'               || "2000-01-01T00:00Z[GMT]"            | "UTC timezone"
        "2000-01-01T00:00:11.111Z" | 'Z'               || "2000-01-01T00:00:11.111Z[GMT]"     | "UTC timezone with millis"
        "2000-01-01T00:00:11.000Z" | 'Z'               || "2000-01-01T00:00:11Z[GMT]"         | "Empty millis are removed"
        "2000-01-01T00:00:00Z"     | 'GMT+05:00'       || "2000-01-01T05:00+05:00[GMT+05:00]" | "timezone ahead of UTC"
        "2000-01-01T00:00:00Z"     | 'GMT-05:00'       || "1999-12-31T19:00-05:00[GMT-05:00]" | "timezone behind UTC"
    }

    void 'parse should successfully convert a Date to a ZonedDateTime for case: #scenario'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone(timezoneToDisplay)

        and:
        Date date = newDate(givenDate)

        when:
        ZonedDateTime zdt = zonedDateTimeParser.parse(date)

        then:
        assert zdt.toString() == expectedConvertedDate

        where:
        givenDate                | timezoneToDisplay || expectedConvertedDate               | scenario
        "2000-01-01T00:00Z"      | 'Z'               || "2000-01-01T00:00Z[GMT]"            | "UTC to UTC (no conversion)"
        "2000-01-01T00:00+05:00" | 'Z'               || "1999-12-31T19:00Z[GMT]"            | "+05 to UTC (backwards conversion)"
        "2000-01-01T00:00-05:00" | 'Z'               || "2000-01-01T05:00Z[GMT]"            | "-05 to UTC (forwards conversion)"
        "2000-01-01T00:00Z"      | 'GMT+05:00'       || "2000-01-01T05:00+05:00[GMT+05:00]" | "UTC to +05 (forwards conversion)"
        "2000-01-01T00:00+05:00" | 'GMT+05:00'       || "2000-01-01T00:00+05:00[GMT+05:00]" | "+05 to +05 (no conversion)"
        "2000-01-01T00:00+06:00" | 'GMT+05:00'       || "1999-12-31T23:00+05:00[GMT+05:00]" | "+06 to +05 (backwards conversion)"
    }

    void 'parse should successfully convert a LocalDate to a ZonedDateTime for case: #scenario'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone(timezoneToDisplay)

        and:
        LocalDate localDate = LocalDate.of(year, month, day)

        expect:
        assert zonedDateTimeParser.parse(localDate).toString() == expectedConvertedDate

        where:
        year | month | day | timezoneToDisplay || expectedConvertedDate               | scenario
        2000 | 1     | 1   | "Z"               || "2000-01-01T00:00Z[GMT]"            | "UTC timezone"
        2000 | 1     | 1   | "GMT+05:00"       || "2000-01-01T00:00+05:00[GMT+05:00]" | "timezone ahead of UTC"
        2000 | 1     | 1   | "GMT-05:00"       || "2000-01-01T00:00-05:00[GMT-05:00]" | "timezone behind UTC"
    }

    void 'parse should successfully convert a Calendar to an Instant for case: #scenario'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone(timezoneToDisplay)

        and:
        Calendar calendar = new Calendar.Builder()
                .setDate(year, month, day)
                .setTimeOfDay(hour, min, sec)
                .setTimeZone(TimeZone.getTimeZone(timezoneToDisplay))
                .build()

        expect:
        assert zonedDateTimeParser.parse(calendar).toString() == expectedConvertedDate

        where:
        year | month | day | hour | min | sec | timezoneToDisplay || expectedConvertedDate               | scenario
        2000 | 0     | 1   | 0    | 0   | 0   | "Z"               || "2000-01-01T00:00Z[GMT]"            | "UTC timezone"
        2000 | 0     | 1   | 0    | 0   | 0   | "GMT+05:00"       || "2000-01-01T00:00+05:00[GMT+05:00]" | "timezone ahead of UTC"
        2000 | 0     | 1   | 0    | 0   | 0   | "GMT-05:00"       || "2000-01-01T00:00-05:00[GMT-05:00]" | "timezone behind UTC"
    }

    /**
     * Convenience method to build a Date for tests.
     */
    Date newDate(String date){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX").parse(date)
    }
}
