/**
 * Copyright (c) 2026 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.core

import java.util.regex.Pattern

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

/**
 * Minimal replacement for the browser-detection plugin's userAgentIdentService
 * (the plugin has no Grails 7 release). OpenBoxes only ever called isMobile(),
 * which routes a few controllers to the mobile dashboard view, so a simple
 * User-Agent pattern check preserves that behavior.
 */
class UserAgentIdentService {

    static transactional = false

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        /(?i)\b(android|iphone|ipod|ipad|windows phone|blackberry|bb10|opera mini|iemobile|mobile)\b/)

    boolean isMobile() {
        String userAgent = currentUserAgent()
        return userAgent ? MOBILE_PATTERN.matcher(userAgent).find() : false
    }

    private static String currentUserAgent() {
        def attributes = RequestContextHolder.requestAttributes
        if (attributes instanceof GrailsWebRequest) {
            return attributes.currentRequest.getHeader('User-Agent')
        }
        return null
    }
}
