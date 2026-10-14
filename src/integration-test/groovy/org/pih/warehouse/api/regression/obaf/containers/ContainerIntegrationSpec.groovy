package org.pih.warehouse.api.regression.obaf.containers

import grails.util.GrailsWebMockUtil
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.PendingFeature

import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.LotStatusCode
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerApiController
import org.pih.warehouse.shipping.ContainerStatus
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * Containers / LPNs on a real database: ShipmentService.packItem (whole and split packs, packing
 * into an LPN that already holds the lot), generateContainerNumber, and ContainerApiController's
 * create and updateStatus (MISSING zeroes the LPN and marks its lots; leaving MISSING restores).
 * The controller is driven in-process with a mock request bound (no HTTP); the mobile contract
 * suite covers the same routes over HTTP.
 */
class ContainerIntegrationSpec extends ObafTaskIntegrationSpec {

    static class Box {
        String dc, product, shipment, lot
        List<String> items = []
    }

    /** An unshipped shipment from the depot with one item per quantity, all the same lot. */
    Box box(String prefix, List<Integer> quantities, String lotNumber = 'LPN-LOT') {
        Box b = new Box()
        b.dc = depot(prefix)
        b.product = product(prefix)
        b.lot = lotNumber
        b.shipment = inboundShipment(b.dc, quantities.collect { [productId: b.product, quantity: it, lotNumber: lotNumber] }, false)
        b.items = tx { Shipment.get(b.shipment).shipmentItems.sort { it.quantity }*.id } as List<String>
        return b
    }

    String lpn(Box b, String number = null) {
        return tx {
            Shipment s = Shipment.get(b.shipment)
            String n = number ?: shipmentService.generateContainerNumber(s)
            Container c = new Container(name: n, containerNumber: n, containerType: ContainerType.get(Constants.PALLET_CONTAINER_TYPE_ID))
            s.addToContainers(c)
            s.save(flush: true, failOnError: true)
            c.id
        } as String
    }

    /** Items of the shipment as [containerNumber or null, quantity], sorted. */
    List<List> contents(Box b) {
        return tx {
            Shipment.get(b.shipment).shipmentItems.collect { [it.container?.containerNumber, it.quantity] }
                    .sort { x, y -> (x[0] <=> y[0]) ?: (x[1] <=> y[1]) }
        } as List<List>
    }

    String number(String containerId) {
        return tx { Container.get(containerId).containerNumber } as String
    }

    Throwable pack(String itemId, String containerId, Integer quantity) {
        try {
            tx { shipmentService.packItem(itemId, containerId, quantity) }
            return null
        } catch (Throwable t) {
            return t
        }
    }

    // ------------------------------------------------------------------ packItem

    void 'pack all of an item into an empty LPN moves the item'() {
        given:
        Box b = box('CT-PA', [6])
        String c = lpn(b)

        expect:
        pack(b.items[0], c, 6) == null
        contents(b) == [[number(c), 6]]
    }

    void 'pack part of an item splits it: the packed part in the LPN, the rest stays unpacked'() {
        given:
        Box b = box('CT-PP', [6])
        String c = lpn(b)

        expect:
        pack(b.items[0], c, 4) == null
        contents(b) == [[null, 2], [number(c), 4]]
    }

    void 'pack: more than the item, an item already in that LPN or an unknown item is refused'() {
        given:
        Box b = box('CT-PX', [6])
        String c = lpn(b)

        expect:
        pack(b.items[0], c, 7) instanceof IllegalArgumentException
        contents(b) == [[null, 6]]

        when:
        pack(b.items[0], c, 6)

        then:
        pack(b.items[0], c, 1) instanceof IllegalStateException
        rootCause(pack('no-such-item', c, 1)) instanceof org.hibernate.ObjectNotFoundException
    }

    @PendingFeature(reason = 'ShipmentService.groovy:1878-1883 and 1898-1903 when the LPN already holds the same lot from the same bin, packItem adds the LPN item\'s quantity onto the SOURCE item and deletes the LPN item without setting the container: the stock ends up unpacked and the LPN empty (whole and partial packs alike)')
    void 'pack into an LPN that already holds the same lot ends up in that LPN'() {
        given:
        Box b = box('CT-SL', [3, 5])
        String c = lpn(b)
        pack(b.items[0], c, 3)
        assert contents(b) == [[null, 5], [number(c), 3]]

        when: 'all 5 of the other item'
        pack(b.items[1], c, 5)

        then:
        contents(b) == [[number(c), 8]]
    }

    @PendingFeature(reason = 'ShipmentService.groovy:1898-1903 the split branch of packItem has the same merge-into-the-source defect: packing part of an item into an LPN holding that lot leaves the packed part unpacked')
    void 'pack part of an item into an LPN that already holds the same lot'() {
        given:
        Box b = box('CT-SLP', [3, 5])
        String c = lpn(b)
        pack(b.items[0], c, 3)

        when: '2 of the other 5'
        pack(b.items[1], c, 2)

        then:
        contents(b) == [[null, 3], [number(c), 5]]
    }

    @PendingFeature(reason = 'ShipmentService.groovy:1862-1869 packItem does not refuse a zero or negative quantity: packing 0 moves an empty item into the LPN, packing -2 creates an unpacked clone holding MORE than the original item (stock is invented on the shipment)')
    void 'pack refuses a zero or negative quantity'() {
        given:
        Box b = box('CT-PN', [6])
        String c = lpn(b)

        expect:
        pack(b.items[0], c, q) instanceof IllegalArgumentException
        contents(b) == [[null, 6]]

        where:
        q << [0, -2]
    }

    // ------------------------------------------------------------------ container numbers

    void 'container numbers: LPN-<shipment number>-<sequence>, the sequence being the number of containers + 1'() {
        given:
        Box b = box('CT-NUM', [1])
        String shipmentNumber = tx { Shipment.get(b.shipment).shipmentNumber } as String

        when:
        String first = number(lpn(b))
        String second = number(lpn(b))

        then:
        first == "LPN-${shipmentNumber}-00001".toString()
        second == "LPN-${shipmentNumber}-00002".toString()
    }

    @PendingFeature(reason = 'ShipmentService.groovy:2495-2497 the next container sequence is containers.size() + 1: once an LPN is removed from a shipment the next one repeats the number of an LPN still on it')
    void 'a new container number never repeats one already on the shipment'() {
        given:
        Box b = box('CT-DUP', [1])
        String first = lpn(b)
        String second = lpn(b)
        tx { Shipment s = Shipment.get(b.shipment); Container c = Container.get(first); s.removeFromContainers(c); c.delete(flush: true) }

        when:
        String third = number(lpn(b))

        then:
        third != number(second)
    }

    // ------------------------------------------------------------------ ContainerApiController

    def withController(Map params, String json, Closure c) {
        GrailsWebRequest web = GrailsWebMockUtil.bindMockWebRequest(applicationContext())
        try {
            web.currentRequest.method = 'POST'
            if (json != null) {
                web.currentRequest.contentType = 'application/json'
                web.currentRequest.characterEncoding = 'UTF-8'
                web.currentRequest.content = json.getBytes('UTF-8')
            }
            params.each { k, v -> web.params.put(k, v) }
            ContainerApiController controller = applicationContext().getBean(ContainerApiController)
            controller.shipmentService = shipmentService
            def result = tx { c.call(controller) }
            return [result: result, response: web.currentResponse]
        } finally {
            RequestContextHolder.resetRequestAttributes()
        }
    }

    org.springframework.context.ApplicationContext applicationContext() {
        return grails.util.Holders.applicationContext
    }

    void 'create: a pallet with a generated LPN number and name; posting an existing container number reuses that container'() {
        given:
        Box b = box('CT-CR', [1])
        String shipmentNumber = tx { Shipment.get(b.shipment).shipmentNumber } as String
        String lpn1 = "LPN-${shipmentNumber}-00001".toString()

        when:
        Map created = withController([:], """{"containerType.id": "${Constants.PALLET_CONTAINER_TYPE_ID}", "shipment.id": "${b.shipment}"}""") { it.create() } as Map

        then:
        created.response.status == 200
        tx { Shipment.get(b.shipment).containers.collect { [it.name, it.containerNumber, it.containerType.id] } } == [[lpn1, lpn1, Constants.PALLET_CONTAINER_TYPE_ID]]

        when:
        Map again = withController([:], """{"containerType.id": "${Constants.PALLET_CONTAINER_TYPE_ID}", "shipment.id": "${b.shipment}", "containerNumber": "${lpn1}"}""") { it.create() } as Map

        then:
        again.response.status == 200
        tx { Shipment.get(b.shipment).containers*.containerNumber } == [lpn1]
    }

    @PendingFeature(reason = 'ContainerApiController.groovy:65-72 create() always generates the next container number and, when the body names no container, uses it as the NAME even when it found an existing container by number: re-posting LPN-...-00001 renames it LPN-...-00002, so its name and number disagree')
    void 'posting an existing container number again leaves its name alone'() {
        given:
        Box b = box('CT-CRN', [1])
        String shipmentNumber = tx { Shipment.get(b.shipment).shipmentNumber } as String
        String lpn1 = "LPN-${shipmentNumber}-00001".toString()
        withController([:], """{"containerType.id": "2", "shipment.id": "${b.shipment}"}""") { it.create() }

        when:
        withController([:], """{"containerType.id": "2", "shipment.id": "${b.shipment}", "containerNumber": "${lpn1}"}""") { it.create() }

        then:
        tx { Shipment.get(b.shipment).containers.collect { [it.name, it.containerNumber] } } == [[lpn1, lpn1]]
    }

    void 'create without a container type or shipment answers HTTP 200 with errorCode 500 and creates nothing (characterised)'() {
        given:
        Box b = box('CT-CRX', [1])

        when:
        Map noType = withController([:], """{"shipment.id": "${b.shipment}"}""") { it.create() } as Map
        Map noShipment = withController([:], """{"containerType.id": "2"}""") { it.create() } as Map

        then:
        noType.response.status == 200
        noType.response.contentAsString.contains('"errorCode":500')
        noType.response.contentAsString.contains('Container type is a required property')
        noShipment.response.contentAsString.contains('Shipment is a required property')
        tx { Shipment.get(b.shipment).containers?.size() ?: 0 } == 0
    }

    String lpnStatus(String containerId) {
        return tx { Container.get(containerId).containerStatus?.name() } as String
    }

    void 'updateStatus walks an LPN through its statuses; an unknown status or container is refused'() {
        given:
        Box b = box('CT-ST', [4])
        String c = lpn(b)
        pack(b.items[0], c, 4)

        when:
        List<String> seen = ['PACKING', 'PACKED', 'LOADING', 'LOADED', 'UNLOADED', 'CLOSED'].collect { String st ->
            withController([id: c], """{"status": "${st}"}""") { it.updateStatus() }
            lpnStatus(c)
        }

        then:
        seen == ['PACKING', 'PACKED', 'LOADING', 'LOADED', 'UNLOADED', 'CLOSED']

        when:
        withController([id: c], '{"status": "FLYING"}') { it.updateStatus() }

        then:
        thrown(IllegalArgumentException)

        when:
        withController([id: 'no-such-container'], '{"status": "PACKED"}') { it.updateStatus() }

        then:
        thrown(IllegalArgumentException)
    }

    void 'updateStatus MISSING zeroes every item in the LPN and marks their lots MISSING'() {
        given:
        Box b = box('CT-MS', [4])
        String c = lpn(b)
        pack(b.items[0], c, 4)

        when:
        withController([id: c], '{"status": "MISSING"}') { it.updateStatus() }

        then:
        lpnStatus(c) == 'MISSING'
        contents(b) == [[number(c), 0]]
        tx { InventoryItem.get(ShipmentItem.get(b.items[0]).inventoryItem.id).lotStatus } == LotStatusCode.MISSING
    }

    @PendingFeature(reason = 'ContainerApiController.groovy:111-115 leaving MISSING restores each item quantity from ShipmentItem.quantityPicked, which falls back to the (now zero) quantity when the item has no picks (an inbound or unpicked LPN): an LPN marked missing and found again keeps 0 on every item')
    void 'an LPN found again (MISSING -> PACKED) gets its item quantities back'() {
        given:
        Box b = box('CT-MF', [4])
        String c = lpn(b)
        pack(b.items[0], c, 4)
        withController([id: c], '{"status": "MISSING"}') { it.updateStatus() }

        when:
        withController([id: c], '{"status": "PACKED"}') { it.updateStatus() }

        then:
        lpnStatus(c) == 'PACKED'
        contents(b) == [[number(c), 4]]
        tx { InventoryItem.get(ShipmentItem.get(b.items[0]).inventoryItem.id).lotStatus } == null
    }

    @PendingFeature(reason = 'ContainerApiController.groovy:104-116 MISSING overwrites the lot status of every lot in the LPN (a lot-wide flag, every bin) and leaving MISSING sets it to null: a RECALLED lot loses its recall after a missing/found round trip')
    void 'a recalled lot stays recalled through MISSING and back'() {
        given:
        Box b = box('CT-MR', [4], "RECALL-${uid()}".toString())
        String c = lpn(b)
        pack(b.items[0], c, 4)
        tx { InventoryItem i = ShipmentItem.get(b.items[0]).inventoryItem; i.lotStatus = LotStatusCode.RECALLED; i.save(flush: true, failOnError: true) }

        when:
        withController([id: c], '{"status": "MISSING"}') { it.updateStatus() }
        withController([id: c], '{"status": "PACKED"}') { it.updateStatus() }

        then:
        tx { ShipmentItem.get(b.items[0]).inventoryItem.lotStatus } == LotStatusCode.RECALLED
    }
}
