/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.grails.web.json.JSONObject
import org.hibernate.Criteria
import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationDataService
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.ProductAvailability
import grails.core.GrailsApplication
import org.springframework.web.multipart.MultipartFile
import org.pih.warehouse.core.LocationStatus

class LocationApiController extends BaseDomainApiController {

    def locationService
    def userService
    GrailsApplication grailsApplication
    def identifierService
    def inventoryService
    def documentService
    LocationDataService locationGormService

    def read() {
        Location location = Location.get(params.id)
        render([data: location] as JSON)
    }

    def list() {

        def minLength = grailsApplication.config.openboxes.typeahead.minLength
        if (params.name && params.name.size() < minLength) {
            render([data: []])
            return
        }

        Location currentLocation = Location.get(session?.warehouse?.id)
        User currentUser = User.get(session?.user?.id)
        boolean isSuperuser = userService.isSuperuser(session?.user)
        String direction = params?.direction
        def fields = params.fields ? params.fields.split(",") : null
        def locations = new HashSet()
        def isRequestor = userService.isUserRequestor(currentUser)
        def requestorInAnyLocation = userService.hasRoleRequestorInAnyLocations(currentUser)
        def inRoleBrowser = currentUser.hasDefaultRole(RoleType.ROLE_BROWSER)
        def inRoleAssistant = currentUser.hasDefaultRole(RoleType.ROLE_ASSISTANT)
        def inRoleManager = currentUser.hasDefaultRole(RoleType.ROLE_MANAGER)
        def inRoleAdmin = currentUser.hasDefaultRole(RoleType.ROLE_ADMIN)
        def inRoleSuperuser = currentUser.hasDefaultRole(RoleType.ROLE_SUPERUSER)


        def requiredRoles = RoleType.listRoleTypesForLocationChooser()


        if (params.locationChooser && isRequestor && !currentUser.locationRoles && !inRoleBrowser) {
            locations = locationService.getLocations(null, null)
            locations = locations.findAll { it.supportedActivities && it.supports(ActivityCode.SUBMIT_REQUEST) && it.status == LocationStatus.ENABLED }
        } else if (params.locationChooser && requestorInAnyLocation && inRoleBrowser) {
            locations = locationService.getRequestorLocations(currentUser)
            locations += locationService.getLocations(fields, params, isSuperuser, direction, currentLocation, currentUser, true)
        } else {
            if (params.locationChooser && requestorInAnyLocation) {
                locations += locationService.getRequestorLocations(currentUser)
            }
            // If a user doesn't have at least one of the requiredRoles by default, get locations where the user HAS any of those roles
            if (params.locationChooser && !inRoleBrowser && !inRoleAssistant && !inRoleManager && !inRoleAdmin && !inRoleSuperuser) {
                currentUser.locationRoles.each { LocationRole locationRole ->
                    if (requiredRoles.contains(locationRole.role.roleType)) {
                        locations += locationRole.location
                    }
                }
            } else {
                locations += locationService.getLocations(fields, params, isSuperuser, direction, currentLocation, currentUser, params.locationChooser ? true : false)
            }
        }

        if (params.presentation == "toBaseJson") {
            locations = locations?.collect { Location location -> location.toBaseJson()}
        }

        render ([data:locations] as JSON)
    }


    def productSummary() {
        Location currentLocation = Location.load(session.warehouse.id)
        def data = ProductAvailability.createCriteria().list {
            resultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
            projections {
                product {
                    groupProperty("id", "productId")
                    groupProperty("name", "productName")
                    groupProperty("productCode", "productCode")
                }
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", currentLocation)
        }
        render ([data:data] as JSON)

    }

    def locationTypes() {
        String[] activityCodes = params.list('activityCode');
        def locationTypes = LocationType.list()

        if (activityCodes.length > 0) {
            locationTypes = locationTypes.findAll { locationType ->
                activityCodes.any { activityCode -> locationType.supports(activityCode) }
            }
        }

        def data = locationTypes.collect { locationType ->
            [
                    id                  : locationType.id,
                    name                : locationType.name,
                    description         : locationType.description,
                    locationTypeCode    : locationType?.locationTypeCode?.name(),
                    supportedActivities : locationType.supportedActivities
            ]
        }

        render ([data:data] as JSON)
    }

    def supportedActivities() {
        def data = ActivityCode.list().collect { it.name() }
        render ([data: data] as JSON)
    }

    def create() {
        Location location = new Location()
        JSONObject jsonObject = request.JSON

        bindLocationData(location, jsonObject)

        boolean useDefaultActivities = Boolean.valueOf(params.useDefaultActivities ?: "false")
        if (useDefaultActivities && location?.supportedActivities) {
            location.supportedActivities.clear()
        }

        // If the organization chosen for the created location is inactive, throw an exception
        if (location.organization && !location.organization?.active) {
            throw new IllegalArgumentException("The organization ${location.organization.name} is inactive, you can't assign it to the location")
        }

        locationGormService.save(location)

        render ([data: location] as JSON)
    }

    def update() {
        JSONObject jsonObject = request.JSON

        Location existingLocation = Location.get(params.id)

        if (!existingLocation) {
            throw new IllegalArgumentException("No Location found for location ID ${params.id}")
        }

        bindLocationData(existingLocation, jsonObject)

        boolean useDefaultActivities = Boolean.valueOf(params.useDefaultActivities ?: "false")
        if (useDefaultActivities && existingLocation?.supportedActivities) {
            existingLocation.supportedActivities.clear()
        }

        existingLocation.address?.validate()
        locationGormService.save(existingLocation)

        render([data: existingLocation] as JSON)
    }

    Location bindLocationData(Location location, JSONObject jsonObject) {
        // We only want to set the supportedActivities if we provide it in the payload
        if (jsonObject.containsKey("supportedActivities")) {
            jsonObject.supportedActivities = jsonObject.supportedActivities ?: [ActivityCode.NONE.id]
        }
        bindData(location, jsonObject, [exclude: ['logo', 'zoneId']])

        String logo = jsonObject.logo

        if (logo) {
            location.logo = logo.decodeBase64()
        }

        if (!location.locationNumber) {
            location.locationNumber = identifierService.generateLocationIdentifier()
        }

        if (!location.inventory) {
            location.inventory = inventoryService.addInventory(location)
        }

        return location
    }

    @Transactional
    def updateForecastingConfiguration() {
        JSONObject jsonObject = request.JSON
        Location existingLocation = Location.get(params.id)

        if (!existingLocation) {
            throw new IllegalArgumentException("No Location found for location ID ${params.id}")
        }

        def inventoryLevelInstance = InventoryLevel.findByInventoryAndProductIsNull(existingLocation.inventory)

        if (!inventoryLevelInstance) {
            inventoryLevelInstance = new InventoryLevel(inventory: existingLocation.inventory)
        }
        bindData(inventoryLevelInstance, jsonObject)

        inventoryLevelInstance.save()

        render(status: 200)
    }

    def delete() {
        def existingLocation = Location.get(params.id)
        if (!existingLocation) {
            throw new IllegalArgumentException("No Location found for location ID ${params.id}")
        }
        if (existingLocation.isZoneLocation() && Location.findAllByZone(existingLocation)) {
            throw new IllegalArgumentException("You cannot delete zone that is assigned to a bin location ${params.id}")
        }

        try {
            locationService.deleteLocation(existingLocation)
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new Exception("${warehouse.message(code: 'default.not.deleted.with.reason.message', args: [warehouse.message(code: 'location.label', default: 'Location'), existingLocation.id, e.message])}")
        }

        render(status: 204)
    }

    def downloadTemplate() {
        def csv = "id,name,active,locationNumber,locationType,locationGroup,parentLocation,organization,streetAddress,streetAddress2,city,stateOrProvince,postalCode,country,description\n"

        response.setHeader("Content-disposition", "attachment; filename=\"Location_template.csv\"")
        render(contentType: "text/csv", text: CSVUtils.prependBomToCsvString(csv.toString()), encoding: "UTF-8")
    }

    def importCsv(ImportDataCommand command) {
        def importFile = command.importFile

        if (importFile.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty")
        }

        if (importFile.contentType != "text/csv") {
            throw new IllegalArgumentException("File must be in CSV format")
        }

        locationService.importLocationCsv(command)

        render status: 200
    }

    def downloadBinLocationTemplate() {
        def filename = "binLocations.xls"
        try {
            def file = documentService.findFile("templates/" + filename)
            response.setHeader 'Content-disposition', "attachment; filename=\"${filename}\""
            response.outputStream << file.bytes
            response.outputStream.flush()
        }
        catch (FileNotFoundException e) {
            render status: 404
        }
    }

    def importBinLocations() {
        try {
            MultipartFile multipartFile = request.getFile('fileContents')
            if (multipartFile.empty) {
                throw new IllegalArgumentException("File cannot be empty")
            }

            locationService.importBinLocations(params.id, multipartFile.inputStream)

        } catch (Exception e) {
            response.status = 500
            render([errorCode: 500, errorMessage: e?.message ?: "An unknown error occurred during import"] as JSON)
            return
        }

        render status: 200
    }
}
