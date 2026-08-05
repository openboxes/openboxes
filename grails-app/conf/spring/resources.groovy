package spring

import grails.util.Environment
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered

import org.pih.warehouse.core.UnifiedLayoutFinder
import org.pih.warehouse.monitoring.SentryGrailsTracingFilter

// This is where we can register spring-specific beans using the Spring Bean DSL.
// Regular beans that conform to Grails conventions don't need to be registered here.
// https://docs.grails.org/latest/guide/spring.html
beans = {

    // Serves layouts/default.gsp in place of layouts/custom.gsp when
    // openboxes.unifiedLayout.enabled is true. Replaces Grails' own
    // groovyPageLayoutFinder, so it has to repeat that bean's wiring —
    // see GroovyPagesGrailsPlugin. The substitution happens after SiteMesh has
    // resolved what the page asked for, which is the only point at which
    // "did this page ask for custom?" can be answered; UnifiedLayoutFinder
    // explains why an interceptor cannot do it.
    groovyPageLayoutFinder(UnifiedLayoutFinder) {
        gspReloadEnabled = Environment.current == Environment.DEVELOPMENT
        defaultDecoratorName = application.config.getProperty('grails.sitemesh.default.layout', String, 'application')
        enableNonGspViews = application.config.getProperty('grails.web.sitemesh.enableNonGspViews', Boolean, false)
        cacheEnabled = Environment.current != Environment.DEVELOPMENT
        viewResolver = ref('jspViewResolver')
    }

    // Override Sentry's default tracing filters since Grails behaves slightly differently than SpringBoot.
    sentryTracingFilter(SentryGrailsTracingFilter)
    sentryTracingFilterRegistration(FilterRegistrationBean) {
        filter = sentryTracingFilter
        urlPatterns = ['/*']
        order = Ordered.HIGHEST_PRECEDENCE + 1
    }
}
