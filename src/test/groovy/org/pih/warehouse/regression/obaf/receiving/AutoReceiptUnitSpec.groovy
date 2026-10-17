package org.pih.warehouse.regression.obaf.receiving

import grails.core.GrailsApplication
import grails.testing.gorm.DataTest
import org.springframework.context.ApplicationContext
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.BackorderService
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.PartialReceiptContainer
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.ShipmentEvent
import org.pih.warehouse.core.WebhookEventType
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * ReceiptService.receiveInboundShipment's gates (in the order the code applies them) and the
 * receipt createAutomaticReceipt builds, with the database calls stubbed. The bulk pass is in
 * ReceiptBulkPassUnitSpec; the same paths on a real database in AutoReceiptIntegrationSpec.
 */
class AutoReceiptUnitSpec extends Specification implements DataTest {

    ReceiptService receipts
    ApplicationContext context = Mock()
    BackorderService backorderService = Mock()

    void setup() {
        receipts = Spy(ReceiptService)
        receipts.grailsApplication = Stub(GrailsApplication) { getMainContext() >> context }
        receipts.backorderService = backorderService
    }

    Location facility(Collection<ActivityCode> activities) {
        Location f = new Location(name: 'F')
        f.supportedActivities = activities*.id as Set<String>
        return f
    }

    Shipment shipment(Map state) {
        Location destination = facility(state.containsKey('activities') ? state.activities : [ActivityCode.AUTO_RECEIVING])
        Errors errors = Mock()
        return Stub(Shipment) {
            getId() >> 's1'
            isFullyReceived() >> (state.fullyReceived ?: false)
            hasShipped() >> (state.shipped != false)
            hasPendingReceipt() >> (state.pending ?: false)
            getDestination() >> destination
            hasErrors() >> (state.invalid ?: false)
            getErrors() >> errors
        }
    }

    void 'a valid shipped shipment is received and SHIPMENT_RECEIVED is published'() {
        given:
        Shipment s = shipment([:])
        PartialReceipt receipt = new PartialReceipt()

        when:
        receipts.receiveInboundShipment(s)

        then:
        1 * receipts.createAutomaticReceipt(s) >> receipt
        1 * receipts.saveAndCompletePartialReceipt(receipt) >> null
        1 * context.publishEvent({ it instanceof ShipmentEvent && it.source == 's1' && it.eventType == WebhookEventType.SHIPMENT_RECEIVED })
    }

    @Unroll
    void 'nothing is received: #why'() {
        given:
        Shipment s = state == null ? null : shipment(state)

        when:
        receipts.receiveInboundShipment(s, force)

        then:
        0 * receipts.createAutomaticReceipt(_)
        0 * receipts.saveAndCompletePartialReceipt(_)
        0 * context.publishEvent(_)

        where:
        why                                          | state                 | force
        'no shipment'                                | null                  | false
        'already fully received'                     | [fullyReceived: true] | true
        'not shipped'                                | [shipped: false]      | true
        'no AUTO_RECEIVING and not forced'           | [activities: []]      | false
        'a pending (partial) receipt is in progress' | [pending: true]       | false
    }

    void 'forced (the auto-receive button) ignores AUTO_RECEIVING'() {
        given:
        Shipment s = shipment(activities: [])

        when:
        receipts.receiveInboundShipment(s, true)

        then:
        1 * receipts.createAutomaticReceipt(s) >> new PartialReceipt()
        1 * receipts.saveAndCompletePartialReceipt(_) >> null
    }

    void 'CROSS_DOCKING destination: invalid backorder references are logged per error and nothing is received'() {
        given:
        Shipment s = shipment(activities: [ActivityCode.AUTO_RECEIVING, ActivityCode.CROSS_DOCKING], invalid: true)
        ObjectError e1 = new ObjectError('shipment', 'one')
        ObjectError e2 = new ObjectError('shipment', 'two')
        s.errors.getAllErrors() >> [e1, e2]

        when:
        receipts.receiveInboundShipment(s)

        then:
        1 * backorderService.validateBackorderReferences(s)
        1 * receipts.logShipmentEvent('s1', e1) >> null
        1 * receipts.logShipmentEvent('s1', e2) >> null
        0 * receipts.createAutomaticReceipt(_)
    }

    void 'CROSS_DOCKING destination with valid references is received'() {
        given:
        Shipment s = shipment(activities: [ActivityCode.AUTO_RECEIVING, ActivityCode.CROSS_DOCKING])

        when:
        receipts.receiveInboundShipment(s)

        then:
        1 * backorderService.validateBackorderReferences(s)
        1 * receipts.createAutomaticReceipt(s) >> new PartialReceipt()
        1 * receipts.saveAndCompletePartialReceipt(_) >> null
    }

    // ------------------------------------------------------------------ createAutomaticReceipt

    void 'the automatic receipt receives what remains of every item into the receiving bin, in the default (no LPN) container'() {
        given:
        Product p = new Product(productCode: 'P')
        Container lpn = new Container(name: 'LPN-1')
        ShipmentItem loose = new ShipmentItem(product: p, quantity: 5, lotNumber: 'A')
        loose.id = 'i1'
        ShipmentItem packed = new ShipmentItem(product: p, quantity: 3, lotNumber: 'B', container: lpn)
        packed.id = 'i2'
        Shipment s = new Shipment(expectedShippingDate: new Date() - 3)
        s.id = 's2'
        s.shipmentItems = [loose, packed] as Set
        Location bin = new Location(name: 'SORT')
        PartialReceipt fromShipment = new PartialReceipt()
        fromShipment.partialReceiptContainers.add(new PartialReceiptContainer(container: lpn,
                partialReceiptItems: [new PartialReceiptItem(shipmentItem: packed)]))
        receipts.getPartialReceipt('s2', '1') >> fromShipment
        receipts.findReceivingLocation(s) >> bin

        when:
        PartialReceipt r = receipts.createAutomaticReceipt(s)
        PartialReceiptContainer defaultContainer = r.findDefaultPartialReceiptContainer()

        then:
        r.dateShipped == s.expectedShippingDate
        defaultContainer.partialReceiptItems.collect { [it.shipmentItem.id, it.quantityReceiving, it.binLocation.name, it.shouldSave, it.lotNumber] }
                .sort { it[0] } == [['i1', 5, 'SORT', true, 'A'], ['i2', 3, 'SORT', true, 'B']]
        r.partialReceiptItems.findAll { it.shouldSave }*.shipmentItem*.id.sort() == ['i1', 'i2']
    }

    void 'the receiving location: the first INBOUND_SORTATION internal location, else a temporary receiving bin'() {
        given:
        LocationType binType = new LocationType(locationTypeCode: LocationTypeCode.BIN_LOCATION)
        Location f = new Location(name: 'F')
        Location store = new Location(name: 'STORE', locationType: binType, parentLocation: f)
        store.supportedActivities = [ActivityCode.PICK_STOCK.id] as Set
        Location sort = new Location(name: 'SORT', locationType: binType, parentLocation: f)
        sort.supportedActivities = [ActivityCode.INBOUND_SORTATION.id] as Set
        Location temporary = new Location(name: 'R-S1')
        Shipment s = new Shipment(destination: f)

        when:
        f.locations = [store, sort] as Set
        Location found = receipts.findReceivingLocation(s)

        then:
        found.is(sort)
        0 * receipts.createTemporaryReceivingBin(_)

        when:
        f.locations = [store] as Set
        Location created = receipts.findReceivingLocation(s)

        then:
        1 * receipts.createTemporaryReceivingBin(s) >> temporary
        created.is(temporary)
    }
}
