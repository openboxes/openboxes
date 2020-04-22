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

import org.apache.commons.collections.comparators.NullComparator
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import util.ConfigHelper

import javax.xml.bind.ValidationException


class LocationService {

    def grailsApplication
    boolean transactional = true


    Location findInternalLocation(Location parentLocation, String[] names) {
        return Location.createCriteria().get {
            eq("parentLocation", parentLocation)
            'in'("name", names)
        }
    }

    Location findOrCreateInternalLocation(String shipmentNumber, String locationNumber, LocationType locationType, Location parentLocation) {
        log.info "find or create internal location name=${shipmentNumber}, type=${locationType}"
        if (!shipmentNumber || !locationNumber || !locationType || !parentLocation) {
            throw new IllegalArgumentException("Must specify name, location number, location type, and parent location in order to create internal location")
        }

        String name = getReceivingLocationName(shipmentNumber)
        String[] receivingLocationNames = [name, "Receiving ${shipmentNumber}"]
        Location location = findInternalLocation(parentLocation, receivingLocationNames)
        if (!location) {
            log.info "creating internal location name=${name}, type=${locationType}"
            location = new Location()
            location.name = name
            location.locationNumber = locationNumber
            location.locationType = locationType
            location.parentLocation = parentLocation
            location.save(failOnError: true)
        }
        return location
    }


    def getAllLocations() {
        return getLocations(null, [:])
    }

    def getLocations(String[] fields, Map params) {

        LocationTypeCode locationTypeCode = params.locationTypeCode ?: null

        def locations = Location.createCriteria().list() {
            if (fields) {
                projections {
                    fields.each { field ->
                        property(field)
                    }
                }
            }

            if (params.name) {
                ilike("name", "%" + params.name + "%")
            }

            if (params.locationTypeCode) {
                locationType {
                    eq("locationTypeCode", locationTypeCode)
                }
            }

            eq("active", Boolean.TRUE)
            isNull("parentLocation")
        }
        return locations
    }

    def getLocations(String[] fields, Map params, Boolean isSuperuser, String direction, Location currentLocation, User user) {

        def locations = new HashSet()
        locations += getLocations(fields, params)

        if (params.applyUserFilter) {
            locations = locations.findAll { location -> user.hasPrimaryRole(location) }
        }

        if (params.activityCodes) {
            ActivityCode[] activityCodes = params.list("activityCodes") as ActivityCode[]
            return locations.findAll {
                it.supportsAll(activityCodes)
            }
        }

        if (!isSuperuser) {
            if (direction == "INBOUND") {
                return locations.findAll {
                    it.locationType.locationTypeCode == LocationTypeCode.SUPPLIER
                }
            }
            if (direction == "OUTBOUND") {
                return locations.findAll {
                    (it.locationGroup == currentLocation.locationGroup) ||
                            (it.locationGroup != currentLocation.locationGroup && it.locationType.locationTypeCode == LocationTypeCode.DEPOT)
                }
            }
        } else {
            if (direction == "INBOUND") {
                return locations.findAll {
                    it.locationType.locationTypeCode == LocationTypeCode.SUPPLIER || !it.supports(ActivityCode.MANAGE_INVENTORY)
                }
            }
        }

        if (params.locationTypeCode) {
            LocationTypeCode locationTypeCode = params.locationTypeCode as LocationTypeCode
            return locations.findAll { it.locationType.locationTypeCode == locationTypeCode }
        }

        return locations
    }

    def getLocations(LocationType locationType, LocationGroup locationGroup, String query, Integer max, Integer offset) {
        log.info "Location type " + locationType?.locationTypeCode
        def terms = "%" + query + "%"
        def locations = Location.createCriteria().list(max: max, offset: offset) {
            if (query) {
                ilike("name", terms)
            }

            if (locationType) {
                eq("locationType", locationType)

            }
            if (locationGroup) {
                eq("locationGroup", locationGroup)
            }

            if (locationType?.locationTypeCode == LocationTypeCode.BIN_LOCATION) {
                isNotNull("parentLocation")
            } else {
                isNull("parentLocation")
            }


        }
        return locations

    }


    def getLoginLocations(Integer currentLocationId) {
        return getLoginLocations(Location.get(currentLocationId))
    }

    def getLoginLocations(Location currentLocation) {
        log.info "Get login locations (currentLocation=${currentLocation?.name})"

        // Get all locations that match the required activity (using inclusive OR)
        def locations = new HashSet()
        def requiredActivities = ConfigHelper.listValue(grailsApplication.config.openboxes.chooseLocation.requiredActivities)
        if (requiredActivities) {
            requiredActivities.each { activity ->
                locations += getAllLocations()?.findAll { it.supports(activity) }
            }
        }
        return locations
    }


    Map getLoginLocationsMap(User user, Location currentLocation) {
        log.info "Get login locations for user ${user} and location ${currentLocation})"
        def locationMap = [:]
        def nullHigh = new NullComparator(true)
        def locations = getLoginLocations(currentLocation)
        if (locations) {
            locations = locations.findAll { Location location -> user.hasPrimaryRole(location) }
            locations = locations.collect { Location location ->
                [
                        id           : location?.id,
                        name         : location?.name,
                        locationType : location.locationType?.name,
                        locationGroup: location?.locationGroup?.name,

                ]
            }
            locationMap = locations.groupBy { it?.locationGroup }
            locationMap = locationMap.sort { a, b -> nullHigh.compare(a?.key, b?.key) }
        }
        return locationMap
    }

    List getInternalLocations(Location parentLocation) {
        return getInternalLocations(parentLocation, null)
    }

    List getInternalLocations(Location parentLocation, ActivityCode[] activityCodes) {
        return getInternalLocations(parentLocation, [LocationTypeCode.INTERNAL] as LocationTypeCode[], activityCodes)
    }

    List getInternalLocations(Location parentLocation, LocationTypeCode[] locationTypeCodes, ActivityCode[] activityCodes) {
        return getInternalLocations(parentLocation, locationTypeCodes, activityCodes, null)
    }

    List getInternalLocations(Location parentLocation, LocationTypeCode[] locationTypeCodes, ActivityCode[] activityCodes, String[] locationNames) {

        List<Location> internalLocationsSupportingActivityCodes = []

        if (parentLocation.hasBinLocationSupport()) {
            log.info "Get internal locations for parent ${parentLocation} with activity codes ${activityCodes} and location type codes ${locationTypeCodes}"
            List<Location> internalLocations = Location.createCriteria().list() {
                eq("active", Boolean.TRUE)
                eq("parentLocation", parentLocation)
                or {
                    locationType {
                        'in'("locationTypeCode", locationTypeCodes)
                    }
                    if (locationNames) {
                        'in'("name", locationNames)
                    }
                }
            }

            // Filter by activity code
            if (activityCodes) {
                activityCodes.each { activityCode ->
                    internalLocations = internalLocations.findAll { internalLocation ->
                        internalLocation.supports(activityCode) || (locationNames && internalLocation.name in locationNames)
                    }
                    internalLocationsSupportingActivityCodes.addAll(internalLocations)
                }
            } else {
                internalLocationsSupportingActivityCodes.addAll(internalLocations)
            }

            // Sort locations by sort order, then name
            internalLocationsSupportingActivityCodes =
                    internalLocationsSupportingActivityCodes.sort { a, b -> a.sortOrder <=> b.sortOrder ?: a.name <=> b.name }

            internalLocationsSupportingActivityCodes = internalLocationsSupportingActivityCodes.unique()
        }

        return internalLocationsSupportingActivityCodes
    }

    List getPutawayLocations(Location parentLocation) {
        return getInternalLocations(parentLocation, [ActivityCode.PUTAWAY_STOCK])
    }

    List getPickingLocations(Location parentLocation) {
        return getInternalLocations(parentLocation, [ActivityCode.PICK_STOCK])
    }

    List getReceivingLocations(Location parentLocation) {
        return getInternalLocations(parentLocation, [ActivityCode.RECEIVE_STOCK])
    }

    List getCrossDockingLocations(Location parentLocation) {
        return getInternalLocations(parentLocation, [ActivityCode.CROSS_DOCKING])
    }

    List getDepots() {
        return getAllLocations()?.findAll { it.supports(ActivityCode.MANAGE_INVENTORY) }
    }

    List getNearbyLocations(Location currentLocation) {
        return Location.findAllByActiveAndLocationGroup(true, currentLocation.locationGroup)
    }

    List getExternalLocations() {
        return getAllLocations()?.findAll { it.supports(ActivityCode.EXTERNAL) }
    }

    List getDispensaries(Location currentLocation) {
        return getNearbyLocations(currentLocation)?.findAll {
            it.supports(ActivityCode.RECEIVE_STOCK) && !it.supports(ActivityCode.EXTERNAL)
        }
    }

    List getLocationsSupportingActivity(ActivityCode activity) {
        return getAllLocations()?.findAll { it.supports(activity) }

    }

    List getShipmentOrigins() {
        return getLocationsSupportingActivity(ActivityCode.SEND_STOCK)
    }

    List getShipmentDestinations() {
        return getLocationsSupportingActivity(ActivityCode.RECEIVE_STOCK)
    }

    List getOrderSuppliers(Location currentLocation) {
        return getLocationsSupportingActivity(ActivityCode.FULFILL_ORDER) - currentLocation
    }

    List getRequestOrigins(Location currentLocation) {
        return getLocationsSupportingActivity(ActivityCode.FULFILL_REQUEST)// - currentLocation
    }

    List getRequestDestinations(Location currentLocation) {
        return getLocationsSupportingActivity(ActivityCode.RECEIVE_STOCK)// - currentLocation
    }

    List getTransactionSources(Location currentLocation) {
        return getLocationsSupportingActivity(ActivityCode.SEND_STOCK) - currentLocation
    }

    List getTransactionDestinations(Location currentLocation) {
        // Always get nearby locations
        def locations = getNearbyLocations(currentLocation)

        // Get all external locations (if supports external)
        if (currentLocation.supports(ActivityCode.EXTERNAL)) {
            locations += getExternalLocations()
        }

        // Of those locations remaining, we need to return only locations that can receive stock
        locations = locations.findAll { it.supports(ActivityCode.RECEIVE_STOCK) }

        // Remove current location from list
        locations = locations?.unique() - currentLocation

        return locations

    }

    boolean importBinLocations(String locationId, InputStream inputStream) {
        try {

            Location location = Location.get(locationId)
            if (!location) {
                throw new ValidationException("location.cannotImportBinLocationsWithoutParentLocation.message")
            }

            LocationType defaultLocationType = LocationType.findByLocationTypeCode(LocationTypeCode.BIN_LOCATION)
            if (!defaultLocationType) {
                throw new ValidationException("locationType.noDefaultForBinLocation.message")
            }

            List binLocations = parseBinLocations(inputStream)
            log.info "Bin locations " + binLocations
            if (binLocations) {
                binLocations.each {
                    Location binLocation = Location.findByNameAndParentLocation(it.name, location)
                    if (!binLocation) {
                        binLocation = new Location()
                        binLocation.name = it.name
                        binLocation.locationNumber = it.name
                        binLocation.parentLocation = location
                        binLocation.locationType = defaultLocationType
                        location.addToLocations(binLocation)
                    } else {
                        log.info "Bin location ${it.name} already exists"
                    }
                }
                location.save()

            } else {
                throw new ValidationException("location.cannotImportEmptyBinLocations.message")
            }
        } catch (Exception e) {
            log.error("Unable to bin locations due to exception: " + e.message, e)
            throw new RuntimeException(e.message)
        }
        finally {
            inputStream.close()
        }

        return true
    }


    List parseBinLocations(InputStream inputStream) {

        List binLocations = []

        HSSFWorkbook workbook = new HSSFWorkbook(inputStream)
        HSSFSheet worksheet = workbook.getSheetAt(0)

        Iterator<Row> rowIterator = worksheet.iterator()
        int cellIndex = 0
        Row row
        while (rowIterator.hasNext()) {
            row = rowIterator.next()

            // Skip the first row
            if (row.getRowNum() == 0) {
                continue
            }

            try {
                cellIndex = 0
                def name = getStringCellValue(row.getCell(cellIndex++))
                binLocations << [name: name]
            }
            catch (IllegalStateException e) {
                log.error("Error parsing XLS file " + e.message, e)
                throw new RuntimeException("Error parsing XLS file at row " + (row.rowNum + 1) + " column " + cellIndex + " caused by: " + e.message, e)
            }
            catch (Exception e) {
                log.error("Error parsing XLS file " + e.message, e)
                throw new RuntimeException("Error parsing XLS file at row " + (row.rowNum + 1) + " column " + cellIndex + " caused by: " + e.message, e)

            }


        }
        return binLocations
    }

    String getStringCellValue(Cell cell) {
        String value = null
        if (cell) {
            try {
                value = cell.getStringCellValue()
            }
            catch (IllegalStateException e) {
                log.warn("Error parsing string cell value [${cell}]: " + e.message, e)
                value = Integer.valueOf((int) cell.getNumericCellValue())
            }
        }
        return value?.trim()
    }

    String getReceivingLocationName(String identifier) {
        String receivingLocationPrefix = grailsApplication.config.openboxes.receiving.receivingLocation.prefix
        return "${receivingLocationPrefix}-${identifier}"
    }

}
