package spring

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered

import org.pih.warehouse.monitoring.QuietExceptionResolver
import org.pih.warehouse.monitoring.SentryGrailsTracingFilter

// This is where we can register spring-specific beans using the Spring Bean DSL.
// Regular beans that conform to Grails conventions don't need to be registered here.
// https://docs.grails.org/latest/guide/spring.html
beans = {

    // Replace the default Grails exception resolver (see ControllersGrailsPlugin) so that exceptions we already
    // handle with a 4xx response aren't logged as errors and reported to Sentry. The mapping config is the same.
    exceptionHandler(QuietExceptionResolver) {
        exceptionMappings = ['java.lang.Exception': '/error']
    }

    // Override Sentry's default tracing filters since Grails behaves slightly differently than SpringBoot.
    sentryTracingFilter(SentryGrailsTracingFilter)
    sentryTracingFilterRegistration(FilterRegistrationBean) {
        filter = sentryTracingFilter
        urlPatterns = ['/*']
        order = Ordered.HIGHEST_PRECEDENCE + 1
    }
}
