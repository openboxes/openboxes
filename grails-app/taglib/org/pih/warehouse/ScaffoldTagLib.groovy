package org.pih.warehouse

import grails.core.GrailsControllerClass

class ScaffoldTagLib {

    static namespace = "scaffold"

    private static final String SHOW_ACTION = "show"

    /**
     * Show pages for associated domains whose details are not served by the conventional
     * "<domainName>/show/<id>" URL, keyed by the domain's decapitalized name.
     *
     * By default, an association on a scaffolded page links to "<domainName>/show/<id>" when that
     * domain has a controller with a "show" action, and renders as plain text otherwise. Add an entry
     * here when a domain's details live elsewhere (e.g. Transaction has no TransactionController and is
     * shown via inventory/showTransaction), otherwise its links on scaffolded pages will be unclickable.
     */
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
        return hasShowAction(controllerName) ? [controller: controllerName, action: SHOW_ACTION] : null
    }

    private boolean hasShowAction(String controllerName) {
        GrailsControllerClass controllerClass = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName) as GrailsControllerClass
        return controllerClass?.clazz?.methods?.any { it.name == SHOW_ACTION } ?: false
    }
}
