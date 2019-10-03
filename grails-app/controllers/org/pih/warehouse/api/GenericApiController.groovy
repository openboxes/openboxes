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
import org.grails.web.json.JSONArray

class GenericApiController {

    GenericApiService genericApiService

    def list = {
        List data = genericApiService.getList(params.resource, params)
        render([data: data] as JSON)
    }

    def search = {
        def jsonObject = request.JSON
        List data = genericApiService.searchObjects(params.resource, jsonObject, params)
        render([data: data] as JSON)
    }

    def read = {
        Object domainObject = genericApiService.getObject(params.resource, params.id)
        render([data: domainObject] as JSON)
    }

    def create = {
        Object result
        def jsonObject = request.JSON
        if (jsonObject instanceof JSONArray) {
            result = genericApiService.createObjects(params.resource, jsonObject)
        } else {
            result = genericApiService.createObject(params.resource, jsonObject)
        }
        response.status = 201
        render([data: result] as JSON)
    }

    def update = {
        Object domainObject = genericApiService.updateObject(params.resource, params.id, request.JSON)
        render([data: domainObject] as JSON)
    }

    def delete = {
        genericApiService.deleteObject(params.resource, params.id)
        render status: 204
    }

}
