package org.pih.warehouse.core

import org.grails.web.sitemesh.GroovyPageLayoutFinder
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import spock.lang.Specification

/**
 * The post-processor repoints Grails' own groovyPageLayoutFinder at our
 * subclass by changing the bean's class, rather than redeclaring the bean and
 * copying the GSP plugin's wiring. The point of that choice is that the
 * plugin's properties survive — including any a future Grails version adds —
 * so that is what these assert.
 */
class UnifiedLayoutFinderPostProcessorSpec extends Specification {

    private DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory()

    void "the finder bean is repointed at UnifiedLayoutFinder"() {
        given:
        beanFactory.registerBeanDefinition(
                UnifiedLayoutFinderPostProcessor.BEAN_NAME,
                new RootBeanDefinition(GroovyPageLayoutFinder))

        when:
        new UnifiedLayoutFinderPostProcessor().postProcessBeanFactory(beanFactory)

        then:
        beanFactory.getBeanDefinition(UnifiedLayoutFinderPostProcessor.BEAN_NAME)
                .beanClassName == UnifiedLayoutFinder.name
    }

    void "properties configured by the GSP plugin are preserved"() {
        given: 'a bean definition carrying the kind of wiring the plugin supplies'
        RootBeanDefinition definition = new RootBeanDefinition(GroovyPageLayoutFinder)
        definition.propertyValues.add('gspReloadEnabled', true)
        definition.propertyValues.add('cacheEnabled', false)
        definition.propertyValues.add('defaultDecoratorName', 'application')
        definition.propertyValues.add('enableNonGspViews', true)
        beanFactory.registerBeanDefinition(UnifiedLayoutFinderPostProcessor.BEAN_NAME, definition)

        when:
        new UnifiedLayoutFinderPostProcessor().postProcessBeanFactory(beanFactory)

        then: 'the class changed and every property came through untouched'
        def result = beanFactory.getBeanDefinition(UnifiedLayoutFinderPostProcessor.BEAN_NAME)
        result.beanClassName == UnifiedLayoutFinder.name
        result.propertyValues.getPropertyValue('gspReloadEnabled').value == true
        result.propertyValues.getPropertyValue('cacheEnabled').value == false
        result.propertyValues.getPropertyValue('defaultDecoratorName').value == 'application'
        result.propertyValues.getPropertyValue('enableNonGspViews').value == true
    }

    void "a missing finder bean is survivable rather than fatal"() {
        when: 'the bean has been renamed or removed by a future Grails version'
        new UnifiedLayoutFinderPostProcessor().postProcessBeanFactory(beanFactory)

        then: 'startup is not broken over a layout substitution'
        noExceptionThrown()
        !beanFactory.containsBeanDefinition(UnifiedLayoutFinderPostProcessor.BEAN_NAME)
    }

    void "UnifiedLayoutFinder is a drop-in for the bean it replaces"() {
        expect:
        GroovyPageLayoutFinder.isAssignableFrom(UnifiedLayoutFinder)
    }
}
