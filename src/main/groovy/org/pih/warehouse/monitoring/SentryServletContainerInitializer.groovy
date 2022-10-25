package org.pih.warehouse.monitoring

import io.sentry.Sentry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext
import javax.servlet.ServletException

/**
 * Configure the sentry-servlet library to send URL/user-agent data to Sentry.
 *
 * https://docs.sentry.io/platforms/java/guides/servlet/#configure
 */
final class SentryServletContainerInitializer implements ServletContainerInitializer {

    private final static Logger log = LoggerFactory.getLogger(SentryServletContainerInitializer)

    @Override
    void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        log.info 'Initializing sentry-servlet'
        Properties properties = new Properties()
        try {
            InputStream is = this.class.classLoader.getResourceAsStream('git.properties')
            properties.load(is)
        } catch (Exception e) {
            log.warn 'Unable to load git.properties file', e
        }
        Sentry.init {
            it.with {
                addInAppInclude('org.pih.warehouse')
                attachServerName = true
                enableAutoSessionTracking = true
                // without this setting, SENTRY_DSN env. var is ignored
                enableExternalConfiguration = true
                release = properties.getProperty('git.commit.id', 'unknown')
                sendDefaultPii = false
            }
        }
    }
}
