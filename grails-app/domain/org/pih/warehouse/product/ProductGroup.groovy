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

/**
 *
 */
class ProductGroup implements Comparable, Serializable {

    // Base product information
    String id
    String name
    String description
    Category category

    // Auditing
    Date dateCreated
    Date lastUpdated

    static belongsTo = Product
    static hasMany = [products: Product]
    static mapping = {
        id generator: 'uuid'
        products joinTable: [name: 'product_group_product', column: 'product_id', key: 'product_group_id']
        category ignoreNotFound: true
    }


    static constraints = {
        name(nullable: false, blank: false, maxSize: 255)
        category(nullable: true)
        description(nullable: true)
    }

    String toString() { return "$name" }

    int compareTo(obj) {
        this.name <=> obj.name
    }

    @Override
    int hashCode() {
        if (this.id != null) {
            return this.id.hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof ProductGroup) {
            ProductGroup that = (ProductGroup) o
            return this.id == that.id
        }
        return false
    }
}

