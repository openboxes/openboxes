package org.pih.warehouse
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

/**
 * Lets a single request pick its own SiteMesh layout:
 *
 *     /inventory/browse                  the instance's configured layout
 *     /inventory/browse?layout=default   the unified design, just this once
 *     /inventory/browse?layout=custom    the original, just this once
 *
 * Those two names are the only ones accepted. Anything else is ignored and
 * the request renders normally.
 *
 * This exists so the two can be compared side by side on a running instance
 * without a redeploy or a config change. It overrides in both directions, so
 * an instance that has opted in can still pull up the original.
 *
 * Whether an instance uses the unified layout by default is a config setting
 * (openboxes.layout.unified.enabled) applied by UnifiedLayoutFinder, not by
 * this class — an interceptor runs before the view is chosen, so it cannot
 * tell which layout a page declares, and forcing one on every request wraps
 * React, mobile and print pages in the wrong chrome. UnifiedLayoutFinder
 * carries the detail.
 *
 * Mechanism: GroovyPageLayoutFinder reads the "org.grails.layout.name" request
 * attribute FIRST and only falls back to the page's own meta tag when that
 * attribute is absent, so setting it here takes precedence for this request.
 */
class LayoutInterceptor {

    /** Grails' GroovyPageLayoutFinder.LAYOUT_ATTRIBUTE. */
    private static final String LAYOUT_ATTRIBUTE = 'org.grails.layout.name'

    /**
     * Marks the layout as deliberately chosen for this request, so
     * UnifiedLayoutFinder leaves it alone. Without this, ?layout=custom on an
     * opted-in instance would be resolved as "custom" and then substituted
     * straight back to the unified layout — the override would do nothing in
     * the one direction a reviewer most needs it.
     */
    static final String OVERRIDE_ATTRIBUTE = 'org.pih.warehouse.layout.override'

    /**
     * Only these two may be requested. An allowlist rather than a pattern:
     * any other name that reached the view resolver would be honoured, and
     * several of them strip the page's chrome entirely. SiteMesh reserves
     * "_none_" for "render undecorated", and print/email/mobile are real
     * layouts with no navigation — all of which passed a name-shaped regex
     * and removed the navigation from any page, for anyone.
     *
     * This parameter exists so the two candidates can be compared on a
     * running instance; it is not a general layout selector.
     */
    private static final Set<String> SELECTABLE_LAYOUTS =
            Collections.unmodifiableSet(['custom', 'default'] as Set)

    /**
     * Whether a ?layout= value may be honoured. Separated out so the allowlist
     * can be tested without standing up the interceptor.
     */
    static boolean isSelectable(String requested) {
        return SELECTABLE_LAYOUTS.contains(requested)
    }

    LayoutInterceptor() {
        // Without an explicit match, an interceptor only applies to the
        // controller matching its own name — i.e. it would never run.
        matchAll().except(uri: '/static/**')
    }

    boolean before() {
        String requested = params.layout
        if (isSelectable(requested)) {
            request[LAYOUT_ATTRIBUTE] = requested
            request[OVERRIDE_ATTRIBUTE] = Boolean.TRUE
        }
        return true
    }
}
