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
class UtilFilters {

    def filters = {

        profiler(controller: '*', action: '*') {
            before = {
                if (params.showTime) {
                    session._showTime = params.showTime == "on"
                }

                request._timeBeforeRequest = System.currentTimeMillis()
            }

            after = { Map model ->
                request._timeAfterRequest = System.currentTimeMillis()
                if (session._showTime) {
                    request.actionDuration = request._timeAfterRequest - request._timeBeforeRequest
                }
            }

            afterView = { Exception e ->

                log.info "Request " + request
                if (session._showTime) {
                    flash.viewDuration = System.currentTimeMillis() - request._timeAfterRequest
                    log.info("Request duration for (${controllerName}/${actionName}): ${request.actionDuration}ms/${flash.viewDuration}ms")
                }
            }
        }
    }

}