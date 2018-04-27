/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup

class TransactionFact {

    // Dimension Keys
    LotDimension lotKey
	ProductDimension productKey
    LocationDimension locationKey
    DateDimension transactionDateKey
    TransactionTypeDimension transactionTypeKey


    // Transaction facts
    Date transactionDate
    String transactionNumber
    Transaction transaction
    TransactionEntry transactionEntry

    // Facts
    Boolean canceled = Boolean.FALSE
    Boolean substituted = Boolean.FALSE
    Boolean modified = Boolean.FALSE
    String reasonCode = ""

    BigDecimal quantity = 0;
    BigDecimal quantityRequested = 0
    BigDecimal quantityIssued = 0
    BigDecimal quantityConsumed = 0
    BigDecimal quantityExpired = 0
    BigDecimal quantityCanceled = 0
    BigDecimal quantityDemand = 0
    BigDecimal quantitySubstituted = 0
    BigDecimal quantityModified = 0

    static mapping = {
    }

    static constraints = {
        lotKey(nullable:false)
        productKey(nullable:false)
        locationKey(nullable:false)
        transactionDateKey(nullable:false)
        transactionTypeKey(nullable:false)
        transactionDate(nullable:false)
        transactionNumber(nullable:false)
        transaction(nullable:false)
        transactionEntry(nullable:false)
        canceled(nullable:true)
        substituted(nullable:true)
        modified(nullable:true)
        reasonCode(nullable:true)
        quantity(nullable:true)
        quantityRequested(nullable:true)
        quantityIssued(nullable:true)
        quantityConsumed(nullable:true)
        quantityExpired(nullable:true)
        quantityCanceled(nullable:true)
        quantityDemand(nullable:true)
        quantitySubstituted(nullable:true)
        quantityModified(nullable:true)
    }
}
