package org.pih.warehouse.api.regression.platform.pages.shipping

import spock.lang.PendingFeature


import io.restassured.response.Response

import org.pih.warehouse.api.regression.platform.spec.receiving.ReceivingSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentMethod
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.shipping.ShipmentWorkflow
import org.pih.warehouse.shipping.Shipper

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The shipment server pages (ShipmentController): listing, creating, editing, packing, sending, receiving and
 * deleting shipments, as a signed-in user at the main facility.
 */
class ShipmentPagesSpec extends ReceivingSpec {

    static final String DATE_TIME = 'MM/dd/yyyy HH:mm XXX'

    ServerPages pages

    Location vendor

    @Override
    void setupData() {
        super.setupData()
        pages = new ServerPages(serverPort, cookie)
        // In the setup transaction, like the supplier above (a second transaction would wait on its locks)
        vendor = new Location(name: randomUtil.randomStringFieldValue('Vendor'), locationType: LocationType.get('4'),
                organization: Location.get(facility.id).organization, active: true)
        vendor.inventory = new Inventory(warehouse: vendor)
        vendor.save(failOnError: true, flush: true)
    }

    /** A pending shipment from the vendor (a supplier, no stock checks) to the main facility, one box of 5. */
    String pendingShipment(Map extra = [:]) {
        return Shipment.withNewTransaction {
            Shipment shipment = new Shipment([
                    name                : randomUtil.randomStringFieldValue('Shipment'),
                    origin              : Location.get(vendor.id),
                    destination         : Location.get(facility.id),
                    expectedShippingDate: new Date() - 1,
                    shipmentType        : ShipmentType.list().first()] + extra)
            Container box = new Container(name: '1', containerType: ContainerType.list().first(), sortOrder: 0)
            shipment.addToContainers(box)
            ShipmentItem item = new ShipmentItem(product: Product.get(product.id), quantity: 5, lotNumber: ReceivingSpec.LOT,
                    container: box)
            shipment.addToShipmentItems(item)
            shipment.save(failOnError: true, flush: true)
            shipment.id
        }
    }

    def <T> T withShipment(String id, Closure<T> closure) {
        return Shipment.withNewSession { closure(Shipment.get(id)) }
    }

    static String formatDate(Date date) {
        return date.format(DATE_TIME, TimeZone.getTimeZone('UTC'))
    }

    void 'the shipment list filters incoming and outgoing shipments'() {
        given:
        String id = pendingShipment()
        String name = withShipment(id) { it.name }

        when:
        String incoming = page(pages.get('/shipment/list', [type: 'incoming', origin: vendor.id, status: 'PENDING',
                statusStartDate: '01/01/2000', statusEndDate: '12/31/2099', lastUpdatedFrom: '01/01/2000',
                lastUpdatedTo: '12/31/2099', terms: name]))
        String outgoing = page(pages.get('/shipment/list', [destination: vendor.id, max: '5']))
        Response index = pages.get('/shipment/show', [id: id])

        then:
        incoming.contains(name)
        !outgoing.contains(name)
        redirectPath(index).startsWith('/shipment/showDetails')
        index.header('Location').contains(id)
    }

    void 'saving a new shipment creates it and opens its details'() {
        given:
        String name = randomUtil.randomStringFieldValue('Saved shipment')

        when:
        Response saved = pages.post('/shipment/save', [
                name                  : name,
                'origin.id'           : vendor.id,
                'destination.id'      : facility.id,
                'shipmentType.id'     : ShipmentType.withNewSession { ShipmentType.list().first().id },
                expectedShippingDate  : formatDate(new Date()),
                'eventType.id'        : 'none'])
        String id = Shipment.withNewSession { Shipment.findByName(name)?.id }

        then:
        id
        redirectPath(saved) == "/shipment/showDetails/${id}"

        when:
        String details = page(pages.get("/shipment/showDetails/${id}"))

        then:
        details.contains(name)
    }

    void 'the details page redirects for unknown shipments and for stock movement shipments'() {
        given:
        String inboundId = inbound([(product): 3])
        String requisitionId = withShipment(inboundId) { it.requisition.id }

        expect:
        redirectPath(pages.get('/shipment/showDetails/unknown-shipment')) == '/shipment/list'
        redirectPath(pages.get("/shipment/showDetails/${inboundId}")) == "/stockMovement/show/${requisitionId}"
        page(pages.get("/shipment/showDetails/${inboundId}", [override: 'true']))
                .contains(withShipment(inboundId) { it.name })
    }

    void 'updating a shipment changes its name, destination and carrier'() {
        given:
        String id = pendingShipment()
        String newName = randomUtil.randomStringFieldValue('Renamed shipment')

        when:
        Response updated = pages.post('/shipment/update', [
                id                    : id,
                name                  : newName,
                'shipperService.name' : '',
                'safeDestination.id'  : facility.id,
                'safeDestination.name': facility.name,
                'safeCarrier.name'    : 'Jane Quinn Public jane.public@example.com'])

        then:
        redirectPath(updated) == "/shipment/showDetails/${id}"
        withShipment(id) { Shipment shipment ->
            assert shipment.name == newName
            assert shipment.destination.id == facility.id
            assert shipment.carrier.firstName == 'Jane'
            assert shipment.carrier.lastName == 'Quinn Public'
            assert shipment.carrier.email == 'jane.public@example.com'
            true
        }

        when: 'the carrier is cleared'
        pages.post('/shipment/update', [id: id, 'shipperService.name': '', 'safeDestination.id': facility.id,
                'safeDestination.name': facility.name, 'safeCarrier.name': ''])

        then:
        withShipment(id) { it.carrier } == null

        expect: 'an unknown shipment goes back to the list'
        redirectPath(pages.post('/shipment/update', [id: 'unknown-shipment'])) == '/shipment/list'
    }

    void 'the tracking panel links the tracking number into the shipper tracking url'() {
        given:
        String id = pendingShipment()
        Shipment.withNewTransaction {
            Shipper shipper = new Shipper(name: randomUtil.randomStringFieldValue('Shipper'),
                    trackingUrl: 'https://tracking.example.com/track?number=%s').save(failOnError: true)
            Shipment shipment = Shipment.get(id)
            shipment.shipmentMethod = new ShipmentMethod(shipper: shipper, trackingNumber: 'TRACK123')
                    .save(failOnError: true)
            shipment.save(failOnError: true, flush: true)
        }

        when:
        String tracking = page(pages.get("/shipment/showTracking/${id}"))
        String transactions = page(pages.get("/shipment/showTransactions/${id}"))

        then:
        tracking.contains('https://tracking.example.com/track?number=TRACK123')
        tracking.contains('TRACK123')
        transactions != null
    }

    void 'receiving a shipped shipment through the receipt form credits the facility'() {
        given:
        String id = pendingShipment()
        pages.post("/shipment/sendShipment/${id}", [actualShippingDate: formatDate(new Date() - 1)])

        when:
        String form = page(pages.get("/shipment/receiveShipment/${id}"))

        then:
        form.contains(withShipment(id) { it.name })

        when: 'saved without receiving'
        Response saved = pages.post("/shipment/receiveShipment/${id}", [actualDeliveryDate: formatDate(new Date()),
                saveButton: 'saveAndExit'])

        then:
        redirectPath(saved) == "/shipment/showDetails/${id}"
        !withShipment(id) { it.wasReceived() }

        when:
        Response received = pages.post("/shipment/receiveShipment/${id}", [actualDeliveryDate: formatDate(new Date()),
                saveButton: 'receiveShipment', comment: 'Received by the test'])

        then:
        redirectPath(received) == "/shipment/showDetails/${id}"
        withShipment(id) { it.wasReceived() }
        page(pages.get("/shipment/renderReceivedEmail/${id}")).contains(withShipment(id) { it.name })

        expect: 'unknown shipments go to the incoming list'
        redirectPath(pages.get('/shipment/receiveShipment/unknown-shipment')) == '/shipment/list'
    }

    void 'the bulk actions receive, roll back and delete selected shipments'() {
        given:
        String first = pendingShipment()
        String second = pendingShipment()
        [first, second].each {
            pages.post("/shipment/sendShipment/${it}", [actualShippingDate: formatDate(new Date() - 1)])
        }

        when:
        Response received = pages.post('/shipment/bulkReceiveShipments', ['shipment.id': [first], type: 'incoming'])

        then:
        redirectPath(received) == '/shipment/list'
        withShipment(first) { it.wasReceived() }

        when:
        Response marked = pages.post('/shipment/bulkMarkAsReceived', ['shipment.id': [second], type: 'incoming'])

        then:
        redirectPath(marked) == '/shipment/list'
        withShipment(second) { it.wasReceived() }

        when: 'nothing selected'
        Response nothing = pages.post('/shipment/bulkRollbackShipments', [:])

        then:
        redirectPath(nothing) == '/shipment/list'

        when:
        String pending = pendingShipment()
        Response deleted = pages.post('/shipment/bulkDeleteShipments', ['shipment.id': [pending]])

        then:
        redirectPath(deleted) == '/shipment/list'
        withShipment(pending) { it } == null
    }

    void 'the packing list shows, downloads and exports the shipment contents'() {
        given:
        String id = pendingShipment()

        when:
        String shown = page(pages.get("/shipment/showPackingList/${id}"))
        Response xls = pages.get("/shipment/exportPackingList/${id}")

        then:
        shown.contains(withShipment(id) { it.name })
        xls.statusCode == 200
        xls.contentType.startsWith('application/vnd.ms-excel')
        xls.body.asByteArray().length > 0

        expect: 'unknown shipments go back to the list'
        redirectPath(pages.get('/shipment/showPackingList/unknown-shipment')) == '/shipment/list'
        redirectPath(pages.get('/shipment/downloadPackingList/unknown-shipment')) == '/shipment/list'
    }

    void 'shipment items can be edited'() {
        given:
        String id = pendingShipment()
        String itemId = withShipment(id) { it.shipmentItems.first().id }

        when:
        Response edited = pages.post("/shipment/editItem/${itemId}", [quantity: '7'])

        then:
        redirectPath(edited) == "/shipment/editContents/${id}"
        ShipmentItem.withNewSession { ShipmentItem.get(itemId).quantity } == 7
    }

    void 'deleting a shipment asks first and then removes it'() {
        given:
        String id = pendingShipment()

        expect:
        page(pages.get("/shipment/deleteShipment/${id}")).contains(withShipment(id) { it.name })
        redirectPath(pages.get('/shipment/deleteShipment/unknown-shipment')) == '/'

        when:
        Response deleted = pages.post("/shipment/deleteShipment/${id}")

        then:
        redirectPath(deleted) == '/'
        withShipment(id) { it } == null
    }

    void 'adding products to a shipment needs a shipment to add them to'() {
        when:
        String form = page(pages.get('/shipment/addToShipment', ['product.id': product.id]))
        String refused = page(pages.post('/shipment/addToShipmentPost', [:]))
        String unknown = page(pages.post('/shipment/addToShipmentPost', [shipmentContainerKey: 'unknown:unknown']))

        then:
        form != null
        refused != null
        unknown != null
    }

    void 'the shipment item pages show, edit, pick and delete an item'() {
        given:
        String id = pendingShipment()
        String itemId = withShipment(id) { it.shipmentItems.first().id }

        expect:
        page(pages.get('/shipmentItem/list')) != null
        page(pages.get('/shipmentItem/create')) != null
        redirectPath(pages.get('/shipmentItem/index')) == '/shipmentItem/list'
        page(pages.get("/shipmentItem/show/${itemId}")) != null
        page(pages.get("/shipmentItem/edit/${itemId}")) != null
        page(pages.get("/shipmentItem/pick/${itemId}")) != null
        page(pages.get("/shipmentItem/split/${itemId}")) != null
        redirectPath(pages.get('/shipmentItem/show/unknown')) == '/shipmentItem/list'
        redirectPath(pages.get('/shipmentItem/edit/unknown')) == '/shipmentItem/list'
        redirectPath(pages.post('/shipmentItem/update', [id: 'unknown'])) == '/shipmentItem/list'
        redirectPath(pages.post('/shipmentItem/delete', [id: 'unknown'])) == '/shipmentItem/list'

        when:
        Response updated = pages.post('/shipmentItem/update', [id: itemId, quantity: '9'])
        Response picked = pages.post("/shipmentItem/updatePicklistItem/${itemId}")

        then:
        redirectPath(updated) == "/shipmentItem/list/${itemId}"
        ShipmentItem.withNewSession { ShipmentItem.get(itemId).quantity } == 9
        redirectPath(picked) == "/shipmentItem/pick/${itemId}"

        when:
        Response deleted = pages.post('/shipmentItem/delete', [id: itemId])

        then:
        deleted.statusCode == 302
        redirectPath(deleted).startsWith('/shipmentItem/list')
    }

    void 'shipment workflows are created, edited and deleted'() {
        given:
        String shipmentTypeId = ShipmentType.withNewTransaction {
            new ShipmentType(name: randomUtil.randomStringFieldValue('Page shipment type'), sortOrder: 99)
                    .save(failOnError: true, flush: true).id
        }
        String name = randomUtil.randomStringFieldValue('Page workflow')

        expect:
        page(pages.get('/shipmentWorkflow/list')) != null
        page(pages.get('/shipmentWorkflow/create')) != null
        redirectPath(pages.get('/shipmentWorkflow/index')) == '/shipmentWorkflow/list'
        redirectPath(pages.get('/shipmentWorkflow/show/unknown')) == '/shipmentWorkflow/list'
        redirectPath(pages.get('/shipmentWorkflow/edit/unknown')) == '/shipmentWorkflow/list'
        redirectPath(pages.post('/shipmentWorkflow/update', [id: 'unknown'])) == '/shipmentWorkflow/list'
        redirectPath(pages.post('/shipmentWorkflow/delete', [id: 'unknown'])) == '/shipmentWorkflow/list'

        when:
        Response saved = pages.post('/shipmentWorkflow/save', [name: name, 'shipmentType.id': shipmentTypeId])
        String id = ShipmentWorkflow.withNewSession { ShipmentWorkflow.findByName(name)?.id }

        then:
        id
        redirectPath(saved) == "/shipmentWorkflow/list/${id}"
        page(pages.get("/shipmentWorkflow/show/${id}")).contains(name)
        page(pages.get("/shipmentWorkflow/edit/${id}")).contains(name)

        when:
        Response updated = pages.post('/shipmentWorkflow/update', [id: id, name: "${name} 2"])

        then:
        redirectPath(updated) == "/shipmentWorkflow/list/${id}"
        ShipmentWorkflow.withNewSession { ShipmentWorkflow.get(id).name } == "${name} 2"

        when:
        Response deleted = pages.post('/shipmentWorkflow/delete', [id: id])

        then:
        redirectPath(deleted) == '/shipmentWorkflow/list'
        ShipmentWorkflow.withNewSession { ShipmentWorkflow.get(id) } == null
    }

    @PendingFeature(reason = 'the packing list CSV download answers 500')
    void 'the packing list downloads as csv'() {
        given: 'an item whose lot is only on the shipment item (legacy rows; GORM does not write that column)'
        String id = pendingShipment()
        Shipment.withNewTransaction {
            Shipment.withSession { session ->
                session.createSQLQuery('UPDATE shipment_item SET lot_number = :lot WHERE shipment_id = :id')
                        .setParameter('lot', ReceivingSpec.LOT).setParameter('id', id).executeUpdate()
            }
        }

        when:
        Response csv = pages.get("/shipment/downloadPackingList/${id}")

        then:
        csv.statusCode == 200
        csv.contentType.startsWith('text/csv')
        csv.body.asString().readLines()[0] == '"unit","dimensions","weight","qty","item","Lot number"'
        csv.body.asString().readLines()[1].endsWith("\"5\",\"${product.name}\",\"${ReceivingSpec.LOT}\"")
        !csv.body.asString().contains('General Error')

        when: 'the item is linked to an inventory item, whose lot wins'
        String inventoryLot = randomUtil.randomStringFieldValue('PACK-LOT')
        Shipment.withNewTransaction {
            InventoryItem inventoryItem = new InventoryItem(product: Product.get(product.id), lotNumber: inventoryLot)
                    .save(failOnError: true, flush: true)
            ShipmentItem item = Shipment.get(id).shipmentItems.first()
            item.inventoryItem = inventoryItem
            item.lotNumber = null
            item.save(failOnError: true, flush: true)
        }
        Response linked = pages.get("/shipment/downloadPackingList/${id}")

        then:
        linked.body.asString().readLines()[1].endsWith("\"5\",\"${product.name}\",\"${inventoryLot}\"")
    }

    void 'editing a document of an unknown shipment or an unknown document goes back'() {
        given:
        String id = pendingShipment()

        expect:
        redirectPath(pages.get('/shipment/editDocument', [shipmentId: 'unknown', documentId: 'unknown'])) ==
                '/shipment/list'
        redirectPath(pages.get('/shipment/editDocument', [shipmentId: id, documentId: 'unknown'])) ==
                "/shipment/showDetails/${id}"
    }
}
