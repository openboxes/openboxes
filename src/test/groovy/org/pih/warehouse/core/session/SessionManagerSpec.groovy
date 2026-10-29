package org.pih.warehouse.core.session

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

class SessionManagerSpec extends Specification {

    SessionManager sessionManager = new SessionManager()

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    void 'reads return nothing when the current thread is not handling an HTTP request'() {
        given: 'no request bound to the thread, as in jobs and event listeners'
        RequestContextHolder.resetRequestAttributes()

        expect:
        sessionManager.session == null
        sessionManager.locale == null
        sessionManager.user == null
        sessionManager.currentLocation == null
        !sessionManager.inLocalizationMode
    }

    void 'writes fail when the current thread is not handling an HTTP request'() {
        given:
        RequestContextHolder.resetRequestAttributes()

        when:
        sessionManager.locale = Locale.FRENCH

        then:
        IllegalStateException e = thrown()
        e.message == 'Cannot set LOCALE outside of an HTTP request'
    }

    void 'reads and writes go to the session of the current request'() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest()
        RequestContextHolder.requestAttributes = new ServletRequestAttributes(request)

        when:
        sessionManager.locale = Locale.FRENCH

        then:
        sessionManager.session.is(request.session)
        sessionManager.locale == Locale.FRENCH
    }
}
