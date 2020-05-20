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

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User


class ProductPackage implements Comparable<ProductPackage>, Serializable {

    def beforeInsert = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                createdBy = currentUser
                updatedBy = currentUser
            }
        }
    }
    def beforeUpdate = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                updatedBy = currentUser
            }
        }
    }

    String id
    String name                // Name of product as it appears on the package
    String description        // Description of the package
    String gtin                // Global trade identification number
    Integer quantity        // Number of units (each) in the box
    Float price
    UnitOfMeasure uom        // Unit of measure of the package (e.g. box, case, etc)

    ProductSupplier productSupplier

    // Auditing
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static belongsTo = [product: Product]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        name(nullable: true)
        description(nullable: true)
        gtin(nullable: true)
        uom(nullable: true)
        price(nullable: true)
        quantity(nullable: false)
        createdBy(nullable: true)
        updatedBy(nullable: true)
        productSupplier(nullable: true)
    }

    String toString() {
        return name
    }

    /**
     * Sort by quantity
     */
    int compareTo(ProductPackage other) {
        return other.quantity <=> quantity
    }


}
