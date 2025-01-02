package org.pih.warehouse.monitoring

import io.sentry.Sentry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

/**
 * Initialize Servlet-specific Sentry properties.
 *
 * ServletContextListener classes are triggered during ServletContext lifecycle changes, such as when a new deploy
 * happens.
 *
 * The "WebListener" annotation saves us from needing to configure the listener in the WEB-INF or META-INF folders,
 * or in a web.xml file. It also allows this flow to be invoked when running via an embedded server, which
 * is very helpful for local testing.
 */
@WebListener
class SentryServletContextListener implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(SentryServletContextListener)

    @Override
    void contextInitialized(ServletContextEvent event) {
        log.info('Initializing sentry-servlet')
        try {
            // Anything defined here will be overwritten by values in sentry.properties.
            // Note that while we're using the "sentry-servlet" dependency, this init block is required.
            // https://www.baeldung.com/ops/java-sentry Once we switch to using the "sentry-spring-boot-starter"
            // dependency, we might be able to remove this file and the "sentry-servlet" dependency entirely.
            Sentry.init { options ->

                // If "SENTRY_DSN" is defined, or a dsn is provided in sentry.properties, those will take precedence
                // over "SENTRY_DSN_BACKEND". If none of these are defined, Sentry will be disabled.
                options.dsn = System.getenv("SENTRY_DSN_BACKEND") ?: ""

                // We set the git commit of the release as the Sentry release tag to be able to group
                // Sentry errors by release. Once we switch to use the "io.sentry:sentry-spring-boot-starter"
                // dependency, this line can be replaced with "use-git-commit-id-as-release=true" in application.yml.
                options.release = fetchGitCommit()

                // Merges any config defined here with any in environment variables (such as SENTRY_DSN) as well as
                // any in the sentry.properties and application.yml files.
                options.enableExternalConfiguration = true
            }
        } catch (Exception e) {
            log.warn('Unable to initialize sentry-servlet', e)
        }
    }

    @Override
    void contextDestroyed(ServletContextEvent event) {
        Sentry.close()
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
