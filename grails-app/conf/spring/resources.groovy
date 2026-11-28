package spring

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered

import org.pih.warehouse.core.UnifiedLayoutFinderPostProcessor
import org.pih.warehouse.monitoring.SentryGrailsTracingFilter

// This is where we can register spring-specific beans using the Spring Bean DSL.
// Regular beans that conform to Grails conventions don't need to be registered here.
// https://docs.grails.org/latest/guide/spring.html
beans = {

    // Points Grails' own groovyPageLayoutFinder at UnifiedLayoutFinder by
    // changing that bean's class, so every property the GSP plugin configures
    // on it survives — including anything a later Grails version adds. See
    // UnifiedLayoutFinderPostProcessor for why this is preferred over
    // declaring the bean here and copying the plugin's wiring.
    unifiedLayoutFinderPostProcessor(UnifiedLayoutFinderPostProcessor)

    // Override Sentry's default tracing filters since Grails behaves slightly differently than SpringBoot.
    sentryTracingFilter(SentryGrailsTracingFilter)
    sentryTracingFilterRegistration(FilterRegistrationBean) {
        filter = sentryTracingFilter
        urlPatterns = ['/*']
        order = Ordered.HIGHEST_PRECEDENCE + 1
    }
}
