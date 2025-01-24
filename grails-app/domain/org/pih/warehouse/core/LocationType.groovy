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


/**
 * Represents the type of a Location
 *
 */
class LocationType implements Comparable, Serializable {

    String id
    String name
    String description
    Integer sortOrder = 0
    Date dateCreated
    Date lastUpdated

    LocationTypeCode locationTypeCode

    static hasMany = [supportedActivities: String]

    static mapping = {
        id generator: 'uuid'
        cache true
    }

    static transients = ["defaultInternalLocationType", "internalLocationTypes",
                         "zoneLocationTypes", "defaultZoneLocationType"]

    static constraints = {
        name(nullable: false, maxSize: 255)
        locationTypeCode(nullable: false)
        description(nullable: true, maxSize: 255)
        supportedActivities(nullable: true, display: false)
        sortOrder(nullable: true)
        dateCreated(display: false)
        lastUpdated(display: false)
        supportedActivities(validator: { Set<String> activities, LocationType locationType ->
            // Don't allow having NONE supported activity in combination with any other supported activity
            if (activities?.contains(ActivityCode.NONE.id) && activities?.size() > 1) {
                return ['invalid.supportedActivities']
            }
            return true
        })
    }

    /**
     * Indicates whether the location type supports the given activity.
     *
     * @param activity the given activity
     * @return true if the activity is supported, false otherwise
     */
    Boolean supports(ActivityCode activity) {
        return supports(activity.id)
    }

    /**
     * Indicates whether the location type supports the given activity.
     *
     * @param activity the given activity id
     * @return true if the activity is supported, false otherwise
     */
    Boolean supports(String activity) {
        return supportedActivities?.any { a -> activity == a.toString() }
    }

    static LocationType getDefaultInternalLocationType() {
        def locationTypes = internalLocationTypes
        return locationTypes ? locationTypes[0] : null
    }

    static List<LocationType> getInternalLocationTypes() {
        return getInternalLocationTypes(null)
    }

    static List<LocationType> getInternalLocationTypes(ActivityCode[] activityCodes) {
        def internalLocationTypesSupportingActivityCodes = []
        def internalLocationTypes = LocationType.createCriteria().list {
            'in'("locationTypeCode", [LocationTypeCode.BIN_LOCATION, LocationTypeCode.INTERNAL])
        }

        if (activityCodes) {
            activityCodes.each { activityCode ->
                internalLocationTypesSupportingActivityCodes << internalLocationTypes.findAll { internalLocationType ->
                    internalLocationType.supports(activityCode)
                }
            }
        } else {
            internalLocationTypesSupportingActivityCodes = internalLocationTypes
        }
        return internalLocationTypesSupportingActivityCodes?.sort()

    }

    static List<LocationType> getZoneLocationTypes() {
        return LocationType.createCriteria().list {
            'in'("locationTypeCode", LocationTypeCode.listZoneTypeCodes())
        }
    }

    static LocationType getDefaultZoneLocationType() {
        def locationTypes = zoneLocationTypes
        return locationTypes ? locationTypes[0] : null
    }

    String toString() {
        return "${name}"
    }

    int compareTo(obj) {
        return name <=> obj?.name
    }

    Boolean isDepot() {
        return locationTypeCode == LocationTypeCode.DEPOT
    }

    Boolean isWard() {
        return locationTypeCode == LocationTypeCode.WARD
    }

    Boolean isDispensary() {
        return locationTypeCode == LocationTypeCode.DISPENSARY
    }

    Boolean isBinLocation() {
        return locationTypeCode == LocationTypeCode.BIN_LOCATION
    }

    Boolean isSupplier() {
        return locationTypeCode == LocationTypeCode.SUPPLIER
    }

    Boolean isDonor() {
        return locationTypeCode == LocationTypeCode.DONOR
    }

    Boolean isVirtual() {
        return locationTypeCode == LocationTypeCode.VIRTUAL
    }

    Boolean isWardOrPharmacy() {
        return (locationTypeCode in [LocationTypeCode.DISPENSARY, LocationTypeCode.WARD])
    }

    Boolean isDepotWardOrPharmacy() {
        return (locationTypeCode in [LocationTypeCode.DEPOT, LocationTypeCode.DISPENSARY, LocationTypeCode.WARD])
    }

    Boolean isInternalLocation() {
        return (locationTypeCode in LocationTypeCode.listInternalTypeCodes())
    }

    Boolean isFacilityLocation() {
        return (locationTypeCode in LocationTypeCode.listFacilityTypeCodes())
    }

    Boolean isZone() {
        return (locationTypeCode in LocationTypeCode.listZoneTypeCodes())
    }

}
