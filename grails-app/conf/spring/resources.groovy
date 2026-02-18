package spring

import org.pih.warehouse.inboundSortation.strategy.CrossDockingStrategy
import org.pih.warehouse.inboundSortation.strategy.CrossDockingBackorderReferenceStrategy
import org.pih.warehouse.inboundSortation.strategy.DefaultSlottingStrategy
import org.pih.warehouse.inboundSortation.strategy.RandomSlottingStrategy
import org.pih.warehouse.inboundSortation.SlottingService
import org.pih.warehouse.product.ProductValidator
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered

import org.pih.warehouse.monitoring.SentryGrailsTracingFilter

// This is where we can register spring-specific beans using the Spring Bean DSL.
// Regular beans that conform to Grails conventions don't need to be registered here.
// https://docs.grails.org/latest/guide/spring.html
beans = {


    // Slotting strategies
    crossDockingBackorderReferenceStrategy(CrossDockingBackorderReferenceStrategy)
    crossDockingStrategy(CrossDockingStrategy) {
        demandService = ref('demandService')
    }
    defaultSlottingStrategy(DefaultSlottingStrategy)
    randomSlottingStrategy(RandomSlottingStrategy)
    slottingService(SlottingService) {
        strategies = [
                ref('crossDockingBackorderReferenceStrategy'), // fallback if none of the strategies worked, executed as the last one
                ref('crossDockingStrategy'), //cross docking
                ref('defaultSlottingStrategy'), // directed putaway to preferred bin
                ref('randomSlottingStrategy') // fallback if none of the strategies worked, executed as the last one
        ]
    }

    // Override Sentry's default tracing filters since Grails behaves slightly differently than SpringBoot.
    sentryTracingFilter(SentryGrailsTracingFilter)
    sentryTracingFilterRegistration(FilterRegistrationBean) {
        filter = sentryTracingFilter
        urlPatterns = ['/*']
        order = Ordered.HIGHEST_PRECEDENCE + 1
    }
    productValidator(ProductValidator)
}
