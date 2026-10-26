package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.util.concurrent.PollingConditions

import org.pih.warehouse.api.regression.platform.client.core.LocationApi
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType

/**
 * Creating, changing and deleting locations the way the locations configuration page and the stock movement
 * wizard's "add destination" dialog do, the location type and activity lookups those pages load, the bin location
 * template and import, and the current location's product summary.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class LocationWriteApiSpec extends RegressionApiSpec {

    static final String DEPOT_TYPE_ID = '2'
    static final String BIN_LOCATION_TYPE_ID = 'cab2b4f35ba2d867015ba2e17e390001'

    @Autowired
    LocationApi locationApi

    private static String uniqueName(String prefix) {
        return "${prefix} ${UUID.randomUUID().toString().take(8)}"
    }

    private static Location reload(String id) {
        return Location.withNewSession { Location.get(id) }
    }

    void 'create a location as the configuration page does: number and inventory are generated'() {
        given:
        String name = uniqueName('Created Depot')
        String organizationId = facility.organization.id

        when:
        Response response = locationApi.create(JsonOutput.toJson([
                name                : name,
                active              : true,
                locationType        : [id: DEPOT_TYPE_ID],
                organization        : [id: organizationId],
                supportedActivities : [ActivityCode.MANAGE_INVENTORY.id, ActivityCode.RECEIVE_STOCK.id],
        ]), [useDefaultActivities: false], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.name', Matchers.equalTo(name))
                .expectBody('data.locationNumber', Matchers.not(Matchers.isEmptyOrNullString()))
                .build())
        String id = response.jsonPath().getString('data.id')

        then:
        Location.withNewSession {
            Location created = Location.get(id)
            assert created.locationType.id == DEPOT_TYPE_ID
            assert created.organization.id == organizationId
            assert created.inventory != null
            assert created.supportedActivities == [ActivityCode.MANAGE_INVENTORY.id, ActivityCode.RECEIVE_STOCK.id] as Set
            true
        }
    }

    void 'create a destination as the add-destination dialog does: default activities and the current location group'() {
        given: 'a type the dialog offers (one supporting dynamic creation, such as a ward)'
        String name = uniqueName('Created Destination')
        String typeId = LocationType.withNewSession {
            LocationType.list().find { it.supports(ActivityCode.DYNAMIC_CREATION) }.id
        }

        when:
        Response response = locationApi.create(JsonOutput.toJson([name: name, locationType: typeId]),
                [useDefaultActivities: true, assignCurrentLocationGroup: true], responseSpecUtil.OK_RESPONSE_SPEC)

        then: 'no activities of its own (the type decides) and the facility group'
        Location.withNewSession {
            Location created = Location.get(response.jsonPath().getString('data.id'))
            assert created.name == name
            assert created.locationType.id == typeId
            assert !created.supportedActivities
            assert created.locationGroup?.id == facility.locationGroup?.id
            true
        }
    }

    @PendingFeature(reason = 'a location create without a name is not refused with a 400 in the error shape (200)')
    void 'create refuses a location without a name: 400'() {
        expect:
        locationApi.create(JsonOutput.toJson([locationType: [id: DEPOT_TYPE_ID]]), [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
    }

    @PendingFeature(reason = 'a create with an inactive organization answers 500')
    void 'create refuses an inactive organization: 400'() {
        given:
        String organizationId = Organization.withNewTransaction {
            Organization organization = new Organization(name: uniqueName('Inactive Org'), code: uniqueName('IO'),
                    active: false, partyType: PartyType.findByCode(Constants.DEFAULT_ORGANIZATION_CODE))
            organization.save(failOnError: true, flush: true)
            organization.id
        }

        expect:
        locationApi.create(JsonOutput.toJson([name: uniqueName('Org Check'), locationType: [id: DEPOT_TYPE_ID],
                                              organization: [id: organizationId]]), [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorMessage', Matchers.containsString('inactive'))
                .build())
    }

    void 'update a location with #method changes its name and keeps the rest'() {
        given:
        String id = Location.withNewTransaction {
            new LocationTestBuilder().name(uniqueName('To Rename')).asFacility().build(true).id
        }
        String newName = uniqueName('Renamed')

        when:
        locationApi.update(id, JsonOutput.toJson([name: newName]), [:], method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.name', Matchers.equalTo(newName))
                .build())

        then:
        Location.withNewSession {
            Location updated = Location.get(id)
            assert updated.name == newName
            assert updated.locationType.id == DEPOT_TYPE_ID
            true
        }

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'update with useDefaultActivities drops the location own activities'() {
        given:
        String id = Location.withNewTransaction {
            Location location = new LocationTestBuilder().name(uniqueName('Own Activities')).asFacility().build()
            location.supportedActivities = [ActivityCode.MANAGE_INVENTORY.id] as Set
            location.save(failOnError: true, flush: true)
            location.id
        }

        when:
        locationApi.update(id, JsonOutput.toJson([name: uniqueName('Defaults')]), [useDefaultActivities: true],
                Method.POST, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Location.withNewSession { !Location.get(id).supportedActivities }
    }

    @PendingFeature(reason = 'updating an unknown location answers 500')
    void 'update an unknown location: 404'() {
        expect:
        locationApi.update(INVALID_ID, JsonOutput.toJson([name: 'x']), [:], Method.PUT, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_NOT_FOUND))
                .build())
    }

    @PendingFeature(reason = 'every location delete through the API fails (500)')
    void 'delete a location: 204 and it is gone'() {
        given:
        String id = Location.withNewTransaction {
            new LocationTestBuilder().name(uniqueName('To Delete')).build(true).id
        }

        when:
        locationApi.delete(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        reload(id) == null
    }

    @PendingFeature(reason = 'deleting an unknown location answers 500')
    void 'delete an unknown location: 404'() {
        expect:
        locationApi.delete(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'deleting a location in use answers 500')
    void 'delete a zone that bins are assigned to: 409, nothing deleted'() {
        given:
        Map ids = Location.withNewTransaction {
            Location zone = new LocationTestBuilder().name(uniqueName('Zone')).build()
            zone.locationType = LocationType.findByLocationTypeCode(org.pih.warehouse.core.LocationTypeCode.ZONE)
            zone.parentLocation = Location.get(facility.id)
            zone.save(failOnError: true, flush: true)
            Location bin = new LocationTestBuilder().name(uniqueName('Zoned Bin')).asBinLocation(Location.get(facility.id)).build()
            bin.zone = zone
            bin.save(failOnError: true, flush: true)
            [zone: zone.id, bin: bin.id]
        }

        expect:
        locationApi.delete(ids.zone, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CONFLICT)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_CONFLICT))
                .build())
        reload(ids.zone) != null
    }

    void 'location types: all of them, or those supporting an activity'() {
        given:
        List<Map> all = LocationType.withNewSession { LocationType.list().collect { [id: it.id, supportsDynamic: it.supports(ActivityCode.DYNAMIC_CREATION)] } }

        expect:
        locationApi.locationTypes([], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.containsInAnyOrder(all*.id.toArray()))
                .build())
        locationApi.locationTypes([ActivityCode.DYNAMIC_CREATION.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.containsInAnyOrder(all.findAll { it.supportsDynamic }*.id.toArray()))
                .build())
    }

    void 'supported activities are every activity code'() {
        expect:
        locationApi.supportedActivities(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.equalTo(ActivityCode.list()*.name()))
                .build())
    }

    void 'the product summary of the current location has the quantity on hand'() {
        given:
        setStock(product, null, null, 7)

        expect: 'once the availability refresh has run'
        new PollingConditions(timeout: 30, delay: 0.5).eventually {
            Response response = locationApi.productSummary(facility.id, responseSpecUtil.OK_RESPONSE_SPEC)
            Map row = response.jsonPath().getList('data').find { it.productId == product.id } as Map
            assert row?.productCode == product.productCode
            assert row?.quantityOnHand == 7
        }
    }

    @PendingFeature(reason = 'the bin location template download has no content type')
    void 'the bin location template is the Excel file'() {
        when:
        Response response = locationApi.downloadBinLocationTemplate(responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        response.header('Content-disposition').contains('binLocations.xls')
        response.contentType.startsWith('application/vnd.ms-excel')
        new HSSFWorkbook(new ByteArrayInputStream(response.asByteArray())).getSheetAt(0) != null
    }

    private static byte[] binSheet(List<List<String>> rows) {
        HSSFWorkbook workbook = new HSSFWorkbook()
        HSSFSheet sheet = workbook.createSheet('Bins')
        sheet.createRow(0).with { createCell(0).setCellValue('Name'); createCell(1).setCellValue('Zone') }
        rows.eachWithIndex { List<String> cells, int index ->
            def row = sheet.createRow(index + 1)
            cells.eachWithIndex { String value, int column -> if (value != null) { row.createCell(column).setCellValue(value) } }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        return out.toByteArray()
    }

    void 'import bin locations: new bins under the location, an existing one kept'() {
        given: 'bin names of this run (a bin number is its name, and numbers are unique across all locations)'
        String prefix = UUID.randomUUID().toString().take(6)
        List<String> names = ['A-01', 'A-02', 'A-03'].collect { "${prefix}-${it}".toString() }
        Map ids = Location.withNewTransaction {
            Location depot = new LocationTestBuilder().name(uniqueName('Bin Depot')).asFacility().build(true)
            Location existing = new LocationTestBuilder().name(names[0]).asBinLocation(depot).build(true)
            [depot: depot.id, existing: existing.id]
        }

        when:
        locationApi.importBinLocations(ids.depot, 'bins.xls', binSheet([[names[0], null], [names[1], null], [" ${names[2]} ".toString(), '']]),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Location.withNewSession {
            List<Location> bins = Location.findAllByParentLocation(Location.get(ids.depot))
            assert bins*.name.sort() == names
            assert bins.find { it.name == names[0] }.id == ids.existing
            assert bins.every { it.locationType.id == BIN_LOCATION_TYPE_ID }
            true
        }
    }

    @PendingFeature(reason = 'an unknown zone answers 500')
    void 'import bin locations with an unknown zone: 400, nothing imported'() {
        given:
        String depotId = Location.withNewTransaction {
            new LocationTestBuilder().name(uniqueName('Zone Check Depot')).asFacility().build(true).id
        }

        expect:
        locationApi.importBinLocations(depotId, 'bins.xls', binSheet([["${UUID.randomUUID().toString().take(6)}-B-01".toString(), 'No Such Zone']]), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorMessage', Matchers.containsString('No Such Zone'))
                .build())
        Location.withNewSession { Location.findAllByParentLocation(Location.get(depotId)).isEmpty() }
    }

    @PendingFeature(reason = 'no bins answers 500')
    void 'import bin locations without rows: 400'() {
        expect:
        locationApi.importBinLocations(facility.id, 'bins.xls', binSheet([]), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
    }

    @PendingFeature(reason = 'an unknown location answers 500')
    void 'import bin locations into an unknown location: 404'() {
        expect:
        locationApi.importBinLocations(INVALID_ID, 'bins.xls', binSheet([["${UUID.randomUUID().toString().take(6)}-C-01".toString(), null]]),
                responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'an unreadable file answers 500')
    void 'import bin locations from a file that is not a spreadsheet: 400'() {
        expect:
        locationApi.importBinLocations(facility.id, 'bins.xls', 'not a spreadsheet'.bytes, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
    }
}
