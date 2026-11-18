package org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType

class StockMovementServiceSpec extends Specification implements ServiceUnitTest<StockMovementService>, DataTest {

    static final String SHIPMENT_NUMBER = "SHIP-1"
    static final String RECEIVING_BIN_NAME = "R-${SHIPMENT_NUMBER}"
    static final String LEGACY_RECEIVING_BIN_NAME = "Receiving ${SHIPMENT_NUMBER}"

    LocationService locationService
    ShipmentService shipmentService
    LocationType depotType
    LocationType binType
    Location oldDestination
    Location newDestination

    void setupSpec() {
        mockDomains(Location, LocationType, Organization, Shipment, ShipmentType)
    }

    void setup() {
        locationService = Mock(LocationService) {
            getReceivingLocationName(_ as String) >> { String identifier -> "R-${identifier}".toString() }
        }
        shipmentService = Mock(ShipmentService)
        service.locationService = locationService
        service.shipmentService = shipmentService

        depotType = new LocationType(name: "Depot").save(validate: false, flush: true)
        binType = new LocationType(name: "Receiving").save(validate: false, flush: true)
        Organization organization = new Organization(name: "Org").save(validate: false, flush: true)
        oldDestination = new Location(name: "Old Depot", locationType: depotType, organization: organization)
                .save(validate: false, flush: true)
        newDestination = new Location(name: "New Depot", locationType: depotType, organization: organization)
                .save(validate: false, flush: true)
    }

    // OBPIH-8105: sanity check that the happy path from the fix works.
    void 'updateShipment should move the receiving bin to the new destination of a shipped shipment'() {
        given:
        Shipment shipment = buildPersistedShipment(ShipmentStatusCode.SHIPPED)
        Location receivingBin = buildReceivingBin(RECEIVING_BIN_NAME, oldDestination)
        locationService.findInternalLocation(oldDestination, _) >> receivingBin

        when:
        service.updateShipmentForShipmentBasedStockMovement(buildIssuedStockMovement(shipment, newDestination))

        then:
        assert receivingBin.parentLocation == newDestination
    }

    // OBPIH-8105 regression: createFromShipment sets statusCode=ISSUED for every non-PENDING shipment, so the bin
    // block now runs for RECEIVED shipments too. Only PARTIALLY_RECEIVED is guarded, so a fully received bin
    // (which holds received stock in the old depot's inventory) is moved to another depot.
    void 'updateShipment should not move the receiving bin of a fully received shipment'() {
        given:
        Shipment shipment = buildPersistedShipment(ShipmentStatusCode.RECEIVED)
        Location receivingBin = buildReceivingBin(RECEIVING_BIN_NAME, oldDestination)
        locationService.findInternalLocation(oldDestination, _) >> receivingBin

        when:
        service.updateShipmentForShipmentBasedStockMovement(buildIssuedStockMovement(shipment, newDestination))

        then:
        thrown(IllegalArgumentException)
        assert receivingBin.parentLocation == oldDestination
    }

    // OBPIH-8105 not fixed: receiving (ReceiptService, LocationService.findOrCreateInternalLocation) also matches
    // the legacy "Receiving <shipmentNumber>" bin name, but the destination change only looks up the prefixed name.
    // The legacy bin stays at the old destination and receiving at the new one still hits the unique error.
    void 'updateShipment should move a legacy-named receiving bin to the new destination'() {
        given:
        Shipment shipment = buildPersistedShipment(ShipmentStatusCode.SHIPPED)
        Location legacyBin = buildReceivingBin(LEGACY_RECEIVING_BIN_NAME, oldDestination)
        locationService.findInternalLocation(oldDestination, _) >> { Location parent, String[] names ->
            names.contains(LEGACY_RECEIVING_BIN_NAME) ? legacyBin : null
        }

        when:
        service.updateShipmentForShipmentBasedStockMovement(buildIssuedStockMovement(shipment, newDestination))

        then:
        assert legacyBin.parentLocation == newDestination
    }

    // OBPIH-8105 regression: the bin is saved with flush + failOnError, but the shipment is saved with a plain
    // save(). When the shipment fails validation nothing is thrown, so the transaction commits with the bin under
    // the new destination and the shipment still at the old one.
    void 'updateShipment should throw when the shipment fails validation after the bin was moved'() {
        given:
        Shipment shipment = buildPersistedShipment(ShipmentStatusCode.SHIPPED)
        Location receivingBin = buildReceivingBin(RECEIVING_BIN_NAME, oldDestination)
        locationService.findInternalLocation(oldDestination, _) >> receivingBin
        StockMovement stockMovement = buildIssuedStockMovement(shipment, newDestination)
        stockMovement.shipmentType = null  // makes the shipment invalid (shipmentType is required)

        when:
        service.updateShipmentForShipmentBasedStockMovement(stockMovement)

        then:
        thrown(ValidationException)
    }

    void 'getStockMovementReceiptItems should return all receipt items by default'() {
        given:
        ReceiptItem receivedReceiptItem = new ReceiptItem(sortOrder: 1, quantityReceived: 5)
        ReceiptItem nullQuantityReceiptItem = new ReceiptItem(sortOrder: 2)
        StockMovement stockMovement = buildStockMovement([buildReceipt([receivedReceiptItem, nullQuantityReceiptItem])])

        when:
        List<ReceiptItem> result = service.getStockMovementReceiptItems(stockMovement)

        then:
        assert result == [receivedReceiptItem, nullQuantityReceiptItem]
    }

    void 'getStockMovementReceiptItems should filter out the items without quantity received or canceled'() {
        given:
        ReceiptItem receivedReceiptItem = new ReceiptItem(sortOrder: 1, quantityReceived: 5)
        ReceiptItem canceledReceiptItem = new ReceiptItem(sortOrder: 2, quantityReceived: 0, quantityCanceled: 3)
        ReceiptItem zeroQuantityReceiptItem = new ReceiptItem(sortOrder: 3, quantityReceived: 0, quantityCanceled: 0)
        ReceiptItem nullQuantityReceiptItem = new ReceiptItem(sortOrder: 4)
        Receipt receipt = buildReceipt(
                [receivedReceiptItem, canceledReceiptItem, zeroQuantityReceiptItem, nullQuantityReceiptItem])
        StockMovement stockMovement = buildStockMovement([receipt])

        when:
        List<ReceiptItem> result = service.getStockMovementReceiptItems(stockMovement, true)

        then:
        assert result == [receivedReceiptItem, canceledReceiptItem]
    }

    void 'getStockMovementReceiptItems should sort by requisition item, shipment item, receipt item and product'() {
        given:
        ReceiptItem receiptItem1 = buildReceiptItem(0, 9, 9, "Z")
        ReceiptItem receiptItem2 = buildReceiptItem(1, 0, 9, "Z")
        ReceiptItem receiptItem3 = buildReceiptItem(1, 1, 0, "Z")
        ReceiptItem receiptItem4 = buildReceiptItem(1, 1, 1, "A")
        ReceiptItem receiptItem5 = buildReceiptItem(1, 1, 1, "B")
        StockMovement stockMovement = buildStockMovement([
                buildReceipt([receiptItem5, receiptItem4, receiptItem3, receiptItem2, receiptItem1]),
        ])

        when:
        List<ReceiptItem> result = service.getStockMovementReceiptItems(stockMovement)

        then:
        assert result == [receiptItem1, receiptItem2, receiptItem3, receiptItem4, receiptItem5]
    }

    private static StockMovement buildStockMovement(List<Receipt> receipts) {
        return new StockMovement(shipment: new Shipment(receipts: receipts as SortedSet))
    }

    private static Receipt buildReceipt(List<ReceiptItem> receiptItems) {
        return new Receipt(receiptItems: receiptItems as Set)
    }

    private static ReceiptItem buildReceiptItem(Integer orderIndex, Integer shipmentItemSortOrder, Integer sortOrder,
                                                String productName) {
        return new ReceiptItem(
                shipmentItem: new ShipmentItem(
                        sortOrder: shipmentItemSortOrder,
                        requisitionItem: new RequisitionItem(orderIndex: orderIndex),
                ),
                sortOrder: sortOrder,
                inventoryItem: new InventoryItem(product: new Product(name: productName)),
        )
    }

    private Shipment buildPersistedShipment(ShipmentStatusCode status) {
        Shipment shipment = new Shipment(
                name: "Shipment",
                shipmentNumber: SHIPMENT_NUMBER,
                origin: new Location(name: "Supplier", locationType: depotType).save(validate: false, flush: true),
                destination: oldDestination,
                shipmentType: new ShipmentType(name: "Default").save(validate: false, flush: true),
                expectedShippingDate: new Date(),
        ).save(validate: false, flush: true)
        // beforeInsert derives currentStatus from events, so set it after saving
        shipment.currentStatus = status
        return shipment
    }

    private Location buildReceivingBin(String name, Location parent) {
        return new Location(name: name, locationNumber: SHIPMENT_NUMBER, locationType: binType, parentLocation: parent)
                .save(validate: false, flush: true)
    }

    private static StockMovement buildIssuedStockMovement(Shipment shipment, Location destination) {
        StockMovement stockMovement = new StockMovement(
                id: shipment.id,
                identifier: shipment.shipmentNumber,
                statusCode: RequisitionStatus.ISSUED.toString(),
                origin: shipment.origin,
                destination: destination,
                shipmentType: shipment.shipmentType,
                dateShipped: shipment.expectedShippingDate,
        )
        // toJson() is only used for logging and needs the tag lib bean, which unit tests don't have
        stockMovement.metaClass.toJson = { -> [:] }
        return stockMovement
    }
}
