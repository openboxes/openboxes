package org.pih.warehouse.monitoring

import io.sentry.protocol.TransactionNameSource
import io.sentry.spring.tracing.TransactionNameProvider
import javax.servlet.http.HttpServletRequest

/**
 * Defines how Sentry transaction names for HTTP requests should be structured.
 */
class GrailsHttpTransactionNameProvider implements TransactionNameProvider {

    String provideTransactionName(HttpServletRequest var1) {
        return "${var1.method} ${var1.requestURI}"

        // If we want to make this look more like Spring MVC traces, we could have done the following instead, but
        // we decided that "GET /openboxes/api/invoices" is easier to debug than "invoiceApi/list".
        // return "${var1.getAttribute("org.grails.CONTROLLER_NAME_ATTRIBUTE")}/${var1.getAttribute("org.grails.ACTION_NAME_ATTRIBUTE")}"
    }

    TransactionNameSource provideTransactionSource() {
        // Do what SpringMvcTransactionNameProvider does, which is the provider that Sentry normally uses.
        return TransactionNameSource.ROUTE
    }
}
