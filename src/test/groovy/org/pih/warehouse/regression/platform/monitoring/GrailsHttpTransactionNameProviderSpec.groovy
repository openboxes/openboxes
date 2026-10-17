package org.pih.warehouse.regression.platform.monitoring

import org.pih.warehouse.monitoring.*
import io.sentry.protocol.TransactionNameSource
import org.grails.testing.GrailsUnitTest
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

class GrailsHttpTransactionNameProviderSpec extends Specification implements GrailsUnitTest {

    MockHttpServletRequest request = new MockHttpServletRequest('GET', '/openboxes/api/invoices')

    void setup() {
        request.setAttribute('org.grails.CONTROLLER_NAME_ATTRIBUTE', 'invoiceApi')
        request.setAttribute('org.grails.ACTION_NAME_ATTRIBUTE', 'list')
    }

    void 'transactions are named after the #format'() {
        given:
        config.sentry.httpTransactionNameFormat = format

        expect:
        new GrailsHttpTransactionNameProvider().provideTransactionName(request) == name
        new GrailsHttpTransactionNameProvider().provideTransactionSource() == source

        where:
        format              || name                           | source
        'URI'               || 'GET /openboxes/api/invoices'  | TransactionNameSource.ROUTE
        'CONTROLLER_METHOD' || 'invoiceApi/list'              | TransactionNameSource.COMPONENT
    }

    void 'an unknown format is rejected'() {
        given:
        config.sentry.httpTransactionNameFormat = 'PATH'

        when:
        new GrailsHttpTransactionNameProvider().provideTransactionName(request)

        then:
        thrown(IllegalArgumentException)
    }
}
