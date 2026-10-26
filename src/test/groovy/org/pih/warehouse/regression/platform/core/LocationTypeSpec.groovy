package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class LocationTypeSpec extends Specification implements DomainUnitTest<LocationType> {

    void 'supports should be true only for the activities of the location type'() {
        given:
        domain.supportedActivities = [ActivityCode.MANAGE_INVENTORY.id, ActivityCode.RECEIVE_STOCK.id] as Set

        expect:
        domain.supports(ActivityCode.MANAGE_INVENTORY)
        domain.supports(ActivityCode.RECEIVE_STOCK.id)
        !domain.supports(ActivityCode.PICK_STOCK)
        !domain.supports('UNKNOWN')
    }

    void 'supports should be false when the location type has no supported activities'() {
        expect:
        !domain.supports(ActivityCode.MANAGE_INVENTORY)
        !domain.supports(ActivityCode.NONE)
    }

    void 'validate should be #valid for supported activities #activities'() {
        given:
        domain.name = 'Depot'
        domain.locationTypeCode = LocationTypeCode.DEPOT
        domain.supportedActivities = activities as Set

        expect:
        domain.validate() == valid
        domain.errors.getFieldError('supportedActivities')?.code == errorCode

        where:
        activities                                                     || valid | errorCode
        null                                                           || true  | null
        []                                                             || true  | null
        [ActivityCode.NONE.id]                                         || true  | null
        [ActivityCode.MANAGE_INVENTORY.id, ActivityCode.PICK_STOCK.id] || true  | null
        [ActivityCode.NONE.id, ActivityCode.MANAGE_INVENTORY.id]       || false | 'invalid.supportedActivities'
    }

    void 'validate should reject a location type without #field'() {
        given:
        domain.name = 'Depot'
        domain.locationTypeCode = LocationTypeCode.DEPOT
        domain[field] = null

        expect:
        !domain.validate()
        domain.errors.getFieldError(field).code == 'nullable'

        where:
        field << ['name', 'locationTypeCode']
    }

    void 'validate should reject a name longer than 255 characters'() {
        given:
        domain.name = 'x' * 256
        domain.locationTypeCode = LocationTypeCode.DEPOT

        expect:
        !domain.validate()
        domain.errors.getFieldError('name').code == 'maxSize.exceeded'
    }

    void 'location type predicates should match the location type code #code'() {
        given:
        domain.locationTypeCode = code

        expect:
        domain.isDepot() == (code == LocationTypeCode.DEPOT)
        domain.isWard() == (code == LocationTypeCode.WARD)
        domain.isDispensary() == (code == LocationTypeCode.DISPENSARY)
        domain.isBinLocation() == (code == LocationTypeCode.BIN_LOCATION)
        domain.isSupplier() == (code == LocationTypeCode.SUPPLIER)
        domain.isDonor() == (code == LocationTypeCode.DONOR)
        domain.isVirtual() == (code == LocationTypeCode.VIRTUAL)
        domain.isWardOrPharmacy() == wardOrPharmacy
        domain.isDepotWardOrPharmacy() == depotWardOrPharmacy
        domain.isInternalLocation() == internal
        domain.isFacilityLocation() == facility
        domain.isZone() == zone

        where:
        code                          || wardOrPharmacy | depotWardOrPharmacy | internal | facility | zone
        LocationTypeCode.DEFAULT      || false          | false               | false    | false    | false
        LocationTypeCode.DEPOT        || false          | true                | false    | true     | false
        LocationTypeCode.ZONE         || false          | false               | false    | false    | true
        LocationTypeCode.BIN_LOCATION || false          | false               | true     | false    | false
        LocationTypeCode.INTERNAL     || false          | false               | true     | false    | false
        LocationTypeCode.DISPENSARY   || true           | true                | false    | true     | false
        LocationTypeCode.WARD         || true           | true                | false    | true     | false
        LocationTypeCode.SUPPLIER     || false          | false               | false    | false    | false
        LocationTypeCode.DONOR        || false          | false               | false    | false    | false
        LocationTypeCode.CONSUMER     || false          | false               | false    | false    | false
        LocationTypeCode.DISTRIBUTOR  || false          | false               | false    | false    | false
        LocationTypeCode.DISPOSAL     || false          | false               | false    | false    | false
        LocationTypeCode.VIRTUAL      || false          | false               | false    | false    | false
        null                          || false          | false               | false    | false    | false
    }

    void 'compareTo should sort by sort order, then location type code order, then name'() {
        given:
        LocationType binB = new LocationType(name: 'B', sortOrder: 0, locationTypeCode: LocationTypeCode.BIN_LOCATION)
        LocationType binA = new LocationType(name: 'A', sortOrder: 0, locationTypeCode: LocationTypeCode.BIN_LOCATION)
        LocationType depot = new LocationType(name: 'Z', sortOrder: 0, locationTypeCode: LocationTypeCode.DEPOT)
        LocationType first = new LocationType(name: 'Y', sortOrder: -1, locationTypeCode: LocationTypeCode.VIRTUAL)
        LocationType last = new LocationType(name: 'A', sortOrder: 5, locationTypeCode: LocationTypeCode.DEPOT)

        expect:
        [last, binB, depot, binA, first].sort()*.name == ['Y', 'Z', 'A', 'B', 'A']
        binA.compareTo(new LocationType(name: 'A', sortOrder: 0, locationTypeCode: LocationTypeCode.BIN_LOCATION)) == 0
    }

    void 'toString should return the name'() {
        given:
        domain.name = 'Bin location'

        expect:
        domain.toString() == 'Bin location'
    }

    void 'getInternalLocationTypes should return the bin location and internal types in sort order'() {
        given:
        save('Depot', LocationTypeCode.DEPOT, 0)
        save('Internal', LocationTypeCode.INTERNAL, 2)
        save('Bin', LocationTypeCode.BIN_LOCATION, 2)
        save('Receiving', LocationTypeCode.INTERNAL, 1)
        save('Zone', LocationTypeCode.ZONE, 0)

        expect:
        LocationType.internalLocationTypes*.name == ['Receiving', 'Bin', 'Internal']
        LocationType.getInternalLocationTypes(null)*.name == ['Receiving', 'Bin', 'Internal']
        LocationType.defaultInternalLocationType.name == 'Receiving'
    }

    void 'getZoneLocationTypes should return the zone types'() {
        given:
        save('Depot', LocationTypeCode.DEPOT, 0)
        save('Bin', LocationTypeCode.BIN_LOCATION, 0)
        save('Zone', LocationTypeCode.ZONE, 0)

        expect:
        LocationType.zoneLocationTypes*.name == ['Zone']
        LocationType.defaultZoneLocationType.name == 'Zone'
    }

    void 'default internal and zone location types should be null when none exist'() {
        given:
        save('Depot', LocationTypeCode.DEPOT, 0)

        expect:
        LocationType.internalLocationTypes == []
        LocationType.defaultInternalLocationType == null
        LocationType.zoneLocationTypes == []
        LocationType.defaultZoneLocationType == null
    }

    private static LocationType save(String name, LocationTypeCode code, Integer sortOrder) {
        return new LocationType(name: name, locationTypeCode: code, sortOrder: sortOrder).save(failOnError: true, flush: true)
    }
}
