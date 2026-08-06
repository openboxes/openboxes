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
 * HOW A REQUEST FLOWS THROUGH THIS CLASS
 *
 * 1. When Grails renders a decorated page, SiteMesh asks the
 *    `groovyPageLayoutFinder` bean (this class, swapped in by
 *    UnifiedLayoutFinderPostProcessor) for a decorator.
 * 2. super.findLayout() runs stock Grails resolution, in the framework's own
 *    order: the `org.grails.layout.name` request attribute first — which is
 *    how the interceptor's ?layout= override wins — then the page's
 *    `meta layout` declaration, then the application default.
 * 3. Only when that resolved to "custom", the flag is on, and the request did
 *    not name a layout itself (OVERRIDE_ATTRIBUTE below), the "default"
 *    decorator is looked up with the same getNamedDecorator() call Grails
 *    uses internally, and returned instead.
 * 4. Everything else — react, print, mobile, email, resolution failures —
 *    returns exactly what stock Grails resolved.
 *
 * WHY THIS RUNS HERE, AND NOT IN AN INTERCEPTOR
 *
 * An interceptor's before() runs before the view is chosen, so it cannot know
 * which layout a page declares — it can only set the layout for *every*
 * request. Doing that wraps React, mobile and print pages in the legacy
 * chrome: measured on this branch, /product/list came back carrying both a
 * React root and the legacy navbar, and /stockMovement/list rendered
 * essentially empty. Scoping by controller does not work either, because the
 * same controllers serve both kinds of page — ProductController.list()
 * renders React while ProductController.edit() renders a legacy GSP.
 *
 * WHY @CompileStatic IS LOAD-BEARING
 *
 * GroovyPageLayoutFinder overloads findLayout() for both Page and Content,
 * and only the Page overload is overridden here (the Content one delegates to
 * it in the superclass). Under dynamic dispatch the super call in step 2
 * re-entered this subclass instead of the superclass (the stack showed
 * super$2$findLayout -> findLayout repeating) until the stack overflowed.
 * Static compilation binds the call to the superclass method. The
 * integration spec exercises both entry points against the real bean so this
 * cannot quietly regress.
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
        if (resolved == null) {
            return null
        }
        boolean explicitOverride = request.getAttribute(OVERRIDE_ATTRIBUTE) != null
        if (!shouldSubstitute(resolved.name, isEnabled(), explicitOverride)) {
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
     * The whole policy, separated from the SiteMesh plumbing so it can be
     * tested without a Spring context. Substitute only when the page asked for
     * the legacy layout, the instance has opted in, and this request did not
     * name a layout itself.
     */
    static boolean shouldSubstitute(String resolvedLayout, boolean enabled, boolean explicitOverride) {
        return resolvedLayout == DECLARED_LAYOUT && enabled && !explicitOverride
    }

    /**
     * Read per request rather than cached: config can come from an external
     * file, and a cached value would need a restart to pick up a change.
     */
    private static boolean isEnabled() {
        return Holders.config?.getProperty('openboxes.unifiedLayout.enabled', Boolean, Boolean.FALSE)
    }
}
