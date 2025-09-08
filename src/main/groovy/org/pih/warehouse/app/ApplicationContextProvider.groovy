package org.pih.warehouse.app

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.beans.BeansException
import org.springframework.stereotype.Component

/**
 * Wraps the application context so that we can access it statically via non-Spring components.
 * Functions the same as Grails' Holders.grailsApplication.mainContext but for Spring.
 *
 * We should use this class sparingly. Whenever possible, inject beans directly into the class that uses them.
 * Doing so helps us ensure that we're not hiding our dependency chains, which allows us to maintain them more easily.
 */
@Component
class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext
    }

    /**
     * Returns the Spring managed bean instance of the given class type.
     */
    static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass)
    }
}