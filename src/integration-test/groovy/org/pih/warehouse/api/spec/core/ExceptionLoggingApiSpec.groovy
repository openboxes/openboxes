package org.pih.warehouse.api.spec.core

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.apache.http.HttpStatus
import org.grails.exceptions.reporting.DefaultStackTraceFilterer
import org.grails.web.errors.GrailsExceptionResolver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.generic.GenericApiWrapper
import org.pih.warehouse.api.client.generic.GenericResource
import org.pih.warehouse.api.spec.base.ApiSpec

import static io.restassured.RestAssured.given

/**
 * Checks what the server logs when a request fails with an exception. This matters because the Sentry Logback
 * appender turns every ERROR log into a Sentry event, so exceptions that we already handle with a 4xx response
 * must not be logged as errors (see QuietExceptionResolver). The test server runs in the same JVM as the tests,
 * which lets us capture its logs with a Logback appender.
 */
class ExceptionLoggingApiSpec extends ApiSpec {

    // The two loggers that GrailsExceptionResolver writes to when it handles an exception.
    static final List<String> EXCEPTION_LOGGERS = [DefaultStackTraceFilterer.STACK_LOG_NAME, GrailsExceptionResolver.name]

    @Autowired
    GenericApiWrapper genericApiWrapper

    ListAppender<ILoggingEvent> appender

    @Override
    void setupData() {
        appender = new ListAppender<>()
        appender.start()
        EXCEPTION_LOGGERS.each { getLogger(it).addAppender(appender) }
    }

    @Override
    void cleanupData() {
        EXCEPTION_LOGGERS.each { getLogger(it).detachAppender(appender) }
        appender.stop()
    }

    void 'not found response should not be logged as an error'() {
        when:
        genericApiWrapper.get404(GenericResource.PRODUCT, INVALID_ID)

        then:
        errorEvents().isEmpty()
        appender.list.find { it.level == Level.INFO && it.formattedMessage.contains('ObjectNotFoundException') }
    }

    void 'unhandled exception should still be logged as an error with its stack trace'() {
        when: 'requesting a resource type that the generic API does not know about'
        given(authenticatedApiContext.baseRequestSpec)
                .get("/generic/unknownResource/${INVALID_ID}")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)

        then:
        errorEvents()*.loggerName.toSet() == EXCEPTION_LOGGERS.toSet()
        errorEvents().every { it.throwableProxy != null }
    }

    private List<ILoggingEvent> errorEvents() {
        return appender.list.findAll { it.level == Level.ERROR }
    }

    private static Logger getLogger(String name) {
        return (Logger) LoggerFactory.getLogger(name)
    }
}
