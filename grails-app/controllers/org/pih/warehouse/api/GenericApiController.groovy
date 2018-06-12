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
        log.info "List " + params
        List data = genericApiService.getList(params.resource, params)
        render (["data":["${params.resource}":data.collect { it.toJson() }]] as JSON)
	}

    def read = {
        log.info "Read " + params
        Object object = genericApiService.getObject(params.resource, params.id)
        render ([data:["${params.resource}":object.toJson()]] as JSON)
    }



}
