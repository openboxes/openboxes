package unit.org.pih.warehouse.core.date

import java.time.LocalDate
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.DateDisplayFormat
import org.pih.warehouse.core.date.DateDisplayStyle
import org.pih.warehouse.core.date.DateFormatterContext
import org.pih.warehouse.core.date.DatePatternLocalizer
import org.pih.warehouse.core.date.LocalDateFormatter
import org.pih.warehouse.core.localization.LocaleManager

@Unroll
class LocalDateFormatterSpec extends Specification {

    static final LocalDate JAN_1ST_2000 = LocalDate.of(2000, 1, 1)

    @Shared
    DatePatternLocalizer datePatternLocalizerStub

    @Shared
    LocaleManager localeManagerStub

    @Shared
    LocalDateFormatter localDateFormatter

    void setup() {
        localDateFormatter = new LocalDateFormatter()
        datePatternLocalizerStub = Stub(DatePatternLocalizer)
        localDateFormatter.datePatternLocalizer = datePatternLocalizerStub
        localeManagerStub = Stub(LocaleManager)
        localDateFormatter.localeManager = localeManagerStub
    }

    void 'format can handle null inputs gracefully'() {
        expect:
        localDateFormatter.format(null) == null
    }

    void 'format succeeds when given no format options'() {
        given:
        localeManagerStub.getCurrentLocale() >> Locale.ENGLISH

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ENGLISH) >> 'dd/MMM/yyyy'

        expect:
        localDateFormatter.format(JAN_1ST_2000) == '01/Jan/2000'
    }

    void 'format succeeds when given a display format for scenario: #scenario'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withDisplayFormat(format)
                .build()

        and:
        localeManagerStub.getCurrentLocale() >> locale

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ENGLISH) >> 'dd/MMM/yyyy'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ITALIAN) >> 'MMM-dd-yyyy'

        expect:
        localDateFormatter.format(JAN_1ST_2000, context) == expectedResult

        where:
        locale         | format                 || expectedResult | scenario
        Locale.ENGLISH | DateDisplayFormat.GSP  || '01/Jan/2000'  | 'GSP format localization in english'
        Locale.ITALIAN | DateDisplayFormat.GSP  || 'gen-01-2000'  | 'GSP format localization in non-english'
        Locale.ENGLISH | DateDisplayFormat.CSV  || '01/Jan/2000'  | 'CSV format localization in english'
        Locale.ITALIAN | DateDisplayFormat.CSV  || '01/gen/2000'  | 'CSV format localization in non-english'
        Locale.ENGLISH | DateDisplayFormat.JSON || '2000-01-01'   | 'JSON format should use ISO strings (english)'
        Locale.ITALIAN | DateDisplayFormat.JSON || '2000-01-01'   | 'JSON format should use ISO strings (non-english)'
    }

    void 'format succeeds when given a pattern override for scenario: #scenario'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withPatternOverride('yyyy.MMM.dd')
                .build()

        and:
        localeManagerStub.getCurrentLocale() >> locale

        expect:
        localDateFormatter.format(JAN_1ST_2000, context) == expectedResult

        where:
        locale         || expectedResult | scenario
        Locale.ENGLISH || '2000.Jan.01'  | 'format override localization in english'
        Locale.ITALIAN || '2000.gen.01'  | 'format override localization in non-english'
    }

    void 'format succeeds when given a display style for scenario: #scenario'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withDisplayStyleOverride(DateDisplayStyle.DATE)
                .build()

        and:
        localeManagerStub.getCurrentLocale() >> locale

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ENGLISH) >> 'dd/MMM/yyyy'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE, Locale.ITALIAN) >> 'MMM-dd-yyyy'

        expect:
        localDateFormatter.format(JAN_1ST_2000, context) == expectedResult

        where:
        locale         || expectedResult | scenario
        Locale.ENGLISH || '01/Jan/2000'  | 'DATE style localization in english'
        Locale.ITALIAN || 'gen-01-2000'  | 'DATE style localization in non-english'
    }
}
