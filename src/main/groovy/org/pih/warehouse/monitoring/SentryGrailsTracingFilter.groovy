package org.pih.warehouse.monitoring

import io.sentry.BaggageHeader
import io.sentry.CustomSamplingContext
import io.sentry.HubAdapter
import io.sentry.IHub
import io.sentry.ITransaction
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SentryTraceHeader
import io.sentry.SpanStatus
import io.sentry.TransactionContext
import io.sentry.exception.InvalidSentryTraceHeaderException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Create HTTP request transactions for Sentry performance tracing.
 *
 * Grails and Spring MVC have different approaches for mapping URIs to controllers. Because there's no official Grails
 * support, we need to use the SpringBoot Sentry integration and then manually create transactions ourselves.
 *
 * Note that this bean is registered in grails-app/conf/spring/resource.groovy
 *
 * https://forum.sentry.io/t/sentry-java-spring-boot-and-grails-performance-tracing-not-working/15765/3
 */
class SentryGrailsTracingFilter extends OncePerRequestFilter {
    private static final String TRANSACTION_OP = "http.server"

    private final IHub hub

    SentryGrailsTracingFilter() {
        this(HubAdapter.getInstance())
    }

    SentryGrailsTracingFilter(final IHub hub) {
        this.hub = hub
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse,
            final FilterChain filterChain)
            throws ServletException, IOException {

        if (!hub.isEnabled()) {
            filterChain.doFilter(httpRequest, httpResponse)
            return
        }

        // The request doesn't have the attributes that we need yet so all we can do at this point is start the
        // transaction, which we do to ensure that all other filters are captured by the trace.
        final ITransaction transaction = startTransaction(httpRequest)
        try {
            // Run all other filters first.
            filterChain.doFilter(httpRequest, httpResponse)
        } catch (Throwable e) {
            // Properly set the status for any other exceptions thrown during the filter process.
            transaction.setStatus(SpanStatus.INTERNAL_ERROR)
            throw e
        } finally {
            // Now that all other filters have ran, we know we have the request attributes that we need.
            transaction.setName(httpRequest.getRequestURI())
            transaction.setOperation(TRANSACTION_OP)
            if (transaction.getStatus() == null) {
                transaction.setStatus(SpanStatus.fromHttpStatusCode(httpResponse.getStatus()))
            }
            transaction.finish()
        }
    }

    private ITransaction startTransaction(final HttpServletRequest httpRequest) {

        final String name = "${httpRequest.getMethod()} ${httpRequest.getRequestURI()}"

        final CustomSamplingContext customSamplingContext = new CustomSamplingContext()
        customSamplingContext.set("request", httpRequest)

        // This header will be provided if the frontend/client is also running sentry tracing. Using this header
        // enables distributed tracing (ie connecting frontend and backend traces together).
        // https://docs.sentry.io/platforms/java/guides/spring-boot/tracing/trace-propagation/
        final String sentryTraceHeader = httpRequest.getHeader(SentryTraceHeader.SENTRY_TRACE_HEADER)
        if (sentryTraceHeader != null) {
            try {
                final List<String> baggageHeader = httpRequest.getHeaders(BaggageHeader.BAGGAGE_HEADER).toList()
                final TransactionContext transactionContext = Sentry.continueTrace(sentryTraceHeader, baggageHeader)
                if (transactionContext != null) {
                    transactionContext.name = name
                    transactionContext.operation = TRANSACTION_OP
                    return hub.startTransaction(transactionContext, customSamplingContext, true)
                }
            } catch (InvalidSentryTraceHeaderException e) {
                hub.getOptions().getLogger().log(
                        SentryLevel.DEBUG, e, "Failed parsing Sentry trace header: %s", e.getMessage())
            }
        }

        // Otherwise this is the entrypoint for the trace.
        return hub.startTransaction(name, TRANSACTION_OP, customSamplingContext, true)
    }
}
