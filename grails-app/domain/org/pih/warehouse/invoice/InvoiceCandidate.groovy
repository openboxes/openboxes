/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.invoice

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.UnitOfMeasure

class InvoiceCandidate {

    String id

    String orderNumber
    String shipmentNumber

    BudgetCode budgetCode
    GlAccount glAccount

    String productCode
    String description

    Integer quantity
    Integer quantityToInvoice

    UnitOfMeasure quantityUom
    BigDecimal unitPrice
    BigDecimal quantityPerUom = 1

    String currencyCode

    Organization vendor


    static mapping = {
        id generator: 'uuid'
        version false
        cache usage: "read-only"
    }

    static constraints = {
    }

    static transients = ['unitOfMeasure']

    String getUnitOfMeasure() {
        if (quantityUom) {
            return "${quantityUom?.code}/${quantityPerUom as Integer}"
        }
        else {
            def g = ApplicationHolder.application.mainContext.getBean( 'org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib' )
            return "${g.message(code:'default.ea.label').toUpperCase()}/1"
        }
    }

    Map toJson() {
        return [
                orderNumber: orderNumber,
                shipmentNumber: shipmentNumber,
                budgetCode: budgetCode?.code,
                glCode: glAccount?.code,
                productCode: productCode,
                description: description,
                quantity: quantity,
                quantityToInvoice: quantityToInvoice,
                uom: unitOfMeasure,
                unitPrice: unitPrice
        ]
    }
}
