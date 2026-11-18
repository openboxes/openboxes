package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class LocationServiceSpec extends Specification implements ServiceUnitTest<LocationService>, DataTest {

    static final String SHIPMENT_NUMBER = "SHIP-1"

    LocationType depotType
    LocationType receivingType
    Location oldDestination
    Location newDestination

    void setupSpec() {
        mockDomains(Location, LocationType, Organization)
    }

    void setup() {
        depotType = new LocationType(name: "Depot").save(validate: false, flush: true)
        receivingType = new LocationType(name: "Receiving").save(validate: false, flush: true)
        oldDestination = new Location(name: "Old Depot", locationType: depotType).save(validate: false, flush: true)
        newDestination = new Location(name: "New Depot", locationType: depotType).save(validate: false, flush: true)
    }

    // OBPIH-8105 not fixed at the root: the receiving bin is looked up only under the shipment's current destination,
    // but locationNumber is globally unique. Any bin left under a previous destination (shipments re-destined before
    // the fix, legacy-named bins, or destination changes made outside the stock movement API) makes receiving fail
    // with the 'unique' error.
    void 'findOrCreateInternalLocation should reuse the receiving bin left under a previous destination'() {
        given:
        String binName = service.getReceivingLocationName(SHIPMENT_NUMBER)
        Location strandedBin = new Location(
                name: binName,
                locationNumber: SHIPMENT_NUMBER,
                locationType: receivingType,
                parentLocation: oldDestination,
        ).save(failOnError: true, flush: true)

        when:
        Location result = service.findOrCreateInternalLocation(
                SHIPMENT_NUMBER, SHIPMENT_NUMBER, receivingType, newDestination)

        then:
        noExceptionThrown()
        assert result.id == strandedBin.id
        assert result.parentLocation == newDestination
    }
}
