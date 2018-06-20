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
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition

class StocklistApiController {

    def requisitionService

    def list = {
        Requisition requisition = new Requisition(params)
        requisition.isTemplate = true
        List<Requisition> requisitions = requisitionService.getAllRequisitionTemplates(requisition, params)
		render ([data:requisitions] as JSON)
	}

    def read = {
        Requisition requisition = Requisition.findByIdAndIsTemplate(params.id, true)
        if (!requisition) {
            throw new ObjectNotFoundException(params.id, Requisition.class.toString())
        }
        render ([data: requisition] as JSON)
    }


}
