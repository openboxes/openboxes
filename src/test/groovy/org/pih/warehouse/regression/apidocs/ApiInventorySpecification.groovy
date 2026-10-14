package org.pih.warehouse.regression.apidocs

import grails.testing.web.UrlMappingsUnitTest
import grails.web.mapping.UrlMappingInfo
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.web.mapping.mvc.GrailsControllerUrlMappingInfo
import org.pih.warehouse.UrlMappings
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import spock.lang.Specification

/**
 * Unit-test context for the route inventory: the real UrlMappings with every application
 * controller registered, so matching uses Grails' own controller/action lookup and precedence.
 */
abstract class ApiInventorySpecification extends Specification implements UrlMappingsUnitTest<UrlMappings> {

    static List<Class> applicationControllers() {
        ControllerArtefactHandler handler = new ControllerArtefactHandler()
        new PathMatchingResourcePatternResolver()
                .getResources('classpath*:org/pih/warehouse/**/*Controller.class')
                .collect { resource ->
                    String url = resource.URL.toString()
                    String className = url.substring(url.indexOf('org/pih/warehouse/'), url.length() - '.class'.length()).replace('/', '.')
                    Class.forName(className, false, ApiInventorySpecification.classLoader)
                }
                .findAll { handler.isArtefact(it) }
                .sort { it.name }
    }

    /** Subclasses do not carry the trait's type argument, so name the mappings class here. */
    @Override
    Class<UrlMappings> getTypeUnderTest() {
        UrlMappings
    }

    @Override
    Class[] getControllersToMock() {
        applicationControllers() as Class[]
    }

    /** The controller/action a request would be dispatched to, as Grails picks it: the first match whose controller and action exist. */
    String resolve(String method, String uri) {
        request.method = method
        UrlMappingInfo[] infos = urlMappingsHolder.matchAll(uri, method)
        GrailsControllerUrlMappingInfo winner = (GrailsControllerUrlMappingInfo) infos.find { it instanceof GrailsControllerUrlMappingInfo }
        if (!winner) {
            return null
        }
        // A method missing from a mapping's method map dispatches to the controller's default action.
        String action = winner.actionName ?: winner.controllerClass.defaultAction
        winner.controllerClass.actions.contains(action) ? "${winner.controllerClass.logicalPropertyName}/${action}" : null
    }

    /**
     * OBAF splits its mappings over several classes (PickUrlMappings, PutawayUrlMappings next to
     * UrlMappings). Every *UrlMappings class on the classpath except the type under test, registered as well.
     */
    static List<Class> additionalUrlMappings() {
        new PathMatchingResourcePatternResolver()
                .getResources('classpath*:org/pih/warehouse/**/*UrlMappings.class')
                .collect { resource ->
                    String url = resource.URL.toString()
                    String className = url.substring(url.indexOf('org/pih/warehouse/'), url.length() - '.class'.length()).replace('/', '.')
                    Class.forName(className, false, ApiInventorySpecification.classLoader)
                }
                .findAll { it != UrlMappings && !it.name.contains('$') }
                .sort { it.name }
    }

    ApiRouteInventory buildInventory() {
        additionalUrlMappings().each { mockArtefact(it) }
        new ApiRouteInventory(urlMappingsHolder, grailsApplication, this.&resolve).build()
    }
}
