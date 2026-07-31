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
 * Allows the SiteMesh layout to be overridden per request with a ?layout=
 * parameter, so the unified design can be compared against the previous one
 * without a redeploy:
 *
 *     /inventory/browse                  -> layouts/custom.gsp  (the page's own meta;
 *                                                                now the unified theme)
 *     /inventory/browse?layout=legacy    -> layouts/legacy.gsp  (the previous appearance)
 *
 * This works because GroovyPageLayoutFinder reads the "org.grails.layout.name"
 * request attribute FIRST and only falls back to the page's <meta name="layout">
 * when that attribute is absent — so setting it here takes precedence.
 *
 * The override is per-request: it is not carried across links, so append the
 * parameter to each URL being compared.
 *
 * Intended as a temporary aid while the unified layout is being reviewed.
 */
class LayoutInterceptor {

    /** Grails' GroovyPageLayoutFinder.LAYOUT_ATTRIBUTE. */
    private static final String LAYOUT_ATTRIBUTE = 'org.grails.layout.name'

    /**
     * A layout name resolves to a GSP path under /layouts, so refuse anything
     * that is not a plain name — this keeps "../" and other path tricks out of
     * the view resolver. An unknown-but-well-formed name simply renders the
     * page undecorated.
     */
    private static final java.util.regex.Pattern SAFE_LAYOUT_NAME = ~/^[a-zA-Z0-9_-]{1,50}$/

    LayoutInterceptor() {
        // Without an explicit match, an interceptor only applies to the
        // controller matching its own name — i.e. it would never run.
        matchAll().except(uri: '/static/**')
    }

    boolean before() {
        String requested = params.layout
        if (requested && SAFE_LAYOUT_NAME.matcher(requested).matches()) {
            request[LAYOUT_ATTRIBUTE] = requested
        }
        return true
    }
}
