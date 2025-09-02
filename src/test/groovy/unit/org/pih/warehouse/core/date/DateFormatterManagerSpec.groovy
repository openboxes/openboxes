package unit.org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.DateDisplayFormat
import org.pih.warehouse.core.date.DateFormatterManager
import org.pih.warehouse.core.session.SessionManager

import static org.pih.warehouse.core.date.DateFormatterManager.DateFormatterContext

/**
 * Tests the DateFormatterManager.
 * We don't test the specific formatters themselves. We let those be tested separately.
 */
@Unroll
class DateFormatterManagerSpec extends Specification {

    @Shared
    DateFormatterManager dateFormatterManager

    @Shared
    SessionManager sessionManagerStub

    void setup() {
        dateFormatterManager = new DateFormatterManager()

        sessionManagerStub = Stub(SessionManager)
        dateFormatterManager.sessionManager = sessionManagerStub
    }

    void 'format does not error when given an Instant and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')

        and:
        Instant instant = Instant.now()

        when:
        String result = dateFormatterManager.format(instant)

        then:
        assert StringUtils.isNotBlank(result)
        assert result.endsWith('Z')
    }

    void 'format does not error when given an Instant and overrides'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(Locale.ENGLISH)
                .withPatternOverride('XXX')
                .withTimezoneOverride(ZoneId.of('+01:00'))
                .withDisplayFormat(DateDisplayFormat.GSP)
                .build()

        and:
        Instant instant = Instant.now()

        when:
        String result = dateFormatterManager.format(instant, context)

        then:
        assert result == '+01:00'
    }

    void 'format does not error when given a ZonedDateTime and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')

        and:
        ZonedDateTime zdt = ZonedDateTime.now()

        when:
        String result = dateFormatterManager.format(zdt)

        then:
        assert StringUtils.isNotBlank(result)
        assert result.endsWith('Z')
    }

    void 'format does not error when given a ZonedDateTime and overrides'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(Locale.ENGLISH)
                .withPatternOverride('XXX')
                .withTimezoneOverride(ZoneId.of('+01:00'))
                .withDisplayFormat(DateDisplayFormat.GSP)
                .build()

        and:
        ZonedDateTime zdt = ZonedDateTime.now()

        when:
        String result = dateFormatterManager.format(zdt, context)

        then:
        assert result == '+01:00'
    }

    void 'format does not error when given a LocalDate and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')

        and:
        LocalDate localDate = LocalDate.of(2000, 1, 1)

        when:
        String result = dateFormatterManager.format(localDate)

        then:
        assert result == '01/Jan/2000'
    }

    void 'format does not error when given a LocalDate and overrides'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(Locale.ENGLISH)
                .withPatternOverride('yyyy-MM-dd')
                .withTimezoneOverride(ZoneId.of('+01:00'))  // Does nothing for LocalDate
                .withDisplayFormat(DateDisplayFormat.GSP)
                .build()

        and:
        LocalDate localDate = LocalDate.of(2000, 1, 1)

        when:
        String result = dateFormatterManager.format(localDate, context)

        then:
        assert result == '2000-01-01'
    }
}
