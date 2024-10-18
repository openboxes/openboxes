package org.pih.warehouse.monitoring

import io.sentry.Sentry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext
import javax.servlet.ServletException

/**
 * Configures Sentry for Servlets. This gives us info from Tomcat such user-agent and url.
 *
 * This flow is only executed when deploying Grails via WAR file to an external Servlet (ie not when running with
 * "grails run-app"), and so should only contain Servlet specific configuration. Regular Sentry configuration is
 * defined in the sentry.properties file.
 *
 * https://docs.sentry.io/platforms/java/guides/servlet/#configure
 */
final class SentryServletContainerInitializer implements ServletContainerInitializer {

    private final static Logger log = LoggerFactory.getLogger(SentryServletContainerInitializer)

    @Override
    void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        log.info('Initializing sentry-servlet')
        try {
            Sentry.init { options ->
                // We set the git commit of the release as the Sentry release tag to be able to group
                // Sentry errors by release. This technically doesn't need to be configured here, but
                // it's the easiest place to do it with the current configuration options.
                options.release = fetchGitCommit()
            }
        } catch (Exception e) {
            log.warn('Unable to initialize sentry-servlet', e)
        }
    }

    private String fetchGitCommit() {
        Properties properties = new Properties()
        try {
            InputStream is = this.class.classLoader.getResourceAsStream('git.properties')
            properties.load(is)
        } catch (Exception e) {
            log.warn('Unable to load git.properties file for sentry-servlet', e)
        }
        return properties.getProperty('git.commit.id', 'unknown')
    }
}
