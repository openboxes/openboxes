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

class ConsumptionFact {

    Long id

    ProductDimension productKey
    LotDimension lotKey
    LocationDimension locationKey
    DateDimension transactionDateKey

    // Transaction metadata
    String transactionNumber
    String transactionCode
    String transactionType

    BigDecimal quantity = 0.0
    BigDecimal unitPrice = 0.0
    BigDecimal unitCost = 0.0

    Date lastUpdated
    Date dateCreated

    static mapping = {
        id generator: 'increment'
        cache true
    }

    String toString() {
        return "${id}"
    }

    static constraints = {
        productKey(nullable: false)
        lotKey(nullable: false)
        locationKey(nullable: false)
        transactionDateKey(nullable: false)
        transactionNumber(nullable: false)
        transactionCode(nullable: false)
        transactionType(nullable: false)
        quantity(nullable: false)
        unitPrice(nullable: false)
        unitCost(nullable: false)
    }
}
