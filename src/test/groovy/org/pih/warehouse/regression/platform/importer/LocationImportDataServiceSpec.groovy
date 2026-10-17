package org.pih.warehouse.regression.platform.importer

import org.pih.warehouse.importer.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Address
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.OrganizationService
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.inventory.Inventory

/**
 * The organization service is mocked: it resolves organizations by name or code from the in-memory fixtures, and
 * records the organizations the import asks it to create.
 */
@Unroll
class LocationImportDataServiceSpec extends Specification implements ServiceUnitTest<LocationImportDataService>, DataTest {

    LocationType depotType
    LocationType supplierType
    LocationType binType
    LocationGroup bostonGroup
    Organization acme
    Location bostonDepot

    OrganizationService organizationService = Mock()

    void setupSpec() {
        mockDomains(Location, LocationType, LocationGroup, Organization, PartyRole, Address, Inventory)
    }

    void setup() {
        service.organizationService = organizationService
        organizationService.findOrganizations(_, _) >> { String name, String code -> findOrganizations(name, code) }
        organizationService.findOrganization(_, _) >> { String name, String code -> findOrganizations(name, code)?.find() }

        depotType = saveLocationType('lt-depot', 'Depot|fr:D\u00e9p\u00f4t', LocationTypeCode.DEPOT)
        supplierType = saveLocationType('lt-supplier', 'Supplier', LocationTypeCode.SUPPLIER)
        binType = saveLocationType('lt-bin', 'Bin Location', LocationTypeCode.BIN_LOCATION)
        // A second type whose name starts like the depot type, to prove the lookup matches the whole default name
        saveLocationType('lt-depot-2', 'Depot Annex', LocationTypeCode.DEPOT)

        bostonGroup = new LocationGroup(name: 'Boston').save(validate: false)
        acme = saveOrganization('ACME', 'Acme', true)
        bostonDepot = new Location(name: 'Boston Depot', locationNumber: 'BOS', locationType: depotType,
                organization: acme, active: true).save(validate: false)
    }

    void 'validateData should accept a new depot with an organization'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow()])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should match a location type on its default name only'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(locationType: 'Depot Annex')])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should accept the active flag given as #active'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(active: active)])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()

        where:
        active << [true, false, 'TRUE', 'false']
    }

    void 'validateData should reject #description'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(overrides)])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == expectedErrors

        where:
        description                          | overrides                     || expectedErrors
        'an id that does not exist'          | [id: 'missing']               || ["Row 1: Id 'missing' do not exists in the system."]
        'a name that already exists'         | [name: 'Boston Depot']        || ["Row 1: 'Boston Depot' already exists in the system. Choose a unique name"]
        'a missing active flag'              | [active: null]                || ['Row 1: Active field is obligatory. Please fill with TRUE or FALSE']
        'an active flag that is not boolean' | [active: 'yes']               || ['Row 1: Active field is incorrectly filled. Please fill with TRUE or FALSE']
        'a location number already in use'   | [locationNumber: 'BOS']       || ["Row 1: 'BOS' already exists in the system. Choose a unique location number"]
        'a location group that is unknown'   | [locationGroup: 'Nowhere']    || ["Row 1: Location Group 'Nowhere' does not exist"]
        'a location type that is unknown'    | [locationType: 'Warehouse']   || ["Row 1: Location Type 'Warehouse' does not exist"]
        'a depot without an organization'    | [organization: null]          || ["Row 1: Location Type 'Depot' requires an organization"]
        'a depot with a parent location'     | [parentLocation: 'Boston Depot'] || ["Row 1: Cannot assign a parent location to a location of type 'Depot'"]
    }

    void 'validateData should allow keeping the name and number of the location being edited'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(id: bostonDepot.id, name: 'Boston Depot', locationNumber: 'BOS')])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should reject a parent location that does not exist'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [binRow(parentLocation: 'Nowhere')])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [
                "Row 1: Parent Location 'Nowhere' does not exist",
                "Row 1: Location Type 'Bin Location' requires a parent location",
        ]
    }

    void 'validateData should accept a bin location with an existing parent and no organization'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [binRow()])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should reject names and numbers repeated within the import'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [
                depotRow(name: 'Twin', locationNumber: 'T1'),
                depotRow(name: 'Twin', locationNumber: 'T1'),
                depotRow(name: 'Other'),
        ])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [
                "Row 1: 'Twin' is not a unique name in imported data",
                "Row 1: 'T1' is not a unique location number in imported data",
                "Row 2: 'Twin' is not a unique name in imported data",
                "Row 2: 'T1' is not a unique location number in imported data",
        ]
    }

    void 'validateData should reject an organization identifier that matches several organizations'() {
        given:
        saveOrganization('ACME2', 'Acme', true)
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(organization: 'Acme')])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [
                "Row 1: Organization identifier 'Acme' matches '2' records. Please specify a unique organization code instead",
        ]
    }

    void 'validateData should reject an inactive organization'() {
        given:
        saveOrganization('GONE', 'Gone Inc', false)
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(organization: 'GONE')])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ["Row 1: Organization Gone Inc is inactive. You can't assign it to any location"]
    }

    void 'validateData should not allow an internal location to become a depot'() {
        given:
        Location bin = new Location(name: 'Bin A', locationType: binType, parentLocation: bostonDepot, active: true).save(validate: false)
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(id: bin.id, name: 'Bin A')])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ["Row 1: Changing Location Type from internal to 'Depot' is not possible"]
    }

    void 'importData should create a depot with an inventory, an address and the existing organization'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(
                locationNumber: 'NEW-1', locationGroup: 'Boston', active: 'false',
                streetAddress: '1 Main St', city: 'Boston', postalCode: '02110')])

        when:
        service.importData(command)

        then:
        0 * organizationService.createOrganization(*_)
        0 * organizationService.createSupplierOrganization(*_)

        and:
        Location location = Location.findByName('New Depot')
        location.locationNumber == 'NEW-1'
        location.active == false
        location.locationType == depotType
        location.locationGroup == bostonGroup
        location.organization == acme
        location.parentLocation == null
        location.inventory != null
        location.inventory.warehouse == location
        location.address.address == '1 Main St'
        location.address.address2 == ''
        location.address.city == 'Boston'
        location.address.postalCode == '02110'
    }

    void 'importData should create an unknown supplier organization once for all rows that name it'() {
        given:
        Organization created = saveOrganization('NEWCO', 'New Co', true)
        ImportDataCommand command = new ImportDataCommand(data: [
                depotRow(name: 'Supplier One', locationType: 'Supplier', organization: 'New Co'),
                depotRow(name: 'Supplier Two', locationType: 'Supplier', organization: 'New Co'),
        ])

        when:
        service.importData(command)

        then: 'the lookup in the setup stub is overridden so the organization is unknown at bind time'
        _ * organizationService.findOrganization(_, _) >> null
        1 * organizationService.createSupplierOrganization('New Co') >> created
        0 * organizationService.createOrganization(*_)

        and:
        Location.findByName('Supplier One').organization == created
        Location.findByName('Supplier Two').organization == created
    }

    void 'importData should create a regular organization for a depot with an unknown organization'() {
        given:
        Organization created = saveOrganization('FRESH', 'Fresh Org', true)
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(organization: 'Fresh Org')])

        when:
        service.importData(command)

        then:
        _ * organizationService.findOrganization(_, _) >> null
        1 * organizationService.createOrganization('Fresh Org') >> created
        0 * organizationService.createSupplierOrganization(*_)

        and:
        Location.findByName('New Depot').organization == created
    }

    void 'importData should create a bin location under its parent without an inventory or organization'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [binRow(parentLocation: 'BOS')])

        when:
        service.importData(command)

        then:
        0 * organizationService.createOrganization(*_)

        and: 'the parent is resolved by its location number'
        Location bin = Location.findByName('Bin A')
        bin.parentLocation == bostonDepot
        bin.locationType == binType
        bin.inventory == null
        bin.organization == null
    }

    void 'importData should update an existing location by id and reuse its address'() {
        given:
        Address address = new Address(address: 'Old street').save(validate: false)
        bostonDepot.address = address
        bostonDepot.supportedActivities = ['MANAGE_INVENTORY'] as Set
        bostonDepot.save(validate: false)

        and:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(
                id: bostonDepot.id, name: 'Boston Main', locationNumber: 'BOS', streetAddress: 'New street')])

        when:
        service.importData(command)

        then:
        Location location = Location.get(bostonDepot.id)
        location.name == 'Boston Main'
        location.address.id == address.id
        location.address.address == 'New street'

        and: 'the type did not change so the supported activities are kept'
        location.supportedActivities == ['MANAGE_INVENTORY'] as Set
    }

    void 'importData should match an existing location by name or number when no id is given'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(name: 'Boston Renamed', locationNumber: 'BOS')])

        when:
        service.importData(command)

        then:
        Location.count() == 1
        Location.get(bostonDepot.id).name == 'Boston Renamed'
    }

    void 'importData should clear the supported activities when the location type changes'() {
        given:
        bostonDepot.supportedActivities = ['MANAGE_INVENTORY'] as Set
        bostonDepot.save(validate: false)

        when:
        service.importData(new ImportDataCommand(data: [depotRow(id: bostonDepot.id, name: 'Boston Depot', locationType: 'Supplier')]))

        then:
        Location location = Location.get(bostonDepot.id)
        location.locationType == supplierType
        !location.supportedActivities
    }

    void 'importData should keep the type of an internal location that is imported as a depot'() {
        given:
        Location bin = new Location(name: 'Bin A', locationType: binType, parentLocation: bostonDepot, active: true).save(validate: false)

        when:
        service.importData(new ImportDataCommand(data: [binRow(id: bin.id, locationType: 'Depot')]))

        then:
        Location.get(bin.id).locationType == binType
    }

    void 'importData should fail when a location cannot be validated'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [depotRow(locationType: 'Warehouse')])

        when:
        service.importData(command)

        then:
        ValidationException e = thrown()
        e.message.startsWith('Invalid location New Depot')
        e.errors.getFieldError('locationType')
    }

    void 'bindAddress should create a new address when the id is unknown and default blank street lines'() {
        when:
        Address address = service.bindAddress([city: 'Boston', country: 'US'], 'missing')

        then:
        address.id == null
        address.address == ''
        address.address2 == ''
        address.city == 'Boston'
        address.country == 'US'
    }

    private Map depotRow(Map overrides = [:]) {
        return [name: 'New Depot', active: true, locationType: 'Depot', organization: 'ACME'] + overrides
    }

    private Map binRow(Map overrides = [:]) {
        return [name: 'Bin A', active: true, locationType: 'Bin Location', parentLocation: 'Boston Depot'] + overrides
    }

    private static List<Organization> findOrganizations(String name, String code) {
        Organization byCode = code ? Organization.findByCode(code) : null
        return byCode ? [byCode] : (name ? Organization.findAllByName(name) : [])
    }

    private static LocationType saveLocationType(String id, String name, LocationTypeCode code) {
        LocationType locationType = new LocationType(name: name, locationTypeCode: code)
        locationType.id = id
        return locationType.save(validate: false)
    }

    private static Organization saveOrganization(String code, String name, boolean active) {
        return new Organization(code: code, name: name, active: active).save(validate: false)
    }
}
