package org.pih.warehouse.core

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory

/**
 * Points Grails' own groovyPageLayoutFinder bean at {@link UnifiedLayoutFinder}
 * by changing its class, leaving every property the GSP plugin configured on it
 * in place.
 *
 * The alternative — declaring the bean ourselves in resources.groovy — means
 * copying the plugin's wiring (view resolver, reload flag, cache flag, default
 * decorator name, non-GSP views). That copy is a snapshot: it drops any setting
 * the plugin derives rather than hard-codes, and silently omits anything a
 * later Grails version adds. Swapping only the class keeps this working across
 * upgrades unless the bean is renamed outright, which is loud rather than
 * silent.
 */
@Slf4j
@CompileStatic
class UnifiedLayoutFinderPostProcessor implements BeanFactoryPostProcessor {

    static final String BEAN_NAME = 'groovyPageLayoutFinder'

    @Override
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBeanDefinition(BEAN_NAME)) {
            // Renamed or removed upstream: leave the context alone and say so,
            // rather than failing startup over a layout substitution.
            log.warn("Bean '${BEAN_NAME}' not found; the unified layout will not be applied")
            return
        }
        BeanDefinition definition = beanFactory.getBeanDefinition(BEAN_NAME)
        definition.beanClassName = UnifiedLayoutFinder.name
        log.debug("Bean '${BEAN_NAME}' now backed by ${UnifiedLayoutFinder.name}")
    }
}
