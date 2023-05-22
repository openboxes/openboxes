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

class MultiLocationInventoryReportCommand {

    Location[] locations
    Category[] categories
    Boolean includeSubcategories = Boolean.TRUE
    String buttonAction

    Map<Product, Map<Location, Integer>> entries = [:]

    static transients = ["isIncludeSubcategoriesEnabled"]

    static constraints = {
        locations(nullable: true)
        categories(nullable: true)
        includeSubcategories(nullable: true)
        buttonAction(nullable: true)
    }

    void setIncludeSubcategories(String includeCategoryChildren) {
        if ((buttonAction?.equalsIgnoreCase("run") || (buttonAction?.equalsIgnoreCase("download"))) && !includeCategoryChildren) {
            includeSubcategories = Boolean.FALSE
        }
    }
}
