package unit.org.pih.warehouse.utils.databinding

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.databinding.InstantValueConverter

@Unroll
class InstantValueConverterSpec extends Specification {

    @Shared
    InstantValueConverter converter

    void setupSpec() {
        converter = new InstantValueConverter()
    }

    void 'convertString should successfully parse in valid strings for case: #scenario'() {
        expect:
        assert converter.convertString(givenDate).toString() == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00.000Z"  || "2000-01-01T00:00:00Z" | "Full string in proper ISO format"
        "2000-01-01T00:00:00.000"   || "2000-01-01T00:00:00Z" | "Full string, no timezone"
        "2000-01-01T00:00:00.00"    || "2000-01-01T00:00:00Z" | "Shorter millis"
        "2000-01-01T00:00:00.0"     || "2000-01-01T00:00:00Z" | "Shorter millis"
        "2000-01-01T00:00:00"       || "2000-01-01T00:00:00Z" | "No millis"
        "2000-01-01T00:00"          || "2000-01-01T00:00:00Z" | "No seconds"
        "2000-01-01"                || "2000-01-01T00:00:00Z" | "No time"
        "2000 01 01"                || "2000-01-01T00:00:00Z" | "Date with spaces"
        "2000/01/01"                || "2000-01-01T00:00:00Z" | "Date with slashes"
        "20000101"                  || "2000-01-01T00:00:00Z" | "Date with no separator"
        "2000-01-01 00:00:00.000Z"  || "2000-01-01T00:00:00Z" | "Replace 'T' with space"
        "2000-01-01T000000000Z"     || "2000-01-01T00:00:00Z" | "Time with no separator"
        "2000-01-01T00000000Z"      || "2000-01-01T00:00:00Z" | "Time with no separator, shorter millis"
        "2000-01-01T0000000Z"       || "2000-01-01T00:00:00Z" | "Time with no separator, shorter millis"
        "2000-01-01T000000Z"        || "2000-01-01T00:00:00Z" | "Time with no separator, no millis"
        "2000-01-01T0000Z"          || "2000-01-01T00:00:00Z" | "Time with no separator, no seconds"
        "2000-01-01T00:00:00:000Z"  || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':'"
        "2000-01-01T00:00:00:00Z"   || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':', shorter millis"
        "2000-01-01T00:00:00:0Z"    || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':', shorter millis"
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
}
