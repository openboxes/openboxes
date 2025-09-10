package unit.org.pih.warehouse.core.date

import java.time.LocalDate
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.DateDisplayFormat
import org.pih.warehouse.core.date.DateDisplayStyle
import org.pih.warehouse.core.date.DatePatternLocalizer
import org.pih.warehouse.core.date.TemporalAccessorDateFormatter

@Unroll
class TemporalAccessorDateFormatterSpec extends Specification {

    static final LocalDate JAN_1ST_2000 = LocalDate.of(2000, 1, 1)

    @Shared
    DatePatternLocalizer datePatternLocalizerStub

    void setup() {
        datePatternLocalizerStub = Stub(DatePatternLocalizer)
    }

    void 'format can handle null inputs gracefully'() {
        given:
        TemporalAccessorDateFormatter formatter = initFormatter(Locale.ENGLISH, null, null, null)

        expect:
        formatter.format(null) == ''
    }

    void 'format errors when given no format options'() {
        given:
        TemporalAccessorDateFormatter formatter = initFormatter(Locale.ENGLISH, null, null, null)

        when:
        formatter.format(JAN_1ST_2000)

        then:
        thrown(IllegalArgumentException)
    }

    void 'format succeeds when given a display format for scenario: #scenario'() {
        given:
        TemporalAccessorDateFormatter formatter = initFormatter(locale, format, null, null)

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ENGLISH) >> 'dd/MMM/yyyy'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ITALIAN) >> 'MMM-dd-yyyy'

        expect:
        formatter.format(JAN_1ST_2000) == expectedResult

        where:
        locale         | format                 || expectedResult | scenario
        Locale.ENGLISH | DateDisplayFormat.GSP  || '01/Jan/2000'  | 'GSP format localization in english'
        Locale.ITALIAN | DateDisplayFormat.GSP  || 'gen-01-2000'  | 'GSP format localization in non-english'
        Locale.ENGLISH | DateDisplayFormat.CSV  || '2000-01-01'   | 'CSV format should use ISO strings (english)'
        Locale.ITALIAN | DateDisplayFormat.CSV  || '2000-01-01'   | 'CSV format should use ISO strings (non-english)'
        Locale.ENGLISH | DateDisplayFormat.JSON || '2000-01-01'   | 'JSON format should use ISO strings (english)'
        Locale.ITALIAN | DateDisplayFormat.JSON || '2000-01-01'   | 'JSON format should use ISO strings (non-english)'
    }

    void 'format succeeds when given a pattern override for scenario: #scenario'() {
        given:
        TemporalAccessorDateFormatter formatter = initFormatter(locale, null, 'yyyy.MMM.dd', null)

        expect:
        formatter.format(JAN_1ST_2000) == expectedResult

        where:
        locale         || expectedResult | scenario
        Locale.ENGLISH || '2000.Jan.01'  | 'format override localization in english'
        Locale.ITALIAN || '2000.gen.01'  | 'format override localization in non-english'
    }

    void 'format succeeds when given a display style for scenario: #scenario'() {
        given:
        TemporalAccessorDateFormatter formatter = initFormatter(locale, null, null, DateDisplayStyle.DATE)

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ENGLISH) >> 'dd/MMM/yyyy'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ITALIAN) >> 'MMM-dd-yyyy'

        expect:
        formatter.format(JAN_1ST_2000) == expectedResult

        where:
        locale         || expectedResult | scenario
        Locale.ENGLISH || '01/Jan/2000'  | 'DATE style localization in english'
        Locale.ITALIAN || 'gen-01-2000'  | 'DATE style localization in non-english'
    }

    private TemporalAccessorDateFormatter<LocalDate> initFormatter(
            Locale locale,
            DateDisplayFormat displayFormat,
            String patternOverride,
            DateDisplayStyle displayStyleOverride) {

        TemporalAccessorDateFormatter<LocalDate> formatter = Spy(new TemporalAccessorDateFormatter(
                locale, displayFormat, patternOverride, displayStyleOverride))

        formatter.getLocalizer() >> datePatternLocalizerStub

        return formatter
    }
}
