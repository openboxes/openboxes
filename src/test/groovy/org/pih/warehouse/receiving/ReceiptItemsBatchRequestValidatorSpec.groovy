package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

@Unroll
class ReceiptItemsBatchRequestValidatorSpec extends Specification implements DataTest {

    ReceiptItemsBatchRequestValidator validator = new ReceiptItemsBatchRequestValidator()

    void setupSpec() {
        mockDomains(Receipt, ReceiptItem, Product, Shipment, ShipmentItem, Location)
    }

    void cleanup() {
        // The current location lives in a static thread-local; clear it so it does not leak into
        // whatever spec runs next on this thread.
        new AuthService().setCurrentLocation(null)
    }

    void 'doValidate should reject editing a receipt when #scenario'() {
        given: 'a pending receipt on a shipment received into a destination'
        Location destination = saveLocation("Destination")
        Location currentLocation = currentLocationName ? saveLocation(currentLocationName) : null
        Receipt receipt = buildPendingReceipt()
        receipt.shipment = new Shipment(destination: destination)

        and:
        new AuthService().setCurrentLocation(currentLocation)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(receipt: receipt))

        then:
        assert !result.valid
        assert result.errors*.code == ["receiptItemsBatchRequest.receipt.isDifferentLocation"]

        where:
        scenario                                 | currentLocationName
        'logged in at another location'          | "Other location"
        'no location is selected in the session' | null
    }

    void 'doValidate should accept editing a receipt when logged in at the destination of its shipment'() {
        given:
        Location destination = saveLocation("Destination")
        Receipt receipt = buildPendingReceipt()
        receipt.shipment = new Shipment(destination: destination)

        and:
        new AuthService().setCurrentLocation(destination)

        expect:
        assert validator.doValidate(new ReceiptItemsBatchRequest(receipt: receipt)).valid
    }

    void 'doValidate should reject saving items into a bin location of another location, once per bin location'() {
        given: 'a pending receipt on a shipment received into the destination the user is logged in at'
        Location destination = saveLocation("Destination")
        Receipt receipt = buildPendingReceipt()
        receipt.shipment = new Shipment(destination: destination)
        new AuthService().setCurrentLocation(destination)

        and: 'a bin of the destination and a bin of another location'
        Location destinationBin = saveBinLocation("Destination bin", destination)
        Location foreignBin = saveBinLocation("Foreign bin", saveLocation("Other location"))

        when: 'two lines are saved into the foreign bin and one into the bin of the destination'
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToSave: [
                        buildUpsertRequest(null, foreignBin),
                        buildUpsertRequest(null, foreignBin),
                        buildUpsertRequest(null, destinationBin),
                ],
        ))

        then: 'only the foreign bin is rejected, and only once'
        assert !result.valid
        assert result.errors*.code == ["receiptItemsBatchRequest.itemsToSave.binLocationNotAtDestination"]
        assert result.errors.first().arguments == ["Foreign bin"] as Object[]
    }

    void 'doValidate should accept saving items into a bin location of the destination or without a bin location'() {
        given:
        Location destination = saveLocation("Destination")
        Receipt receipt = buildPendingReceipt()
        receipt.shipment = new Shipment(destination: destination)
        new AuthService().setCurrentLocation(destination)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToSave: [
                        buildUpsertRequest(null, saveBinLocation("Destination bin", destination)),
                        buildUpsertRequest(null),
                ],
        ))

        then:
        assert result.valid
    }

    void 'doValidate should reject deleting an original item'() {
        given: 'an original line and a split line'
        Receipt receipt = buildPendingReceipt()
        ReceiptItem originalItem = buildReceiptItem(receipt, false)
        ReceiptItem splitItem = buildReceiptItem(receipt, true)

        when: 'both are requested to be deleted'
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToDelete: [originalItem.id, splitItem.id],
        ))

        then: 'only the original line is rejected'
        assert !result.valid
        assert result.errors*.code == ["receiptItemsBatchRequest.itemsToDelete.originalItem"]
        assert result.errors.first().arguments.toString().contains(originalItem.id.toString())
        assert !result.errors.first().arguments.toString().contains(splitItem.id.toString())
    }

    void 'doValidate should accept deleting split items'() {
        given:
        Receipt receipt = buildPendingReceipt()
        ReceiptItem splitItem = buildReceiptItem(receipt, true)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToDelete: [splitItem.id],
        ))

        then:
        assert result.valid
    }

    void 'doValidate should leave unknown item identifiers for the service to report'() {
        given:
        Receipt receipt = buildPendingReceipt()

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToDelete: ["unknown-id"],
        ))

        then:
        assert result.valid
    }

    void 'doValidate should reject removing the bin location at a location supporting #supportedActivity'() {
        given: 'a pending receipt with one line put away to a bin and one line without a bin'
        Receipt receipt = buildPendingReceipt()
        ReceiptItem itemWithBin = buildReceiptItem(receipt, false, new Location(name: "Bin"))
        ReceiptItem itemWithoutBin = buildReceiptItem(receipt, true)
        receiveInto(receipt, supportedActivity)

        when: 'both are saved without a bin location'
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToSave: [buildUpsertRequest(itemWithBin), buildUpsertRequest(itemWithoutBin)],
        ))

        then: 'only the line that would lose its bin is rejected'
        assert !result.valid
        assert result.errors*.code == ["receiptItemsBatchRequest.itemsToSave.binLocationRemoved"]
        assert result.errors.first().arguments.toString().contains(itemWithBin.id.toString())
        assert !result.errors.first().arguments.toString().contains(itemWithoutBin.id.toString())

        where: 'either activity on its own makes the location track bin locations'
        supportedActivity << [ActivityCode.PICK_STOCK, ActivityCode.PUTAWAY_STOCK]
    }

    void 'doValidate should accept moving an item to another bin location'() {
        given:
        Receipt receipt = buildPendingReceipt()
        ReceiptItem receiptItem = buildReceiptItem(receipt, false, new Location(name: "Bin"))
        receiveInto(receipt, ActivityCode.PUTAWAY_STOCK)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToSave: [buildUpsertRequest(receiptItem, new Location(name: "Another bin"))],
        ))

        then:
        assert result.valid
    }

    void 'doValidate should accept a new item without a bin location'() {
        given: 'a receipt received into a bin tracking location'
        Receipt receipt = buildPendingReceipt()
        receiveInto(receipt, ActivityCode.PUTAWAY_STOCK)

        when: 'a line that does not exist yet is saved without a bin location'
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToSave: [buildUpsertRequest(null)],
        ))

        then: 'there is no bin location to remove'
        assert result.valid
    }

    void 'doValidate should accept removing the bin location at a location that does not track bin locations'() {
        given: 'a receipt received into a location without bin location support'
        Receipt receipt = buildPendingReceipt()
        ReceiptItem receiptItem = buildReceiptItem(receipt, false, new Location(name: "Bin"))
        receiveInto(receipt, ActivityCode.RECEIVE_STOCK)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToSave: [buildUpsertRequest(receiptItem)],
        ))

        then: 'the bin is not tracked there, so the client is free to drop it'
        assert result.valid
    }


    private static Receipt buildPendingReceipt() {
        Receipt receipt = new Receipt(receiptStatusCode: ReceiptStatusCode.PENDING, actualDeliveryDate: new Date())
        receipt.save(failOnError: true, flush: true)
        return receipt
    }

    private static ReceiptItem buildReceiptItem(Receipt receipt, Boolean isSplitItem, Location binLocation = null) {
        ReceiptItem receiptItem = new ReceiptItem(
                product: new Product(name: "Product"),
                quantityShipped: isSplitItem ? 0 : 100,
                isSplitItem: isSplitItem,
                binLocation: binLocation,
        )
        receipt.addToReceiptItems(receiptItem)
        receiptItem.save(failOnError: true, flush: true)
        return receiptItem
    }

    /**
     * Puts the receipt on a shipment received into a location supporting the given activity, which is what decides
     * whether that location tracks bin locations (Location.hasBinLocationSupport). The shipment is attached after the
     * items are persisted so that it stays out of the datastore.
     */
    private static void receiveInto(Receipt receipt, ActivityCode supportedActivity) {
        receipt.shipment = new Shipment(destination: new Location(
                name: "Destination",
                supportedActivities: [supportedActivity.id] as Set,
        ))
    }

    private static ReceiptItemUpsertRequest buildUpsertRequest(ReceiptItem receiptItem, Location binLocation = null) {
        return new ReceiptItemUpsertRequest(
                receiptItem: receiptItem,
                shipmentItem: new ShipmentItem(),
                quantityReceiving: 5,
                binLocation: binLocation,
        )
    }

    /**
     * Persists a location so that it carries an id, which is what the current location and the parent location of
     * a bin location are compared by.
     */
    private static Location saveLocation(String name) {
        Location location = new Location(name: name)
        location.save(validate: false, flush: true)
        return location
    }

    private static Location saveBinLocation(String name, Location parentLocation) {
        Location binLocation = new Location(name: name, parentLocation: parentLocation)
        binLocation.save(validate: false, flush: true)
        return binLocation
    }
}
