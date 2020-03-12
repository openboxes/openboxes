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

import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventorySnapshotEvent
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

/**
 * A location can be a customer, warehouse, or supplier.
 */
class Location implements Comparable<Location>, java.io.Serializable {

    def publishPersistenceEvent = {
        publishEvent(new InventorySnapshotEvent(this))
    }

    def afterInsert = publishPersistenceEvent
    def afterUpdate = publishPersistenceEvent
    def afterDelete = publishPersistenceEvent


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

    User manager                                // the person in charge of the warehouse
    Inventory inventory                            // each warehouse has a single inventory
    Boolean local = Boolean.TRUE
    // indicates whether this warehouse is being managed on the locally deployed system
    Boolean active = Boolean.TRUE
    // indicates whether this warehouse is currently active
    Integer sortOrder

    Date dateCreated
    Date lastUpdated

    static belongsTo = [parentLocation: Location]
    static hasMany = [locations: Location, supportedActivities: String, employees: User]

    static constraints = {
        name(nullable: false, blank: false, maxSize: 255, unique: 'parentLocation')
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
        bgColor(nullable: true, validator: { bgColor, obj ->
            def fgColor = obj.properties['fgColor']
            if (fgColor == null) return true
            bgColor != fgColor ? true : ['invalid.matchingcolor']
        })
        fgColor(nullable: true)
        logo(nullable: true, maxSize: 10485760) // 10 MBs
        local(nullable: true)
        manager(nullable: true)
        inventory(nullable: true)
        active(nullable: false)
        dateCreated(display: false)
        lastUpdated(display: false)
        sortOrder(nullable: true)
    }

    static mapping = {
        id generator: 'uuid'
        cache true
    }

    static transients = ["transactions", "events", "shipments", "requests", "orders"]

    List getTransactions() { return Transaction.findAllByDestinationOrSource(this, this) }

    List getEvents() { return Event.findAllByEventLocation(this) }

    List getShipments() { return Shipment.findAllByOriginOrDestination(this, this) }

    List getRequests() { return Requisition.findAllByOriginOrDestination(this, this) }

    List getOrders() { return Order.findAllByOriginOrDestination(this, this) }

    List getUsers() { return User.findAllByWarehouse(this) }

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

    /**
     * Indicates whether this location requires outbound quantity validation.
     *
     * @return
     */
    Boolean requiresOutboundQuantityValidation() {
        return active && local && isWarehouse()
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
        return supportsAny(requiredActivities) && !binLocations?.empty
    }

    /**
     * @return all physical locations
     */
    @Deprecated
    static AllDepotWardAndPharmacy() {
        Location.list().findAll { it.isDepotWardOrPharmacy() }.sort { it.name }
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
        internalLocations = internalLocations.sort { a, b -> a.sortOrder <=> b.sortOrder ?: a.name <=> b.name }
        return internalLocations
    }

}
