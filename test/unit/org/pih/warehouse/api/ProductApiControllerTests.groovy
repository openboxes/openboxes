/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.test.ControllerUnitTestCase
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class ProductApiControllerTests extends ControllerUnitTestCase {
    def product
    def location

    protected void setUp() {
        super.setUp()

        controller.grailsApplication = new DefaultGrailsApplication()
        controller.grailsApplication.config.openboxes.typeahead.minLength = 3

        product = new Product(id: "1", name: "Product")
        mockDomain(Product, [product])

        LocationType depotType = new LocationType(id: "depot", name: "Depot")
        LocationGroup boston = new LocationGroup(id: "boston", name: "Boston")
        mockDomain(LocationType, [depotType])
        mockDomain(LocationGroup, [boston])

        location = new Location(id: "locationId", name: "Boston", locationType: depotType, locationGroup: boston)
        mockDomain(Location, [location])

        controller.session.warehouse = location

        JSON.registerObjectMarshaller(Location) { Location location ->
            [
                    id                   : location.id,
                    name                 : location.name,
                    description          : location.description,
                    locationNumber       : location.locationNumber,
                    locationGroup        : location.locationGroup,
                    parentLocation       : location.parentLocation,
                    locationType         : location.locationType,
                    sortOrder            : location.sortOrder,
                    hasBinLocationSupport: location.hasBinLocationSupport()
            ]
        }

        JSON.registerObjectMarshaller(Product) { Product product ->
            [
                    id         : product.id,
                    productCode: product.productCode,
                    name       : product.name,
                    description: product.description
            ]
        }
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testDemand() {
        controller.params.id = "1"
        controller.forecastingService = [
                getDemand: { Location l, Product p -> return ["demand"] }
        ]
        //WHEN
        controller.demand()
        //THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        assertEquals(["demand"], jsonResponse.data.demand)
        assertEquals("Boston", jsonResponse.data.location.name)
        assertEquals("Product", jsonResponse.data.product.name)
    }

    void testDemandSummary() {
        // GIVEN
        controller.params.id = "1"
        controller.forecastingService = [
                getDemandSummary: { Location l, Product p -> return ["test"] }
        ]
        //WHEN
        controller.demandSummary()
        //THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        assertEquals(["test"], jsonResponse.data)
    }

    void testList() {
        // GIVEN
        controller.params.name = "Product"
        controller.productService = [
                searchProducts: { String[] terms, List<Category> categories -> return [product] }
        ]
        // WHEN
        controller.list()
        // THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        assertEquals(jsonResponse.data.get(0).name, "Product")
    }
}
