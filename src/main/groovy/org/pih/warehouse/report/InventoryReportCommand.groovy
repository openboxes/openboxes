/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.report

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class InventoryReportCommand {

    Product product
    Location location
    Date startDate
    Date endDate
    Category category
    Category rootCategory

    Boolean includeChildren
    Boolean hideInactiveProducts
    Boolean showEntireHistory
    Boolean showTransferBreakdown
    Boolean insertPageBreakBetweenCategories

    Map<Product, InventoryReportEntryCommand> entries = [:]


    static constraints = {
        product(nullable: true)
        location(nullable: false)
        startDate(nullable: true)
        endDate(nullable: true)
        category(nullable: false)
        includeChildren(nullable: true)
        showEntireHistory(nullable: true)
        showTransferBreakdown(nullable: true)
        hideInactiveProducts(nullable: true)
        insertPageBreakBetweenCategories(nullable: true)
    }


    InventoryReportEntryCommand getProductEntry(Product product) {
        return entries[product]
    }

    Set<Product> getProducts() {
        return entries.keySet()
    }

    Collection getProducts(Category category) {
        return getProductsByCategory()[category]
    }

    Map getProductsByCategory() {
        return getProducts()?.groupBy { it.category }
    }


    String toString() {
        return "Location: " + location
    }

}