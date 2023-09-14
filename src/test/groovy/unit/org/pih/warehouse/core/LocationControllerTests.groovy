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

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationController
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.inventory.InventoryService
import spock.lang.Ignore
import spock.lang.Specification

@TestFor(LocationController)
@Mock([Location, LocationService, InventoryService])
@TestMixin(DomainClassUnitTestMixin)
class LocationControllerTests extends Specification {
	def stubMessager = new Expando()

	void setup() {
		def depot = new LocationType(id: "1", name: "Depot")
		def ward = new LocationType(id: "2", name: "Ward")
		def boston = new LocationGroup(id: "boston", name: "Boston")
		def mirebalais = new LocationGroup(id: "boston", name: "Mirebalais")

		def main = new Organization(id: "MAIN", code: "MAIN", name: "Main Org")
		def sister = new Organization(id: "SIS", code: "SIS", name: "Sister Org")

		// FIXME this is the old GrailsUnitTestCase API
//		mockConfig """
//		openboxes {
//			identifier {
//				organization {
//					minSize = 2
//					maxSize = 4
//				}
//			}
//		}"""


		mockDomain(Location, [
			new Location(id: "1", name: "Boston", locationType: depot, locationGroup: boston, organization: main),
			new Location(id: "2", name: "Miami", locationType: depot, locationGroup: boston, organization: main),
			new Location(id: "3", name: "Mirebalais", locationType: depot, locationGroup: mirebalais, organization: sister),
			new Location(id: "4", name: "Mirebalais > Pediatrics", locationType: ward, locationGroup: mirebalais, organization: sister)
		])
		mockDomain(LocationType, [depot, ward])
		mockDomain(LocationGroup, [boston, mirebalais])
		mockDomain(Organization, [main, sister])

		controller.inventoryService = [
				getLocation: { locationId -> Location.get(locationId) },
				saveLocation: { location -> location }
		]

//		def locationServiceMock = mockFor(LocationService)
//		locationServiceMock.demand.getLocations { organization, locationType, locationGroup, query, max, offset ->
//			if (query=="Bos") {
//				return new PagedResultList([Location.get(1)], 1)
//			}
//			return new PagedResultList(Location.list(), 4)
//		}
//
//		controller.locationService = locationServiceMock.createMock()

		depot = LocationType.get("1")
		assert depot != null
	}

	void "index should redirect to list page"() {
		when:
		controller.index()

		then:
		response.redirectedUrl == '/location/list'
	}

	void "should list all locations"() {
		when:
		def model = controller.list()

		then:
		model.locationInstanceList.size() == 4
		model.locationInstanceTotal == 4
	}

	void "should list locations matching LocationType 1 and query param"() {
		when:
		controller.params.q = "Bos"
		controller.params.locationType = "1"
		def model = controller.list()

		then:
		model.locationInstanceList.size() == 1
		model.locationInstanceTotal == 1
	}

	// Show action redirects to edit action
	@Ignore("Fix this test")
	void test_edit_shouldIncludeLocationInModel() {
		when:
		controller.params.id = "1"
		def model = controller.edit()

		then:
		view == '/location/edit'
		model.locationInstance.name == "Boston"
	}
}
