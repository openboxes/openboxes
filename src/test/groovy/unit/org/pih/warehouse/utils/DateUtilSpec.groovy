package unit.org.pih.warehouse.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.DateUtil

@Unroll
class DateUtilSpec extends Specification {

    void 'asDate should successfully parse using the default format for case: #scenario'() {
        given:
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))

        when: 'we parse to a Date in the system local timezone'
        Date date = DateUtil.asDate(givenDate)

        then: 'we format the Date to UTC for asserting against'
        assert expectedConvertedDate == format.format(date)

        where:
        givenDate                   || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00Z"      || "2000-01-01T00:00:00Z" | "Full string in proper ISO format"
        "2000-01-01T00:00Z"         || "2000-01-01T00:00:00Z" | "No seconds"
        "2000-01-01T00:00:00-05"    || "2000-01-01T05:00:00Z" | "Timezone conforming to X format w/ negative"
        "2000-01-01T00:00:00-0500"  || "2000-01-01T05:00:00Z" | "Timezone conforming to XX format w/ negative"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z" | "Timezone conforming to XXX format w/ negative"
        "2000-01-01T00:00:00+00"    || "2000-01-01T00:00:00Z" | "Timezone conforming to X format w/ zero"
        "2000-01-01T00:00:00+0000"  || "2000-01-01T00:00:00Z" | "Timezone conforming to XX format w/ zero"
        "2000-01-01T00:00:00+00:00" || "2000-01-01T00:00:00Z" | "Timezone conforming to XXX format w/ zero"
        "2000-01-01T00:00:00+05"    || "1999-12-31T19:00:00Z" | "Timezone conforming to X format w/ positive"
        "2000-01-01T00:00:00+0500"  || "1999-12-31T19:00:00Z" | "Timezone conforming to XX format w/ positive"
        "2000-01-01T00:00:00+05:00" || "1999-12-31T19:00:00Z" | "Timezone conforming to XXX format w/ positive"

        "01/01/2000 00:00:00 Z"     || "2000-01-01T00:00:00Z" | "Full string in our format"
        "01/01/2000 00:00 Z"        || "2000-01-01T00:00:00Z" | "No seconds our format"
        "01/01/2000 00:00 -05"      || "2000-01-01T05:00:00Z" | "Timezone conforming to X format w/ negative our format"
        "01/01/2000 00:00 -0500"    || "2000-01-01T05:00:00Z" | "Timezone conforming to XX format w/ negative our format"
        "01/01/2000 00:00 -05:00"   || "2000-01-01T05:00:00Z" | "Timezone conforming to XXX format w/ negative our format"
        "01/01/2000 00:00 +00"      || "2000-01-01T00:00:00Z" | "Timezone conforming to X format w/ zero our format"
        "01/01/2000 00:00 +0000"    || "2000-01-01T00:00:00Z" | "Timezone conforming to XX format w/ zero our format"
        "01/01/2000 00:00 +00:00"   || "2000-01-01T00:00:00Z" | "Timezone conforming to XXX format w/ zero our format"
        "01/01/2000 00:00 +05"      || "1999-12-31T19:00:00Z" | "Timezone conforming to X format w/ positive our format"
        "01/01/2000 00:00 +0500"    || "1999-12-31T19:00:00Z" | "Timezone conforming to XX format w/ positive our format"
        "01/01/2000 00:00 +05:00"   || "1999-12-31T19:00:00Z" | "Timezone conforming to XXX format w/ positive our format"

        // When not given a time, we default to midnight.
        "01/01/2000 Z"              || "2000-01-01T00:00:00Z" | "No time our format"
        "2000-01-01 Z"              || "2000-01-01T00:00:00Z" | "No time ISO format"
    }

    void 'asDate should successfully parse when no timezone is given for case: #scenario'() {
        given: 'the current timezone offset of the system'
        ZoneOffset zone = DateUtil.getSystemZoneOffset()

        and: 'a format to make assertions with'
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone(zone))

        when:'we parse to a Date in the system local timezone'
        Date date = DateUtil.asDate(givenDate)

        then: 'we format the Date (still in the system local time) for asserting against'
        assert expectedConvertedDate + zone == format.format(date)

        where:
        givenDate          || expectedConvertedDate | scenario
        "01/01/2000 00:00" || "2000-01-01T00:00:00" | "No timezone our format"
        "2000-01-01 00:00" || "2000-01-01T00:00:00" | "No timezone ISO format"
        "01/01/2000"       || "2000-01-01T00:00:00" | "No time or timezone our format"
        "2000-01-01"       || "2000-01-01T00:00:00" | "No time or timezone ISO format"
    }

    void 'asDate should fail to parse using the default format for case: #failureReason'() {
        when:
        DateUtil.asDate(givenDate)

        then:
        thrown(exception)

        where:
        givenDate    || exception              | failureReason
        "01 01 2000" || DateTimeParseException | "spaces not supported as separator"
        "01/01/00"   || DateTimeParseException | "two digit year format not supported"
    }

    void 'asDate should successfully convert a LocalDate to a Date'(){
        given:
        LocalDate localDate = LocalDate.of(2000, 1, 1)

        and: 'the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()

        and: 'a format to make assertions with'
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone(offset))

        when:
        Date date = DateUtil.asDate(localDate)

        then: 'format the Date to a string in current the system offset for asserting against'
        assert format.format(date) == '2000-01-01T00:00:00' + offset
    }

    void 'asInstant should successfully convert a Date to an Instant for case: #scenario'() {
        given:
        Date date = newDate(givenDate)

        expect:
        assert DateUtil.asInstant(date).toString() == expectedConvertedDate

        where:
        givenDate                || expectedConvertedDate  | scenario
        "2000-01-01T00:00Z"      || "2000-01-01T00:00:00Z" | "UTC timezone"
        "2000-01-01T00:00+05:00" || "1999-12-31T19:00:00Z" | "timezone ahead of UTC"
        "2000-01-01T00:00-05:00" || "2000-01-01T05:00:00Z" | "timezone behind UTC"
    }

    void 'asInstant should successfully convert a ZonedDateTime to an Instant for case: #scenario'() {
        given:
        ZonedDateTime zdt = ZonedDateTime.parse(givenDate)

        expect:
        assert DateUtil.asInstant(zdt).toString() == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00Z"      || "2000-01-01T00:00:00Z" | "Full string in proper ISO format"
        "2000-01-01T00:00Z"         || "2000-01-01T00:00:00Z" | "No seconds"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z" | "Timezone conforming to XXX format w/ negative"
        "2000-01-01T00:00:00+00:00" || "2000-01-01T00:00:00Z" | "Timezone conforming to XXX format w/ zero"
        "2000-01-01T00:00:00+05:00" || "1999-12-31T19:00:00Z" | "Timezone conforming to XXX format w/ positive"
    }

    void 'asZonedDateTime should successfully convert an Instant to a ZonedDateTime for case: #scenario'() {
        given:
        Instant instant = Instant.parse(givenDate)

        and:
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC)

        when: 'we parse to a ZonedDateTime in the system local timezone'
        ZonedDateTime zdt = DateUtil.asZonedDateTime(instant)

        then: 'we format the ZonedDateTime to UTC for asserting against'
        assert formatter.format(zdt) == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate      | scenario
        "2000-01-01T00:00:11.111Z"  || "2000-01-01T00:00:11.111Z" | "Full string in proper ISO format"
        "2000-01-01T00:00:11.000Z"  || "2000-01-01T00:00:11Z"     | "Empty millis are removed"
        "2000-01-01T00:00:11Z"      || "2000-01-01T00:00:11Z"     | "No millis provided"
    }

    void 'asZonedDateTime should successfully convert an Instant with tz to a ZonedDateTime for case: #scenario'() {
        given:
        Instant instant = Instant.parse(givenDate)
        ZoneId zone = ZoneId.of(timezoneToDisplay)

        when:
        ZonedDateTime zdt = DateUtil.asZonedDateTime(instant, zone)

        then:
        assert zdt.toString() == expectedConvertedDate

        where:
        givenDate              | timezoneToDisplay || expectedConvertedDate    | scenario
        "2000-01-01T00:00:00Z" | 'Z'               || "2000-01-01T00:00Z"      | "UTC timezone"
        "2000-01-01T00:00:00Z" | '+05:00'          || "2000-01-01T05:00+05:00" | "timezone ahead of UTC"
        "2000-01-01T00:00:00Z" | '-05:00'          || "1999-12-31T19:00-05:00" | "timezone behind UTC"
    }

    void 'asZonedDateTime should successfully convert a Date to a ZonedDateTime for case: #scenario'() {
        given:
        Date date = newDate(givenDate)

        and:
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC)

        when: 'we parse to a ZonedDateTime in the system local timezone'
        ZonedDateTime zdt = DateUtil.asZonedDateTime(date)

        then: 'we format the ZonedDateTime to UTC for asserting against'
        assert formatter.format(zdt) == expectedConvertedDate

        where:
        givenDate                || expectedConvertedDate  | scenario
        "2000-01-01T00:00Z"      || "2000-01-01T00:00:00Z" | "UTC timezone"
        "2000-01-01T00:00+05:00" || "1999-12-31T19:00:00Z" | "timezone ahead of UTC"
        "2000-01-01T00:00-05:00" || "2000-01-01T05:00:00Z" | "timezone behind UTC"
    }

    void 'asZonedDateTime should successfully convert a Date with tz to a ZonedDateTime for case: #scenario'() {
        given:
        Date date = newDate(givenDate)
        ZoneId zone = ZoneId.of(timezoneToDisplay)

        when:
        ZonedDateTime zdt = DateUtil.asZonedDateTime(date, zone)

        then:
        assert zdt.toString() == expectedConvertedDate

        where:
        givenDate                | timezoneToDisplay || expectedConvertedDate    | scenario
        "2000-01-01T00:00Z"      | 'Z'               || "2000-01-01T00:00Z"      | "UTC to UTC (no conversion)"
        "2000-01-01T00:00+05:00" | 'Z'               || "1999-12-31T19:00Z"      | "+05 to UTC (backwards conversion)"
        "2000-01-01T00:00-05:00" | 'Z'               || "2000-01-01T05:00Z"      | "-05 to UTC (forwards conversion)"
        "2000-01-01T00:00Z"      | '+05:00'          || "2000-01-01T05:00+05:00" | "UTC to +05 (forwards conversion)"
        "2000-01-01T00:00+05:00" | '+05:00'          || "2000-01-01T00:00+05:00" | "+05 to +05 (no conversion)"
        "2000-01-01T00:00+06:00" | '+05:00'          || "1999-12-31T23:00+05:00" | "+06 to +05 (backwards conversion)"
    }

    void 'asLocalDate should successfully convert a Date to a LocalDate'() {
        given: 'a date with the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()
        Date date = newDate("2000-01-01T00:00${offset}")

        expect:
        assert DateUtil.asLocalDate(date).toString() == "2000-01-01"
    }

    void 'asLocalDate should successfully convert a Date with tz to a LocalDate for case: #scenario'(){
        given:
        Date date = newDate(givenDate)
        ZoneId zone = ZoneId.of(timezoneToDisplay)

        when:
        LocalDate localDate = DateUtil.asLocalDate(date, zone)

        then:
        assert localDate.toString() == expectedConvertedDate

        where:
        givenDate                | timezoneToDisplay || expectedConvertedDate | scenario
        "2000-01-01T00:00Z"      | 'Z'               || "2000-01-01"          | "UTC to UTC (no conversion)"
        "2000-01-01T00:00+05:00" | 'Z'               || "1999-12-31"          | "+05 to UTC (backwards conversion)"
        "2000-01-01T00:00-05:00" | 'Z'               || "2000-01-01"          | "-05 to UTC (forwards conversion)"
        "2000-01-01T00:00Z"      | '+05:00'          || "2000-01-01"          | "UTC to +05 (forwards conversion)"
        "2000-01-01T00:00+05:00" | '+05:00'          || "2000-01-01"          | "+05 to +05 (no conversion)"
        "2000-01-01T00:00+06:00" | '+05:00'          || "1999-12-31"          | "+06 to +05 (backwards conversion)"
    }

    void 'asDateTimeForDisplay should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert DateUtil.asDateTimeForDisplay(date) == ''
    }

    void 'asDateTimeForDisplay should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()
        Date date = newDate("2000-01-01T00:00" + offset)

        expect:
        assert DateUtil.asDateTimeForDisplay(date) == "01/Jan/2000 00:00:00 " + offset
    }

    void 'asDateTimeForDisplay should successfully convert a Date with a tz to a String for case: #scenario'() {
        given:
        Date date = newDate(givenDate)
        ZoneId zone = ZoneId.of(timezoneToDisplay)

        expect:
        assert DateUtil.asDateTimeForDisplay(date, zone) == expectedConvertedDate

        where:
        givenDate           | timezoneToDisplay || expectedConvertedDate         | scenario
        "2000-01-01T00:00Z" | 'Z'               || "01/Jan/2000 00:00:00 Z"      | "UTC timezone"
        "2000-01-01T00:00Z" | '+05:00'          || "01/Jan/2000 05:00:00 +05:00" | "timezone ahead of UTC"
        "2000-01-01T00:00Z" | '-05:00'          || "31/Dec/1999 19:00:00 -05:00" | "timezone behind UTC"
    }

    void 'asDateForDisplay should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert DateUtil.asDateForDisplay(date) == ''
    }

    void 'asDateForDisplay should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()
        Date date = newDate("2000-01-01T00:00" + offset)

        expect:
        assert DateUtil.asDateForDisplay(date) == "01/Jan/2000"
    }

    void 'asDateForDisplay should successfully convert a Date with a tz to a String for case: #scenario'() {
        given:
        Date date = newDate(givenDate)
        ZoneId zone = ZoneId.of(timezoneToDisplay)

        expect:
        assert DateUtil.asDateForDisplay(date, zone) == expectedConvertedDate

        where:
        givenDate           | timezoneToDisplay || expectedConvertedDate | scenario
        "2000-01-01T00:00Z" | 'Z'               || "01/Jan/2000"         | "UTC timezone"
        "2000-01-01T00:00Z" | '+05:00'          || "01/Jan/2000"         | "timezone ahead of UTC"
        "2000-01-01T00:00Z" | '-05:00'          || "31/Dec/1999"         | "timezone behind UTC"
    }

    /**
     * Convenience method to build a Date for tests.
     */
    Date newDate(String date){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX").parse(date)
    }
}
