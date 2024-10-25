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

        // Start the transaction before applying all other filters so that they're all captured by the trace.
        final ITransaction transaction = startTransaction(httpRequest)
        try {
            filterChain.doFilter(httpRequest, httpResponse)
        } catch (Throwable e) {
            // Capture any internal errors thrown during the request process.
            transaction.status = SpanStatus.INTERNAL_ERROR
            throw e
        } finally {
            transaction.status ?: SpanStatus.fromHttpStatusCode(httpResponse.status)
            transaction.finish()
        }
    }

    private ITransaction startTransaction(final HttpServletRequest httpRequest) {

        final String name = "${httpRequest.method} ${httpRequest.requestURI}"

        final CustomSamplingContext customSamplingContext = new CustomSamplingContext()
        customSamplingContext.set("request", httpRequest)

        // If the Sentry trace and baggage headers are set on the request, it means the frontend is also running
        // Sentry tracing. We want to connect frontend and backend traces together (ie distributed tracing) so instead
        // of starting a new trace, we continue the existing one by attaching the backend transaction to it.
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
                        SentryLevel.DEBUG, e, "Failed parsing Sentry trace header: %s", e.message)
            }
        }

        // Otherwise this is the entrypoint for the trace so no need to connect to anything.
        return hub.startTransaction(name, TRANSACTION_OP, customSamplingContext, true)
    }
}
