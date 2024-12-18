/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.util.Holders
import org.hibernate.sql.JoinType
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventorySnapshotEvent
import org.pih.warehouse.inventory.RefreshProductAvailabilityEvent
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

/**
 * A location can be a customer, warehouse, or supplier.
 */
class Location implements Comparable<Location>, java.io.Serializable {

    def publishPersistenceEvent() {
        Holders.grailsApplication.mainContext.publishEvent(new InventorySnapshotEvent(this))
        Holders.grailsApplication.mainContext.publishEvent(new RefreshProductAvailabilityEvent(this))
    }

    def afterInsert() {
        publishPersistenceEvent()
    }
    def afterUpdate() {
        publishPersistenceEvent()
    }
    def afterDelete() {
        publishPersistenceEvent()
    }


    String id
    String name
    String description
    String locationNumber

    byte[] logo                // logo
    Address address
    String fgColor = "000000"
    String bgColor = "FFFFFF"

    Location parentLocation
    LocationType locationType
    LocationGroup locationGroup
    Organization organization
    Location zone

    User manager                                // the person in charge of the warehouse
    Inventory inventory                            // each warehouse has a single inventory

    // indicates whether this warehouse is being managed on the locally deployed system
    Boolean active = Boolean.TRUE
    // indicates whether this warehouse is currently active
    Integer sortOrder

    Date dateCreated
    Date lastUpdated

    static belongsTo = [parentLocation: Location, organization: Organization]
    static hasMany = [locations: Location, supportedActivities: String, employees: User]
    static mappedBy = [locations: "parentLocation"]

    static constraints = {
        name(nullable: false, blank: false, maxSize: 255, validator: { String name, Location obj ->
            Location.withNewSession {
                List<Location> otherLocations
                if (obj.parentLocation) {
                    otherLocations = Location.findAllByNameAndParentLocation(name, obj.parentLocation)
                } else {
                    otherLocations = Location.findAllByNameAndParentLocationIsNull(name)
                }

                // Exclude edited location from otherLocations (since it is validated withNewSession)
                if (obj.id) {
                    otherLocations = otherLocations?.findAll { it.id != obj.id}
                }

                return otherLocations?.size() > 0 ? ['validator.unique'] : true
            }
        })
        description(nullable: true)
        address(nullable: true)
        organization(nullable: true, validator: { organization, Location obj ->
            def locationTypeCodes = [LocationTypeCode.DEPOT, LocationTypeCode.SUPPLIER]
            if (obj?.locationType?.locationTypeCode in locationTypeCodes && !organization) {
                return ['validator.required', locationTypeCodes]
            }
        })
        locationType(nullable: false)
        locationNumber(nullable: true, unique: true)
        locationGroup(nullable: true)
        parentLocation(nullable: true)
        zone(nullable: true)
        bgColor(nullable: true, validator: { bgColor, obj ->
            def fgColor = obj.properties['fgColor']
            if (fgColor == null) return true
            bgColor != fgColor ? true : ['invalid.matchingcolor']
        })
        fgColor(nullable: true)
        logo(nullable: true, maxSize: 10485760) // 10 MBs
        manager(nullable: true)
        inventory(nullable: true)
        active(nullable: false)
        dateCreated(display: false)
        lastUpdated(display: false)
        sortOrder(nullable: true)
        supportedActivities(validator: { Set<String> activities, Location location ->
            // Don't allow having NONE supported activity in combination with any other supported activity
            if (activities?.contains(ActivityCode.NONE.id) && activities?.size() > 1) {
                return ['invalid.supportedActivities']
            }
            return true
        })
    }

    static mapping = {
        id generator: 'uuid'
        cache true
    }

    static transients = ["managedLocally", "status", "approvalRequired"]

    String toString() { return this.name }

    /**
     * Compares location by sort order and name.
     * @param location
     * @return
     */
    int compareTo(Location location) {
        return sortOrder <=> location?.sortOrder ?: name <=> location?.name
    }


    Boolean supportsAll(ActivityCode[] activityCodes) {
        activityCodes.every { supports(it) }
    }

    Boolean supportsAny(ActivityCode[] activityCodes) {
        activityCodes.any { supports(it) }
    }

    /**
     * Indicates whether the location supports the given activity.
     *
     * @param activity the given activity
     * @return true if the activity is supported, false otherwise
     */
    Boolean supports(ActivityCode activity) {
        return supports(activity.id)
    }

    /**
     * Indicates whether the location supports the given activity.
     *
     * @param activity the given activity id
     * @return true if the activity is supported, false otherwise
     */
    Boolean supports(String activity) {
        boolean supportsActivity = false
        if (supportedActivities) {
            supportsActivity = supportedActivities?.any { a -> activity == a.toString() }
        } else {
            supportsActivity = locationType?.supports(activity)
        }
        return supportsActivity

    }

    Boolean isManagedLocally() {
        return supports(ActivityCode.MANAGE_INVENTORY)
    }

    /**
     * Indicates whether this location requires outbound quantity validation.
     *
     * @return
     */
    Boolean requiresOutboundQuantityValidation() {
        return active && managedLocally && isWarehouse()
    }

    /**
     * @return true if location is a warehouse (depot)
     *
     * @deprecated use{@link #isDepot()} instead.
     */
    @Deprecated
    Boolean isWarehouse() {
        return locationType.locationTypeCode == LocationTypeCode.DEPOT ||
                // FIXME Keep for backwards compatibility or until we migrate all locations
                supports(ActivityCode.MANAGE_INVENTORY)
    }


    /**
     * @return true if location is a ward or pharmacy
     */
    @Deprecated
    Boolean isWardOrPharmacy() {
        return (locationType.locationTypeCode in [LocationTypeCode.DISPENSARY, LocationTypeCode.WARD] ||
                // FIXME Keep for backwards compatibility or until we migrate all locations
                locationType.description in ["Pharmacy", "Ward"])
    }

    /**
     * @return true if location is a depot, ward, or pharmacy
     */
    @Deprecated
    Boolean isDepotWardOrPharmacy() {
        return (locationType.locationTypeCode in [LocationTypeCode.DEPOT, LocationTypeCode.DISPENSARY, LocationTypeCode.WARD] ||
                // FIXME Keep for backwards compatibility or until we migrate all locations
                locationType.description in ["Depot", "Pharmacy", "Ward"])
    }

    Boolean isDepot() {
        return locationType.locationTypeCode == LocationTypeCode.DEPOT
    }

    Boolean isWard() {
        return locationType.locationTypeCode == LocationTypeCode.WARD
    }

    Boolean isDispensary() {
        return locationType.locationTypeCode == LocationTypeCode.DISPENSARY
    }

    Boolean isBinLocation() {
        return locationType.locationTypeCode == LocationTypeCode.BIN_LOCATION
    }

    Boolean isSupplier() {
        return locationType.locationTypeCode == LocationTypeCode.SUPPLIER
    }

    Boolean isDonor() {
        return locationType.locationTypeCode == LocationTypeCode.DONOR
    }

    Boolean isVirtual() {
        return locationType.locationTypeCode == LocationTypeCode.VIRTUAL
    }

    Boolean hasBinLocationSupport() {
        ActivityCode[] requiredActivities = [ActivityCode.PICK_STOCK, ActivityCode.PUTAWAY_STOCK]
        return supportsAny(requiredActivities)
    }

    def countBinLocations = { Location location ->
        Location.createCriteria().count {
            eq("parentLocation", location)
            locationType {
                eq("locationTypeCode", LocationTypeCode.BIN_LOCATION)
            }
        }
    }

    Boolean isInternalLocation() {
        return locationType?.isInternalLocation()
    }

    Boolean isFacilityLocation() {
        return locationType?.isFacilityLocation()
    }

    Boolean isZoneLocation() {
        return locationType?.isZone()
    }

    /**
     * @return all physical locations
     */
    @Deprecated
    static AllDepotWardAndPharmacy() {
        Location.list().findAll { it.isDepotWardOrPharmacy() }.sort { it.name }
    }

    Location getDefaultLocation() {
        return null
    }

    /**
     * Find any internal location with the given name.
     * @param name
     * @return
     */
    Location getInternalLocation(String name) {
        if (name?.equalsIgnoreCase(Constants.DEFAULT_BIN_LOCATION_NAME)) {
            return defaultLocation
        }

        // FIXME If location.name is not unique then we need to do some error handling
        return locations.find { it.name.equalsIgnoreCase(name) }
    }

    List<Location> getInternalLocations() {
        return locations?.toList()?.findAll { it.isInternalLocation() }
    }

    List<Location> getInternalLocationsByZone(Location zone) {
        return locations?.toList()?.findAll { it.isInternalLocation() && it.zone?.id == zone?.id }
    }

    /**
     * Gets all bin locations for the given location.
     *
     * @return a sorted list of bin locations
     */
    List<Location> getBinLocations() {
        return getInternalLocations([LocationTypeCode.BIN_LOCATION])
    }

    /**
     * Gets all bin locations for the given location.
     *
     * @return a sorted list of bin locations
     */
    List<Location> getInternalLocations(List<LocationTypeCode> locationTypeCodes) {

        List<Location> internalLocations = locations?.toList()

        // Filter by given location type codes
        if (locationTypeCodes) {
            internalLocations?.findAll { it.locationType?.locationTypeCode in locationTypeCodes }
        }
        internalLocations = internalLocations?.sort { a, b -> a.sortOrder <=> b.sortOrder ?: a.name <=> b.name }
        return internalLocations
    }

    Boolean isAccountingRequired() {
        return Holders.config.openboxes.accounting.enabled && supports(ActivityCode.REQUIRE_ACCOUNTING)
    }

    Boolean isOnHold() {
        return supports(ActivityCode.HOLD_STOCK)
    }

    Boolean isPickable() {
        return !onHold
    }

    Boolean isDownstreamConsumer() {
        return !supports(ActivityCode.MANAGE_INVENTORY) && supports(ActivityCode.SUBMIT_REQUEST)
    }

    LocationStatus getStatus() {
        if (organization) {
            return (active && organization.active) ? LocationStatus.ENABLED : LocationStatus.DISABLED
        }
        return active ? LocationStatus.ENABLED : LocationStatus.DISABLED
    }

    Boolean isApprovalRequired() {
        return supports(ActivityCode.APPROVE_REQUEST)
    }

    Boolean requiresMobilPicking() {
        return supports(ActivityCode.REQUIRE_MOBILE_PICKING)
    }

    static List<Location> listNonInternalLocations() {
        return createCriteria().list {
            createAlias("locationType", "locationType", JoinType.LEFT_OUTER_JOIN)
            and {
                eq('active', true)
                not {
                    'in'('locationType.locationTypeCode', LocationTypeCode.listInternalTypeCodes())
                }
            }
        }
    }

    Map toBaseJson() {
        return [
                id: id,
                name: name,
                locationNumber: locationNumber,
                active: active,
                locationType: locationType,
                locationTypeCode: locationType?.locationTypeCode?.name(),
        ]
    }

    Map toJson() {
        return [
                id                         : id,
                name                       : name,
                description                : description,
                locationNumber             : locationNumber,
                locationGroup              : locationGroup,
                parentLocation             : parentLocation,
                locationType               : locationType,
                sortOrder                  : sortOrder,
                hasBinLocationSupport      : hasBinLocationSupport(),
                hasPackingSupport          : supports(ActivityCode.PACK_SHIPMENT),
                hasPartialReceivingSupport : supports(ActivityCode.PARTIAL_RECEIVING),
                hasCentralPurchasingEnabled: supports(ActivityCode.ENABLE_CENTRAL_PURCHASING),
                organizationName           : organization?.name,
                organizationCode           : organization?.code,
                backgroundColor            : bgColor,
                zoneName                   : zone?.name,
                zoneId                     : zone?.id,
                active                     : active,
                organization               : organization,
                manager                    : manager,
                address                    : address,
                supportedActivities        : supportedActivities ?: locationType?.supportedActivities,
        ]
    }

    Map toJson(locationTypeCode) {
        Map json = toBaseJson()
        switch (locationTypeCode) {
            case LocationTypeCode.INTERNAL:
            case LocationTypeCode.BIN_LOCATION:
               json += [
                        zoneId: zone?.id,
                        zoneName: zone?.name,
                        active: active
                ]
        }
        return json
    }

    static Map toJson(Location location) {
        return location ? location.toJson(location?.locationType?.locationTypeCode) : null
    }

    static PROPERTIES = [
            "id"              : "id",
            "name"            : "name",
            "active"          : "active",
            "locationNumber"  : "locationNumber",
            "locationType"    : "locationType.name",
            "locationGroup"   : "locationGroup.name",
            "parentLocation"  : "parentLocation.name",
            "organization"    : "organization.name",
            "streetAddress"   : "address.address",
            "streetAddress2"  : "address.address2",
            "city"            : "address.city",
            "stateOrProvince" : "address.stateOrProvince",
            "postalCode"      : "address.postalCode",
            "country"         : "address.country",
            "description"     : "address.description",
    ]
}
