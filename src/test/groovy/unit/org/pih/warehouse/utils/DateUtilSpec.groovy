package unit.org.pih.warehouse.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.DateUtil

@Unroll
class DateUtilSpec extends Specification {

    void 'asDate should successfully parse using the default format for case: #scenario'() {
        given: 'a format to Stringify the parsed Date as (for asserting against)'
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))

        when:
        Date date = DateUtil.asDate(givenDate)

        then:
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

        // This case fails because when no timezone is provided to Date, it uses the system local timezone instead of
        // UTC. This can cause unexpected behaviour! Adding "TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))"
        // to Bootstrap.groovy will change the default to always be UTC. This fixes the issue by making the behaviour
        // consistent (and would allow us to re-enable this test).
        //"01/01/2000"                || "2000-01-01T00:00:00Z" | "No time or timezone our format"

        // It's noteworthy that the "yyyy" format for java.util.Date supports a two year format but it is year 0 based,
        // not based on the year of this century (ie "25" is year 25, not 2025)!
        "01/01/00 00:00 Z"          || "0001-01-01T00:00:00Z" | "No time or timezone our format two digit year"
    }

    void 'asDate should fail to parse using the default format for case: #failureReason'() {
        when:
        DateUtil.asDate(givenDate)

        then:
        thrown(ParseException)

        where:
        givenDate    || failureReason
        "01 01 2000" || "spaces not supported as separator"
    }

    void 'asInstant should successfully convert a Date to an Instant for case: #scenario'() {
        given:
        Date date = new Date(givenDate)

        expect:
        assert DateUtil.asInstant(date).toString() == expectedConvertedDate

        where:
        givenDate                          || expectedConvertedDate  | scenario
        "Sat, 01 Jan 2000 00:00:00 UTC"    || "2000-01-01T00:00:00Z" | "UTC timezone"
        "Sat, 01 Jan 2000 00:00:00 UTC+05" || "1999-12-31T19:00:00Z" | "timezone ahead of UTC"
        "Sat, 01 Jan 2000 00:00:00 UTC-05" || "2000-01-01T05:00:00Z" | "timezone behind UTC"

        // This case fails because when no timezone is provided to Date, it uses the system local timezone instead of
        // UTC. This can cause unexpected behaviour! Adding "TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))"
        // to Bootstrap.groovy will change the default to always be UTC. This fixes the issue by making the behaviour
        // consistent (and would allow us to re-enable this test).
        //"Sat, 01 Jan 2000 00:00:00"        || "2000-01-01T00:00:00Z" | "no timezone given"
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

        expect:
        assert DateUtil.asZonedDateTime(instant).toString() == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate      | scenario
        "2000-01-01T00:00:11.111Z"  || "2000-01-01T00:00:11.111Z" | "Full string in proper ISO format"
        "2000-01-01T00:00:11.000Z"  || "2000-01-01T00:00:11Z"     | "Empty millis are removed"
        "2000-01-01T00:00:11Z"      || "2000-01-01T00:00:11Z"     | "No millis provided"
        "2000-01-01T00:00:00Z"      || "2000-01-01T00:00Z"        | "Empty seconds are removed"
    }

    void 'asZonedDateTime should successfully convert an Instant to a ZonedDateTime with tz for case: #scenario'() {
        given:
        Instant instant = Instant.parse(givenDate)

        expect:
        assert DateUtil.asZonedDateTime(instant, givenTimezone).toString() == expectedConvertedDate

        where:
        givenDate              | givenTimezone      || expectedConvertedDate    | scenario
        "2000-01-01T00:00:00Z" | ZoneId.of('Z')     || "2000-01-01T00:00Z"      | "UTC timezone"
        "2000-01-01T00:00:00Z" | ZoneId.of('+05')   || "2000-01-01T05:00+05:00" | "timezone ahead of UTC"
        "2000-01-01T00:00:00Z" | ZoneId.of('-05')   || "1999-12-31T19:00-05:00" | "timezone behind UTC"
    }

    void 'asZonedDateTime should successfully convert a Date to a ZonedDateTime for case: #scenario'() {
        given:
        Date date = new Date(givenDate)

        expect:
        assert DateUtil.asZonedDateTime(date).toString() == expectedConvertedDate

        where:
        givenDate                          || expectedConvertedDate | scenario
        "Sat, 01 Jan 2000 00:00:00 UTC"    || "2000-01-01T00:00Z"   | "UTC timezone"
        "Sat, 01 Jan 2000 00:00:00 UTC+05" || "1999-12-31T19:00Z"   | "timezone ahead of UTC"
        "Sat, 01 Jan 2000 00:00:00 UTC-05" || "2000-01-01T05:00Z"   | "timezone behind UTC"

        // This case fails because when no timezone is provided to Date, it uses the system local timezone instead of
        // UTC. This can cause unexpected behaviour! Adding "TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))"
        // to Bootstrap.groovy will change the default to always be UTC. This fixes the issue by making the behaviour
        // consistent (and would allow us to re-enable this test).
        //"Sat, 01 Jan 2000 00:00:00"        || "2000-01-01T00:00Z"   | "no timezone given"
    }
}
