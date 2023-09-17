/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON

class UserApiController extends BaseDomainApiController {

    def userService

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 15, 100)
        params.roleTypes = params.list("roleTypes")
        params.location = params.location ?: session.warehouse?.id
        String query = params.q ? "%" + params.q + "%" : ""

        def users = userService.findUsers(query, params)
        render([data: users] as JSON)

    }
}
