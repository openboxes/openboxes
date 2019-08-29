/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

class InventoryCommand {

    def shipment
    def warehouseInstance                                // warehouseInstance
    def searchTerms                                    // request-level search terms
    def categoryInstance                                // categoryInstance
    def subcategoryInstance
    // child category to show within the categoryInstance

    // InventoryItemCommand objects
    def inventoryItems

    // Product groups
    def productGroups

    def showHiddenProducts = Boolean.FALSE
    // indicates whether to display hidden products
    def showUnsupportedProducts = Boolean.FALSE
    // indicates whether unsupported products for the warehouse should be included
    def showNonInventoryProducts = Boolean.FALSE
    // indicates whether non-inventory products for the warehouse should be included
    def showOutOfStockProducts = Boolean.TRUE
    // indicates whether out of stock products for the warehouse should be included
    def categoryToProductMap = {}
    // all of the resulting ProductCommands above, organized by Category
    Boolean searchPerformed = Boolean.FALSE

    def maxResults = 0
    def offset = 0
    def numResults = 0

    // Tags
    List tags
    // Catalogs
    List catalogs

    static constraints = {
        shipment(nullable: true)
        warehouseInstance(nullable: true)
        searchTerms(nullable: true)
        categoryInstance(nullable: true)
        subcategoryInstance(nullable: true)
        showHiddenProducts(nullable: true)
        showUnsupportedProducts(nullable: true)
        showNonInventoryProducts(nullable: true)
        showOutOfStockProducts(nullable: true)
        categoryToProductMap(nullable: true)
        inventoryItems(nullable: true)
        productGroups(nullable: true)
        searchPerformed(nullable: true)
    }
}
