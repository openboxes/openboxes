package org.pih.warehouse

import io.sentry.Sentry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class SentryContextListener implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(SentryContextListener)

    @Override
    void contextInitialized(ServletContextEvent event) {
        log.info('Initializing sentry-servlet')
        log.error('*'*300)
        log.error('hi')
        log.error('*'*300)
        try {
            Sentry.init { options ->
                // We set the git commit of the release as the Sentry release tag to be able to group
                // Sentry errors by release. Once we switch to use the "io.sentry:sentry-spring-boot-starter"
                // dependency, this line can be removed.
                options.release = fetchGitCommit()
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
