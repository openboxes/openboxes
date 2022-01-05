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
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.models.media.Schema as SchemaObject
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventorySnapshotEvent
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

@Schema(description="A location can be a customer, warehouse, supplier, etc.")
class Location implements Comparable<Location>, java.io.Serializable {

    @Hidden
    def publishPersistenceEvent = {
        Holders.grailsApplication.mainContext.publishEvent(new InventorySnapshotEvent(this))
    }

    @Hidden
    def afterInsert = publishPersistenceEvent
    @Hidden
    def afterUpdate = publishPersistenceEvent
    @Hidden
    def afterDelete = publishPersistenceEvent

    String id
    String name
    String description
    String locationNumber

    @Hidden
    byte[] logo
    @Hidden
    Address address
    @Hidden
    String fgColor = "000000"

    @Schema(name="backgroundColor")
    String bgColor = "FFFFFF"

    Location parentLocation
    LocationType locationType
    LocationGroup locationGroup
    @Hidden
    Organization organization

    @Hidden
    @Schema(description="the person in charge of the warehouse")
    User manager

    @Hidden
    @Schema(description="each warehouse has exactly one inventory")
    Inventory inventory

    @Hidden
    @Schema(description=" indicates whether this warehouse is being managed on the locally deployed system")
    Boolean local = Boolean.TRUE

    @Hidden
    @Schema(description="indicates whether this warehouse is currently active")
    Boolean active = Boolean.TRUE

    Integer sortOrder

    @Hidden
    Date dateCreated
    @Hidden
    Date lastUpdated

    static belongsTo = [parentLocation: Location, organization: Organization]
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

    @Hidden
    List getTransactions() { return Transaction.findAllByDestinationOrSource(this, this) }
    @Hidden
    List getEvents() { return Event.findAllByEventLocation(this) }
    @Hidden
    List getShipments() { return Shipment.findAllByOriginOrDestination(this, this) }
    @Hidden
    List getRequests() { return Requisition.findAllByOriginOrDestination(this, this) }
    @Hidden
    List getOrders() { return Order.findAllByOriginOrDestination(this, this) }
    @Hidden
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
    @Hidden
    Boolean isWarehouse() {
        return locationType.locationTypeCode == LocationTypeCode.DEPOT ||
                // FIXME Keep for backwards compatibility or until we migrate all locations
                supports(ActivityCode.MANAGE_INVENTORY)
    }


    /**
     * @return true if location is a ward or pharmacy
     */
    @Hidden
    @Deprecated
    Boolean isWardOrPharmacy() {
        return (locationType.locationTypeCode in [LocationTypeCode.DISPENSARY, LocationTypeCode.WARD] ||
                // FIXME Keep for backwards compatibility or until we migrate all locations
                locationType.description in ["Pharmacy", "Ward"])
    }

    /**
     * @return true if location is a depot, ward, or pharmacy
     */
    @Hidden
    @Deprecated
    Boolean isDepotWardOrPharmacy() {
        return (locationType.locationTypeCode in [LocationTypeCode.DEPOT, LocationTypeCode.DISPENSARY, LocationTypeCode.WARD] ||
                // FIXME Keep for backwards compatibility or until we migrate all locations
                locationType.description in ["Depot", "Pharmacy", "Ward"])
    }

    @Hidden
    Boolean isDepot() {
        return locationType.locationTypeCode == LocationTypeCode.DEPOT
    }

    @Hidden
    Boolean isWard() {
        return locationType.locationTypeCode == LocationTypeCode.WARD
    }

    @Hidden
    Boolean isDispensary() {
        return locationType.locationTypeCode == LocationTypeCode.DISPENSARY
    }

    @Hidden
    Boolean isBinLocation() {
        return locationType.locationTypeCode == LocationTypeCode.BIN_LOCATION
    }

    @Hidden
    Boolean isSupplier() {
        return locationType.locationTypeCode == LocationTypeCode.SUPPLIER
    }

    @Hidden
    Boolean isDonor() {
        return locationType.locationTypeCode == LocationTypeCode.DONOR
    }

    @Hidden
    Boolean isVirtual() {
        return locationType.locationTypeCode == LocationTypeCode.VIRTUAL
    }

    @Schema(description="override")
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
    @Hidden
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

    @Hidden
    Boolean isAccountingRequired() {
        return Holders.config.openboxes.accounting.enabled && supports(ActivityCode.REQUIRE_ACCOUNTING)
    }

    /**
     * Add fields to a Swagger schema object that Swagger itself can't detect.
     *
     * N.B., These fields are all defined in BootStrap.groovy, which, it
     * seems, neither Swagger nor this author can parse correctly. ;-)
     *
     * @param schema a Swagger schema object (*not* a Schema annotation!)
     * @return a Swagger schema object with implicit fields added
     */
    static SchemaObject addImplicitProperties(SchemaObject schema) {
        schema.addProperties("hasBinLocationSupport", new SchemaObject().name("hasBinLocationSupport").type("boolean"))
        schema.addProperties("hasPackingSupport", new SchemaObject().name("hasPackingSupport").type("boolean"))
        schema.addProperties("hasPartialReceivingSupport", new SchemaObject().name("hasPartialReceivingSupport").type("boolean"))
        schema.addProperties("organizationName", new SchemaObject().name("organizationName").type("string"))
        return schema
    }
}
