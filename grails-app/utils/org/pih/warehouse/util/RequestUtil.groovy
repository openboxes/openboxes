/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.util

class RequestUtil {

    static boolean isAjax(request) {
        // If incoming request is in JSON or Accept is JSON, then we should
        def accept = request.getHeader("Accept")
        def contentType = request.getHeader("Content-Type")
        return request.isXhr() ||
                (contentType && contentType?.contains("application/json") ||
                        (accept && accept?.contains("application/json")))
    }

}
