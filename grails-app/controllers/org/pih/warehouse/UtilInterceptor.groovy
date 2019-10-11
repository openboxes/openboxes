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
 * To give credit where it's due, I borrowed this request profiling code from user deluan.
 *
 * https://gist.github.com/deluan/744828
 */
class UtilInterceptor {

    UtilInterceptor() {
        matchAll().except(uri: '/static/**')
    }

    boolean before() {
        request._timeBeforeRequest = System.currentTimeMillis()
        return true
    }

    boolean after() {
        request._timeAfterRequest = System.currentTimeMillis()
        request?.pageLoadInMilliseconds = request?._timeAfterRequest - request?._timeBeforeRequest
        return true
    }

}
