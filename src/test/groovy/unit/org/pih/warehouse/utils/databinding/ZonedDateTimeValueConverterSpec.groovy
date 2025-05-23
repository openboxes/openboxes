package unit.org.pih.warehouse.utils.databinding

import java.time.DateTimeException
import java.time.format.DateTimeParseException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.databinding.ZonedDateTimeValueConverter

@Unroll
class ZonedDateTimeValueConverterSpec extends Specification {

    @Shared
    ZonedDateTimeValueConverter converter

    void setupSpec() {
        converter = new ZonedDateTimeValueConverter()
    }

    void 'convertString should successfully parse valid strings for case: #scenario'() {
        expect:
        assert converter.convertString(givenDate).toString() == expectedConvertedDate

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
    }

    void 'convertString should fail to parse invalid strings for case: #failureReason'() {
        when:
        converter.convertString(givenDate)

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
        "2000-01-01"              || DateTimeException      | "No time"
        "2000 01 01"              || DateTimeException      | "Date with spaces, no time"
        "2000/01/01"              || DateTimeException      | "Date with slashes, no time"
        "01/01/2000"              || DateTimeException      | "Date with slashes, Excel format, no time"
        "20000101"                || DateTimeException      | "Date with no separator, no time"
    }
}
