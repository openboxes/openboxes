package spring

import org.pih.warehouse.inboundSortation.strategy.CrossDockingBackorderReferencePutawayStrategy
import org.pih.warehouse.inboundSortation.strategy.DefaultLocationPutawayStrategy
import org.pih.warehouse.inboundSortation.PutawayStrategyService
import org.pih.warehouse.monitoring.SentryGrailsTracingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered
import org.springframework.transaction.event.TransactionalEventListenerFactory

// This is where we can register spring-specific beans using the Spring Bean DSL.
// Regular beans that conform to Grails conventions don't need to be registered here.
// https://docs.grails.org/latest/guide/spring.html
beans = {
    // The listeners are ordinary services, so Grails auto-registers them - no
    // wiring needed there. This is the one bean the pattern does need: Grails
    // uses GORM's @Transactional, not Spring's @EnableTransactionManagement, so
    // it never registers a TransactionalEventListenerFactory, and without one
    // every @TransactionalEventListener silently no-ops. Declaring it here is
    // what makes the AFTER_COMMIT phases fire.
    // See: https://grails.apache.org/guides/grails-transactional-events/8/guide/index.html (I know it is Grails Version: 8)
    transactionalEventListenerFactory(TransactionalEventListenerFactory)

    // Putaway strategies
    crossDockingBackorderReferencePutawayStrategy(CrossDockingBackorderReferencePutawayStrategy)
    defaultLocationPutawayStrategy(DefaultLocationPutawayStrategy)
    // randomLocationPutawayStrategy(RandomLocationPutawayStrategy)
    putawayStrategyService(PutawayStrategyService) {
        strategies = [
                ref('crossDockingBackorderReferencePutawayStrategy'), // fallback if none of the strategies worked, executed as the last one
                ref('defaultLocationPutawayStrategy'), // directed putaway to preferred bin
                // ref('randomLocationPutawayStrategy') // fallback if none of the strategies worked, executed as the last one
        ]
    }

    // Override Sentry's default tracing filters since Grails behaves slightly differently than SpringBoot.
    sentryTracingFilter(SentryGrailsTracingFilter)
    sentryTracingFilterRegistration(FilterRegistrationBean) {
        filter = sentryTracingFilter
        urlPatterns = ['/*']
        order = Ordered.HIGHEST_PRECEDENCE + 1
    }
}
