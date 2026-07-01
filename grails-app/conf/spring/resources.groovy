package spring

import grails.config.Config
import grails.util.Holders
import org.pih.warehouse.inboundSortation.strategy.CrossDockingBackorderReferenceStrategy
import org.pih.warehouse.inboundSortation.strategy.DefaultSlottingStrategy
import org.pih.warehouse.inboundSortation.strategy.RandomSlottingStrategy
import org.pih.warehouse.inboundSortation.SlottingService
import org.pih.warehouse.monitoring.SentryGrailsTracingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.event.TransactionalEventListenerFactory

// This is where we can register spring-specific beans using the Spring Bean DSL.
// Regular beans that conform to Grails conventions don't need to be registered here.
// https://docs.grails.org/latest/guide/spring.html
beans = {
    // Default executor for @Async methods. Named "taskExecutor" so Spring's @EnableAsync
    // picks it up automatically without needing a qualifier on each @Async annotation.
    // Pool sizes are configurable via openboxes.async.executor.* in application.yml.
    // For  more info see: https://grails.apache.org/guides/grails-transactional-events/8/guide/index.html#asyncDispatch
    taskExecutor(ThreadPoolTaskExecutor) {
        Config config = Holders.config
        corePoolSize = config.getProperty('openboxes.async.executor.corePoolSize', Integer, 2)
        maxPoolSize = config.getProperty('openboxes.async.executor.maxPoolSize', Integer, 10)
        queueCapacity = config.getProperty('openboxes.async.executor.queueCapacity', Integer, 25)
        awaitTerminationSeconds = config.getProperty('openboxes.async.executor.awaitTerminationSeconds', Integer, 30)
        threadNamePrefix = "async-event-"
        waitForTasksToCompleteOnShutdown = true
    }
    // The listeners are ordinary services, so Grails auto-registers them - no
    // wiring needed there. This is the one bean the pattern does need: Grails
    // uses GORM's @Transactional, not Spring's @EnableTransactionManagement, so
    // it never registers a TransactionalEventListenerFactory, and without one
    // every @TransactionalEventListener silently no-ops. Declaring it here is
    // what makes the AFTER_COMMIT phases fire.
    // See: https://grails.apache.org/guides/grails-transactional-events/8/guide/index.html (I know it is Grails Version: 8)
    transactionalEventListenerFactory(TransactionalEventListenerFactory)

    // Slotting strategies
    crossDockingBackorderReferenceStrategy(CrossDockingBackorderReferenceStrategy)
    defaultSlottingStrategy(DefaultSlottingStrategy)
    randomSlottingStrategy(RandomSlottingStrategy)
    slottingService(SlottingService) {
        strategies = [
                ref('crossDockingBackorderReferenceStrategy'), // fallback if none of the strategies worked, executed as the last one
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
}
