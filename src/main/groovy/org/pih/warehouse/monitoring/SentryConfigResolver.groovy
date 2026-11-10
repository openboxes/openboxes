package org.pih.warehouse.monitoring

import grails.util.Holders
import groovy.json.JsonOutput
import io.sentry.Sentry
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.core.parser.DoubleParser

/**
 * Resolves the Sentry settings that the server passes to the frontend.
 *
 * Methods are static so that they can be called directly from GSPs. See webpack.config.js and _sentry.gsp for usages.
 */
class SentryConfigResolver {

    /**
     * Fetch the frontend (React/GSP web) Sentry config as JSON.
     *
     * We don't pass these settings via ApiController.getAppContext() because we need it as early as possible to
     * capture data from the true start of the session.
     *
     * @param user the currently logged in user (if any) to tag Sentry events with
     * @param location the current location (if any) to tag Sentry events with
     */
    static String getWebConfigJson(User user=null, Location location=null) {
        DoubleParser doubleParser = new DoubleParser()
        Map config = [
                dsn: Holders.config.getProperty('sentry.web.dsn') ?: null,
                environment: Holders.config.getProperty('sentry.web.environment') ?: null,
                tracesSampleRate: doubleParser.parse(Holders.config.getProperty('sentry.web.tracesSampleRate')),
                replaysSampleRate: doubleParser.parse(Holders.config.getProperty('sentry.web.replaysSampleRate')),
                replaysErrorSampleRate: doubleParser.parse(Holders.config.getProperty('sentry.web.replaysErrorSampleRate')),
                user: user ? [
                        id: user.id,
                        username: user.username,
                        email: user.email,
                        locale: user.locale,
                        timezone: user.timezone,

                ] : null,
                location: location ? [
                        id: location.id,
                        name: location.name
                ] : null,
                sessionId: getSessionId(),
        ]
        // Escape "</" so that config values can't close the <script> tag that the JSON is wrapped with.
        return JsonOutput.toJson(config).replace('</', '<\\/')
    }

    /**
     * The id of the current HTTP session (if any).
     */
    private static String getSessionId() {
        return (RequestContextHolder.requestAttributes as ServletRequestAttributes)?.request?.getSession(false)?.id
    }

    /**
     * The "sentry-trace" value of the current backend trace (if any). Rendered as a meta tag on GSPs so that the
     * frontend can link with the backend's trace. See _sentry.gsp for usages.
     */
    static String getTraceparent() {
        return Sentry.traceparent?.value
    }

    /**
     * The "baggage" value of the current backend trace (if any). Rendered as a meta tag on GSPs so that the
     * frontend can link with the backend's trace. See _sentry.gsp for usages.
     */
    static String getBaggage() {
        return Sentry.baggage?.value
    }
}
