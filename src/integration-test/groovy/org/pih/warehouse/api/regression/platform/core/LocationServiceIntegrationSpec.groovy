package org.pih.warehouse.api.regression.platform.core

import spock.lang.PendingFeature



import spock.lang.Ignore
import org.pih.warehouse.core.*
import grails.validation.ValidationException
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook

import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
/**
 * The location queries against a real database: a depot with a bin, a zone, a receiving and a hold location, a ward
 * that submits requests, and a supplier.
 */
class LocationServiceIntegrationSpec extends ServiceIntegrationSpec {

    public static final String RECEIVING_LOCATION_TYPE_ID = 'ff8081816482352b01648249e8cc0001'
    public static final String DISPENSARY_LOCATION_TYPE_ID = '3'

    LocationService locationService

    Location depot
    Location bin
    Location zone
    Location receiving
    Location hold
    Location ward
    Location supplier

    void setup() {
        depot = newDepot('Location depot')
        bin = newBin(depot, uniqueName('B'))
        zone = newLocation('Z', 'ZONE', null, depot)
        receiving = internal(uniqueName('R'), RECEIVING_LOCATION_TYPE_ID)
        hold = internal(uniqueName('H'), 'HOLD_LOCATION')
        ward = newLocation('Ward', DISPENSARY_LOCATION_TYPE_ID, [ActivityCode.SUBMIT_REQUEST.id, ActivityCode.RECEIVE_STOCK.id])
        supplier = newLocation('Supplier', Constants.SUPPLIER_LOCATION_TYPE_ID)
    }

    private Location internal(String name, String locationTypeId) {
        return committed {
            new Location(name: name, locationNumber: name, parentLocation: Location.get(depot.id), locationType: LocationType.get(locationTypeId),
                    active: true).save(failOnError: true, flush: true)
        }
    }

    private static List<String> ids(def locations) {
        return locations*.id
    }

    void 'the internal locations of a depot, by type, activity and name'() {
        when:
        Map result = asAdminAt(depot) {
            Location parent = Location.get(depot.id)
            [
                    bins       : ids(locationService.getBinLocations(parent)) as Set,
                    zones      : ids(locationService.getZones(parent)),
                    internal   : ids(locationService.getInternalLocations(parent)) as Set,
                    putaway    : ids(locationService.getInternalLocations(parent, [ActivityCode.PUTAWAY_STOCK] as ActivityCode[])) as Set,
                    picking    : ids(locationService.getInternalLocations(parent, [ActivityCode.PICK_STOCK] as ActivityCode[])),
                    receiving  : ids(locationService.getInternalLocations(parent, [ActivityCode.RECEIVE_STOCK] as ActivityCode[])),
                    crossDock  : ids(locationService.getInternalLocations(parent, [ActivityCode.CROSS_DOCKING] as ActivityCode[])),
                    putawayList: ids(locationService.getPutawayLocations(parent)) as Set,
                    pickingList: ids(locationService.getPickingLocations(parent)),
                    receiveList: ids(locationService.getReceivingLocations(parent)),
                    crossList  : ids(locationService.getCrossDockingLocations(parent)),
                    byTypes    : ids(locationService.getInternalLocations(parent,
                            [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION] as LocationTypeCode[],
                            [ActivityCode.PICK_STOCK] as ActivityCode[], [hold.name] as String[])) as Set,
                    noBins     : locationService.getInternalLocations(Location.get(ward.id)),
                    anyPick    : ids(locationService.getInternalLocations(new InternalLocationSearchCommand(location: parent,
                            anyActivityCodes: [ActivityCode.PICK_STOCK], ignoreActivityCodes: [ActivityCode.RECEIVE_STOCK]))),
                    allHold    : ids(locationService.getInternalLocations(new InternalLocationSearchCommand(location: parent,
                            allActivityCodes: [ActivityCode.PUTAWAY_STOCK, ActivityCode.HOLD_STOCK]))),
                    named      : ids(locationService.getInternalLocations(new InternalLocationSearchCommand(location: parent,
                            locationNames: [zone.name], anyActivityCodes: [ActivityCode.RECEIVE_STOCK]))) as Set,
                    searched   : ids(locationService.searchInternalLocations([parentLocation: [id: depot.id], searchTerm: receiving.name],
                            [LocationTypeCode.INTERNAL] as LocationTypeCode[])),
            ]
        }

        then:
        result.bins == [bin.id, receiving.id, hold.id] as Set
        result.zones == [zone.id]
        result.internal == [receiving.id, hold.id] as Set
        result.putaway == [receiving.id, hold.id] as Set
        result.picking == [receiving.id]
        result.receiving == [receiving.id]
        result.crossDock == []
        result.putawayList == result.putaway
        result.pickingList == result.picking
        result.receiveList == result.receiving
        result.crossList == []
        result.byTypes == [bin.id, receiving.id, hold.id] as Set
        result.noBins == []
        result.anyPick == [bin.id]
        result.allHold == [hold.id]
        result.named == [receiving.id, zone.id] as Set
        result.searched == [receiving.id]
    }

    void 'receiving locations of a shipment are found or created once'() {
        when:
        Location created = asAdminAt(depot) {
            locationService.findOrCreateInternalLocation('SHIP-1', uniqueName('N'), LocationType.get(RECEIVING_LOCATION_TYPE_ID),
                    Location.get(depot.id))
        }
        Map result = asAdminAt(depot) {
            Location found = locationService.findOrCreateInternalLocation('SHIP-1', uniqueName('N'),
                    LocationType.get(RECEIVING_LOCATION_TYPE_ID), Location.get(depot.id))
            [
                    name   : created.name,
                    sameId : created.id == found.id,
                    lookup : locationService.findInternalLocation(Location.get(depot.id), ['R-SHIP-1'] as String[])?.id == created.id,
                    prefix : locationService.getReceivingLocationName('SHIP-2'),
            ]
        }

        then:
        result.name == 'R-SHIP-1'
        result.sameId
        result.lookup
        result.prefix == 'R-SHIP-2'

        when:
        asAdminAt(depot) { locationService.findOrCreateInternalLocation('SHIP-1', null, null, null) }

        then:
        thrown(IllegalArgumentException)
    }

    void 'top-level locations are listed by name, type, organization, activity and direction'() {
        given:
        User admin = asAdminAt(depot) { User.findByUsername('admin') }

        when:
        Map result = asAdminAt(depot) {
            Location current = Location.get('1')
            User user = User.get(admin.id)
            Closure<List<String>> filtered = { Map params, String direction = null, boolean excludeDisabled = false ->
                ids(locationService.getLocations(null, params, true, direction, current, user, excludeDisabled))
            }
            [
                    all          : ids(locationService.getAllLocations()),
                    byName       : ids(locationService.getLocations(null, [name: depot.name])),
                    projected    : locationService.getLocations(['id', 'name'] as String[],
                            [name: depot.name, locationTypeCode: LocationTypeCode.DEPOT, withOrganization: true]).collect { it as List },
                    inbound      : filtered([name: supplier.name], 'INBOUND'),
                    outbound     : filtered([name: depot.name], 'OUTBOUND'),
                    returns      : filtered([name: ward.name, isReturnOrder: true]),
                    depotsOnly   : filtered([name: depot.name, locationTypeCode: 'DEPOT']),
                    noDepot      : filtered([name: ward.name, locationTypeCode: 'DEPOT']),
                    userFiltered : filtered([name: depot.name, applyUserFilter: true, withOrganization: true], null, true),
                    paged        : ids(locationService.getLocations(Location.get('1').organization, LocationType.get('2'), null,
                            depot.name, 10, 0, 'name', 'asc')),
                    pagedBins    : ids(locationService.getLocations(null, LocationType.get(BIN_LOCATION_TYPE_ID), null,
                            bin.name, 10, 0, 'name', 'asc')),
            ]
        }

        then:
        result.all.containsAll([depot.id, ward.id, supplier.id, '1'])
        !result.all.contains(bin.id)
        result.byName == [depot.id]
        result.projected == [[depot.id, depot.name]]
        result.inbound == [supplier.id]
        result.outbound == [depot.id]
        result.returns == [ward.id]
        result.depotsOnly == [depot.id]
        result.noDepot == []
        result.userFiltered == [depot.id]
        result.paged == [depot.id]
        result.pagedBins == [bin.id]
    }

    void 'locations by the activities they support'() {
        when:
        Map result = asAdminAt(depot) {
            Location parent = Location.get(depot.id)
            [
                    depots               : ids(locationService.getDepots()),
                    nearby               : ids(locationService.getNearbyLocations(parent)),
                    external             : ids(locationService.getExternalLocations()),
                    dispensaries         : ids(locationService.getDispensaries(parent)),
                    submitRequest        : ids(locationService.getLocationsSupportingActivity(ActivityCode.SUBMIT_REQUEST)),
                    shipmentOrigins      : ids(locationService.getShipmentOrigins()),
                    shipmentDestinations : ids(locationService.getShipmentDestinations()),
                    requestOrigins       : ids(locationService.getRequestOrigins(parent)),
                    requestDestinations  : ids(locationService.getRequestDestinations(parent)),
                    transactionSources   : ids(locationService.getTransactionSources(parent)),
                    transactionTargets   : ids(locationService.getTransactionDestinations(parent)),
                    orderSuppliers       : ids(locationService.getOrderSuppliers(parent)),
                    suppliers            : ids(locationService.getSuppliers(supplier.name, 10, 0)),
                    byOrganization       : locationService.getSuppliers(Location.get('1').organization.name, 1000, 0).size(),
                    byActivity           : ids(locationService.getLocations(null, new grails.web.servlet.mvc.GrailsParameterMap(
                            [name: depot.name, activityCodes: ['MANAGE_INVENTORY', 'PICK_STOCK']], new org.springframework.mock.web.MockHttpServletRequest()),
                            true, null, Location.get('1'), User.findByUsername('admin'))),
                    wardByActivity       : ids(locationService.getLocations(null, new grails.web.servlet.mvc.GrailsParameterMap(
                            [name: ward.name, activityCodes: 'MANAGE_INVENTORY'], new org.springframework.mock.web.MockHttpServletRequest()),
                            true, null, Location.get('1'), User.findByUsername('admin'))),
            ]
        }

        then:
        result.depots.contains(depot.id)
        !result.depots.contains(ward.id)
        result.nearby.containsAll([depot.id, ward.id])
        result.external.contains(depot.id)
        result.dispensaries.contains(ward.id)
        !result.dispensaries.contains(depot.id)
        result.submitRequest.containsAll([depot.id, ward.id])
        result.shipmentOrigins.containsAll([depot.id, supplier.id])
        result.shipmentDestinations.containsAll([depot.id, ward.id])
        result.requestOrigins.contains(depot.id)
        !result.requestOrigins.contains(ward.id)
        result.requestDestinations.contains(ward.id)
        result.transactionSources.contains(supplier.id)
        !result.transactionSources.contains(depot.id)
        result.transactionTargets.containsAll([depot.id, ward.id])
        !result.transactionTargets.contains(supplier.id)
        !result.orderSuppliers.contains(depot.id)
        result.suppliers == [supplier.id]
        result.byOrganization >= 1
        result.byActivity == [depot.id]
        result.wardByActivity == []
    }

    /** The role of the type; the requestor role is not part of the installed data. */
    private static Role role(RoleType roleType) {
        return Role.findByRoleType(roleType) ?: new Role(roleType: roleType, name: roleType.name()).save(failOnError: true, flush: true)
    }

    private User newUser(List<RoleType> roles, Map<Location, RoleType> locationRoles) {
        return committed {
            String name = uniqueName('user')
            User user = new User(username: name, password: 'password', passwordConfirm: 'password', firstName: name,
                    lastName: 'Tester', email: "${name}@example.com", active: true)
            roles.each { user.addToRoles(role(it)) }
            locationRoles.each { Location location, RoleType roleType ->
                user.addToLocationRoles(new LocationRole(location: Location.get(location.id), role: role(roleType)))
            }
            user.save(failOnError: true, flush: true)
        }
    }

    private Map<String, List<String>> loginLocations(User user) {
        return asAdminAt(depot) {
            locationService.getLoginLocationsMap(User.get(user.id), Location.get(depot.id)).collectEntries { key, value ->
                [(key): value*.id]
            }
        }
    }

    void 'locations are created with the current location group and deleted from their parent'() {
        given:
        String name = uniqueName('Created')

        when:
        Map result = asAdminAt(depot) {
            Location location = new Location(name: name, locationType: LocationType.get(DISPENSARY_LOCATION_TYPE_ID), active: true)
            location.addToSupportedActivities(ActivityCode.SUBMIT_REQUEST.id)
            location.inventory = new org.pih.warehouse.inventory.Inventory(warehouse: location)
            Location created = locationService.createLocation(location, true, true)
            Location binWithZone = Location.get(bin.id)
            binWithZone.zone = Location.get(zone.id)
            binWithZone.save(flush: true)
            locationService.deleteLocation(binWithZone)
            [
                    id        : created.id,
                    activities: created.supportedActivities,
                    group     : created.locationGroup?.id,
            ]
        }

        then:
        result.id
        !result.activities
        result.group == asAdminAt(depot) { Location.get(depot.id).locationGroup?.id }
        asAdminAt(depot) { Location.get(bin.id) } == null

        when:
        asAdminAt(depot) {
            Organization inactive = new Organization(name: uniqueName('Inactive org'), code: uniqueName('IO'), active: false,
                    partyType: Location.get('1').organization.partyType).save(failOnError: true, flush: true)
            locationService.createLocation(new Location(name: uniqueName('Refused'), locationType: LocationType.get('2'),
                    organization: inactive, active: true), false, false)
        }

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('is inactive')
    }

    private static InputStream workbook(List<List<Object>> rows) {
        HSSFWorkbook workbook = new HSSFWorkbook()
        HSSFSheet sheet = workbook.createSheet('Bins')
        rows.eachWithIndex { List<Object> values, int rowIndex ->
            HSSFRow row = sheet.createRow(rowIndex)
            values.eachWithIndex { Object value, int cellIndex ->
                if (value instanceof Number) {
                    row.createCell(cellIndex).setCellValue((value as Number).doubleValue())
                } else if (value != null) {
                    row.createCell(cellIndex).setCellValue(value as String)
                }
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        return new ByteArrayInputStream(out.toByteArray())
    }

    @PendingFeature(reason = 'a bin import error is thrown as ValidationException(String), a constructor that does not exist')
    void 'bin locations are imported from a spreadsheet, with their zones'() {
        given:
        String binName = uniqueName('IB')
        Integer numericName = Math.abs(new Random().nextInt(900000)) + 100000

        when:
        boolean imported = asAdminAt(depot) {
            locationService.importBinLocations(depot.id, workbook([['Name', 'Zone'], [binName, zone.name], [numericName, null], [null, null]]))
        }
        Map bins = asAdminAt(depot) {
            Location parent = Location.get(depot.id)
            [
                    zoned  : Location.findByNameAndParentLocation(binName, parent)?.zone?.id,
                    numeric: Location.findByNameAndParentLocation(numericName.toString(), parent)?.locationType?.id,
            ]
        }

        then:
        imported
        bins.zoned == zone.id
        bins.numeric

        when:
        asAdminAt(depot) { locationService.importBinLocations(depot.id, workbook([['Name', 'Zone'], [uniqueName('IB'), 'No such zone']])) }

        then:
        ValidationException unknownZone = thrown()
        unknownZone.message.contains('No such zone')

        when:
        asAdminAt(depot) { locationService.importBinLocations(depot.id, workbook([['Name', 'Zone']])) }

        then:
        ValidationException empty = thrown()
        empty.message.contains('at least one bin location')

        when:
        asAdminAt(depot) { locationService.importBinLocations('no-such-location', workbook([['Name', 'Zone'], [uniqueName('IB'), null]])) }

        then:
        IllegalArgumentException noLocation = thrown()
        noLocation.message == 'location.cannotImportBinLocationsWithoutParentLocation.message'

    }
}
