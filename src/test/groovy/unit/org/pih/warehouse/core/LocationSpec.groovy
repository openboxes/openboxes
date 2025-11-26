/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package unit.org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode

@Unroll
class LocationSpec extends Specification implements DomainUnitTest<Location> {

    void 'supports should return false if the location has no supported activities'() {
        expect:
        assert domain.supports(ActivityCode.MANAGE_INVENTORY) == false
    }

    void 'supports should return true if the location supports the activity'() {
        given:
        domain.addToSupportedActivities(ActivityCode.MANAGE_INVENTORY.id)

        expect:
        assert domain.supports(ActivityCode.MANAGE_INVENTORY) == true
    }

    void 'supports should return false if the location type has the activity but the domain override does not'(){
        given:
        domain.addToSupportedActivities('SOMETHING_ELSE')
        domain.locationType = new LocationType(supportedActivities: [ActivityCode.MANAGE_INVENTORY.id])

        expect:
        assert domain.supports(ActivityCode.MANAGE_INVENTORY) == false
    }

    void 'supports should return true if the location type has the activity and the domain has none'(){
        given:
        domain.locationType = new LocationType(supportedActivities: [ActivityCode.MANAGE_INVENTORY.id])

        expect:
        assert domain.supports(ActivityCode.MANAGE_INVENTORY) == true
    }

    void 'supportsAny should return true if the location has at least one of the activities'() {
        given:
        domain.addToSupportedActivities(ActivityCode.MANAGE_INVENTORY.id)

        expect:
        assert domain.supportsAny(ActivityCode.RECEIVE_STOCK, ActivityCode.MANAGE_INVENTORY) == true
    }

    void 'supportsAll should return false if the location has some but not all of the activities'() {
        given:
        domain.addToSupportedActivities(ActivityCode.MANAGE_INVENTORY.id)

        expect:
        assert domain.supportsAll(ActivityCode.RECEIVE_STOCK, ActivityCode.MANAGE_INVENTORY) == false
    }

    void 'supportsAll should return true if the location has all of the activities'() {
        given:
        domain.addToSupportedActivities(ActivityCode.MANAGE_INVENTORY.id)
        domain.addToSupportedActivities(ActivityCode.RECEIVE_STOCK.id)

        expect:
        assert domain.supportsAll(ActivityCode.RECEIVE_STOCK, ActivityCode.MANAGE_INVENTORY) == true
    }

    void 'validate should succeed when a name already exists but the location has a different parent'() {
        given: 'a location with a parent location'
        Location parent = new Location(name: "parent").save(validate: false)
        new Location(name: "name", parentLocation: parent).save(validate: false)

        and: 'another location with the same name but a different parent'
        domain.name = "name"
        domain.parentLocation = null

        expect:
        assert domain.validate(['name'])
    }

    @Ignore('this should fail but it does not')
    void 'validate should fail when a name already exists for another location with the same parent'() {
        given: 'a location with a parent location'
        Location parent = new Location(name: "parent").save(validate: false)
        new Location(name: "name", parentLocation: parent).save(validate: false)

        and: 'another location with the same name and the same parent'
        domain.name = "name"
        domain.parentLocation = parent

        expect:
        assert !domain.validate(['name'])  // Why does this not throw validation errors??
        assert domain.errors['name'] == 'validator.unique'
    }

    void 'validate should return true for a valid location'() {
        given:
        Location location = new Location(
                name: "Default location",
                locationType: new LocationType(name: "Depot", description: "Depot"),
        )

        expect:
        assert location.validate()
    }

    void 'isWardOrPharmacy should return true for wards and dispensaries'() {
        given:
        LocationType type = new LocationType(locationTypeCode: locationType, description: description)
        Location location = new Location(locationType: type)

        expect:
        assert location.isWardOrPharmacy() == expectedResult

        where:
        locationType                | description || expectedResult
        LocationTypeCode.WARD       | null        || true
        null                        | 'Ward'      || true
        LocationTypeCode.DISPENSARY | null        || true
        null                        | 'Pharmacy'  || true
        LocationTypeCode.SUPPLIER   | null        || false
    }

    void 'isDepotWardOrPharmacy should return true for depots, wards and dispensaries'() {
        given:
        LocationType type = new LocationType(locationTypeCode: locationType, description: description)
        Location location = new Location(locationType: type)

        expect:
        assert location.isDepotWardOrPharmacy() == expectedResult

        where:
        locationType                | description || expectedResult
        LocationTypeCode.DEPOT      | null        || true
        null                        | 'Depot'     || true
        LocationTypeCode.WARD       | null        || true
        null                        | 'Ward'      || true
        LocationTypeCode.DISPENSARY | null        || true
        null                        | 'Pharmacy'  || true
        LocationTypeCode.SUPPLIER   | null        || false
    }

    void 'sort should sort by sortOrder then by name'() {
        given:
        Location location1 = new Location(sortOrder: 0, name: 'zzz')
        Location location2 = new Location(sortOrder: 1, name: 'aaa')
        Location location3 = new Location(sortOrder: 1, name: 'bbb')

        List<Location> locations = [
                location3,
                location1,
                location2,
        ]

        expect:
        assert locations.sort() == [
                location1,
                location2,
                location3,
        ]
    }
}
