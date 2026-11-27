package org.pih.warehouse.monitoring

import javax.servlet.http.HttpServletRequest

import grails.validation.ValidationException
import org.apache.http.auth.AuthenticationException
import org.grails.web.errors.GrailsExceptionResolver
import org.hibernate.ObjectNotFoundException

/**
 * Keeps exceptions that we already turn into a 4xx response out of the error logs, and therefore out of Sentry.
 *
 * Note that this bean is registered in grails-app/conf/spring/resources.groovy, replacing the default
 * "exceptionHandler" bean that Grails provides.
 *
 * UrlMappings maps a handful of exceptions to ErrorsController actions that respond with a 4xx status (not found,
 * validation errors, unauthorized). Before Grails forwards to those actions, GrailsExceptionResolver logs the
 * exception at ERROR twice: once with the full stack trace on the "StackTrace" logger (from inside
 * DefaultStackTraceFilterer.filter) and once on its own logger (from logStackTrace). The Sentry Logback appender
 * turns every ERROR into a Sentry event, so each handled 404 or 400 showed up in Sentry as a crash.
 *
 * For the quiet exceptions we skip both ERROR logs and write a single INFO line instead, which Sentry only records
 * as a breadcrumb. Every other exception keeps the default behaviour (stack trace at ERROR, reported to Sentry).
 * The response that the client receives is unchanged either way.
 *
 * The quiet exceptions must stay in sync with the "500" mappings in UrlMappings. UrlMappingsSpec checks this.
 */
class QuietExceptionResolver extends GrailsExceptionResolver {

    static final List<Class<? extends Throwable>> QUIET_EXCEPTIONS = [
            ObjectNotFoundException,
            ValidationException,
            AuthenticationException,
    ].asImmutable()

    /**
     * Whether the exception, or its root cause, is one that UrlMappings turns into a 4xx response. We check the
     * root cause as well because Grails often wraps the original exception before the resolver sees it, and
     * GrailsExceptionResolver.matchStatusCode checks both too.
     */
    static boolean isQuiet(Throwable e) {
        if (e == null) {
            return false
        }
        Throwable rootCause = getRootCause(e)
        return QUIET_EXCEPTIONS.any { Class<? extends Throwable> quietType ->
            quietType.isInstance(e) || quietType.isInstance(rootCause)
        }
    }

    /**
     * DefaultStackTraceFilterer logs the full stack trace at ERROR on the "StackTrace" logger before trimming it, so
     * for quiet exceptions we skip the filtering entirely. The only visible effect is that the stack trace we don't
     * log anyway is left untrimmed.
     */
    @Override
    protected void filterStackTrace(Exception e) {
        if (isQuiet(e)) {
            return
        }
        super.filterStackTrace(e)
    }

    @Override
    protected void logStackTrace(Exception e, HttpServletRequest request) {
        if (isQuiet(e)) {
            Throwable rootCause = getRootCause(e)
            LOG.info("${rootCause.class.simpleName} occurred when processing request: " +
                    "[${request.method}] ${request.requestURI}: ${rootCause.message}")
            return
        }
        super.logStackTrace(e, request)
    }
}
