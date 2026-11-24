package org.pih.warehouse

import grails.core.GrailsControllerClass

class ScaffoldTagLib {

    static namespace = "scaffold"

    private static final Map<String, Map<String, String>> CUSTOM_SHOW_PAGES = [
            transaction: [controller: "inventory", action: "showTransaction"],
    ]

    Closure associationLink = { attrs ->
        if (!attrs.value) {
            return
        }
        String label = attrs.value.toString().encodeAsHTML()
        Map<String, String> showPage = resolveShowPage(attrs.controller as String)
        if (!showPage) {
            out << label
            return
        }
        out << g.link(showPage + [id: attrs.value.id]) { label }
    }

    private Map<String, String> resolveShowPage(String controllerName) {
        if (CUSTOM_SHOW_PAGES.containsKey(controllerName)) {
            return CUSTOM_SHOW_PAGES[controllerName]
        }
        return hasShowAction(controllerName) ? [controller: controllerName, action: "show"] : null
    }

    private boolean hasShowAction(String controllerName) {
        GrailsControllerClass controllerClass = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName) as GrailsControllerClass
        return controllerClass?.clazz?.methods?.any { it.name == "show" } ?: false
    }
}
