package org.pih.warehouse.regression.platform.databinding

import org.pih.warehouse.databinding.*
import grails.databinding.SimpleMapDataBindingSource
import java.time.ZoneId
import java.time.ZonedDateTime
import spock.lang.Specification

import org.pih.warehouse.core.date.ZonedDateTimeParser
import org.pih.warehouse.core.session.SessionManager

class ZonedDateTimeBindingEditorSpec extends Specification {

    ZonedDateTimeBindingEditor editor

    void setup() {
        SessionManager sessionManager = Stub(SessionManager) {
            getTimezone() >> TimeZone.getTimeZone('Africa/Kigali')
        }
        editor = new ZonedDateTimeBindingEditor(
                sessionManager: sessionManager, zonedDateTimeParser: new ZonedDateTimeParser(sessionManager: sessionManager))
    }

    void 'the editor targets ZonedDateTime'() {
        expect:
        assert editor.targetType == ZonedDateTime
    }

    void 'getDate should interpret the calendar fields in the user timezone'() {
        given: 'a calendar built in a different timezone than the user'
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone('America/Los_Angeles'))
        calendar.clear()
        calendar.set(2024, Calendar.MARCH, 1, 8, 0)

        expect:
        assert editor.getDate(calendar) == ZonedDateTime.of(2024, 3, 1, 8, 0, 0, 0, ZoneId.of('Africa/Kigali'))
    }

    void 'assemble should bind date picker params as a datetime in the user timezone'() {
        given:
        SimpleMapDataBindingSource source = new SimpleMapDataBindingSource([
                expiry       : 'date.struct',
                expiry_year  : '2025',
                expiry_month : '12',
                expiry_day   : '31',
                expiry_hour  : '23',
                expiry_minute: '59',
        ])

        when:
        ZonedDateTime bound = editor.assemble('expiry', source)

        then:
        assert bound == ZonedDateTime.of(2025, 12, 31, 23, 59, 0, 0, ZoneId.of('Africa/Kigali'))
        assert bound.toInstant().toString() == '2025-12-31T21:59:00Z'
    }
}
