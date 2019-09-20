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

class TransactionFact {

    Long id

    // Dimension Keys
    LotDimension lotKey
    ProductDimension productKey
    LocationDimension locationKey
    DateDimension transactionDateKey
    TransactionTypeDimension transactionTypeKey

    // Transaction facts
    Date transactionDate
    String transactionNumber

    BigDecimal quantity = 0

    static mapping = {
        id generator: 'increment'
        cache true
    }

    static constraints = {
        lotKey(nullable: false)
        productKey(nullable: false)
        locationKey(nullable: false)
        transactionDateKey(nullable: false)
        transactionTypeKey(nullable: false)
        transactionDate(nullable: false)
        transactionNumber(nullable: false)
        quantity(nullable: true)
    }

    static namedQueries = {
        minTransactionDate { locationId ->
            projections {
                min 'transactionDate'
            }
            locationKey {
                eq("locationId", locationId)
            }
            uniqueResult = true
        }

        maxTransactionDate { locationId ->
            projections {
                max 'transactionDate'
            }
            locationKey {
                eq("locationId", locationId)
            }
            uniqueResult = true
        }

        countDistinctProducts { locationId ->
            projections {
                productKey {
                    countDistinct 'productId'
                }
            }
            locationKey {
                eq("locationId", locationId)
            }
            uniqueResult = true
        }
    }
}
