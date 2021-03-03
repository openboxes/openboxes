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
import org.pih.warehouse.core.Party
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User

class Invoice implements Serializable {

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
    String invoiceNumber
    String referenceNumber
    String name
    String description

    InvoiceType invoiceType

    Party partyFrom // Party generating the invoice
    Party party // Party responsible for paying

    // Date fields
    Date dateInvoiced
    Date dateSubmitted
    Date dateDue
    Date datePaid

    // Currency
    UnitOfMeasure currencyUom

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        invoiceNumber(nullable: false, blank: false, unique: true, maxSize: 255)
        referenceNumber(nullable: true, maxSize: 255)
        name(nullable: true, maxSize: 255)
        description(nullable: true, maxSize: 255)

        invoiceType(nullable: true)

        partyFrom(nullable: true)
        party(nullable: true)

        dateInvoiced(nullable: true)
        dateSubmitted(nullable: true)
        dateDue(nullable: true)
        datePaid(nullable: true)

        currencyUom(nullable: true)

        updatedBy(nullable: true)
        createdBy(nullable: true)
    }
}
