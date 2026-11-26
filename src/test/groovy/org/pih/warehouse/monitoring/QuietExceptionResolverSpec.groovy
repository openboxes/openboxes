package org.pih.warehouse.monitoring

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import grails.validation.ValidationException
import org.apache.http.auth.AuthenticationException
import org.grails.exceptions.reporting.DefaultStackTraceFilterer
import org.grails.web.errors.GrailsExceptionResolver
import org.hibernate.ObjectNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.validation.BeanPropertyBindingResult
import spock.lang.Specification
import spock.lang.Unroll

class QuietExceptionResolverSpec extends Specification {

    // The two loggers that GrailsExceptionResolver.resolveException writes to when it handles an exception.
    static final List<String> EXCEPTION_LOGGERS = [DefaultStackTraceFilterer.STACK_LOG_NAME, GrailsExceptionResolver.name]

    QuietExceptionResolver resolver
    MockHttpServletRequest request
    ListAppender<ILoggingEvent> appender

    void setup() {
        resolver = new QuietExceptionResolver()
        resolver.stackFilterer = new DefaultStackTraceFilterer()
        request = new MockHttpServletRequest('GET', '/openboxes/api/products/123')

        appender = new ListAppender<>()
        appender.start()
        EXCEPTION_LOGGERS.each { getLogger(it).addAppender(appender) }
    }

    void cleanup() {
        EXCEPTION_LOGGERS.each { getLogger(it).detachAppender(appender) }
        appender.stop()
    }

    @Unroll
    void 'quiet exception #exception.class.simpleName is logged once at INFO without a stack trace'() {
        when:
        resolve(exception)

        then:
        errorEvents().isEmpty()
        infoEvents().size() == 1
        infoEvents()[0].loggerName == GrailsExceptionResolver.name
        infoEvents()[0].throwableProxy == null
        infoEvents()[0].formattedMessage.contains(exception.class.simpleName)
        infoEvents()[0].formattedMessage.contains(exception.message)
        infoEvents()[0].formattedMessage.contains('[GET] /openboxes/api/products/123')

        where:
        exception << [
                new ObjectNotFoundException('123', 'Product'),
                new ValidationException('Product is invalid', new BeanPropertyBindingResult(new Object(), 'product')),
                new AuthenticationException('Bad credentials'),
        ]
    }

    void 'exception wrapping a quiet exception is logged once at INFO without a stack trace'() {
        given:
        Exception exception = new RuntimeException('wrapper', new ObjectNotFoundException('123', 'Product'))

        when:
        resolve(exception)

        then:
        errorEvents().isEmpty()
        infoEvents().size() == 1
        infoEvents()[0].throwableProxy == null
        infoEvents()[0].formattedMessage.contains('ObjectNotFoundException')
    }

    void 'unmapped exception is still logged at ERROR with its stack trace on both loggers'() {
        given:
        Exception exception = new IllegalStateException('Something unexpected happened')

        when:
        resolve(exception)

        then:
        infoEvents().isEmpty()
        errorEvents()*.loggerName.toSet() == EXCEPTION_LOGGERS.toSet()
        errorEvents().every { it.throwableProxy?.className == IllegalStateException.name }
    }

    @Unroll
    void 'isQuiet is #expected for #description'() {
        expect:
        QuietExceptionResolver.isQuiet(exception) == expected

        where:
        description                            | exception                                                                  | expected
        'null'                                 | null                                                                       | false
        'an unmapped exception'                | new IllegalStateException('boom')                                          | false
        'an unmapped exception with a cause'   | new IllegalStateException('boom', new IllegalArgumentException('why'))     | false
        'a quiet exception'                    | new AuthenticationException('Bad credentials')                             | true
        'a wrapped quiet exception'            | new RuntimeException('wrapper', new ObjectNotFoundException('1', 'User'))  | true
        'a doubly wrapped quiet exception'     | new RuntimeException(new RuntimeException(new AuthenticationException())) | true
    }

    /**
     * Runs the two steps of GrailsExceptionResolver.resolveException that log the exception.
     */
    private void resolve(Exception exception) {
        resolver.filterStackTrace(exception)
        resolver.logStackTrace(exception, request)
    }

    private List<ILoggingEvent> errorEvents() {
        return appender.list.findAll { it.level == Level.ERROR }
    }

    private List<ILoggingEvent> infoEvents() {
        return appender.list.findAll { it.level == Level.INFO }
    }

    private static Logger getLogger(String name) {
        return (Logger) LoggerFactory.getLogger(name)
    }
}
