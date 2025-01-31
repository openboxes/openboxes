package unit.org.pih.warehouse.utils.databinding

import java.time.format.DateTimeParseException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.databinding.LocalDateValueConverter

@Unroll
class LocalDateValueConverterSpec extends Specification {

    @Shared
    LocalDateValueConverter converter

    void setupSpec() {
        converter = new LocalDateValueConverter()
    }

    void 'convertString should successfully parse in valid strings: #scenario'() {
        expect:
        assert converter.convertString(givenDate).toString() == expectedResult

        where:
        givenDate    || expectedResult | scenario
        "2000-01-01" || "2000-01-01"   | "Date with dashes"
        "2000 01 01" || "2000-01-01"   | "Date with spaces"
        "2000/01/01" || "2000-01-01"   | "Date with slashes"
        "20000101"   || "2000-01-01"   | "Date with no separator"
    }

    void 'convertString should fail to parse invalid strings because: #failureReason'() {
        when:
        converter.convertString(givenDate)

        then:
        thrown(DateTimeParseException)

        where:
        givenDate          || failureReason
        "2000-01-01T00:00" || "LocalDate should have no time component"
        "2000-01-32"       || "day out of range"
        "2000-13-01"       || "month out of range"
        "10000-13-01"      || "year out of range"
    }
}
