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
import org.pih.warehouse.product.Product

class Consumption {

    String id

	Product product
    String productCode
    String productName
    String categoryName
//    String catalogNames
//    String tagNames

    InventoryItem inventoryItem
    String lotNumber
    Date expirationDate

	Location location
	String locationName
    String locationGroup
    String locationType

    String transactionNumber
    String transactionCode
    String transactionType

    Date transactionDate
    int day
    int month
    int year

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

    static constraints = {
        transactionNumber(nullable:true)
        transactionCode(nullable:true)
        lotNumber(nullable:true)
        expirationDate(nullable:true)
        reasonCode(nullable:true)
    }
}
