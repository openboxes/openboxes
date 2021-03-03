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

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class InvoiceItem implements Serializable {

    def beforeInsert = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            createdBy = currentUser
            updatedBy = currentUser
        }
    }

    def beforeUpdate = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            updatedBy = currentUser
        }
    }

    String id

    Invoice invoice
    Product product
    GlAccount glAccount
    BudgetCode budgetCode

    Integer quantity
    UnitOfMeasure quantityUom
    BigDecimal quantityPerUom = 1
    BigDecimal amount

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        invoice(nullable: false)
        product(nullable: true)
        glAccount(nullable: true)
        budgetCode(nullable: true)

        quantity(nullable: false, min: 1)
        quantityUom(nullable: true)
        quantityPerUom(nullable: false)
        amount(nullable: true)

        updatedBy(nullable: true)
        createdBy(nullable: true)
    }
}
