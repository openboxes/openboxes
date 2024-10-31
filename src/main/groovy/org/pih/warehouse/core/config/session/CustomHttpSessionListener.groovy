package org.pih.warehouse.core.config.session

import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

/**
 * Called whenever a session is created or destroyed, and so is useful for configuring the session with any
 * custom logic that we need. The @WebListener annotation ensures that this class is also invoked when deploying
 * to an external Servlet (not only when running with an embedded tomcat).
 */
@WebListener
class CustomHttpSessionListener implements HttpSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomHttpSessionListener)

    @Value('${server.session.timeout}')
    Integer sessionTimeoutInterval

    @Override
    void sessionCreated(HttpSessionEvent event) {
        logger.debug("Creating session with id ${event.session.id}")

        setSessionTimeout(event)
    }

    @Override
    void sessionDestroyed(HttpSessionEvent event) {
        String creationDatetime = new Date(event.session.creationTime).format("yyyy-MM-dd HH:mm:ss,SSS")
        logger.debug("Destroying session with id ${event.session.id}. Session was created at ${creationDatetime}")
    }

    /**
     * Apply a timeout to all sessions. Without defining it here, the timeout property would only apply
     * when running the server with an embedded servlet.
     */
    private void setSessionTimeout(HttpSessionEvent event) {
        event.session.setMaxInactiveInterval(sessionTimeoutInterval)
    }
}