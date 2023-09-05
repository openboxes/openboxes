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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.apache.commons.collections.comparators.NullComparator
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import grails.plugins.csv.CSVMapReader
import org.hibernate.sql.JoinType
import org.pih.warehouse.data.LocationDataService
import org.pih.warehouse.importer.ImportDataCommand
import util.ConfigHelper

@Transactional
class LocationService {

    GrailsApplication grailsApplication
    UserService userService
    LocationDataService locationDataService

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

    def getZones(Location parentLocation) {
        return Location.createCriteria().list {
            eq("active", Boolean.TRUE)
            eq("parentLocation", parentLocation)
            locationType {
                'in'("locationTypeCode", LocationTypeCode.listZoneTypeCodes())
            }

            order("name")
        }
    }

    def getBinLocations(Location parentLocation) {
        return Location.createCriteria().list {
            eq("active", Boolean.TRUE)
            eq("parentLocation", parentLocation)
            locationType {
                'in'("locationTypeCode", LocationTypeCode.listInternalTypeCodes())
            }
        }
    }

    def getAllLocations() {
        return getLocations(null, [:])
    }

    def getLocations(String[] fields, Map params) {

        LocationTypeCode locationTypeCode = params?.locationTypeCode ?: null

        def locations = Location.createCriteria().list() {
            if (fields) {
                projections {
                    fields.each { field ->
                        property(field)
                    }
                }
            }

            if (params?.name) {
                ilike("name", "%" + params.name + "%")
            }

            if (params?.locationTypeCode) {
                locationType {
                    eq("locationTypeCode", locationTypeCode)
                }
            }

            if(params?.withOrganization) {
                isNotNull("organization")
            }

            eq("active", Boolean.TRUE)
            isNull("parentLocation")
        }
        return locations
    }

    def getLocations(String[] fields, Map params, Boolean isSuperuser, String direction, Location currentLocation, User user, Boolean excludeDisabled = false) {
        def locations = new HashSet()
        locations += getLocations(fields, params)

        if (excludeDisabled) {
            locations = locations.findAll { Location location -> location.status == LocationStatus.ENABLED }
        }

        if (params.applyUserFilter) {
            locations = locations.findAll { location -> user.hasPrimaryRole(location) }
        }

        if (params.activityCodes) {
            ActivityCode[] activityCodes = params.list("activityCodes") as ActivityCode[]
            return locations.findAll {
                it.supportsAll(activityCodes)
            }
        }

        if (params.isReturnOrder) {
            if (isSuperuser) {
                return locations.findAll { Location it ->
                    !it.supports(ActivityCode.MANAGE_INVENTORY) && it.locationType.locationTypeCode != LocationTypeCode.SUPPLIER
                }
            } else {
                return locations.findAll { Location it ->
                    !it.supports(ActivityCode.MANAGE_INVENTORY) && it.locationGroup == currentLocation.locationGroup && it.locationType.locationTypeCode != LocationTypeCode.SUPPLIER
                }
            }
        }

        def outboundMovementLocations = locations.findAll {
            (it.locationGroup == currentLocation.locationGroup) ||
                    (it.locationGroup != currentLocation.locationGroup && it.locationType.locationTypeCode == LocationTypeCode.DEPOT)
        }

        if (!isSuperuser) {
            if (direction == "INBOUND") {
                return locations.findAll {
                    it.locationType.locationTypeCode == LocationTypeCode.SUPPLIER
                }
            }
            if (direction == "OUTBOUND") {
                return outboundMovementLocations
            }
        } else {
            if (direction == "INBOUND") {
                return locations.findAll {
                    it.locationType.locationTypeCode == LocationTypeCode.SUPPLIER || !it.supports(ActivityCode.MANAGE_INVENTORY)
                }
            }
            if (direction == "OUTBOUND") {
                return outboundMovementLocations
            }
        }

        if (params.locationTypeCode) {
            LocationTypeCode locationTypeCode = params.locationTypeCode as LocationTypeCode
            return locations.findAll { it.locationType.locationTypeCode == locationTypeCode }
        }

        return locations
    }

    def getLocations(Organization organization, LocationType locationType, LocationGroup locationGroup, String query, Integer max, Integer offset) {
        def terms = "%" + query + "%"
        def locations = Location.createCriteria().list(max: max, offset: offset) {
            if (query) {
                ilike("name", terms)
            }

            if (organization) {
                eq("organization", organization)
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
            order("name")
        }
        return locations

    }

    def getRequestorLocations(User user) {
        def locations = new HashSet()
        List locationRolesForRequestor = user?.locationRoles.findAll {it.role.roleType == RoleType.ROLE_REQUESTOR}

        if (locationRolesForRequestor) {
            locations = locationRolesForRequestor.collect {it.location}
            locations = locations.findAll {it -> it.supports(ActivityCode.SUBMIT_REQUEST) && !it.supports(ActivityCode.MANAGE_INVENTORY)}
        }

        return locations
    }

    def getSuppliers(String query, Integer max, Integer offset) {
        def terms = "%" + query + "%"
        def locations = Supplier.createCriteria().list(max: max, offset: offset) {
            if (query) {
                or {
                    ilike("name", terms)
                    organization(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                        ilike("name", terms)
                    }
                }
            }

            order("name")
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


    Map getLoginLocationsMap(User user, Location currentLocation, Boolean excludeDisabled = false) {
        log.info "Get login locations for user ${user} and location ${currentLocation})"
        def locationMap = [:]
        def locations = new HashSet()
        def nullHigh = new NullComparator(true)
        def isRequestor = userService.isUserRequestor(user)
        def requestorInAnyLocation = userService.hasRoleRequestorInAnyLocations(user)
        def inRoleBrowser = user.hasDefaultRole(RoleType.ROLE_BROWSER)
        def inRoleAssistant = user.hasDefaultRole(RoleType.ROLE_ASSISTANT)
        def inRoleManager = user.hasDefaultRole(RoleType.ROLE_MANAGER)
        def inRoleAdmin = user.hasDefaultRole(RoleType.ROLE_ADMIN)
        def inRoleSuperuser = user.hasDefaultRole(RoleType.ROLE_SUPERUSER)

        def requiredRoles = RoleType.listRoleTypesForLocationChooser()

        if (isRequestor && !user.locationRoles && !inRoleBrowser) {
            locations = getLocations(null, null)
            locations = locations.findAll {it -> it.supportedActivities && it.supports(ActivityCode.SUBMIT_REQUEST) }
        } else if (requestorInAnyLocation && inRoleBrowser) {
            locations = getRequestorLocations(user)
            locations += getLoginLocations(currentLocation)
        } else {
            if (requestorInAnyLocation) {
                locations += getRequestorLocations(user)
            }
            // If a user doesn't have at least one of the requiredRoles by default, get locations where the user HAS any of those roles
            if (!inRoleBrowser && !inRoleAssistant && !inRoleManager && !inRoleAdmin && !inRoleSuperuser) {
                user.locationRoles.each { LocationRole locationRole ->
                    if (requiredRoles.contains(locationRole.role.roleType)) {
                        locations += locationRole.location
                    }
                }
            } else {
                locations += getLoginLocations(currentLocation)
            }
        }

        if (locations) {
            if (excludeDisabled) {
                locations = locations.findAll{ Location location -> location.status == LocationStatus.ENABLED }
            }
            locations = locations.collect { Location location ->
                                        [
                                                id              : location?.id,
                                                name            : location?.name,
                                                foregroundColor : location.fgColor,
                                                backgroundColor : location?.bgColor,
                                                organizationName: location?.organization?.name,
                                                locationType    : location.locationType?.name,
                                                locationGroup   : location?.locationGroup?.name,

                                        ]
                                    }
            locationMap = locations.groupBy { it?.organizationName }
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

    List getInternalLocations(Location parentLocation, LocationTypeCode[] locationTypeCodes, Map params) {
        getInternalLocations(parentLocation, locationTypeCodes, params, null)
    }

    // FIXME This is a hotfix for issue OBPIH-5466
    // The issue is with including and excluding certain locations like RECEIVING and HOLD.
    // An example for this issue was that we needed to include HOlD BINS and to do that
    // we needed to include LocationTypeCode = BIN_LOCATION and INTERNAL.
    // By including LocationTypeCode INTERNAL we also get a bunch of Receiving Locations which we don't need in most of the places
    // so we needed to have a way to exclude them
    List getInternalLocations(Location parentLocation, LocationTypeCode[] locationTypeCodes, Map params, String[] locationNames) {
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

        // locations that must be included regardless of the conditions set
        List<Location> includedLocations = []
        if (locationNames) {
            includedLocations = internalLocations.findAll {it.name in locationNames }
            internalLocations.removeAll(includedLocations)

        }

        // Filter by activity code
        if (params?.allActivityCodes) {
            internalLocations = internalLocations.findAll { Location location ->
                params?.allActivityCodes?.every{ location.supports(it) }
            }
        } else if (params?.anyActivityCodes){
            internalLocations = internalLocations.findAll { Location location ->
                params?.anyActivityCodes?.any{ location.supports(it) }
            }
        }
        if (params?.ignoreActivityCodes) {
            internalLocations = internalLocations.findAll { Location location ->
                params?.ignoreActivityCodes?.every{ !location.supports(it) }
            }
        }
        internalLocations += includedLocations

        // Sort locations by sort order, then name
        internalLocations = internalLocations.sort { a, b -> a.sortOrder <=> b.sortOrder ?: a.name <=> b.name }

        return internalLocations
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
        return getAllLocations()?.findAll { it.supports(ActivityCode.MANAGE_INVENTORY) }?.toArray()?:[]
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
        def locations = [:]

        Location.executeQuery('select l from Location l join l.supportedActivities s where s = :activity', [ activity: activity.id ]).each {
            locations[it.id] = it
        }

        Location.executeQuery('select l from Location l join l.locationType t join t.supportedActivities s where s = :activity', [ activity: activity.id ]).each {
            locations[it.id] = it
        }

        return locations.values().toList()
    }

    List getShipmentOrigins() {
        return getLocationsSupportingActivity(ActivityCode.SEND_STOCK)
    }

    List getShipmentDestinations() {
        return getLocationsSupportingActivity(ActivityCode.RECEIVE_STOCK)
    }

    List getOrderSuppliers(Location currentLocation) {
        def locations = Location.createCriteria().list() {
            organization {
                roles {
                    eq("roleType", RoleType.ROLE_SUPPLIER)
                }
            }

            order("name")
        }

        return locations.findAll { it.supports(ActivityCode.FULFILL_ORDER) } - currentLocation
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
        locations = locations.findAll { !it.parentLocation && !it.supports(ActivityCode.MANAGE_INVENTORY) && !it.isSupplier() }

        // Remove current location from list
        locations = (locations + currentLocation)?.unique()

        return locations

    }

    boolean importBinLocations(String locationId, InputStream inputStream) {
        try {

            Location location = Location.get(locationId)
            location = location?.isZoneLocation() ? location?.parentLocation : location

            if (!location) {
                throw new ValidationException("location.cannotImportBinLocationsWithoutParentLocation.message")
            }

            LocationType defaultLocationType = LocationType.findAllByLocationTypeCode(LocationTypeCode.BIN_LOCATION).min{ it.dateCreated }
            if (!defaultLocationType) {
                throw new ValidationException("locationType.noDefaultForBinLocation.message")
            }

            List binLocations = parseBinLocations(inputStream)
            log.info "Bin locations " + binLocations

            if (!binLocations || binLocations?.isEmpty()) {
                location.errors.rejectValue("locations", "location.cannotImportEmptyBinLocations.message", "Bin locations cannot be empty")
                throw new ValidationException("Import must contain at least one bin location", location.errors)
            }

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

                    }

                    if (it.zoneName) {
                        Location zone = Location.findByNameAndParentLocation(it.zoneName, location)

                        if (!zone) {
                            throw new ValidationException("Zone with name: ${it.zoneName} does not exist", binLocation.errors)
                        } else {
                            binLocation.zone = zone
                        }
                    } else {
                        binLocation.zone = null
                    }

                    if (!binLocation.validate()) {
                        throw new ValidationException("Bin location ${it.name} is invalid", binLocation.errors)
                    }
                }
            }

            if (location.hasErrors()) {
                throw new ValidationException("Location is invalid", location.errors)
            }

        } catch (Exception e) {
            log.error("Unable to import bin locations due to the following error: " + e.message, e)
            throw e;
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
                def zoneName = getStringCellValue(row.getCell(cellIndex++))

                if (name) {
                    binLocations << [name: name, zoneName: zoneName]
                }
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

    List<Location> searchInternalLocations(Map params, LocationTypeCode[] locationTypeCodes) {
        return Location.createCriteria().list(params) {
            if (!params.includeInactive) {
                eq("active", Boolean.TRUE)
            }

            if (params.parentLocation?.id) {
                eq("parentLocation", Location.get(params.parentLocation.id))
            }

            if (locationTypeCodes) {
                locationType {
                    'in'("locationTypeCode", locationTypeCodes)
                }
            }

            if (params.searchTerm) {
                or {
                    ilike("name", "%${params.searchTerm}%")
                    ilike("locationNumber", "%${params.searchTerm}%")
                }
            }

            order("sortOrder", "asc")
            order("name", "asc")
        }
    }

    def importLocationCsv(ImportDataCommand command) {
        String csv = new String(command.importFile.bytes)
        def settings = [separatorChar: ',']
        CSVMapReader csvReader = new CSVMapReader(new StringReader(csv), settings)
        command.data = csvReader.readAll()

        command.errors = null
        locationDataService.validateData(command)

        if (command.errors.allErrors) {
            throw new ValidationException("Failed to import template due to validation errors", command.errors)
        }

        locationDataService.importData(command)
    }

    def deleteLocation(Location existingLocation) {
        Location parentLocation = existingLocation.parentLocation
        if (parentLocation) {
            existingLocation.parentLocation = null
            parentLocation.removeFromLocations(existingLocation)
        }
        Location zone = existingLocation.zone
        if (zone) {
            existingLocation.zone = null
            zone.removeFromLocations(existingLocation)
        }
        existingLocation.delete(flush: true)
    }
}
