/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

import java.time.Instant

/**
 * Represents an association between two products.
 */
class ProductAssociation {

    def beforeDelete() {
        if (mutualAssociation) {
            // Set to null mutual association that references ProductAssociation which is being deleted
            ProductAssociation mutualAssociation = ProductAssociation.get(mutualAssociation.id)
            mutualAssociation.mutualAssociation = null
            mutualAssociation.save()
        }
    }

    String id
    ProductAssociationTypeCode code
    Product associatedProduct
    BigDecimal quantity = 0
    String comments

    ProductAssociation mutualAssociation

    Instant dateCreated
    Instant lastUpdated

    static belongsTo = [product: Product]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        code(nullable: false)
        associatedProduct(nullable: false)
        quantity(nullable: true)
        comments(nullable: true)
        mutualAssociation(nullable: true)
    }

    static PROPERTIES = [
            "id"                     : "id",
            "Type"                   : "code",
            "Product Code"           : "product.productCode",
            "Product Name"           : "product.name",
            "Associated Product Code": "associatedProduct.productCode",
            "Associated Product Name": "associatedProduct.name",
            "Conversion"             : "quantity",
            "Comments"               : "comments",
            "Date created"           : "dateCreated",
            "Last updated"           : "lastUpdated"
    ]
}
