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

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.product.Category

class BrowseInventoryController {

    def dataSource
    def productService
    def inventoryService

    def index = {
        redirect(action: "list")
    }


    def list = {
        def q = params.q
        def category = Category.get(params?.category?.id) ?: null
        def location = Location.get(params?.location?.id) ?: session.warehouse
        if (!location) {
            throw new Exception("Location is required")
        }
        def inventorySnapshots = InventorySnapshot.createCriteria().list(max: params.max ?: 10, offset: params.offset ?: 0) {
            and {
                eq("location", location)
                if (category) {
                    product {
                        eq("category", category)
                    }
                }
                if (q) {
                    product {
                        ilike("name", "%" + q + "%")
                    }
                }
            }
        }

        def locationType = LocationType.findById(Constants.WAREHOUSE_LOCATION_TYPE_ID)
        def locations = Location.findAllWhere(locationType: locationType)
        def categories = Category.list()

        [inventorySnapshots: inventorySnapshots, locations: locations, categories: categories, location: location, category: category]

    }


}