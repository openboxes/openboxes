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
import grails.validation.ValidationException
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class GenericApiController {

    GenericApiService genericApiService

    def list = {
        List data = genericApiService.getList(params.resource, params)
        render (["data":data] as JSON)
	}

    def read = {
        Object domainObject = genericApiService.getObject(params.resource, params.id)
        render ([data:domainObject] as JSON)
    }

    def create = {
        Object domainObject = genericApiService.createObject(params.resource, request.JSON)
        response.status = 201
        render ([data:domainObject] as JSON)
    }

    def update = {
        Object domainObject = genericApiService.updateObject(params.resource, params.id, request.JSON)
        render ([data:domainObject] as JSON)
    }

    def delete = {
        genericApiService.deleteObject(params.resource, params.id)
        response.status = 204
    }

}
