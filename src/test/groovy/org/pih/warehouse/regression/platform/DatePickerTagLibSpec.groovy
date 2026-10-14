package org.pih.warehouse.regression.platform

import spock.lang.PendingFeature

import org.pih.warehouse.*
import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.session.SessionManager

class DatePickerTagLibSpec extends Specification implements TagLibUnitTest<DatePickerTagLib> {

    Closure doWithSpring() {{ ->
        grailsTagDateHelper(org.grails.plugins.web.DefaultGrailsTagDateHelper)
    }}

    void setupSpec() {
        mockTagLib(org.grails.plugins.web.taglib.FormTagLib)
    }

    void setup() {
        tagLib.sessionManager = Stub(SessionManager) {
            getTimezone() >> TimeZone.getTimeZone('America/New_York')
        }
        tagLib.configService = new ConfigService(grailsApplication: grailsApplication)
    }

    private static String selected(String html, String field) {
        def matcher = html =~ /(?s)name="${field}"[^>]*>.*?<option value="([^"]*)" selected="selected"/
        return matcher.find() ? matcher.group(1) : null
    }

    void 'datePicker shows an Instant in the user timezone with minute precision by default'() {
        when:
        String html = tagLib.datePicker(name: 'start', value: Instant.parse('2026-03-05T12:30:00Z')).toString()

        then:
        selected(html, 'start_year') == '2026'
        selected(html, 'start_month') == '3'
        selected(html, 'start_day') == '5'
        selected(html, 'start_hour') == '07'
        selected(html, 'start_minute') == '30'
    }

    void 'datePicker converts a ZonedDateTime to the user timezone'() {
        when:
        String html = tagLib.datePicker(name: 'start',
                value: ZonedDateTime.of(2026, 3, 5, 23, 15, 0, 0, ZoneId.of('UTC'))).toString()

        then:
        selected(html, 'start_day') == '5'
        selected(html, 'start_hour') == '18'
        selected(html, 'start_minute') == '15'
    }

    void 'datePicker uses the configured default precision'() {
        given:
        config.grails.tags.datePicker.default.precision = 'day'

        when:
        String html = tagLib.datePicker(name: 'd', value: LocalDate.of(2026, 1, 31)).toString()

        then:
        selected(html, 'd_day') == '31'
        selected(html, 'd_month') == '1'
        !html.contains('d_hour')
    }

    void 'datePicker refuses time precision for a LocalDate'() {
        when:
        tagLib.datePicker(name: 'd', value: LocalDate.of(2026, 1, 31), precision: 'minute')

        then:
        thrown(IllegalArgumentException)
    }

    void 'an empty datePicker defaults to now in the user timezone unless the field is a Date'() {
        given: 'the clock is read right before and right after each call, so a call that crosses midnight still matches'
        ZoneId zone = ZoneId.of('America/New_York')

        when:
        ZonedDateTime before = ZonedDateTime.now(zone)
        String html = tagLib.datePicker(name: 'd', value: null, precision: 'day').toString()
        ZonedDateTime after = ZonedDateTime.now(zone)

        then:
        [before, after].any { ZonedDateTime now ->
            selected(html, 'd_year') == now.year as String && selected(html, 'd_day') == now.dayOfMonth as String
        }

        when: 'a java.util.Date field keeps the Grails default'
        String yearBefore = new Date().format('yyyy')
        html = tagLib.datePicker(name: 'd', value: null, precision: 'day', fieldType: Date).toString()
        String yearAfter = new Date().format('yyyy')

        then:
        selected(html, 'd_year') in [yearBefore, yearAfter]
    }

    void 'datePicker passes a java.util.Date through'() {
        when:
        String html = tagLib.datePicker(name: 'd', value: new GregorianCalendar(2025, Calendar.JUNE, 2).time,
                precision: 'day').toString()

        then:
        selected(html, 'd_year') == '2025'
        selected(html, 'd_month') == '6'
        selected(html, 'd_day') == '2'
    }

    void 'jqueryDatePicker renders hidden and visible inputs with defaults'() {
        when:
        String html = applyTemplate('<g:jqueryDatePicker name="expires"/>')

        then:
        html.contains("<input id='expires' name='expires' type='hidden' />")
        html.contains("id='expires-datepicker' name='expires-datepicker' autocomplete ='off'")
        html.contains("data-testid='date-picker'")
        html.contains("class=' text large' size=\"10\"")
        html.contains('autoSize: true')
        html.contains('numberOfMonths: 1')
        html.contains('changeMonth: true')
        html.contains('minDate: null')
        html.contains("var dateValue = 'null';")
        !html.contains('readonly')
    }

    void 'jqueryDatePicker uses the given options and formats the initial value'() {
        when:
        String html = tagLib.jqueryDatePicker(id: 'exp', name: 'expires', size: '12', cssClass: 'wide',
                numberOfMonths: 2, readOnly: true, minDate: '01/01/2026', maxDate: '12/31/2026',
                dataBind: 'value: x', placeholder: 'pick', autocomplete: 'on', 'data-testid': 'exp-date',
                value: value).toString()

        then:
        html.contains("<input id='exp' name='expires' type='hidden' data-bind='value: x'/>")
        html.contains("data-testid='exp-date' placeholder='pick' class='wide text large' size=\"12\" readonly='readonly'")
        html.contains("autocomplete ='on'")
        html.contains('autoSize: false')
        html.contains('numberOfMonths: 2')
        html.contains('changeYear: true')
        html.contains("minDate: new Date('01/01/2026')")
        html.contains("maxDate: new Date('12/31/2026')")
        html.contains("var dateValue = '${expected}';")

        where:
        value                                                || expected
        LocalDate.of(2026, 2, 3)                             || '02/03/2026'
        new GregorianCalendar(2026, Calendar.FEBRUARY, 3).time || '02/03/2026'
        '02/03/2026'                                         || '02/03/2026'
        'null'                                               || 'null'
        '  '                                                 || 'null'
    }

    @PendingFeature(reason = 'changeMonthAndYear ?: true turns false into true, so the selectors still show')
    void 'jqueryDatePicker hides the month and year selectors when changeMonthAndYear is false'() {
        when:
        String html = tagLib.jqueryDatePicker(name: 'expires', changeMonthAndYear: false).toString()

        then:
        html.contains('changeMonth: false')
        html.contains('changeYear: false')
    }

    void 'jqueryDatePicker rejects unsupported values and a missing name'() {
        when:
        tagLib.jqueryDatePicker(name: 'd', value: 42)

        then:
        thrown(IllegalArgumentException)

        when:
        tagLib.jqueryDatePicker(id: 'd')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'name parameter must be specified'
    }
}
