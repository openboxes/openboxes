package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType

/**
 * The rules are asserted through doValidate (rather than validate) so that they can be read off the returned
 * result: validate() would only report whether the shipment is valid overall, after copying the errors onto it.
 */
@Unroll
class ShipmentForReceiptValidatorSpec extends Specification implements DataTest {

    ShipmentForReceiptValidator validator

    void setupSpec() {
        mockDomains(Receipt, ReceiptItem, Shipment, ShipmentItem, ShipmentType, Product, InventoryItem, Inventory,
                Location)
    }

    void setup() {
        validator = new ShipmentForReceiptValidator()
    }

    void 'doValidate should pass for a #shipmentStatus shipment with something left to receive'() {
        given:
        Shipment shipment = buildShipment([buildShipmentItem(100)], shipmentStatus)

        expect:
        validator.doValidate(shipment).valid

        where:
        shipmentStatus << [ShipmentStatusCode.SHIPPED, ShipmentStatusCode.PARTIALLY_RECEIVED]
    }

    void 'doValidate should reject a shipment that already carries a pending receipt'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildShipment([shipmentItem])
        createReceipt(shipment, [buildReceiptItem(shipmentItem, 0)], ReceiptStatusCode.PENDING)

        when:
        ObjectValidationResult result = validator.doValidate(shipment)

        then:
        assert !result.valid
        assert result.errors.first().code == "shipment.pendingReceiptExists.message"
    }

    void 'validateForReceivingAccess should let a shipment with a pending receipt through, so it can be resumed'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildShipment([shipmentItem])
        createReceipt(shipment, [buildReceiptItem(shipmentItem, 0)], ReceiptStatusCode.PENDING)

        expect:
        validator.validateForReceivingAccess(shipment, shipment.destination).valid
    }

    void 'doValidate should reject a shipment that is #description as not shipped'() {
        given:
        Shipment shipment = buildShipment([buildShipmentItem(100)], shipmentStatus)

        when:
        ObjectValidationResult result = validator.doValidate(shipment)

        then:
        assert !result.valid
        assert result.errors.first().code == "stockMovement.hasNotBeenShipped.message"

        where:
        shipmentStatus             | description
        ShipmentStatusCode.CREATED | 'created'
        ShipmentStatusCode.PENDING | 'pending'
        null                       | 'without a status at all'
    }

    void 'doValidate should reject a shipment with nothing left to receive'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildShipment([shipmentItem])
        createReceipt(shipment, [buildReceiptItem(shipmentItem, 100)], ReceiptStatusCode.RECEIVED)

        when:
        ObjectValidationResult result = validator.doValidate(shipment)

        then:
        assert !result.valid
        assert result.errors.first().code == "stockMovement.hasAlreadyBeenReceived.message"
    }

    void 'the error should carry the shipment number, so that the message names the shipment'() {
        given:
        Shipment shipment = buildShipment([buildShipmentItem(100)], ShipmentStatusCode.PENDING)
        shipment.shipmentNumber = "S-001"

        expect:
        validator.doValidate(shipment).errors.first().arguments == ["S-001"] as Object[]
    }

    void 'validateForReceivingAccess should pass at the destination of the shipment'() {
        given: 'a saved destination, so that the check compares persisted ids and not two nulls'
        Shipment shipment = buildShipment([buildShipmentItem(100)])
        assert shipment.destination.id

        expect:
        validator.validateForReceivingAccess(shipment, shipment.destination).valid
    }

    void 'validateForReceivingAccess should reject a location other than the destination of the shipment'() {
        given:
        Shipment shipment = buildShipment([buildShipmentItem(100)])
        Location otherLocation = new Location(name: "Other location")
        otherLocation.id = "other-location-id"

        when:
        ObjectValidationResult result = validator.validateForReceivingAccess(shipment, otherLocation)

        then:
        assert !result.valid
        assert result.errors.first().code == "stockMovement.isDifferentLocation.message"
    }

    private static Shipment buildShipment(
            List<ShipmentItem> shipmentItems, ShipmentStatusCode status = ShipmentStatusCode.SHIPPED) {
        Shipment shipment = new Shipment(
                name: "Test shipment",
                origin: new Location(name: "Origin"),
                destination: new Location(name: "Destination", inventory: new Inventory()),
                expectedShippingDate: new Date() - 7,
                shipmentType: new ShipmentType(name: "Default"),
        )
        // The event-based hooks and constraints assume an initialized collection (a TreeSet because the domain
        // declares the property as a SortedSet).
        shipment.events = new TreeSet()
        shipmentItems.each { ShipmentItem shipmentItem -> shipment.addToShipmentItems(shipmentItem) }
        shipment.save(failOnError: true, flush: true)
        shipment.currentStatus = status
        return shipment
    }

    private static ShipmentItem buildShipmentItem(Integer quantity) {
        Product product = new Product(name: "Product")
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: "LOT-1")
        return new ShipmentItem(quantity: quantity, product: product, inventoryItem: inventoryItem)
    }

    private static ReceiptItem buildReceiptItem(ShipmentItem shipmentItem, Integer quantityReceived, Map args = [:]) {
        ReceiptItem receiptItem = new ReceiptItem(
                product: shipmentItem.product,
                inventoryItem: shipmentItem.inventoryItem,
                quantityShipped: args.containsKey("quantityShipped") ? args.quantityShipped : shipmentItem.quantity,
                quantityReceived: quantityReceived,
                isSplitItem: args.isSplitItem ?: false,
        )
        shipmentItem.addToReceiptItems(receiptItem)
        return receiptItem
    }

    private static Receipt createReceipt(
            Shipment shipment, List<ReceiptItem> receiptItems, ReceiptStatusCode statusCode) {
        Receipt receipt = new Receipt(receiptStatusCode: statusCode, actualDeliveryDate: new Date() - 1)
        receiptItems.each { ReceiptItem receiptItem -> receipt.addToReceiptItems(receiptItem) }
        shipment.addToReceipts(receipt)
        receipt.save(failOnError: true, flush: true)
        return receipt
    }
}
