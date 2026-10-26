// Companion spec: extra features for upstream's LocationSpec (the upstream file stays authoritative).
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.testing.gorm.DomainUnitTest
import grails.util.Holders
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode

@Unroll
class LocationMoreSpec extends Specification implements DomainUnitTest<Location> {

    private static Location location(String name, LocationTypeCode locationTypeCode, Map args = [:]) {
        Location location = new Location([name: name, locationType: new LocationType(name: locationTypeCode?.name(), locationTypeCode: locationTypeCode)] + args)
        return location
    }

    void 'location type predicates should match the location type code #locationTypeCode'() {
        given:
        Location location = location('location', locationTypeCode)

        expect:
        assert [
                depot   : location.isDepot(),
                ward    : location.isWard(),
                dispensary: location.isDispensary(),
                bin     : location.isBinLocation(),
                supplier: location.isSupplier(),
                donor   : location.isDonor(),
                virtual : location.isVirtual(),
        ].findAll { it.value }.keySet() as List == expected

        where:
        locationTypeCode              || expected
        LocationTypeCode.DEPOT        || ['depot']
        LocationTypeCode.WARD         || ['ward']
        LocationTypeCode.DISPENSARY   || ['dispensary']
        LocationTypeCode.BIN_LOCATION || ['bin']
        LocationTypeCode.SUPPLIER     || ['supplier']
        LocationTypeCode.DONOR        || ['donor']
        LocationTypeCode.VIRTUAL      || ['virtual']
        LocationTypeCode.CONSUMER     || []
    }

    void 'internal, facility and zone predicates should be derived from the location type code #locationTypeCode'() {
        given:
        Location location = location('location', locationTypeCode)

        expect:
        assert location.isInternalLocation() == internal
        assert location.isFacilityLocation() == facility
        assert location.isZoneLocation() == zone

        where:
        locationTypeCode              || internal | facility | zone
        LocationTypeCode.BIN_LOCATION || true     | false    | false
        LocationTypeCode.INTERNAL     || true     | false    | false
        LocationTypeCode.DEPOT        || false    | true     | false
        LocationTypeCode.WARD         || false    | true     | false
        LocationTypeCode.ZONE         || false    | false    | true
        LocationTypeCode.SUPPLIER     || false    | false    | false
    }

    void 'internal, facility and zone predicates should return null when there is no location type'() {
        given:
        Location location = new Location(name: 'location')

        expect:
        assert location.isInternalLocation() == null
        assert location.isFacilityLocation() == null
        assert location.isZoneLocation() == null
    }

    void 'isWarehouse should be true for depots and for locations that manage inventory'() {
        given:
        Location location = location('location', locationTypeCode)
        activities.each { location.addToSupportedActivities(it.id) }

        expect:
        assert location.isWarehouse() == expected

        where:
        locationTypeCode          | activities                        || expected
        LocationTypeCode.DEPOT    | []                                || true
        LocationTypeCode.WARD     | [ActivityCode.MANAGE_INVENTORY]   || true
        LocationTypeCode.WARD     | [ActivityCode.RECEIVE_STOCK]      || false
    }

    void 'requiresOutboundQuantityValidation should require an active warehouse that manages inventory'() {
        given:
        Location location = location('location', LocationTypeCode.DEPOT, [active: active])
        activities.each { location.addToSupportedActivities(it.id) }

        expect:
        assert location.isManagedLocally() == managedLocally
        assert location.requiresOutboundQuantityValidation() == expected

        where:
        active | activities                      || managedLocally | expected
        true   | [ActivityCode.MANAGE_INVENTORY] || true           | true
        false  | [ActivityCode.MANAGE_INVENTORY] || true           | false
        true   | [ActivityCode.RECEIVE_STOCK]    || false          | false
    }

    void 'activity based predicates should reflect the supported activities #activities'() {
        given:
        Location location = location('location', LocationTypeCode.DEPOT)
        activities.each { location.addToSupportedActivities(it.id) }

        expect:
        assert location.hasBinLocationSupport() == binLocationSupport
        assert location.isOnHold() == onHold
        assert location.isPickable() == !onHold
        assert location.isDownstreamConsumer() == downstreamConsumer
        assert location.isApprovalRequired() == approvalRequired

        where:
        activities                                                     || binLocationSupport | onHold | downstreamConsumer | approvalRequired
        [ActivityCode.NONE]                                            || false              | false  | false              | false
        [ActivityCode.PICK_STOCK]                                      || true               | false  | false              | false
        [ActivityCode.PUTAWAY_STOCK]                                   || true               | false  | false              | false
        [ActivityCode.HOLD_STOCK]                                      || false              | true   | false              | false
        [ActivityCode.SUBMIT_REQUEST]                                  || false              | false  | true               | false
        [ActivityCode.SUBMIT_REQUEST, ActivityCode.MANAGE_INVENTORY]   || false              | false  | false              | false
        [ActivityCode.APPROVE_REQUEST]                                 || false              | false  | false              | true
    }

    void 'isAccountingRequired should require the location to support accounting when accounting is enabled'() {
        given:
        Location location = location('location', LocationTypeCode.DEPOT)
        activities.each { location.addToSupportedActivities(it.id) }

        and: 'accounting on, as application.yml ships it'
        Object originalAccounting = Holders.config.openboxes.accounting.enabled
        Holders.config.openboxes.accounting.enabled = true

        expect:
        assert location.isAccountingRequired() == expected

        cleanup:
        Holders.config.openboxes.accounting.enabled = originalAccounting

        where:
        activities                          || expected
        [ActivityCode.REQUIRE_ACCOUNTING]   || true
        [ActivityCode.MANAGE_INVENTORY]     || false
    }

    void 'getStatus should return #expected for an active: #active location of an organization with active: #organizationActive'() {
        given:
        Location location = location('location', LocationTypeCode.DEPOT, [active: active])
        location.organization = organizationActive == null ? null : new Organization(name: 'org', active: organizationActive)

        expect:
        assert location.status == expected

        where:
        active | organizationActive || expected
        true   | null               || LocationStatus.ENABLED
        false  | null               || LocationStatus.DISABLED
        true   | true               || LocationStatus.ENABLED
        true   | false              || LocationStatus.DISABLED
        false  | true               || LocationStatus.DISABLED
    }

    void 'getInternalLocation should find a child location by name ignoring case'() {
        given:
        Location depot = location('depot', LocationTypeCode.DEPOT)
        Location bin = location('A-01', LocationTypeCode.BIN_LOCATION)
        depot.locations = [bin] as Set

        expect:
        assert depot.getInternalLocation('a-01').is(bin)
        assert depot.getInternalLocation('B-01') == null
        assert depot.getInternalLocation(Constants.DEFAULT_BIN_LOCATION_NAME) == null
        assert depot.defaultLocation == null
    }

    void 'getInternalLocations should only return the internal child locations'() {
        given:
        Location depot = location('depot', LocationTypeCode.DEPOT)
        Location zoneA = location('zone A', LocationTypeCode.ZONE)
        zoneA.id = 'zone-a'
        Location zoneB = location('zone B', LocationTypeCode.ZONE)
        zoneB.id = 'zone-b'
        Location binInZoneA = location('bin A', LocationTypeCode.BIN_LOCATION, [zone: zoneA])
        Location internalInZoneB = location('internal B', LocationTypeCode.INTERNAL, [zone: zoneB])
        Location binWithoutZone = location('bin', LocationTypeCode.BIN_LOCATION)
        Location ward = location('ward', LocationTypeCode.WARD)
        depot.locations = [zoneA, zoneB, binInZoneA, internalInZoneB, binWithoutZone, ward] as Set

        expect:
        assert depot.internalLocations as Set == [binInZoneA, internalInZoneB, binWithoutZone] as Set
        assert depot.getInternalLocationsByZone(zoneA) == [binInZoneA]
        assert depot.getInternalLocationsByZone(zoneB) == [internalInZoneB]
        assert depot.getInternalLocationsByZone(null) == [binWithoutZone]
    }

    void 'getInternalLocations without location type codes should sort the child locations by sort order and then by name'() {
        given:
        Location depot = location('depot', LocationTypeCode.DEPOT)
        Location second = location('b', LocationTypeCode.BIN_LOCATION, [sortOrder: 1])
        Location first = location('z', LocationTypeCode.BIN_LOCATION, [sortOrder: 0])
        Location third = location('c', LocationTypeCode.BIN_LOCATION, [sortOrder: 1])
        depot.locations = [third, second, first] as Set

        expect:
        assert depot.getInternalLocations([]) == [first, second, third]
    }

    void 'getInternalLocations should return null when there are no child locations'() {
        given:
        Location depot = location('depot', LocationTypeCode.DEPOT)

        expect:
        assert depot.internalLocations == null
        assert depot.getInternalLocations([LocationTypeCode.BIN_LOCATION]) == null
    }

    void 'validate should reject a background color that matches the foreground color'() {
        given:
        domain.fgColor = 'FFFFFF'
        domain.bgColor = bgColor

        expect:
        assert domain.validate(['bgColor']) == valid
        assert valid || domain.errors.getFieldError('bgColor').codes.any { it.endsWith('invalid.matchingcolor') }

        where:
        bgColor  || valid
        'FFFFFF' || false
        '000000' || true
    }

    void 'validate should require an organization for #locationTypeCode locations: #required'() {
        given:
        domain.locationType = new LocationType(name: 'type', locationTypeCode: locationTypeCode)
        domain.organization = null

        expect:
        assert domain.validate(['organization']) == !required
        assert !required || domain.errors.getFieldError('organization').codes.any { it.endsWith('validator.required') }

        where:
        locationTypeCode          || required
        LocationTypeCode.DEPOT    || true
        LocationTypeCode.SUPPLIER || true
        LocationTypeCode.WARD     || false
    }

    void 'validate should not combine the NONE activity with other supported activities'() {
        given:
        activities.each { domain.addToSupportedActivities(it.id) }

        expect:
        assert domain.validate(['supportedActivities']) == valid
        assert valid || domain.errors.getFieldError('supportedActivities').codes.any { it.endsWith('invalid.supportedActivities') }

        where:
        activities                                           || valid
        [ActivityCode.NONE]                                  || true
        [ActivityCode.NONE, ActivityCode.MANAGE_INVENTORY]   || false
        [ActivityCode.MANAGE_INVENTORY]                      || true
    }

    void 'toJson should add the zone to the base json of internal and bin locations only'() {
        given:
        Location zone = location('zone', LocationTypeCode.ZONE)
        zone.id = 'zone-1'
        Location location = location('location', locationTypeCode, [locationNumber: 'L1', zone: zone])
        location.id = 'location-1'

        when:
        Map json = Location.toJson(location)

        then:
        assert json.id == 'location-1'
        assert json.name == 'location'
        assert json.locationNumber == 'L1'
        assert json.active == true
        assert json.locationTypeCode == locationTypeCode.name()
        assert json.containsKey('zoneId') == hasZone
        assert !hasZone || (json.zoneId == 'zone-1' && json.zoneName == 'zone')

        where:
        locationTypeCode              || hasZone
        LocationTypeCode.INTERNAL     || true
        LocationTypeCode.BIN_LOCATION || true
        LocationTypeCode.DEPOT        || false
    }

    void 'toJson should return null for a missing location'() {
        expect:
        assert Location.toJson((Location) null) == null
    }

    void 'toJson should describe the supported features and fall back to the activities of the location type'() {
        given:
        LocationType depotType = new LocationType(name: 'Depot', locationTypeCode: LocationTypeCode.DEPOT,
                supportedActivities: [ActivityCode.PICK_STOCK.id, ActivityCode.PACK_SHIPMENT.id])
        Organization organization = new Organization(code: 'ORG', name: 'Organization')
        Location location = new Location(name: 'depot', locationType: depotType, organization: organization, bgColor: '123456')

        when:
        Map json = location.toJson()

        then:
        assert json.hasBinLocationSupport == true
        assert json.hasPackingSupport == true
        assert json.hasPartialReceivingSupport == false
        assert json.hasCentralPurchasingEnabled == false
        assert json.organizationName == 'Organization'
        assert json.organizationCode == 'ORG'
        assert json.backgroundColor == '123456'
        assert json.zoneName == null
        assert json.supportedActivities as Set == [ActivityCode.PICK_STOCK.id, ActivityCode.PACK_SHIPMENT.id] as Set
    }
}
