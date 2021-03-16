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
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Party
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType

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

    static hasMany = [referenceNumbers: ReferenceNumber]

    static mapping = {
        id generator: 'uuid'
        referenceNumbers cascade: "all-delete-orphan"
    }

    static transients = ['vendorInvoiceNumber']

    static constraints = {
        invoiceNumber(nullable: false, blank: false, unique: true, maxSize: 255)
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

    ReferenceNumber getReferenceNumber(String id) {
        def referenceNumberType = ReferenceNumberType.findById(id)
        if (referenceNumberType) {
            for (referenceNumber in referenceNumbers) {
                if (referenceNumber.referenceNumberType == referenceNumberType) {
                    return referenceNumber
                }
            }
        }
        return null
    }

    ReferenceNumber getVendorInvoiceNumber() {
        return getReferenceNumber(Constants.VENDOR_INVOICE_NUMBER_TYPE_ID)
    }

    Map toJson() {
        return [
            id: id,
            invoiceNumber: invoiceNumber,
            vendorInvoiceNumber: vendorInvoiceNumber?.identifier,
            name: name,
            description: description,
            partyFrom: Organization.get(partyFrom?.id),
            dateInvoiced: dateInvoiced.format("MM/dd/yyyy"),
            dateSubmitted: dateSubmitted,
            dateDue: dateDue,
            datePaid: datePaid,
            currencyUom: currencyUom?.id,
            vendor: party?.id
        ]
    }
}
