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

import org.pih.warehouse.core.Constants
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType

class InvoiceService {

    boolean transactional = true

    List getInvoices(def params) {
        return []
    }

    ReferenceNumber createOrUpdateVendorInvoiceNumber(Invoice invoice, String vendorInvoiceNumber) {
        ReferenceNumberType vendorInvoiceNumberType = ReferenceNumberType.findById(Constants.VENDOR_INVOICE_NUMBER_TYPE_ID)
        if (!vendorInvoiceNumberType) {
            throw new IllegalStateException("Must configure reference number type for Vendor Invoice Number with ID '${Constants.VENDOR_INVOICE_NUMBER_TYPE_ID}'")
        }

        ReferenceNumber referenceNumber = invoice.vendorInvoiceNumber

        if (vendorInvoiceNumber) {
            if (!referenceNumber) {
                referenceNumber = new ReferenceNumber()
                referenceNumber.identifier = vendorInvoiceNumber
                referenceNumber.referenceNumberType = vendorInvoiceNumberType
                invoice.addToReferenceNumbers(referenceNumber)
            }
            else {
                referenceNumber.identifier = vendorInvoiceNumber
            }
        }
        else if (referenceNumber) {
            invoice.removeFromReferenceNumbers(referenceNumber)
        }
        return referenceNumber
    }
}
