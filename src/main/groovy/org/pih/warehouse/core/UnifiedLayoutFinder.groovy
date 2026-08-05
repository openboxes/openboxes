package org.pih.warehouse.core

import com.opensymphony.module.sitemesh.Decorator
import com.opensymphony.module.sitemesh.Page
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.web.sitemesh.GroovyPageLayoutFinder

import javax.servlet.http.HttpServletRequest

/**
 * Serves layouts/default.gsp — the unified design — in place of
 * layouts/custom.gsp, for instances that have opted in with
 *
 *     openboxes.unifiedLayout.enabled = true
 *
 * The 252 legacy pages all declare `meta layout="custom"` and none of them
 * change; only which file that name resolves to changes.
 *
 * WHY THIS RUNS HERE, AND NOT IN AN INTERCEPTOR
 *
 * An interceptor's before() runs before the view is chosen, so it cannot know
 * which layout a page declares — it can only set the layout for *every*
 * request. Doing that wraps React, mobile and print pages in the legacy
 * chrome: measured on this branch, /product/list came back carrying both a
 * React root and the legacy navbar, and /stockMovement/list rendered
 * essentially empty.
 *
 * Scoping by controller does not work either, because the same controllers
 * serve both kinds of page — ProductController.list() renders React while
 * ProductController.edit() renders a legacy GSP.
 *
 * By the time findLayout() returns, SiteMesh has already resolved what the
 * page asked for, so the substitution can be exact: swap the decorator only
 * when the resolved one is "custom", and leave react, mobile, print,
 * analytics, email and the rest untouched.
 *
 * CompileStatic is load-bearing. GroovyPageLayoutFinder overloads findLayout
 * for both Page and Content, and under dynamic dispatch `super.findLayout(...)`
 * re-resolves at runtime and can land back on this class — an infinite
 * recursion that surfaces as a StackOverflowError on the first decorated
 * request. Static compilation binds the super call to the Page overload.
 */
@Slf4j
@CompileStatic
class UnifiedLayoutFinder extends GroovyPageLayoutFinder {

    /** The layout the legacy pages declare. */
    static final String DECLARED_LAYOUT = 'custom'

    /** The restyled equivalent served in its place. */
    static final String UNIFIED_LAYOUT = 'default'

    /** Set by LayoutInterceptor when ?layout= named the layout explicitly. */
    static final String OVERRIDE_ATTRIBUTE = 'org.pih.warehouse.layout.override'

    /**
     * Only the Page overload is overridden; the Content overload in the
     * superclass delegates to this one.
     */
    @Override
    Decorator findLayout(HttpServletRequest request, Page page) {
        Decorator resolved = super.findLayout(request, page)
        if (resolved == null || resolved.name != DECLARED_LAYOUT || !isEnabled()) {
            return resolved
        }
        if (request.getAttribute(OVERRIDE_ATTRIBUTE) != null) {
            // ?layout= asked for this by name; honour it in both directions.
            return resolved
        }
        Decorator unified = getNamedDecorator(request, UNIFIED_LAYOUT)
        if (unified == null) {
            // A missing layout would render every page undecorated, so fall
            // back to what the page actually asked for.
            log.warn("layouts/${UNIFIED_LAYOUT}.gsp not found; using ${DECLARED_LAYOUT}")
            return resolved
        }
        return unified
    }

    /**
     * Read per request rather than cached: config can come from an external
     * file, and a cached value would need a restart to pick up a change.
     */
    private static boolean isEnabled() {
        return Holders.config?.getProperty('openboxes.unifiedLayout.enabled', Boolean, Boolean.FALSE)
    }
}
