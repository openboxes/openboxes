package org.pih.warehouse.monitoring

import io.sentry.HubAdapter
import io.sentry.IHub
import io.sentry.spring.tracing.SentryTracingFilter

/**
 * Creates HTTP request transactions for Sentry performance tracing.
 *
 * Note that this bean is registered in grails-app/conf/spring/resources.groovy
 *
 * Because Grails and Spring MVC have different approaches for mapping URIs to controllers, we need to generate
 * the transaction name ourselves for traces to display properly in Sentry.
 *
 * https://forum.sentry.io/t/sentry-java-spring-boot-and-grails-performance-tracing-not-working/15765/3
 */
class SentryGrailsTracingFilter extends SentryTracingFilter {

    SentryGrailsTracingFilter() {
        this(HubAdapter.getInstance())
    }

    SentryGrailsTracingFilter(final IHub hub) {
        super(hub, new GrailsHttpTransactionNameProvider())
    }
}
