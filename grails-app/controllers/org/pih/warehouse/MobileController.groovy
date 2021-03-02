/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSummary

class MobileController {

    def userService
    def locationService
    def megamenuService

    def index = {

        Location location = Location.get(session.warehouse.id)
        def productCount = ProductSummary.countByLocation(location)
        def productListUrl = g.createLink(controller: "mobile", action: "productList")

        def orderCount = Order.createCriteria().count {
            eq("destination", location)
            eq("orderTypeCode", OrderTypeCode.PURCHASE_ORDER)
        }

        def userCount = User.count()

        [
                data: [
                        [name: "Products", class: "fa fa-cubes", count: productCount, url: g.createLink(controller: "mobile", action: "productList")],
                        [name: "Purchase Orders", class: "fa fa-shopping-cart", count: orderCount, url: g.createLink(controller: "order", action: "list")],
                        [name: "Users", class: "fa fa-user", count: userCount, url: g.createLink(controller: "user", action: "list")]
                ]
        ]
    }

    def login = {

    }

    def menu = {
        Map menuConfig = grailsApplication.config.openboxes.megamenu
        //User user = User.get(session?.user?.id)
        //Location location = Location.get(session.warehouse?.id)
        //List translatedMenu = megamenuService.buildAndTranslateMenu(menuConfig, user, location)
        [menuConfig:menuConfig]
    }

    def chooseLocation = {
        User user = User.get(session.user.id)
        Location warehouse = Location.get(session.warehouse.id)
        render (view: "/mobile/chooseLocation",
            model: [savedLocations: user.warehouse ? [user.warehouse] : null, loginLocationsMap: locationService.getLoginLocationsMap(user, warehouse)])
    }

    def productList = {
        Location location = Location.get(session.warehouse.id)
        def productSummaries = ProductSummary.findAllByLocation(location, [max: 10])

        [productSummaries:productSummaries]
    }

    def productDetails = {
        Product product = Product.get(params.id)
        Location location = Location.get(session.warehouse.id)
        def productSummary = ProductSummary.findByProductAndLocation(product, location)

        [productSummary:productSummary]

    }
}
