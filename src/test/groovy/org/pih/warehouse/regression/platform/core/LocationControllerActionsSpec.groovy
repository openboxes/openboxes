package org.pih.warehouse.regression.platform.core

import spock.lang.PendingFeature

import org.pih.warehouse.core.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification
import org.pih.warehouse.MessageTagLib
import org.pih.warehouse.data.DataService
import org.pih.warehouse.inventory.InventoryService

/**
 * Covers the LocationController actions around bin locations, zones, logos and deletion.
 */
class LocationControllerActionsSpec extends Specification implements ControllerUnitTest<LocationController>, DataTest {

    LocationType depotType
    LocationType binType
    LocationType zoneType
    Location depot
    Location zone
    Location binA
    Location binB

    InventoryService inventoryService = Mock()
    LocationService locationService = Mock()

    void setupSpec() {
        mockDomains(Location, LocationType, LocationGroup, Organization)
    }

    void setup() {
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)
        mockTagLib(MessageTagLib)
        defineBeans {
            localizationService(InstanceFactoryBean, Stub(LocalizationService) {
                getCurrentLocale() >> Locale.ENGLISH
            }, LocalizationService)
        }
        depotType = new LocationType(name: "Depot", locationTypeCode: LocationTypeCode.DEPOT).save(validate: false)
        binType = new LocationType(name: "Bin", locationTypeCode: LocationTypeCode.BIN_LOCATION).save(validate: false)
        zoneType = new LocationType(name: "Zone", locationTypeCode: LocationTypeCode.ZONE).save(validate: false)
        depot = new Location(name: "Depot", locationType: depotType).save(validate: false)
        zone = new Location(name: "Zone A", locationType: zoneType, parentLocation: depot).save(validate: false)
        binA = new Location(name: "A-01", locationNumber: "A01", locationType: binType, parentLocation: depot, zone: zone).save(validate: false)
        binB = new Location(name: "B-01", locationNumber: "B01", locationType: binType, parentLocation: depot).save(validate: false)
        depot.locations = [zone, binA, binB] as Set
        depot.save(validate: false)

        controller.inventoryService = inventoryService
        controller.locationService = locationService
    }

    void "show redirects to edit keeping the parameters"() {
        when:
        params.id = depot.id
        controller.show()

        then:
        response.redirectedUrl == "/location/edit/${depot.id}"
    }

    void "edit sends the user back to the list for an unknown location"() {
        when:
        params.id = "missing"
        controller.edit()

        then:
        1 * inventoryService.getLocation("missing") >> null
        response.redirectedUrl == "/location/list"
        flash.message.startsWith("default.not.found.message")
    }

    @PendingFeature(reason = 'update has no return after the not-found redirect and NPEs on parentLocation')
    void "update of an unknown location goes back to the list"() {
        when:
        params.id = "missing"
        controller.update()

        then:
        1 * inventoryService.getLocation("missing") >> null
        0 * inventoryService.saveLocation(_)
        flash.message.startsWith("default.not.found.message")
        response.redirectedUrl == "/location/list"
    }

    void "update refuses a stale version"() {
        given:
        depot.version = 5

        when:
        params.id = depot.id
        params.version = "2"
        controller.update()

        then:
        1 * inventoryService.getLocation(depot.id) >> depot
        0 * inventoryService.saveLocation(_)
        view == "/location/edit"
        model.locationInstance.errors.getFieldError("version").code == "default.optimistic.locking.failure"
    }

    void "update refuses to assign an inactive organization"() {
        given:
        Organization inactive = new Organization(name: "Closed", code: "CL", active: false).save(validate: false)

        when:
        params.id = depot.id
        params['organization.id'] = inactive.id
        controller.update()

        then:
        1 * inventoryService.getLocation(depot.id) >> depot
        IllegalArgumentException e = thrown()
        e.message == "The organization Closed is inactive, you can't assign it to the location"
    }

    void "delete removes a bin and returns to its parent"() {
        when:
        params.id = binB.id
        controller.delete()

        then:
        1 * locationService.deleteLocation(binB)
        response.redirectedUrl == "/location/edit/${depot.id}"
        flash.message.startsWith("default.deleted.message")
    }

    void "delete removes a top level location and returns to the list"() {
        given:
        Location other = new Location(name: "Other", locationType: depotType).save(validate: false)

        when:
        params.id = other.id
        controller.delete()

        then:
        1 * locationService.deleteLocation(other)
        response.redirectedUrl == "/location/list"
    }

    void "delete refuses a zone that still has bins assigned"() {
        when:
        params.id = zone.id
        controller.delete()

        then:
        0 * locationService.deleteLocation(_)
        flash.message == "location.zoneAssigned.message"
        response.redirectedUrl == "/location/edit/${zone.id}"
    }

    void "delete reports integrity violations and unknown locations"() {
        given:
        locationService.deleteLocation(binB) >> { throw new org.springframework.dao.DataIntegrityViolationException("in use") }

        when:
        params.id = binB.id
        controller.delete()

        then:
        flash.message.startsWith("default.not.deleted.message")
        response.redirectedUrl == "/location/edit/${binB.id}"

        when:
        response.reset()
        params.id = "missing"
        controller.delete()

        then:
        flash.message.startsWith("default.not.deleted.message")
        response.redirectedUrl == "/location/edit/missing"
    }

    void "showContents lists the quantities stored in a bin"() {
        when:
        params.id = binA.id
        def model = controller.showContents()

        then:
        1 * inventoryService.getQuantityByBinLocation(depot, binA) >> [[quantity: 3]]
        model.binLocation == binA
        model.contents == [[quantity: 3]]

        when:
        response.reset()
        params.id = "missing"
        controller.showContents()

        then:
        response.text.startsWith("default.not.found.message")
    }

    void "showBinLocations lists the bins of a location or of a zone"() {
        when:
        params.id = depot.id
        def model = controller.showBinLocations()

        then:
        1 * locationService.getBinLocations(depot) >> [binA, binB]
        model.binLocations == [binA, binB]

        when:
        params.id = zone.id
        model = controller.showBinLocations()

        then:
        0 * locationService.getBinLocations(_)
        model.binLocations == [binA]
    }

    void "showZoneLocations lists the zones of a location"() {
        when:
        params.id = depot.id
        def model = controller.showZoneLocations()

        then:
        1 * locationService.getZones(depot) >> [zone]
        model.zoneLocations == [zone]

        when:
        response.reset()
        params.id = "missing"
        controller.showZoneLocations()

        then:
        response.text.startsWith("default.not.found.message")
    }

    void "exportBinLocations writes the bins of a location or zone as CSV"() {
        given:
        DataService dataService = Mock()
        controller.dataService = dataService
        List rows = null

        when:
        params.id = target.call(this)
        controller.exportBinLocations()

        then:
        1 * dataService.generateCsv(_) >> { args -> rows = args[0]; "csv" }
        response.contentType.startsWith("text/csv")
        response.getHeader("Content-disposition").startsWith("attachment; filename=\"BinLocations-Depot-")
        rows*.locationName as Set == expected as Set
        rows.find { it.locationName == "A-01" }.zoneName == "Zone A"
        rows.find { it.locationName == "A-01" }.locationType == LocationTypeCode.BIN_LOCATION

        where:
        target                | expected
        { s -> s.depot.id }   | ["A-01", "B-01"]
        { s -> s.zone.id }    | ["A-01"]
    }

    void "exportBinLocations requires a location and reports when it has no bins"() {
        when:
        params.id = binB.id
        controller.exportBinLocations()

        then:
        flash.message == "No bin locations for location B-01"
        response.redirectedUrl == "/location/edit/${binB.id}"

        when:
        params.id = "missing"
        controller.exportBinLocations()

        then:
        IllegalArgumentException e = thrown()
        e.message == "Must specify location"
    }

    void "importBinLocations imports the uploaded file"() {
        given:
        request.addFile(new MockMultipartFile("fileContents", "bins.csv", "text/csv", "name\nC-01".bytes))

        when:
        params.id = depot.id
        controller.importBinLocations()

        then:
        1 * locationService.importBinLocations(depot.id, _ as InputStream) >> imported
        flash.message == message
        response.redirectedUrl == "/location/edit/${depot.id}"

        where:
        imported | message
        true     | "Successfully imported all bin locations."
        false    | "Failed to import bin locations due to an unknown error."
    }

    void "importBinLocations rejects an empty file and reports import errors"() {
        when:
        request.addFile(new MockMultipartFile("fileContents", "bins.csv", "text/csv", new byte[0]))
        params.id = depot.id
        controller.importBinLocations()

        then:
        flash.message == "File cannot be empty."
        0 * locationService.importBinLocations(*_)
    }

    void "importBinLocations shows the edit page with the import error"() {
        given:
        request.addFile(new MockMultipartFile("fileContents", "bins.csv", "text/csv", "bad".bytes))
        locationService.importBinLocations(*_) >> { throw new IllegalStateException("Row 1 is invalid") }

        when:
        params.id = depot.id
        controller.importBinLocations()

        then:
        flash.message == "Row 1 is invalid"
        view == "/location/edit"
        model.locationInstance == depot
    }

    void "renderLogo and viewLogo stream the stored logo"() {
        given:
        depot.logo = [1, 2, 3] as byte[]
        depot.save(validate: false)

        when:
        params.id = depot.id
        controller.renderLogo()

        then:
        response.contentAsByteArray == [1, 2, 3] as byte[]
        response.contentLength == 3

        when:
        response.reset()
        controller.viewLogo()

        then:
        response.contentAsByteArray == [1, 2, 3] as byte[]
    }

    void "renderLogo answers 404 when there is no logo"() {
        when:
        params.id = binA.id
        controller.renderLogo()

        then:
        response.status == 404
    }

    void "uploadLogo stores an image and rejects other content types"() {
        given:
        request.method = "POST"
        request.addFile(new MockMultipartFile("logo", "logo.png", contentType, [7, 8] as byte[]))

        when:
        params.id = depot.id
        controller.uploadLogo()

        then:
        saves * inventoryService.saveLocation(depot)
        (depot.logo == ([7, 8] as byte[])) == stored
        flash.message.startsWith(message)

        where:
        contentType  | saves | stored | message
        "image/png"  | 1     | true   | "default.updated.message"
        "text/plain" | 0     | false  | "Photo must be one of"
    }

    void "uploadLogo shows the form on GET"() {
        when:
        params.id = depot.id
        def model = controller.uploadLogo()

        then:
        model.locationInstance == depot
    }

    void "deleteLogo clears the logo"() {
        given:
        depot.logo = [1] as byte[]
        controller.locationGormService = Mock(LocationDataService)

        when:
        params.id = depot.id
        controller.deleteLogo()

        then:
        1 * controller.locationGormService.save(depot)
        depot.logo.length == 0
        flash.message == "Logo has been deleted"
        response.redirectedUrl == "/location/uploadLogo/${depot.id}"
    }

    void "resetSupportedActivities clears the location's own activities"() {
        given:
        depot.supportedActivities = ["MANAGE_INVENTORY"] as Set
        controller.locationGormService = Mock(LocationDataService)

        when:
        params.id = depot.id
        controller.resetSupportedActivities()

        then:
        1 * controller.locationGormService.get(depot.id) >> depot
        1 * controller.locationGormService.save(depot)
        depot.supportedActivities.isEmpty()
        response.redirectedUrl == "/location/edit/${depot.id}"
    }
}
