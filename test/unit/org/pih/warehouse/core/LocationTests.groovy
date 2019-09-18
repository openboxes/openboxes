/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.core

import grails.test.*
import org.junit.Ignore
import org.junit.Test
import org.springframework.context.ApplicationEvent

class LocationTests extends GrailsUnitTestCase {
    Location location1
    Location location2
    Location location3
    Location location4
    Location location5

    protected void setUp() {
        super.setUp()
        def depot = new LocationType(name: "Depot", description: "Depot", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def supplier = new LocationType(name: "Supplier", description: "Supplier")
        def ward = new LocationType(name: "Ward", description: "Ward")

        location1 = new Location(name: "Boston", locationType: depot, supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        location2 = new Location(name: "Miami", locationType: depot)
        location3 = new Location(name: "supplier", locationType: supplier, supportedActivities: [ActivityCode.RECEIVE_STOCK])
        location4 = new Location(name: "ward", locationType: ward, supportedActivities: [ActivityCode.RECEIVE_STOCK])
        location5 = new Location(name: "New York", locationType: depot, supportedActivities: [ActivityCode.PICK_STOCK, ActivityCode.PUTAWAY_STOCK])

        mockDomain(LocationType, [depot, supplier, ward])
        mockDomain(Location, [location1, location2, location3, location4])
        Location.metaClass.publishEvent = { ApplicationEvent event -> }
    }

    void test_supports() {

        assert location1.supports(ActivityCode.MANAGE_INVENTORY)
        assert location2.supports(ActivityCode.MANAGE_INVENTORY)
        assert !location3.supports(ActivityCode.MANAGE_INVENTORY)

        // Test supports any
        assertTrue(location5.supportsAny([ActivityCode.RECEIVE_STOCK, ActivityCode.PUTAWAY_STOCK] as ActivityCode[]))
        assertFalse(location5.supportsAny([ActivityCode.SEND_STOCK, ActivityCode.RECEIVE_STOCK] as ActivityCode[]))

        // Test supports all
        assertTrue(location5.supportsAll([ActivityCode.PICK_STOCK, ActivityCode.PUTAWAY_STOCK] as ActivityCode[]))
        assertFalse(location5.supportsAll([ActivityCode.PICK_STOCK, ActivityCode.PUTAWAY_STOCK, ActivityCode.CROSS_DOCKING] as ActivityCode[]))


    }

    void test_shouldValidateOnNameUniqueConstraintIfSameNameButDifferentParentLocation() {
        def locationType = new LocationType(locationTypeCode: LocationTypeCode.BIN_LOCATION, name: "Bin Location")

        def binLocation1 = new Location(name: "AA-01-01-01", parentLocation: location1, locationType: locationType)
        def binLocation2 = new Location(name: "AA-01-01-02", parentLocation: location1, locationType: locationType)
        def binLocation3 = new Location(name: "AA-01-01-01", parentLocation: location2, locationType: locationType)

        mockDomain(Location, [binLocation1, binLocation2, binLocation3])
        binLocation1.save()
        binLocation2.save()

        assertTrue "Should validate with same name but different parent location", binLocation3.validate()
    }

    void test_shouldNotValidateNameUniqueConstraintIfSameName() {

        def locationType = new LocationType(locationTypeCode: LocationTypeCode.BIN_LOCATION, name: "Bin Location")

        def binLocation1 = new Location(name: "AA-01-01-01", parentLocation: location1, locationType: locationType)
        def binLocation2 = new Location(name: "AA-01-01-02", parentLocation: location1, locationType: locationType)
        def binLocation3 = new Location(name: "AA-01-01-01", parentLocation: location2, locationType: locationType)
        def binLocation4 = new Location(name: "AA-01-01-01", parentLocation: location1, locationType: locationType)

        mockDomain(Location, [binLocation1, binLocation2, binLocation3])
        binLocation1.save()
        binLocation2.save()

        assertFalse "Should not validate with the same name and same parent location", binLocation4.validate()
        assertEquals "unique", binLocation4.errors["name"]
    }


    void test_shouldNotValidateLocationNumberOnUniqueConstraintIfSame() {

        def locationType = new LocationType(locationTypeCode: LocationTypeCode.BIN_LOCATION, name: "Bin Location")

        def binLocation1 = new Location(name: "AA-01-01-01", locationNumber: "My Bin", parentLocation: location1, locationType: locationType)
        def binLocation2 = new Location(name: "AA-01-01-02", locationNumber: "My Bin", parentLocation: location1, locationType: locationType)

        mockDomain(Location, [binLocation1])
        binLocation1.save()

        // Should not validate with the same name and same parent location
        assertFalse binLocation2.validate()
        assertEquals "unique", binLocation2.errors["locationNumber"]
    }

    void test_shouldValidateLocationNumberOnUniqueConstraintIfNull() {

        def locationType = new LocationType(locationTypeCode: LocationTypeCode.BIN_LOCATION, name: "Bin Location")

        def binLocation1 = new Location(name: "AA-01-01-01", locationNumber: null, parentLocation: location1, locationType: locationType)
        def binLocation2 = new Location(name: "AA-01-01-02", locationNumber: null, parentLocation: location1, locationType: locationType)
        def binLocation3 = new Location(name: "AA-01-01-03", locationNumber: null, parentLocation: location1, locationType: locationType)

        mockDomain(Location, [binLocation1, binLocation2])

        // Should validate if location number is null for multiple  and same parent location
        assertTrue binLocation1.validate()
        assertTrue binLocation2.validate()
        assertTrue binLocation3.validate()
    }



    void test_shouldSaveLocation() {
        def location = new Location(name: "Default location", locationType: new LocationType(name: "Depot", description: "Depot"))

        if (!location.validate())
            location.errors.allErrors.each { println it }

        assertTrue location.validate()
        location.save()
        assertFalse location.hasErrors()
    }

    void test_shouldHaveLocationType() {
        def location = new Location()
        assertFalse location.validate()
        println location.errors
        assertEquals "nullable", location.errors["locationType"]
    }

    void test_isWardOrPharmacy() {
        assert location1.isWardOrPharmacy() == false
        assert location1.isDepotWardOrPharmacy() == true
        assert location4.isWardOrPharmacy() == true
        assert location4.isDepotWardOrPharmacy() == true
    }

    void test_AllDepotWardAndPharmacy() {
        def locations = Location.AllDepotWardAndPharmacy()
        assert locations.contains(location1)
        assert locations.contains(location2)
        assert locations.contains(location4)
        assert locations.size() == 3
    }


    @Test
    void compareTo_shouldSortByName() {
        def locations = [location3, location2, location1, location4]
        locations = locations.sort()
        assertEquals locations[0], location1
        assertEquals locations[1], location2
        assertEquals locations[2], location3
        assertEquals locations[3], location4
    }



    @Test
    void compareTo_shouldSortBySortOrderThenByName() {
        def locations = [location3, location2, location1, location4]
        location1.sortOrder = 30
        location2.sortOrder = 30
        location3.sortOrder = 20
        location4.sortOrder = 10

        locations = locations.sort()
        assertEquals locations[0], location4
        assertEquals locations[1], location3
        assertEquals locations[2], location1
        assertEquals locations[3], location2

    }
}
