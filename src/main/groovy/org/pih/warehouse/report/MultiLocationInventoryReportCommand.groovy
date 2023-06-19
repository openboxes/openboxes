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

import grails.validation.Validateable
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class MultiLocationInventoryReportCommand implements Validateable {

    String actionButton
    Boolean includeSubcategories = Boolean.TRUE
    List<Location> locations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class))
    List<Category> categories = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class))

    Map<Product, Map<Location, Integer>> entries = [:]

    static transients = ["isActionRun", "isActionDownload"]

    static constraints = {
        locations(nullable: true)
        categories(nullable: true)
        includeSubcategories(nullable: true)
        actionButton(nullable: true, inList: ["run", "download"])
    }

    boolean getIsActionRun() {
        return actionButton?.equalsIgnoreCase("run")
    }

    boolean getIsActionDownload() {
        return actionButton?.equalsIgnoreCase("download")
    }

}
