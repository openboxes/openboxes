package org.pih.warehouse.monitoring

/**
 * The different formats that we support for Sentry's HTTP request transaction names.
 */
enum HttpTransactionNameFormat  {

    /**
     * Results in HTTP Request style transaction names, such as "GET /openboxes/api/invoices"
     */
    URI,

    /**
     * Results in Grails Controller based transaction names, such as "invoiceApi/list"
     */
    CONTROLLER_METHOD,
}
