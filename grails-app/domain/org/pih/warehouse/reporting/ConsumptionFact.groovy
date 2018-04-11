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
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup

class ConsumptionFact {

    String id

    // Dimension Keys
	Product product
    ProductGroup genericProduct
    InventoryItem inventoryItem
    Location location

    // Product dimension
    String productCode
    String productName
    BigDecimal unitCost
    BigDecimal unitPrice
    Category category
    String categoryName

    // Inventory item dimension
    String lotNumber
    Date expirationDate

    // Location dimension
	String locationName
    String locationGroup
    String locationType

    // Transaction dimension
    String transactionNumber
    String transactionCode
    String transactionType

    // Date dimension
    Date transactionDate
    String day
    String week
    String month
    String year
    String monthYear

    // Fact table data
    Boolean canceled
    Boolean substituted
    Boolean modified
    String reasonCode

    BigDecimal quantity = 0;
    BigDecimal quantityRequested = 0
    BigDecimal quantityIssued = 0
    BigDecimal quantityConsumed = 0
    BigDecimal quantityExpired = 0
    BigDecimal quantityCanceled = 0
    BigDecimal quantityDemand = 0
    BigDecimal quantitySubstituted = 0
    BigDecimal quantityModified = 0

    Date lastUpdated
	Date dateCreated

    static mapping = {
        id generator: 'uuid'
        cache true
    }

    String toString() {
        return "${id}:${productCode}:${productName}:${transactionCode}:${transactionDate}:${quantityIssued}"
    }

    static constraints = {
        genericProduct(nullable:true)
        category(nullable:true)
        unitCost(nullable:true)
        unitPrice(nullable:true)
        transactionNumber(nullable:true)
        transactionCode(nullable:true)
        lotNumber(nullable:true)
        expirationDate(nullable:true)
        reasonCode(nullable:true)
    }
}
