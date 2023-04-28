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
                request._timeBeforeRequest = System.currentTimeMillis()
            }

            after = {
                request._timeAfterRequest = System.currentTimeMillis()
            }

            afterView = {

                if (actionName == "logout") {
                    return
                }
                if (params.showTime) {
                    session?._showTime = params.showTime == "on"
                }
                if (session._showTime) {
                    def actionDuration = request?._timeAfterRequest - request?._timeBeforeRequest
                    def viewDuration = System.currentTimeMillis() - request?._timeAfterRequest

                    request?.actionDuration = actionDuration
                    request?.viewDuration = viewDuration
                    log.info("Request duration for (${controllerName}/${actionName}): ${actionDuration}ms/${viewDuration}ms")
                }
            }
        }
    }

}