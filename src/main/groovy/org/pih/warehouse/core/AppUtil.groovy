package org.pih.warehouse.core

import grails.util.Holders

/**
 * Utility functions relating to the Spring / Grails application context.
 */
class AppUtil {

    /**
     * Statically fetch a bean from the application context when you don't have direct access to the context
     * (which is the case for all non-component classes).
     *
     * Note that needing to statically access beans in this way is typically a code smell and should be avoided.
     * Better would be to refactor the code in a way that allows you to autowire the bean into a component.
     */
    static <T> T getBean(Class<T> beanClass, String beanName=null) {
        // Custom beans like "MyComponent" are actually named "myComponent" in the app context. If fetching
        // a built in Grails bean, you may need to manually specify the fully quantified path as the bean name.
        // For example: "org.grails.plugins.web.taglib.ApplicationTagLib"
        String beanNameToUse = beanName ?: beanClass.simpleName.uncapitalize()

        return beanClass.cast(Holders.grailsApplication.mainContext.getBean(beanNameToUse))
    }
}
