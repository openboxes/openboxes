package org.pih.warehouse.core.date

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.date.JavaUtilDateParser

@Unroll
class JavaUtilDateParserSpec extends Specification {

    @Shared
    JavaUtilDateParser javaUtilDateParser

    void setup() {
        javaUtilDateParser = new JavaUtilDateParser()
    }

    void 'parse should safely handle null dates'() {
        expect:
        assert javaUtilDateParser.parse(null) == null
    }

    void 'parse should successfully parse a String using the default format for case: #scenario'() {
        given:
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))

        when: 'we parse to a Date in the system local timezone'
        Date date = javaUtilDateParser.parse(givenDate)

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

    void 'parse should successfully parse a String when no timezone is given for case: #scenario'() {
        given: 'the current timezone offset of the system'
        ZoneOffset zone = DateUtil.getSystemZoneOffset()

        and: 'a format to make assertions with'
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone(zone))

        when:'we parse to a Date in the system local timezone'
        Date date = javaUtilDateParser.parse(givenDate)

        then: 'we format the Date (still in the system local time) for asserting against'
        assert expectedConvertedDate + zone == format.format(date)

        where:
        givenDate          || expectedConvertedDate | scenario
        "01/01/2000 00:00" || "2000-01-01T00:00:00" | "No timezone our format"
        "2000-01-01 00:00" || "2000-01-01T00:00:00" | "No timezone ISO format"
        "01/01/2000"       || "2000-01-01T00:00:00" | "No time or timezone our format"
        "2000-01-01"       || "2000-01-01T00:00:00" | "No time or timezone ISO format"
    }

    void 'parse should fail to parse a String using the default format for case: #failureReason'() {
        when:
        javaUtilDateParser.parse(givenDate)

        then:
        thrown(exception)

        where:
        givenDate    || exception              | failureReason
        "01 01 2000" || DateTimeParseException | "spaces not supported as separator"
        "01/01/00"   || DateTimeParseException | "two digit year format not supported"
    }

    void 'parse should successfully convert an Instant to a Date for case: #scenario'() {
        given:
        Instant instant = Instant.parse(givenDate)

        and: 'a formatter for stringifying the date to make assertions with'
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone("UTC"))

        when:
        Date date = javaUtilDateParser.parse(instant)

        then:
        assert format.format(date) == expectedConvertedDate

        where:
        givenDate                  || expectedConvertedDate     | scenario
        "2000-01-01T00:00:11Z"     || "2000-01-01T00:00:11Z"    | "UTC timezone with seconds precision"
        "2000-01-01T00:00:11.111Z" || "2000-01-01T00:00:11Z"    | "UTC timezone with millis precision"
    }

    void 'parse should successfully convert a ZonedDateTime to a Date'() {
        given:
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(givenDate)

        and: 'a formatter for stringifying the date to make assertions with'
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone("UTC"))

        when:
        Date date = javaUtilDateParser.parse(zonedDateTime)

        then: 'assert the dates match, dynamically appending the offset because it depends on the system running the test'
        assert format.format(date) == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00Z"      || "2000-01-01T00:00:00Z" | "UTC timezone"
        "2000-01-01T00:00:00+05:00" || "1999-12-31T19:00:00Z" | "+05 to UTC (backwards conversion)"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z" | "-05 to UTC (forwards conversion)"
    }

    void 'parse should successfully convert a LocalDate to a Date'(){
        given:
        LocalDate localDate = LocalDate.now()
        String expectedDate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        and: 'the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()

        and: 'a format to make assertions with'
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        format.setTimeZone(TimeZone.getTimeZone(offset))

        when:
        Date date = javaUtilDateParser.parse(localDate)

        then: 'format the Date to a string in current the system offset for asserting against'
        assert format.format(date) == "${expectedDate}T00:00:00${offset}"
    }
}
