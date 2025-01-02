package org.pih.warehouse.monitoring

import grails.util.Holders
import io.sentry.protocol.TransactionNameSource
import io.sentry.spring.tracing.TransactionNameProvider
import javax.servlet.http.HttpServletRequest

/**
 * Defines how Sentry transaction names for HTTP requests should be structured.
 *
 * If we ever add another HttpTransactionNameFormat, we should break this class up into one provider per format
 * and move the switch statement into the SentryGrailsTracingFilter instead.
 */
class GrailsHttpTransactionNameProvider implements TransactionNameProvider {

    String provideTransactionName(HttpServletRequest request) {
        HttpTransactionNameFormat nameFormat = getTransactionNameFormat()
        switch (nameFormat) {
            // If we want HTTP Request style transaction names, such as "GET /openboxes/api/invoices"
            case HttpTransactionNameFormat.URI:
                return "${request.method} ${request.requestURI}"

            // Will result in Grails Controller based transaction names, such as "invoiceApi/list"
            case HttpTransactionNameFormat.CONTROLLER_METHOD:
                return "${request.getAttribute("org.grails.CONTROLLER_NAME_ATTRIBUTE")}/${request.getAttribute("org.grails.ACTION_NAME_ATTRIBUTE")}"

            default:
                throw new IllegalArgumentException("Un-supported transaction name format: ${nameFormat}")
        }
    }

    TransactionNameSource provideTransactionSource() {
        HttpTransactionNameFormat nameFormat = getTransactionNameFormat()
        switch (nameFormat) {
            case HttpTransactionNameFormat.URI:
                return TransactionNameSource.ROUTE

            case HttpTransactionNameFormat.CONTROLLER_METHOD:
                return TransactionNameSource.COMPONENT

            default:
                throw new IllegalArgumentException("Un-supported transaction name format: ${nameFormat}")
        }
    }

    private static HttpTransactionNameFormat getTransactionNameFormat() {
        return HttpTransactionNameFormat.valueOf(Holders.config.getProperty('sentry.httpTransactionNameFormat'))
    }
}
