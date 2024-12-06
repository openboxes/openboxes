/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package unit.org.pih.warehouse.core

import grails.gorm.PagedResultList
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Shared
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationController
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.inventory.InventoryService

/**
 * Test specification for LocationController methods.
 */
class LocationControllerSpec extends Specification implements ControllerUnitTest<LocationController>, DataTest {

    @Shared
    LocationType depotLocationType

    @Shared
    LocationGroup bostonGroup

    @Shared
    Location bostonDepot

    @Shared
    Location miamiDepot

    @Shared
    Organization mainOrg

    void setupSpec() {
        mockDomains(LocationType, LocationGroup, Organization, Location)
    }

    void setup() {
        controller.inventoryService = Stub(InventoryService)
        controller.locationService = Stub(LocationService)

        depotLocationType = createLocationType("Depot")

        bostonGroup = createLocationGroup("Boston")

        mainOrg = createOrganization("MAIN", "Main Org")

        bostonDepot = createLocation("Boston", depotLocationType, bostonGroup, mainOrg)
        miamiDepot = createLocation("Miami", depotLocationType, bostonGroup, mainOrg)
    }

    void "expect index to redirect to list page"() {
        when:
        controller.index()

        then:
        assert response.redirectedUrl == '/location/list'
    }

    void "when fetching locations with no filter expect some locations can be returned"() {
        given:
        params.put('max', 10)
        params.put('offset', 0)

        and:
        controller.locationService.getLocations(_, _, _, _, params.max, params.offset) >>
                buildStubbedPagedResultList([bostonDepot, miamiDepot])

        when:
        def model = controller.list()

        then:
        assert model.locationInstanceList.size() == 2
        assert model.locationInstanceTotal == 2
    }

    void "when fetching locations by some filter criteria expect some locations can be returned"() {
        given:
        params.put('q', "Bos")
        params.put('locationGroup.id', bostonGroup.id)
        params.put('organization.id', mainOrg.id)
        params.put('locationType.id', depotLocationType.id)
        params.put('max', 10)
        params.put('offset', 0)

        and:
        controller.locationService.getLocations(mainOrg, depotLocationType, bostonGroup, params.q, params.max, params.offset) >>
                buildStubbedPagedResultList([bostonDepot])

        when:
        def model = controller.list()

        then:
        assert model.locationInstanceList.size() == 1
        assert model.locationInstanceTotal == 1
    }

    void "when loading the edit page expect the correct location is returned"() {
        given:
        controller.inventoryService.getLocation(bostonDepot.id) >> bostonDepot

        and:
        params.put('id', bostonDepot.id)

        when:
        def model = controller.edit()

        then:
        assert view == '/location/edit.gsp'
        assert model.locationInstance.name == "Boston"
    }

    PagedResultList buildStubbedPagedResultList(ArrayList expectedList) {
        return new PagedResultList(null).with {
            resultList = expectedList
            totalCount = expectedList.size()
            it
        }
    }

    LocationGroup createLocationGroup(String name) {
        return new LocationGroup(name: name).save(validate: false)
    }

    LocationType createLocationType(String name) {
        return new LocationType(name: name).save(validate: false)
    }

    Organization createOrganization(String code, String name) {
        return new Organization(code: code, name: name).save(validate: false)
    }

    Location createLocation(String name, LocationType locationType, LocationGroup locationGroup, Organization organization) {
        return new Location(
                name: name,
                locationType: locationType,
                locationGroup: locationGroup,
                organization: organization)
                .save(validate: false)
    }
}
