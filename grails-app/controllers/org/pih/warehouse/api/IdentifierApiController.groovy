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

class IdentifierApiController extends NoopApiController {

    def identifierService

    def create = {
        log.debug "create " + params
        def identifierType = params.identifierType
        if (!identifierType && !params.identifierFormat) {
            throw new IllegalArgumentException("Must specify identifierType or identifierFormat as a parameter")
        }

        String identifier

        if (params.identifierFormat) {
            identifier = identifierService.generateIdentifier(params.identifierFormat)
        } else {
            switch (identifierType) {
                case "product":
                    identifier = identifierService.generateProductIdentifier()
                    break
                case "productSupplier":
                    identifier = identifierService.generateProductSupplierIdentifier()
                    break
                case "shipment":
                    identifier = identifierService.generateShipmentIdentifier()
                    break
                case "requisition":
                    identifier = identifierService.generateRequisitionIdentifier()
                    break
                case "order":
                    identifier = identifierService.generateOrderIdentifier()
                    break
                case "transaction":
                    identifier = identifierService.generateTransactionIdentifier()
                    break
                default:
                    throw new IllegalArgumentException("Illegal identifier type ${identifierType}")
            }
        }
        render([data: identifier] as JSON)
    }
}
